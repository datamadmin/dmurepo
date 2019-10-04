package com.dataeconomy.migration.app.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.BasicSessionCredentials;
import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.connection.DmuAwsConnectionService;
import com.dataeconomy.migration.app.connection.DmuHdfsConnectionService;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.model.DmuTgtFormatPropDTO;
import com.dataeconomy.migration.app.model.DmuTgtOtherPropDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtFormatEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtOtherPropEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryDetailRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuTgtFormatPropRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuTgtOtherPropRepository;
import com.dataeconomy.migration.app.util.DmuConstants;
import com.dataeconomy.migration.app.util.DmuStatusConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScriptGenerationService {

	@Autowired
	private DmuHistoryDetailRepository historyDetailRepository;

	@Autowired
	private DmuHdfsConnectionService hdfsConnectionService;

	@Autowired
	private DmuTgtFormatPropRepository tgtFormatPropRepository;

	@Autowired
	private DmuTgtOtherPropRepository tgtOtherPropRepository;

	@Autowired
	private DmuAwsConnectionService awsConnectionService;

	@Transactional
	@Timed
	public void proceedScriptGenerationForRequest(String requestNo, Long srNo) {
		log.info("ScriptGenerationService ::  proceedScriptGenerationForRequest :: requestNo :: {}  :: srNo {} ",
				requestNo, srNo);
		try {
			Optional<List<DmuHistoryDetailEntity>> dmuHistoryDetailListOpt = Optional
					.ofNullable(historyDetailRepository.findHistoryDetailsByRequestNumberAndSrNo(requestNo, srNo));

			if (dmuHistoryDetailListOpt.isPresent()) {
				dmuHistoryDetailListOpt.get().stream().forEach(dmuHistoryDetail -> {

					dmuHistoryDetail.setStatus(DmuConstants.IN_PROGRESS);
					historyDetailRepository.save(dmuHistoryDetail);

					Optional<DmuTgtFormatEntity> tgtFormatProp = tgtFormatPropRepository.findById(1L);
					Optional<DmuTgtOtherPropEntity> tgtOtherProp = tgtOtherPropRepository.findById(1L);
					DmuConnectionDTO connectionDto = DmuConnectionDTO.builder().build();
					populateTGTFormatProperties(connectionDto, tgtFormatProp);
					populateTGTOtherProperties(connectionDto, tgtOtherProp);

					if (StringUtils.isBlank(dmuHistoryDetail.getFilterCondition())
							&& DmuConstants.NO.equalsIgnoreCase(dmuHistoryDetail.getIncrementalFlag())
							&& DmuConstants.YES
									.equalsIgnoreCase(connectionDto.getTgtFormatPropDto().getSrcFormatFlag())) {
						String hdfspath = invokeHDFSService(dmuHistoryDetail);
						if (StringUtils.isBlank(hdfspath)) {
							dmuHistoryDetail.setStatus(DmuConstants.FAILED);
							historyDetailRepository.save(dmuHistoryDetail);
							// TODO application.log created and store it in server log with date and time
							// Have the error ( “File Path not found in Create Table Statement”) stored in a
							// file (with file name as “Request No + Sr No”.log) on application server. Have
							// a Hyperlink created in the screen for the ”Failed” status value to this error
							// file.
							return;
						} else {
							proceedScriptGenerationForRequestHelper(dmuHistoryDetail, hdfspath, connectionDto,
									requestNo);
						}
					} else if (DmuConstants.YES.equalsIgnoreCase(dmuHistoryDetail.getIncrementalFlag())) {
						dmuHistoryDetail.setStatus(DmuConstants.NEW_SCENARIO);
						historyDetailRepository.save(dmuHistoryDetail);
					} else if (StringUtils.isNotBlank(dmuHistoryDetail.getFilterCondition()) && DmuConstants.YES
							.equalsIgnoreCase(connectionDto.getTgtFormatPropDto().getSrcFormatFlag())) {
						try {
							DataSource dataSource = hdfsConnectionService.getValidDataSource(DmuConstants.SMALLQUERY);
							String path = "";
							String stored = "";
							path = new JdbcTemplate(dataSource).query(
									"SHOW CREATE TABLE " + dmuHistoryDetail.getSchemaName(),
									new ResultSetExtractor<String>() {

										@Override
										public String extractData(ResultSet rs)
												throws SQLException, DataAccessException {
											while (rs.next()) {
												String showTable = rs.getString(DmuConstants.HDFS_LOCATION);
												String storedAs = rs.getString(DmuConstants.STORED_AS);
												if (StringUtils.isNotBlank(showTable)) {
													// path = (showTable.substring(3, showTable.length() - 1).trim());
												}
											}
											return null;
										}
									});
						} catch (Exception exception) {
							dmuHistoryDetail.setStatus(DmuConstants.FAILED);
							historyDetailRepository.save(dmuHistoryDetail);
						}
					}
				});
			}
		} catch (

		Exception exception) {
			log.error(
					"Exception occurred at ScriptGenerationService ::  proceedScriptGenerationForRequest :: requestNo :: {} \n {} ",
					requestNo, ExceptionUtils.getStackTrace(exception));
		}
	}

	private void proceedScriptGenerationForRequestHelper(DmuHistoryDetailEntity dmuHistoryDetail, String hdfsPath,
			DmuConnectionDTO connectionDto, String requestNo) {
		log.info(
				"called=> ScriptGenerationService ::  proceedScriptGenerationForRequest :: proceedScriptGenerationForRequestHelper :: hdfsPath {} :: busketName :: {} ",
				hdfsPath, dmuHistoryDetail.getTargetS3Bucket());
		BasicSessionCredentials awsCredentials = awsConnectionService.getBasicSessionCredentials();
		if (awsCredentials == null) {
			dmuHistoryDetail.setStatus(DmuConstants.FAILED);
			historyDetailRepository.save(dmuHistoryDetail);
			return;
		}
		StringBuilder sb = new StringBuilder(500);
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
			sshBuilder.append(connectionDto.getTgtOtherPropDto().getHdfsPemLocation());
			sb.append(" ");
			sshBuilder.append(connectionDto.getTgtOtherPropDto().getHdfsUserName());
			sb.append(" ");
			sshBuilder.append(connectionDto.getTgtOtherPropDto().getHdfsEdgeNode());
			sb.append(" ");
			sshBuilder.append(sb.toString());
			log.info(" ssh commands execution => " + sshBuilder.toString());
			Process p = Runtime.getRuntime().exec(sshBuilder.toString());
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

	private String invokeHDFSService(DmuHistoryDetailEntity dmuHistoryDetail) {
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

	private String invokeHDFSServiceForFilterCondition(DmuHistoryDetailEntity dmuHistoryDetail) {
		log.info(" called=> ScriptGenerationService ::  proceedScriptGenerationForRequest :: invokeHDFSService ");
		try {
			Map<String, String> map = new HashMap<>();
			new JdbcTemplate(hdfsConnectionService.getValidDataSource(DmuConstants.SMALLQUERY)).query(
					" SELECT COUNT(*) FROM" + dmuHistoryDetail.getSchemaName() + "." + dmuHistoryDetail.getTableName()
							+ "WHERE " + dmuHistoryDetail.getFilterCondition(),
					new ResultSetExtractor<String>() {

						@Override
						public String extractData(ResultSet rs) throws SQLException, DataAccessException {
							while (rs.next()) {
								String showTable = rs.getString(DmuConstants.HDFS_LOCATION);
								String storedAs = rs.getString(DmuConstants.STORED_AS);
								map.put("HDFS_PATH", (showTable.substring(3, showTable.length() - 1).trim()));
								map.put("STORED_AS", (showTable.substring(3, storedAs.length() - 1).trim()));
							}
							return null;
						}
					});
			return null;
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

	private void populateTGTOtherProperties(DmuConnectionDTO connectionDto,
			Optional<DmuTgtOtherPropEntity> tgtOtherProp) {
		if (tgtOtherProp.isPresent()) {
			DmuTgtOtherPropEntity tgtOtherPropObj = tgtOtherProp.get();
			connectionDto.setTgtOtherPropDto(DmuTgtOtherPropDTO.builder()
					.parallelJobs(tgtOtherPropObj.getParallelJobs())
					.parallelUsrRqst(tgtOtherPropObj.getParallelUsrRqst()).tempHiveDB(tgtOtherPropObj.getTempHiveDB())
					.tempHdfsDir(tgtOtherPropObj.getTempHdfsDir()).tokenizationInd(tgtOtherPropObj.getTokenizationInd())
					.ptgyDirPath(tgtOtherPropObj.getPtgyDirPath()).hdfsEdgeNode(tgtOtherPropObj.getHdfsEdgeNode())
					.hdfsPemLocation(tgtOtherPropObj.getHdfsPemLocation())
					.hdfsUserName(tgtOtherPropObj.getHdfsUserName())
					.hadoopInstallDir(tgtOtherPropObj.getHadoopInstallDir()).build());
		}
	}

	private void populateTGTFormatProperties(DmuConnectionDTO connectionDto,
			Optional<DmuTgtFormatEntity> tgtFormatProp) {
		if (tgtFormatProp.isPresent()) {
			DmuTgtFormatEntity tgtFormatPropObj = tgtFormatProp.get();
			connectionDto.setTgtFormatPropDto(DmuTgtFormatPropDTO.builder()
					.textFormatFlag(tgtFormatPropObj.getTextFormatFlag())
					.srcFormatFlag(tgtFormatPropObj.getSrcFormatFlag())
					.fieldDelimiter(tgtFormatPropObj.getFieldDelimiter())
					.sqncFormatFlag(tgtFormatPropObj.getSqncFormatFlag())
					.srcCmprsnFlag(tgtFormatPropObj.getSrcCmprsnFlag()).rcFormatFlag(tgtFormatPropObj.getRcFormatFlag())
					.avroFormatFlag(tgtFormatPropObj.getAvroFormatFlag())
					.orcFormatFlag(tgtFormatPropObj.getOrcFormatFlag())
					.parquetFormatFlag(tgtFormatPropObj.getParquetFormatFlag())
					.srcCmprsnFlag(tgtFormatPropObj.getSrcCmprsnFlag()).uncmprsnFlag(tgtFormatPropObj.getUncmprsnFlag())
					.gzipCmprsnFlag(tgtFormatPropObj.getGzipCmprsnFlag()).build());
		}
	}

}
