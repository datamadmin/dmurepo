package com.dataeconomy.migration.app.scheduler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.BasicSessionCredentials;
import com.dataeconomy.migration.app.connection.DmuAwsConnectionService;
import com.dataeconomy.migration.app.connection.DmuHdfsConnectionService;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryDetailRepository;
import com.dataeconomy.migration.app.util.DmuConstants;
import com.dataeconomy.migration.app.util.DmuServiceHelper;
import com.dataeconomy.migration.app.util.DmuStatusConstants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Scope("prototype")
public class DmuTableCopyService {

	@Autowired
	private DmuHistoryDetailRepository historyDetailRepository;

	@Autowired
	private DmuHdfsConnectionService hdfsConnectionService;

	@Autowired
	private DmuServiceHelper dmuHelperService;

	@Autowired
	private DmuAwsConnectionService awsConnectionService;

	public void processTableCopy(String requestNo, Long srNo) {
		log.info(" executed => TableCopySchedulerClass :: requestNp {}, srNo {}", requestNo, srNo);
		try {
			historyDetailRepository.updateForRequestNo(requestNo, srNo);

			List<DmuHistoryDetailEntity> dmuHistoryDetailList = historyDetailRepository
					.findHistoryDetailsByRequestNumberAndSrNo(requestNo, srNo);

			DmuConnectionDTO connectionDto = DmuConnectionDTO.builder().build();
			dmuHelperService.populateTGTFormatProperties(connectionDto);
			dmuHelperService.populateTGTOtherProperties(connectionDto);

			if (dmuHistoryDetailList != null && !dmuHistoryDetailList.isEmpty()) {
				dmuHistoryDetailList.stream().forEach(historyDetailEntity -> {
					String hdfspath = getLocationForTableAndSchema(historyDetailEntity);
					log.info(" executed => TableCopySchedulerClass :: hdfsLocation{}", hdfspath);
					if (StringUtils.isBlank(hdfspath)) {
						historyDetailEntity.setStatus(DmuConstants.FAILED);
						historyDetailRepository.save(historyDetailEntity);
						// TODO application.log created and store it in server log with date and time
						// Have the error ( “File Path not found in Create Table Statement”) stored in a
						// file (with file name as “Request No + Sr No”.log) on application server. Have
						// a Hyperlink created in the screen for the ”Failed” status value to this error
						// file.
						return;
					} else {
						processTableCopyHelper(historyDetailEntity, hdfspath, connectionDto, requestNo);
					}
				});
			}
		} catch (Exception exception) {
			log.info(" Exception occured at TableCopySchedulerClass :: processTableCopy {} ",
					ExceptionUtils.getStackTrace(exception));
		}
	}

	private String getLocationForTableAndSchema(DmuHistoryDetailEntity dmuHistoryDetail) {
		log.info(" called=> ScriptGenerationService ::  proceedScriptGenerationForRequest :: invokeHDFSService ");
		try {
			return new JdbcTemplate(hdfsConnectionService.getValidDataSource(DmuConstants.REGULAR)).query(
					"SHOW CREATE TABLE " + dmuHistoryDetail.getSchemaName() + "." + dmuHistoryDetail.getTableName(),
					new ResultSetExtractor<String>() {

						@Override
						public String extractData(ResultSet rs) throws SQLException, DataAccessException {
							while (rs.next()) {
								String showTable = rs.getString(1);
								if (StringUtils.equalsIgnoreCase(showTable, DmuConstants.HDFS_LOCATION)) {
									return (showTable.substring(3, showTable.length() - 1).trim());
								}
							}
							return null;
						}
					});
		} catch (Exception exception) {
			dmuHistoryDetail.setStatus(DmuConstants.FAILED);
			historyDetailRepository.save(dmuHistoryDetail);
			log.error(
					"Exception occurred at ScriptGenerationService ::  proceedScriptGenerationForRequest :: invokeHDFSService :: {}   ",
					ExceptionUtils.getStackTrace(exception));
			// TODO application.log created and store it in server log with date and time
			return null;
		}
	}

