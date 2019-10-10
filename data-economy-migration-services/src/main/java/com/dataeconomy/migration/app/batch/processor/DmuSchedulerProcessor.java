package com.dataeconomy.migration.app.batch.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
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
@StepScope
public class DmuSchedulerProcessor implements ItemProcessor<DmuHistoryDetailEntity, DmuHistoryDetailEntity> {

	@Autowired
	DmuHistoryDetailRepository historyDetailRepository;

	@Autowired
	DmuHdfsConnectionService hdfsConnectionService;

	@Autowired
	DmuAwsConnectionService awsConnectionService;

	@Autowired
	DmuServiceHelper dmuServiceHelper;

	@Autowired
	DmuFilterConditionProcessor dmuFilterConditionProcessor;

	@Override
	@Timed
	@Transactional
	public DmuHistoryDetailEntity process(DmuHistoryDetailEntity dmuHistoryDetailEntity) throws Exception {
		log.info(" DmuSchedulerProcessor =>  : Thread {} processing with requestNo : {} , srNo {} : status : {}",
				Thread.currentThread().getName(), dmuHistoryDetailEntity.getDmuHIstoryDetailPK().getRequestNo(),
				dmuHistoryDetailEntity.getDmuHIstoryDetailPK().getSrNo(), dmuHistoryDetailEntity.getStatus());
		try {

			historyDetailRepository.updateByRequestNoAndSrNoAndStatus(
					dmuHistoryDetailEntity.getDmuHIstoryDetailPK().getRequestNo(),
					dmuHistoryDetailEntity.getDmuHIstoryDetailPK().getSrNo(), DmuConstants.IN_PROGRESS);

			if (StringUtils.isBlank(dmuHistoryDetailEntity.getFilterCondition())
					&& StringUtils.equalsIgnoreCase(DmuConstants.NO, dmuHistoryDetailEntity.getIncrementalFlag())
					&& StringUtils.equalsIgnoreCase(DmuConstants.YES,
							dmuServiceHelper.getProperty(DmuConstants.SRC_FORMAT_FLAG))) {
				getHdfsPath(dmuHistoryDetailEntity)
						.ifPresent(hdfsPath -> migrateDataToS3(dmuHistoryDetailEntity, hdfsPath));
			} else if (StringUtils.equalsIgnoreCase(DmuConstants.YES, dmuHistoryDetailEntity.getIncrementalFlag())) {
				dmuHistoryDetailEntity.setStatus(DmuConstants.NEW_SCENARIO);
				historyDetailRepository.save(dmuHistoryDetailEntity);
			} else if (StringUtils.isNotBlank(dmuHistoryDetailEntity.getFilterCondition()) && StringUtils
					.equalsIgnoreCase(DmuConstants.YES, dmuServiceHelper.getProperty(DmuConstants.SRC_FORMAT_FLAG))) {
				dmuFilterConditionProcessor.processFilterCondition(dmuHistoryDetailEntity);
				migrateDataToS3ForFilterCondition(dmuHistoryDetailEntity);
			} else {
				dmuHistoryDetailEntity.setStatus(DmuConstants.UNKNOWN_CASE);
				historyDetailRepository.save(dmuHistoryDetailEntity);
			}
		} catch (Exception exception) {
			log.info(" Exception occured at DmuSchedulerProcessor :: process {} ",
					ExceptionUtils.getStackTrace(exception));
			historyDetailRepository.updateByRequestNoAndSrNoAndStatus(
					dmuHistoryDetailEntity.getDmuHIstoryDetailPK().getRequestNo(),
					dmuHistoryDetailEntity.getDmuHIstoryDetailPK().getSrNo(), DmuConstants.FAILED);
		}
		return dmuHistoryDetailEntity;
	}

