package com.dataeconomy.migration.app.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
import com.dataeconomy.migration.app.model.DmuTgtFormatPropDTO;
import com.dataeconomy.migration.app.model.DmuTgtOtherPropDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuS3Entity;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtFormatEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtOtherPropEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryDetailRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuS3Repository;
import com.dataeconomy.migration.app.mysql.repository.DmuTgtFormatPropRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuTgtOtherPropRepository;
import com.dataeconomy.migration.app.util.DmuConstants;
import com.dataeconomy.migration.app.util.DmuStatusConstants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Scope("prototype")
public class DemoTableCopyProcessor {

	@Autowired
	private DmuHistoryDetailRepository historyDetailRepository;

	@Autowired
	private DmuHdfsConnectionService hdfsConnectionService;

	@Autowired
	private DmuAwsConnectionService awsConnectionService;

	@Autowired
	private DmuTgtFormatPropRepository tgtFormatPropRepository;

	@Autowired
	private DmuTgtOtherPropRepository tgtOtherPropRepository;

	@Autowired
	private DmuS3Repository dmuS3Repository;

	public void processTableCopy(String requestNo, Long srNo) {
		log.info(" executed => TableCopySchedulerClass :: requestNp {}, srNo {}", requestNo, srNo);
		try {
			historyDetailRepository.updateForRequestNo(requestNo, srNo);

			List<DmuHistoryDetailEntity> dmuHistoryDetailList = historyDetailRepository
					.findHistoryDetailsByRequestNumberAndSrNo(requestNo, srNo);

			DmuConnectionDTO connectionDto = DmuConnectionDTO.builder().build();
			populateTGTOtherProperties(connectionDto);
			populateTGTFormatProperties(connectionDto);
			populateDMUS3Properties(connectionDto);

			if (dmuHistoryDetailList != null && !dmuHistoryDetailList.isEmpty()) {
				dmuHistoryDetailList.parallelStream().forEach(historyDetailEntity -> {
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
			StringBuilder locationHDFS = new StringBuilder();
			return new JdbcTemplate(hdfsConnectionService.getValidDataSource(DmuConstants.REGULAR)).query(
					"SHOW CREATE TABLE " + dmuHistoryDetail.getSchemaName() + "." + dmuHistoryDetail.getTableName(),
					new ResultSetExtractor<String>() {

						@Override
						public String extractData(ResultSet rs) throws SQLException, DataAccessException {
							while (rs.next()) {
								locationHDFS.append(rs.getString(1));
							}
							return StringUtils
									.substring(locationHDFS.toString(), locationHDFS.toString().indexOf("LOCATION") + 8,
											locationHDFS.toString().indexOf("TBLPROPERTIES") - 1)
									.replaceAll("'", "");
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

	private void processTableCopyHelper(DmuHistoryDetailEntity dmuHistoryDetail, String hdfsPath,
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
		StringBuilder sb = new StringBuilder(400);
		if (DmuConstants.YES.equalsIgnoreCase(connectionDto.getTgtFormatPropDto().getSrcCmprsnFlag())
				|| DmuConstants.YES.equalsIgnoreCase(connectionDto.getTgtFormatPropDto().getUncmprsnFlag())) {
			sb.append(connectionDto.getTgtOtherPropDto().getHadoopInstallDir());
			sb.append("/hadoop distcp ");
			// sb.append(
			// "/hadoop distcp -Dfs.s3a.aws.credentials.provider=");
			// sb.append("\"");
			// sb.append("org.apache.hadoop.fs.s3a.TemporaryAWSCredentialsProvider");
			// sb.append("\"");
			sb.append(" -Dfs.s3a.access.key=");
			sb.append("\"");
			sb.append(connectionDto.getAwsAccessIdLc());
			sb.append("\"");
			sb.append(" -Dfs.s3a.secret.key=");
			sb.append("\"");
			sb.append(connectionDto.getAwsSecretKeyLc());
			// sb.append(awsCredentials.getAWSSecretKey());
			sb.append("\"");
			// sb.append(" -Dfs.s3a.session.token=");
			// sb.append("\"");
			// sb.append(awsCredentials.getSessionToken());
			// sb.append("\"");
			sb.append(" ");
			sb.append(hdfsPath);
			sb.append("/* s3a://");
			sb.append(dmuHistoryDetail.getTargetS3Bucket());
			sb.append(" ");
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
			sb.append("/hadoop distcp -Dfs.s3a.aws.credentials.provider=");
			sb.append("\"");
			sb.append("org.apache.hadoop.fs.s3a.TemporaryAWSCredentialsProvider");
			sb.append("\"");
			sb.append(" -Dfs.s3a.access.key=");
			sb.append("\"");
			sb.append(awsCredentials.getAWSAccessKeyId());
			sb.append("\"");
			sb.append(" -Dfs.s3a.secret.key=");
			sb.append("\"");
			sb.append(awsCredentials.getAWSSecretKey());
			sb.append("\"");
			sb.append(" -Dfs.s3a.session.token=");
			sb.append("\"");
			sb.append(awsCredentials.getSessionToken());
			sb.append("\"");
			sb.append(" ");
			sb.append("/* s3a://");
			sb.append(dmuHistoryDetail.getTargetS3Bucket());
			sb.append(" ");
			sb.append(hdfsPath);
			sb.append(" rm –r ");
			sb.append(connectionDto.getTgtOtherPropDto().getTempHdfsDir());
			sb.append(requestNo);
			sb.append("-");
			sb.append(dmuHistoryDetail.getTableName());
		}
		try {
			StringBuilder sshBuilder = new StringBuilder();
			sshBuilder.append("ssh -i ");
			sshBuilder.append(" ");
			sshBuilder.append(connectionDto.getTgtOtherPropDto().getHdfsPemLocation());
			sshBuilder.append(" ");
			sshBuilder.append(connectionDto.getTgtOtherPropDto().getHdfsUserName());
			if (StringUtils.isNotBlank(connectionDto.getTgtOtherPropDto().getHdfsEdgeNode())) {
				sshBuilder.append("@");
				sshBuilder.append(connectionDto.getTgtOtherPropDto().getHdfsEdgeNode());
				sshBuilder.append(" ");
			} else {
				sshBuilder.append(" ");
			}
			sshBuilder.append(sb.toString());
			log.info(" ssh commands execution => " + sshBuilder.toString());

			Process process;
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			if (isWindows) {
				process = Runtime.getRuntime().exec(sshBuilder.toString());
			} else {
				process = Runtime.getRuntime().exec(sshBuilder.toString());
			}

			/*
			 * DmuStreamGobbler streamGobbler = new
			 * DmuStreamGobbler(process.getInputStream(), System.out::println);
			 * java.util.concurrent.Future<?> future =
			 * Executors.newSingleThreadExecutor().submit(streamGobbler);
			 * System.out.println(future.get().toString()); int exitCode =
			 * process.waitFor(); System.out.println(" exitCode " + exitCode);
			 */

			InputStreamReader ise = new InputStreamReader(process.getErrorStream());
			BufferedReader bre = new BufferedReader(ise);
			InputStreamReader iso = new InputStreamReader(process.getInputStream());
			BufferedReader bro = new BufferedReader(iso);

			String line = null;
			while ((line = bre.readLine()) != null) {
				line = line + line;
			}System.out.println(" line +>>>>>>>>>>>>>>>>>>>>>>>>> " + line);
			while ((line = bro.readLine()) != null) {
				line = line + line;
			}
			int exitVal = process.waitFor();
			System.out.println(" line +>>>>>>>>>>>>>>>>>>>>>>>>> " + line);
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

	public void populateTGTOtherProperties(DmuConnectionDTO connectionDto) {
		Optional<DmuTgtOtherPropEntity> tgtOtherProp = tgtOtherPropRepository.findById(1L);
		if (tgtOtherProp.isPresent()) {
			DmuTgtOtherPropEntity tgtOtherPropObj = tgtOtherProp.get();
			connectionDto.setTgtOtherPropDto(DmuTgtOtherPropDTO.builder()
					.parallelJobs(tgtOtherPropObj.getParallelJobs())
					.parallelUsrRqst(tgtOtherPropObj.getParallelUsrRqst()).tempHiveDB(tgtOtherPropObj.getTempHiveDB())
					.tempHdfsDir(tgtOtherPropObj.getTempHdfsDir()).tokenizationInd(tgtOtherPropObj.getTokenizationInd())
					.ptgyDirPath(tgtOtherPropObj.getPtgyDirPath()).hdfsEdgeNode(tgtOtherPropObj.getHdfsEdgeNode())
					.hdfsPemLocation(tgtOtherPropObj.getHdfsPemLocation())
					.hadoopInstallDir(tgtOtherPropObj.getHadoopInstallDir())
					.hdfsUserName(tgtOtherPropObj.getHdfsUserName()).build());
		} else {
			connectionDto.setTgtOtherPropDto(DmuTgtOtherPropDTO.builder().build());
		}
	}

	private void populateTGTFormatProperties(DmuConnectionDTO connectionDto) {
		try {
			Optional<DmuTgtFormatEntity> tgtFormatPropOpt = tgtFormatPropRepository.findById(1L);
			if (tgtFormatPropOpt.isPresent()) {
				DmuTgtFormatEntity tgtFormatPropEntity = tgtFormatPropOpt.get();
				connectionDto.setTgtFormatPropDto(DmuTgtFormatPropDTO.builder().srNo(tgtFormatPropEntity.getSrNo())
						.srcFormatFlag(tgtFormatPropEntity.getSrcFormatFlag())
						.textFormatFlag(tgtFormatPropEntity.getTextFormatFlag())
						.fieldDelimiter(tgtFormatPropEntity.getFieldDelimiter())
						.sqncFormatFlag(tgtFormatPropEntity.getSqncFormatFlag())
						.rcFormatFlag(tgtFormatPropEntity.getRcFormatFlag())
						.avroFormatFlag(tgtFormatPropEntity.getAvroFormatFlag())
						.orcFormatFlag(tgtFormatPropEntity.getOrcFormatFlag())
						.parquetFormatFlag(tgtFormatPropEntity.getParquetFormatFlag())
						.srcCmprsnFlag(tgtFormatPropEntity.getSrcCmprsnFlag())
						.uncmprsnFlag(tgtFormatPropEntity.getUncmprsnFlag())
						.gzipCmprsnFlag(tgtFormatPropEntity.getGzipCmprsnFlag()).build());
			}
		} catch (Exception exception) {
			log.info(
					" Exception occured at TGTFormatPropRepository :: getAllTGTFormatProp :: requestNumber :: {} :: exception => {} ",
					ExceptionUtils.getStackTrace(exception));
		}

	}

	public void populateDMUS3Properties(DmuConnectionDTO connectionDto) {
		Optional<DmuS3Entity> dmuS3 = dmuS3Repository.findById(1L);
		if (dmuS3.isPresent()) {
			DmuS3Entity dmuS3Obj = dmuS3.get();
			connectionDto.setCredentialStrgType(dmuS3Obj.getCredentialStrgType());
			connectionDto.setConnectionType(dmuS3Obj.getCredentialStrgType());

			connectionDto.setAwsAccessIdLc(dmuS3Obj.getAwsAccessIdLc());
			connectionDto.setAwsSecretKeyLc(dmuS3Obj.getAwsSecretKeyLc());
			connectionDto.setAwsAccessIdSc(dmuS3Obj.getAwsAccessIdSc());
			connectionDto.setScCrdntlAccessType(dmuS3Obj.getScCrdntlAccessType());
			connectionDto.setAwsSecretKeySc(dmuS3Obj.getAwsSecretKeySc());
			connectionDto.setAwsAccessIdSc(dmuS3Obj.getAwsAccessIdLc());
			connectionDto.setRoleArn(dmuS3Obj.getRoleArn());
			connectionDto.setPrincipalArn(dmuS3Obj.getPrincipalArn());
			connectionDto.setSamlProviderArn(dmuS3Obj.getSamlProviderArn());
			connectionDto.setRoleSesnName(dmuS3Obj.getRoleSesnName());
			connectionDto.setPolicyArnMembers(dmuS3Obj.getPolicyArnMembers());
			connectionDto.setExternalId(dmuS3Obj.getExternalId());
			connectionDto.setDuration(dmuS3Obj.getDuration() != null ? Math.toIntExact(dmuS3Obj.getDuration()) : 0);
			connectionDto.setLdapUserName(dmuS3Obj.getLdapUserName());
			connectionDto.setLdapUserPassw(dmuS3Obj.getLdapUserPassw());
			connectionDto.setScCrdntlAccessType(dmuS3Obj.getScCrdntlAccessType());
		}
	}
}