	private String retrieveHDFSPath(DmuHistoryDetailEntity dmuHistoryDetail) {
		log.info(" called=> ScriptGenerationService ::  proceedScriptGenerationForRequest :: invokeHDFSService ");
		try {
			return new JdbcTemplate(hdfsConnectionService.getValidDataSource(DmuConstants.REGULAR))
					.query("SHOW CREATE TABLE " + dmuHistoryDetail.getSchemaName(), new ResultSetExtractor<String>() {

						@Override
						public String extractData(ResultSet rs) throws SQLException, DataAccessException {
							while (rs.next()) {
								String showTable = rs.getString(1);
								if (StringUtils.equalsIgnoreCase(showTable, DmuConstants.HDFS_LOCATION)) {
									return (showTable.substring(3, showTable.length() - 1).trim());
								}
							}
							return null;
						}
					});
		} catch (Exception exception) {
			dmuHistoryDetail.setStatus(DmuConstants.FAILED);
			historyDetailRepository.save(dmuHistoryDetail);
			log.error(
					"Exception occurred at ScriptGenerationService ::  proceedScriptGenerationForRequest :: invokeHDFSService :: {}   ",
					ExceptionUtils.getStackTrace(exception));
			// TODO application.log created and store it in server log with date and time
			return null;
		}
	}

