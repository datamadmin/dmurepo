package com.dataeconomy.migration.app.batch.processor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.BasicSessionCredentials;
import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.connection.DmuAwsConnectionService;
import com.dataeconomy.migration.app.connection.DmuHdfsConnectionService;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryDetailRepository;
import com.dataeconomy.migration.app.util.DmuConstants;
import com.dataeconomy.migration.app.util.DmuServiceHelper;
import com.dataeconomy.migration.app.util.DmuStatusConstants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DmuFilterConditionProcessor {

	@Autowired
	DmuHistoryDetailRepository historyDetailRepository;

	@Autowired
	DmuHdfsConnectionService hdfsConnectionService;

	@Autowired
	DmuServiceHelper dmuServiceHelper;

	@Autowired
	DmuAwsConnectionService awsConnectionService;

	@Timed
	void processFilterCondition(DmuHistoryDetailEntity dmuHistoryDetail) {
		log.info(" called=> DmuFilterConditionProcessor ::  invokeHDFSServiceForFilterCondition  ");
		ResultSet resultSet = null;
		try (Connection conn = hdfsConnectionService.getValidDataSource(DmuConstants.SMALLQUERY).getConnection();
				Statement statement = conn.createStatement();) {
			StringBuilder queryBuilder = new StringBuilder(200);
			StringBuilder commandBuilder = new StringBuilder(800);
			queryBuilder.append(DmuConstants.SELECT_COUNT);
			queryBuilder.append(dmuHistoryDetail.getSchemaName());
			queryBuilder.append(DmuConstants.DOT_OPERATOR);
			queryBuilder.append(dmuHistoryDetail.getTableName());
			queryBuilder.append(DmuConstants.WHERE);
			queryBuilder.append(dmuHistoryDetail.getFilterCondition());

			Long recordsCount = 0L;

			resultSet = statement.executeQuery(queryBuilder.toString());
			if (resultSet.next()) {
				recordsCount = resultSet.getLong(1);
			}
			resultSet.close();

			queryBuilder.delete(0, queryBuilder.length());
			queryBuilder.append(DmuConstants.SHOW_CREATE_TABLE);
			queryBuilder.append(dmuHistoryDetail.getSchemaName());
			queryBuilder.append(DmuConstants.DOT_OPERATOR);
			queryBuilder.append(dmuHistoryDetail.getTableName());

			resultSet = statement.executeQuery(queryBuilder.toString());

			queryBuilder.delete(0, queryBuilder.length());
			while (resultSet.next()) {
				queryBuilder.append(resultSet.getString(1));
			}

			commandBuilder.append(DmuConstants.SHOW_CREATE_TABLE);
			commandBuilder.append(dmuServiceHelper.getProperty(DmuConstants.TEMP_HIVE_DB));
			commandBuilder.append(StringUtils.substring(queryBuilder.toString(), queryBuilder.toString().indexOf("."),
					queryBuilder.toString().indexOf("LOCATION") + 8));
			commandBuilder.append(" ");
			commandBuilder.append("\'");
			commandBuilder.append(dmuServiceHelper.getProperty(DmuConstants.TEMP_HDFS_DIR));
			commandBuilder.append("/");
			commandBuilder.append(dmuHistoryDetail.getDmuHIstoryDetailPK().getRequestNo());
			commandBuilder.append("-");
			commandBuilder.append(dmuHistoryDetail.getTableName());
			commandBuilder.append("\'");
			commandBuilder.append(StringUtils.substring(queryBuilder.toString(),
					queryBuilder.toString().indexOf("TBLPROPERTIES") - 1, queryBuilder.toString().length()));

			log.info(
					" called=> DmuFilterConditionProcessor ::  invokeHDFSServiceForFilterCondition  :: commandBuilder {}",
					commandBuilder.toString());

			if (recordsCount > 10000000) {
				processCreateStatementForLargeQuery(dmuHistoryDetail, commandBuilder);
			} else if (recordsCount > 1000000 && recordsCount < 5000000) {
				processCreateStatementForMediumQuery(dmuHistoryDetail, commandBuilder);
			} else {
				processCreateStatementForRegularQuery(dmuHistoryDetail, commandBuilder);
			}
		} catch (Exception exception) {
			historyDetailRepository.updateByRequestNoAndSrNoAndStatus(
					dmuHistoryDetail.getDmuHIstoryDetailPK().getRequestNo(),
					dmuHistoryDetail.getDmuHIstoryDetailPK().getSrNo(), DmuConstants.FAILED);
			log.error(
					"Exception occurred at ScriptGenerationService ::  proceedScriptGenerationForRequest :: invokeHDFSService :: {}   ",
					ExceptionUtils.getStackTrace(exception));
		} finally {
			DbUtils.closeQuietly(resultSet);
		}
	}

	@Timed
	private void migrateDataToS3(DmuHistoryDetailEntity historyEntity, String hdfsPath) {
		BasicSessionCredentials awsCredentials = awsConnectionService.getBasicSessionCredentials();
		if (awsCredentials == null) {
			historyDetailRepository.updateByRequestNoAndSrNoAndStatus(
					historyEntity.getDmuHIstoryDetailPK().getRequestNo(),
					historyEntity.getDmuHIstoryDetailPK().getSrNo(), DmuConstants.FAILED);
		} else {
			String sftcpCommand;
			if (DmuConstants.YES.equalsIgnoreCase(dmuServiceHelper.getProperty(DmuConstants.SRC_CMPRSN_FLAG))
					|| DmuConstants.YES.equalsIgnoreCase(dmuServiceHelper.getProperty(DmuConstants.UNCMPRSN_FLAG))) {
				sftcpCommand = dmuServiceHelper.buildS3MigrationUrl(historyEntity, awsCredentials, hdfsPath);
			} else {
				sftcpCommand = dmuServiceHelper.buildS3MigrationUrl(historyEntity, awsCredentials, hdfsPath);
			}

			StringBuilder sshBuilder = new StringBuilder();
			sshBuilder.append("ssh -i ");
			sshBuilder.append(" ");
			sshBuilder.append(dmuServiceHelper.getProperty(DmuConstants.HDFS_PEM_LOCATION));
			sshBuilder.append(" ");
			sshBuilder.append(dmuServiceHelper.getProperty(DmuConstants.HDFS_USER_NAME));
			if (StringUtils.isNotBlank(dmuServiceHelper.getProperty(DmuConstants.HDFS_EDGE_NODE))) {
				sshBuilder.append("@");
				sshBuilder.append(dmuServiceHelper.getProperty(DmuConstants.HDFS_EDGE_NODE));
				sshBuilder.append(" ");
			} else {
				sshBuilder.append(" ");
			}
			sshBuilder.append(sftcpCommand);
			log.info(" DmuSchedulerProcessor : migrateDataToS3 ssh  : ssh command => " + sshBuilder.toString());

			try {
				Process process = Runtime.getRuntime().exec(sshBuilder.toString());
				try (InputStreamReader errorStream = new InputStreamReader(process.getErrorStream());
						BufferedReader errorBuffer = new BufferedReader(errorStream);
						InputStreamReader inputStream = new InputStreamReader(process.getInputStream());
						BufferedReader inputBuffer = new BufferedReader(inputStream)) {
					log.info(" ssh command success => {}", inputBuffer.lines().collect(Collectors.joining()));
					log.info(" ssh command error => {}", errorBuffer.lines().collect(Collectors.joining()));
				} catch (Exception e) {
					log.error(" ssh command error ");
				}

				int exitVal = process.waitFor();
				if (exitVal == 0) {
					historyDetailRepository.updateByRequestNoAndSrNoAndStatus(
							historyEntity.getDmuHIstoryDetailPK().getRequestNo(),
							historyEntity.getDmuHIstoryDetailPK().getSrNo(), DmuConstants.SUCCESS);
					log.info(
							"called=>  DmuSchedulerProcessor : migrateDataToS3 :: hiveLocation {} :: busketName :: {} :: status => {} ",
							hdfsPath, historyEntity.getTargetS3Bucket(), DmuStatusConstants.HttpConstants.SUCCESS);
				} else {
					historyDetailRepository.updateByRequestNoAndSrNoAndStatus(
							historyEntity.getDmuHIstoryDetailPK().getRequestNo(),
							historyEntity.getDmuHIstoryDetailPK().getSrNo(), DmuConstants.FAILED);
					log.info(
							"called=>  DmuSchedulerProcessor : migrateDataToS3 :: hiveLocation {} :: busketName :: {} :: status => {} ",
							hdfsPath, historyEntity.getTargetS3Bucket(), DmuStatusConstants.HttpConstants.FAILURE);
				}
			} catch (Exception exception) {
				historyDetailRepository.updateByRequestNoAndSrNoAndStatus(
						historyEntity.getDmuHIstoryDetailPK().getRequestNo(),
						historyEntity.getDmuHIstoryDetailPK().getSrNo(), DmuConstants.FAILED);
				log.error("Exception occurred at   DmuSchedulerProcessor : migrateDataToS3 :: {}   ",
						ExceptionUtils.getStackTrace(exception));
			}
		}
	}

	@Timed
	private void processCreateStatementForLargeQuery(DmuHistoryDetailEntity dmuHistoryDetail,
			StringBuilder commandBuilder) {
		try (Connection connection = hdfsConnectionService.getValidDataSource(DmuConstants.LARGEQUERY).getConnection();
				Statement largeQryStmt = connection.createStatement();) {
			largeQryStmt.execute(commandBuilder.toString());
		} catch (Exception exception) {
			log.error(" DmuFilterConditionProcessor : invokeHDFSServiceForFilterCondition : large query : exception {}",
					ExceptionUtils.getStackTrace(exception));
			historyDetailRepository.updateByRequestNoAndSrNoAndStatus(
					dmuHistoryDetail.getDmuHIstoryDetailPK().getRequestNo(),
					dmuHistoryDetail.getDmuHIstoryDetailPK().getSrNo(), DmuConstants.FAILED);
		}
	}

	@Timed
	private void processCreateStatementForRegularQuery(DmuHistoryDetailEntity dmuHistoryDetail,
			StringBuilder commandBuilder) {
		try (Connection connection = hdfsConnectionService.getValidDataSource(DmuConstants.REGULAR).getConnection();
				Statement smallQryStmt = connection.createStatement();) {
			smallQryStmt.execute(commandBuilder.toString());
		} catch (Exception exception) {
			log.error(
					" DmuFilterConditionProcessor : invokeHDFSServiceForFilterCondition : regular query : exception {}",
					ExceptionUtils.getStackTrace(exception));
			historyDetailRepository.updateByRequestNoAndSrNoAndStatus(
					dmuHistoryDetail.getDmuHIstoryDetailPK().getRequestNo(),
					dmuHistoryDetail.getDmuHIstoryDetailPK().getSrNo(), DmuConstants.FAILED);
		}
	}

	@Timed
	private void processCreateStatementForMediumQuery(DmuHistoryDetailEntity dmuHistoryDetail,
			StringBuilder commandBuilder) {
		try (Connection connection = hdfsConnectionService.getValidDataSource(DmuConstants.MEDIUMQUERY).getConnection();
				Statement mediumQryStmt = connection.createStatement();) {
			mediumQryStmt.execute(commandBuilder.toString());
		} catch (Exception exception) {
			log.error(
					" DmuFilterConditionProcessor : invokeHDFSServiceForFilterCondition : medium query : exception {}",
					ExceptionUtils.getStackTrace(exception));
			historyDetailRepository.updateByRequestNoAndSrNoAndStatus(
					dmuHistoryDetail.getDmuHIstoryDetailPK().getRequestNo(),
					dmuHistoryDetail.getDmuHIstoryDetailPK().getSrNo(), DmuConstants.FAILED);
		}
	}

}