	@Timed
	public synchronized Optional<String> getHdfsPath(DmuHistoryDetailEntity historyEntity) {
		log.info(" executing => DmuSchedulerProcessor ::  getHdfsPath :: schemaName => {} , tableName => {} ",
				historyEntity.getSchemaName(), historyEntity.getTableName());

		StringBuilder locationHDFS = new StringBuilder(100);
		locationHDFS.append(DmuConstants.SHOW_CREATE_TABLE);
		locationHDFS.append(historyEntity.getSchemaName());
		locationHDFS.append(DmuConstants.DOT_OPERATOR);
		locationHDFS.append(historyEntity.getTableName());

		try (Connection conn = hdfsConnectionService.getValidDataSource(DmuConstants.REGULAR).getConnection();
				Statement statement = conn.createStatement();
				ResultSet resultSet = statement.executeQuery(locationHDFS.toString());) {
			locationHDFS.delete(0, locationHDFS.length());
			while (resultSet.next()) {
				locationHDFS.append(resultSet.getString(1));
			}
			if (StringUtils.isBlank(locationHDFS.toString())) {
				historyEntity.setStatus(DmuConstants.FAILED);
				historyDetailRepository.save(historyEntity);
				return Optional.empty();
			}
			return Optional.ofNullable(
					StringUtils.substring(locationHDFS.toString(), locationHDFS.toString().indexOf("LOCATION") + 8,
							locationHDFS.toString().indexOf("TBLPROPERTIES") - 1).replace("'", ""));
		} catch (Exception exception) {
			historyEntity.setStatus(DmuConstants.FAILED);
			historyDetailRepository.save(historyEntity);
			log.error("Exception occurred at DmuSchedulerProcessor ::  getHdfsPath =>  {}   ",
					ExceptionUtils.getStackTrace(exception));
			return Optional.empty();
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
			log.info(" DmuSchedulerProcessor : migrateDataToS3ssh  : ssh command => " + sshBuilder.toString());

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
					historyEntity.setStatus(DmuConstants.SUCCESS);
					historyDetailRepository.save(historyEntity);
					log.info(
							"called=> DmuSchedulerProcessor ::  migrateDataToS3 ::  hiveLocation {} :: busketName :: {} :: status => {} ",
							hdfsPath, historyEntity.getTargetS3Bucket(), DmuStatusConstants.HttpConstants.SUCCESS);
				} else {
					historyEntity.setStatus(DmuConstants.FAILED);
					historyDetailRepository.save(historyEntity);
					log.info("called=> DmuSchedulerProcessor hiveLocation {} :: busketName :: {} :: status => {} ",
							hdfsPath, historyEntity.getTargetS3Bucket(), DmuStatusConstants.HttpConstants.FAILURE);
				}
			} catch (Exception exception) {
				historyEntity.setStatus(DmuConstants.FAILED);
				historyDetailRepository.save(historyEntity);
				log.error(
						"Exception occurred at ScriptGenerationService ::  proceedScriptGenerationForRequest :: proceedScriptGenerationForRequestHelper :: {}   ",
						ExceptionUtils.getStackTrace(exception));
			}
		}
	}

	@Timed
	private void migrateDataToS3ForFilterCondition(DmuHistoryDetailEntity historyEntity) {
		BasicSessionCredentials awsCredentials = awsConnectionService.getBasicSessionCredentials();
		if (awsCredentials == null) {
			historyDetailRepository.updateByRequestNoAndSrNoAndStatus(
					historyEntity.getDmuHIstoryDetailPK().getRequestNo(),
					historyEntity.getDmuHIstoryDetailPK().getSrNo(), DmuConstants.FAILED);
		} else {
			String sftcpCommand = dmuServiceHelper.buildS3MigrationUrlForFilterCondition(historyEntity, awsCredentials);
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
			log.info(" DmuSchedulerProcessor : migrateDataToS3ssh  : ssh command => " + sshBuilder.toString());
			try {
				executeSSHCommand(historyEntity, sshBuilder);
			} catch (Exception exception) {
				historyEntity.setStatus(DmuConstants.FAILED);
				historyDetailRepository.save(historyEntity);
				log.error(
						"Exception occurred at ScriptGenerationService ::  proceedScriptGenerationForRequest :: proceedScriptGenerationForRequestHelper :: {}   ",
						ExceptionUtils.getStackTrace(exception));
			}
		}
	}

	private void executeSSHCommand(DmuHistoryDetailEntity historyEntity, StringBuilder sshBuilder)
			throws IOException, InterruptedException {
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
			historyEntity.setStatus(DmuConstants.SUCCESS);
			historyDetailRepository.save(historyEntity);
			log.info(
					"called=> ScriptGenerationService ::  proceedScriptGenerationForRequest :: proceedScriptGenerationForRequestHelper :: hiveLocation {} :: busketName :: {} :: status => {} ",
					historyEntity.getTargetS3Bucket(), DmuStatusConstants.HttpConstants.SUCCESS);
		} else {
			historyEntity.setStatus(DmuConstants.FAILED);
			historyDetailRepository.save(historyEntity);
			log.info(
					"called=> ScriptGenerationService ::  proceedScriptGenerationForRequest :: proceedScriptGenerationForRequestHelper :: hiveLocation {} :: busketName :: {} :: status => {} ",
					historyEntity.getTargetS3Bucket(), DmuStatusConstants.HttpConstants.FAILURE);
		}
	}
}