	private void processTableCopyHelper(DmuHistoryDetailEntity dmuHistoryDetail, String hdfsPath, DmuConnectionDTO connectionDto,
			String requestNo) {
		log.info(
				"called=> ScriptGenerationService ::  proceedScriptGenerationForRequest :: proceedScriptGenerationForRequestHelper :: hdfsPath {} :: busketName :: {} ",
				hdfsPath, dmuHistoryDetail.getTargetS3Bucket());
		BasicSessionCredentials awsCredentials = awsConnectionService.getBasicSessionCredentials();
		if (awsCredentials == null) {
			dmuHistoryDetail.setStatus(DmuConstants.FAILED);
			historyDetailRepository.save(dmuHistoryDetail);
			return;
		}
		StringBuilder sb = new StringBuilder(400);
		if (DmuConstants.YES.equalsIgnoreCase(connectionDto.getTgtFormatPropDto().getSrcCmprsnFlag())
				|| DmuConstants.YES.equalsIgnoreCase(connectionDto.getTgtFormatPropDto().getUncmprsnFlag())) {
			sb.append(connectionDto.getTgtOtherPropDto().getHadoopInstallDir());
			sb.append(
					"/bin/hadoop distcp -Dfs.s3a.aws.credentials.provider=org.apache.hadoop.fs.s3a.TemporaryAWSCredentialsProvider -Dfs.s3a.access.key=");
			sb.append(awsCredentials.getAWSAccessKeyId());
			sb.append(" -Dfs.s3a.secret.key=");
			sb.append(awsCredentials.getAWSSecretKey());
			sb.append(" -Dfs.s3a.session.token=");
			sb.append(awsCredentials.getSessionToken());
			sb.append(" ");
			sb.append(hdfsPath);
			sb.append("/* s3a://");
			sb.append(dmuHistoryDetail.getTargetS3Bucket());
		} else if (DmuConstants.YES.equalsIgnoreCase(connectionDto.getTgtFormatPropDto().getGzipCmprsnFlag())) {
			sb.append(" mkdir ");
			sb.append(connectionDto.getTgtOtherPropDto().getTempHdfsDir());
			sb.append("/");
			sb.append(requestNo);
			sb.append("-");
			sb.append(dmuHistoryDetail.getTableName());
			sb.append(" ");
			sb.append(" gzip –rk  ");
			sb.append(hdfsPath);
			sb.append("/* ");
			sb.append(connectionDto.getTgtOtherPropDto().getTempHdfsDir());
			sb.append("/");
			sb.append(requestNo);
			sb.append("-");
			sb.append(dmuHistoryDetail.getTableName());
			sb.append(" ");
			sb.append(connectionDto.getTgtOtherPropDto().getHadoopInstallDir());
			sb.append(
					"/bin/hadoop distcp -Dfs.s3a.aws.credentials.provider=org.apache.hadoop.fs.s3a.TemporaryAWSCredentialsProvider -Dfs.s3a.access.key=");
			sb.append(awsCredentials.getAWSAccessKeyId());
			sb.append(" -Dfs.s3a.secret.key=");
			sb.append(awsCredentials.getAWSSecretKey());
			sb.append(" -Dfs.s3a.session.token=");
			sb.append(awsCredentials.getSessionToken());
			sb.append(" ");
			sb.append(hdfsPath);
			sb.append("/* s3a://");
			sb.append(dmuHistoryDetail.getTargetS3Bucket());
			sb.append(" ");
			sb.append(" rm –r ");
			sb.append(connectionDto.getTgtOtherPropDto().getTempHdfsDir());
			sb.append(requestNo);
			sb.append("-");
			sb.append(dmuHistoryDetail.getTableName());
		}
		try {
			StringBuilder sshBuilder = new StringBuilder();
			sshBuilder.append("ssh -i ");
			sb.append(" ");
			sshBuilder.append(connectionDto.getTgtOtherPropDto().getHdfsEdgeNode());
			sb.append(" ");
			sshBuilder.append(connectionDto.getTgtOtherPropDto().getHdfsUserName());
			sb.append(" ");
			sshBuilder.append(connectionDto.getTgtOtherPropDto().getHdfsPemLocation());
			sb.append(sb.toString());
			log.info(" ssh commands executi => " + sb.toString());
			Process p = Runtime.getRuntime().exec(sb.toString());
			InputStreamReader ise = new InputStreamReader(p.getErrorStream());
			BufferedReader bre = new BufferedReader(ise);
			InputStreamReader iso = new InputStreamReader(p.getInputStream());
			BufferedReader bro = new BufferedReader(iso);

			// TODO Store the log details of the above command in a log file (with file name
			// as “Request No + Sr No”.log) on application server.

			String errorLine = null;
			String successLine = null;
			while ((errorLine = bre.readLine()) != null) {
				errorLine = (errorLine + errorLine);
			}
			while ((successLine = bro.readLine()) != null) {
				successLine = (successLine + successLine);
			}
			log.info(
					"called=> ScriptGenerationService ::  proceedScriptGenerationForRequest :: proceedScriptGenerationForRequestHelper :: hiveLocation {} :: busketName :: {} :: success response => {} ",
					hdfsPath, dmuHistoryDetail.getTargetS3Bucket(), successLine);

			log.info(
					"called=> ScriptGenerationService ::  proceedScriptGenerationForRequest :: proceedScriptGenerationForRequestHelper :: hiveLocation {} :: busketName :: {} :: error response => {} ",
					hdfsPath, dmuHistoryDetail.getTargetS3Bucket(), errorLine);
			int exitVal = p.waitFor();
			if (exitVal == 0) {
				dmuHistoryDetail.setStatus(DmuStatusConstants.HttpConstants.SUCCESS.name());
				historyDetailRepository.save(dmuHistoryDetail);
				log.info(
						"called=> ScriptGenerationService ::  proceedScriptGenerationForRequest :: proceedScriptGenerationForRequestHelper :: hiveLocation {} :: busketName :: {} :: status => {} ",
						hdfsPath, dmuHistoryDetail.getTargetS3Bucket(), DmuStatusConstants.HttpConstants.SUCCESS);
			} else {
				dmuHistoryDetail.setStatus(DmuStatusConstants.HttpConstants.FAILURE.name());
				historyDetailRepository.save(dmuHistoryDetail);
				log.info(
						"called=> ScriptGenerationService ::  proceedScriptGenerationForRequest :: proceedScriptGenerationForRequestHelper :: hiveLocation {} :: busketName :: {} :: status => {} ",
						hdfsPath, dmuHistoryDetail.getTargetS3Bucket(), DmuStatusConstants.HttpConstants.FAILURE);
			}
		} catch (Exception exception) {
			log.error(
					"Exception occurred at ScriptGenerationService ::  proceedScriptGenerationForRequest :: proceedScriptGenerationForRequestHelper :: {}   ",
					ExceptionUtils.getStackTrace(exception));
		}
	}
}