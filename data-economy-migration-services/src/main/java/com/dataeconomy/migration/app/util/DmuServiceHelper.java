package com.dataeconomy.migration.app.util;

import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.BasicSessionCredentials;
import com.dataeconomy.migration.app.connection.DmuHdfsConnectionService;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.model.DmuTgtFormatPropTempDTO;
import com.dataeconomy.migration.app.model.DmuTgtOtherPropDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuAuthenticationEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuHdfsEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtFormatEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtOtherPropEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuAuthenticationRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuHdfsRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuS3Repository;
import com.dataeconomy.migration.app.mysql.repository.DmuTgtFormatPropRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuTgtOtherPropRepository;
import com.dataeconomy.migration.app.service.DmuTgtOtherPropService;

import lombok.extern.slf4j.Slf4j;

@Component("dmuServiceHelper")
@Slf4j
public class DmuServiceHelper {

	@Autowired
	DmuHdfsRepository hdfsRepository;

	@Autowired
	DmuS3Repository dmuS3Repository;

	@Autowired
	DmuTgtFormatPropRepository tgtFormatPropRepository;

	@Autowired
	DmuTgtOtherPropRepository tgtOtherPropRepository;

	@Autowired
	DmuHdfsConnectionService hdfsConnectionService;

	@Autowired
	DmuAuthenticationRepository authenticationRepository;

	@Autowired
	DmuTgtOtherPropService tgtOtherPropService;

	Map<String, String> propertiesMap = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {

		tgtFormatPropRepository.findById(1L).ifPresent(entity -> {

			if (StringUtils.isNotBlank(entity.getSrcCmprsnFlag())) {
				propertiesMap.put("SRC_CMPRSN_FLAG", entity.getSrcCmprsnFlag());
			} else {
				propertiesMap.put("SRC_CMPRSN_FLAG", StringUtils.EMPTY);
			}

			if (StringUtils.isNotBlank(entity.getGzipCmprsnFlag())) {
				propertiesMap.put("GZIP_CMPRSN_FLAG", entity.getGzipCmprsnFlag());
			} else {
				propertiesMap.put("GZIP_CMPRSN_FLAG", StringUtils.EMPTY);
			}

			if (StringUtils.isNotBlank(entity.getFieldDelimiter())) {
				propertiesMap.put("FIELD_DELIMITER", entity.getGzipCmprsnFlag());
			} else {
				propertiesMap.put("FIELD_DELIMITER", StringUtils.EMPTY);
			}

			if (StringUtils.isNotBlank(entity.getSrcFormatFlag())) {
				propertiesMap.put("SRC_FORMAT_FLAG", entity.getSrcFormatFlag());
			} else {
				propertiesMap.put("SRC_FORMAT_FLAG", StringUtils.EMPTY);
			}

		});

		dmuS3Repository.findById(1L).ifPresent(entity -> {
			propertiesMap.put("CREDENTIAL_STRG_TYPE", entity.getCredentialStrgType());
			if (StringUtils.isNotBlank(entity.getAwsAccessIdLc())) {
				propertiesMap.put("AWS_ACCESS_ID_LC", entity.getAwsAccessIdLc());
			} else {
				propertiesMap.put("AWS_ACCESS_ID_LC", StringUtils.EMPTY);
			}
			if (StringUtils.isNotBlank(entity.getAwsSecretKeyLc())) {
				propertiesMap.put("AWS_SECRET_KEY_LC", entity.getAwsSecretKeyLc());
			} else {
				propertiesMap.put("AWS_SECRET_KEY_LC", StringUtils.EMPTY);
			}
		});

		tgtOtherPropRepository.findById(1L).ifPresent(entity -> {
			propertiesMap.put("PARALLEL_JOBS", String.valueOf(entity.getParallelJobs()));
			propertiesMap.put("PARALLEL_USR_RQST", String.valueOf(entity.getParallelUsrRqst()));
			if (StringUtils.isNotBlank(entity.getTempHdfsDir())) {
				propertiesMap.put("TEMP_HDFS_DIR", entity.getTempHdfsDir());
			} else {
				propertiesMap.put("TEMP_HDFS_DIR", StringUtils.EMPTY);
			}

			if (StringUtils.isNotBlank(entity.getTempHiveDB())) {
				propertiesMap.put("TEMP_HIVE_DB", entity.getTempHiveDB());
			} else {
				propertiesMap.put("TEMP_HIVE_DB", StringUtils.EMPTY);
			}

			if (StringUtils.isNotBlank(entity.getHdfsEdgeNode())) {
				propertiesMap.put("HDFS_EDGE_NODE", entity.getHdfsEdgeNode());
			} else {
				propertiesMap.put("HDFS_EDGE_NODE", StringUtils.EMPTY);
			}

			if (StringUtils.isNotBlank(entity.getHdfsPemLocation())) {
				propertiesMap.put("HDFS_PEM_LOCATION", entity.getHdfsPemLocation());
			} else {
				propertiesMap.put("HDFS_PEM_LOCATION", StringUtils.EMPTY);
			}

			if (StringUtils.isNotBlank(entity.getHadoopInstallDir())) {
				propertiesMap.put("HADOOP_INSTALL_DIR", entity.getHadoopInstallDir());
			} else {
				propertiesMap.put("HADOOP_INSTALL_DIR", StringUtils.EMPTY);
			}

			if (StringUtils.isNotBlank(entity.getHdfsUserName())) {
				propertiesMap.put("HDFS_USER_NAME", entity.getHdfsUserName());
			} else {
				propertiesMap.put("HDFS_USER_NAME", StringUtils.EMPTY);
			}

		});
	}

	public void saveTGTOtherProperties(DmuConnectionDTO connectionDto) {
		try {
			if (connectionDto.getTgtOtherPropDto() != null) {
				Optional<DmuTgtOtherPropEntity> tgtOtherPropOpt = tgtOtherPropRepository.findById(1L);
				if (tgtOtherPropOpt.isPresent()) {
					DmuTgtOtherPropEntity tgtOtherPropOptEntity = tgtOtherPropOpt.get();
					tgtOtherPropOptEntity.setParallelJobs(connectionDto.getTgtOtherPropDto().getParallelJobs());
					tgtOtherPropOptEntity.setParallelUsrRqst(connectionDto.getTgtOtherPropDto().getParallelUsrRqst());
					tgtOtherPropOptEntity.setTempHiveDB(connectionDto.getTgtOtherPropDto().getTempHiveDB());
					tgtOtherPropOptEntity.setTempHdfsDir(connectionDto.getTgtOtherPropDto().getTempHdfsDir());
					tgtOtherPropOptEntity.setTokenizationInd(connectionDto.getTgtOtherPropDto().getTokenizationInd());
					tgtOtherPropOptEntity.setPtgyDirPath(connectionDto.getTgtOtherPropDto().getPtgyDirPath());
					tgtOtherPropOptEntity.setHdfsEdgeNode(connectionDto.getTgtOtherPropDto().getHdfsEdgeNode());
					tgtOtherPropOptEntity.setHdfsUserName(connectionDto.getTgtOtherPropDto().getHdfsUserName());
					tgtOtherPropOptEntity.setHdfsPemLocation(connectionDto.getTgtOtherPropDto().getHdfsPemLocation());
					tgtOtherPropOptEntity.setHadoopInstallDir(connectionDto.getTgtOtherPropDto().getHadoopInstallDir());
					tgtOtherPropRepository.save(tgtOtherPropOptEntity);
				}
			}
		} catch (Exception exception) {
			log.info(" Exception occured at TGTOtherPropService :: getAllTGTOtherProp {} ",
					ExceptionUtils.getStackTrace(exception));
		}
	}

	public void saveTGTFormatProperties(DmuConnectionDTO connectionDto) {
		if (connectionDto.getTgtFormatPropTempDto() != null) {
			DmuTgtFormatPropTempDTO tgtFormatPropObj = connectionDto.getTgtFormatPropTempDto();
			DmuTgtFormatEntity tgtFormatPropEntity = tgtFormatPropRepository.getOne(1L);
			if (tgtFormatPropEntity != null) {

				tgtFormatPropEntity.setTextFormatFlag(DmuConstants.NO);
				tgtFormatPropEntity.setFieldDelimiter(null);
				tgtFormatPropEntity.setSrcFormatFlag(DmuConstants.NO);
				tgtFormatPropEntity.setSqncFormatFlag(DmuConstants.NO);
				tgtFormatPropEntity.setRcFormatFlag(DmuConstants.NO);
				tgtFormatPropEntity.setAvroFormatFlag(DmuConstants.NO);
				tgtFormatPropEntity.setOrcFormatFlag(DmuConstants.NO);
				tgtFormatPropEntity.setParquetFormatFlag(DmuConstants.NO);
				tgtFormatPropEntity.setSrcCmprsnFlag(DmuConstants.NO);
				tgtFormatPropEntity.setUncmprsnFlag(DmuConstants.NO);
				tgtFormatPropEntity.setGzipCmprsnFlag(DmuConstants.NO);

				if (StringUtils.isNotBlank(tgtFormatPropObj.getFieldDelimiter())) {
					tgtFormatPropEntity.setFieldDelimiter(tgtFormatPropObj.getFieldDelimiter());
				}

				if (StringUtils.equalsIgnoreCase(DmuConstants.SOURCE, tgtFormatPropObj.getFormatType())) {
					tgtFormatPropEntity.setSrcFormatFlag(DmuConstants.YES);
				} else if (StringUtils.equalsIgnoreCase(DmuConstants.TEXT, tgtFormatPropObj.getFormatType())) {
					tgtFormatPropEntity.setTextFormatFlag(DmuConstants.YES);
				} else if (StringUtils.equalsIgnoreCase(DmuConstants.SEQUENCE, tgtFormatPropObj.getFormatType())) {
					tgtFormatPropEntity.setSqncFormatFlag(DmuConstants.YES);
				} else if (StringUtils.equalsIgnoreCase(DmuConstants.RECORD_COLUMNAR,
						tgtFormatPropObj.getFormatType())) {
					tgtFormatPropEntity.setRcFormatFlag(DmuConstants.YES);
				} else if (StringUtils.equalsIgnoreCase(DmuConstants.ORC, tgtFormatPropObj.getFormatType())) {
					tgtFormatPropEntity.setOrcFormatFlag(DmuConstants.YES);
				} else if (StringUtils.equalsIgnoreCase(DmuConstants.PARQUET, tgtFormatPropObj.getFormatType())) {
					tgtFormatPropEntity.setParquetFormatFlag(DmuConstants.YES);
				} else if (StringUtils.equalsIgnoreCase(DmuConstants.AVRO, tgtFormatPropObj.getFormatType())) {
					tgtFormatPropEntity.setAvroFormatFlag(DmuConstants.YES);
				} else if (StringUtils.equalsIgnoreCase(DmuConstants.SRC_COMPRESSION,
						tgtFormatPropObj.getFormatType())) {
					tgtFormatPropEntity.setSrcCmprsnFlag(DmuConstants.YES);
				}

				if (tgtFormatPropObj.getCompressionType() != null
						&& tgtFormatPropObj.getCompressionType().equalsIgnoreCase(DmuConstants.SRC_COMPRESSION)) {
					tgtFormatPropEntity.setSrcCmprsnFlag(DmuConstants.YES);
				} else if (StringUtils.equalsIgnoreCase(DmuConstants.UN_COMPRESSED,
						tgtFormatPropObj.getCompressionType())) {
					tgtFormatPropEntity.setUncmprsnFlag(DmuConstants.YES);
				} else if (StringUtils.equalsIgnoreCase(DmuConstants.GZIP, tgtFormatPropObj.getCompressionType())) {
					tgtFormatPropEntity.setGzipCmprsnFlag(DmuConstants.YES);
				} else {
					tgtFormatPropEntity.setSrcCmprsnFlag(DmuConstants.NO);
				}
				System.out.println("**Compressiontype***" + tgtFormatPropObj.getCompressionType());
				tgtFormatPropRepository.save(tgtFormatPropEntity);
			}
		}
	}

	public void saveDMUHdfsEntityProperties(DmuConnectionDTO connectionDto) {
		hdfsRepository.findById(1L).ifPresent(dmuHdfsEntity -> {

			if (connectionDto.isHiveConnEnabled()) {
				dmuHdfsEntity.setHiveCnctnFlag(DmuConstants.YES);
				dmuHdfsEntity.setHiveHostName(connectionDto.getHiveHostName());
				dmuHdfsEntity.setHivePortNmbr(NumberUtils.toLong(connectionDto.getHivePortNmbr(), 0L));
			} else {
				dmuHdfsEntity.setHiveCnctnFlag(DmuConstants.NO);
				dmuHdfsEntity.setHiveHostName(null);
				dmuHdfsEntity.setHivePortNmbr(null);
			}

			if (connectionDto.isImpalaConnEnabled()) {
				dmuHdfsEntity.setImpalaCnctnFlag(DmuConstants.YES);
				dmuHdfsEntity.setImpalaHostName(connectionDto.getImpalaHostName());
				dmuHdfsEntity.setImpalaPortNmbr(NumberUtils.toLong(connectionDto.getImpalaPortNmbr(), 0L));
			} else {
				dmuHdfsEntity.setImpalaCnctnFlag(DmuConstants.NO);
				dmuHdfsEntity.setImpalaHostName(null);
				dmuHdfsEntity.setImpalaPortNmbr(null);
			}

			if (connectionDto.isSparkConnEnabled()) {
				dmuHdfsEntity.setSparkCnctnFlag(DmuConstants.YES);
				dmuHdfsEntity.setHiveMsUri(connectionDto.getHiveMsUri());
				dmuHdfsEntity.setSqlWhDir(connectionDto.getSqlWhDir());
			} else {
				dmuHdfsEntity.setSparkCnctnFlag(DmuConstants.NO);
				dmuHdfsEntity.setSqlWhDir(null);
				dmuHdfsEntity.setHiveMsUri(null);
			}

			hdfsRepository.save(dmuHdfsEntity);
		});
	}

	public void saveDMUS3Properties(DmuConnectionDTO connectionDto) {
		dmuS3Repository.findById(1L).ifPresent(dmuS3Entity -> {

			dmuS3Entity.setCredentialStrgType(null);
			dmuS3Entity.setAwsAccessIdLc(null);
			dmuS3Entity.setAwsSecretKeyLc(null);
			dmuS3Entity.setAwsAccessIdSc(null);
			dmuS3Entity.setAwsSecretKeySc(null);
			dmuS3Entity.setRoleArn(null);
			dmuS3Entity.setPrincipalArn(null);
			dmuS3Entity.setSamlProviderArn(null);
			dmuS3Entity.setRoleSesnName(null);
			dmuS3Entity.setPolicyArnMembers(null);
			dmuS3Entity.setExternalId(null);
			dmuS3Entity.setFdrtdUserName(null);
			dmuS3Entity.setInlineSesnPolicy(null);
			dmuS3Entity.setDuration(0L);
			dmuS3Entity.setLdapUserName(null);
			dmuS3Entity.setLdapDomain(null);
			dmuS3Entity.setLdapUserName(null);
			dmuS3Entity.setLdapUserPassw(null);
			dmuS3Entity.setScCrdntlAccessType(null);

			if (StringUtils.equalsIgnoreCase(DmuConstants.DIRECT_HDFS, connectionDto.getConnectionType())) {
				dmuS3Entity.setCredentialStrgType(DmuConstants.DIRECT_HDFS);
			} else if (StringUtils.equalsIgnoreCase(DmuConstants.DIRECT_LC, connectionDto.getConnectionType())) {
				dmuS3Entity.setCredentialStrgType(DmuConstants.DIRECT_LC);
				dmuS3Entity.setAwsAccessIdLc(connectionDto.getAwsAccessIdLc());
				dmuS3Entity.setAwsSecretKeyLc(connectionDto.getAwsSecretKeyLc());
			} else if (StringUtils.equalsIgnoreCase(DmuConstants.DIRECT_SC, connectionDto.getConnectionType())) {
				dmuS3Entity.setCredentialStrgType(DmuConstants.DIRECT_SC);
				dmuS3Entity.setAwsAccessIdSc(connectionDto.getAwsAccessIdSc());
				dmuS3Entity.setAwsSecretKeySc(connectionDto.getAwsSecretKeySc());
				dmuS3Entity.setRoleArn(connectionDto.getRoleArn());
				dmuS3Entity.setPrincipalArn(connectionDto.getPrincipalArn());
				dmuS3Entity.setSamlProviderArn(connectionDto.getSamlProviderArn());
				dmuS3Entity.setRoleSesnName(connectionDto.getRoleSesnName());
				dmuS3Entity.setPolicyArnMembers(connectionDto.getRoleArn());
				dmuS3Entity.setRoleArn(connectionDto.getRoleArn());
				if (StringUtils.equalsIgnoreCase(connectionDto.getScCrdntlAccessType(), DmuConstants.ASSUME)) {
					dmuS3Entity.setScCrdntlAccessType(DmuConstants.ASSUME);
				} else if (StringUtils.equalsIgnoreCase(connectionDto.getScCrdntlAccessType(),
						DmuConstants.ASSUME_SAML)) {
					dmuS3Entity.setScCrdntlAccessType(DmuConstants.ASSUME_SAML);
				} else if (StringUtils.equalsIgnoreCase(DmuConstants.AWS_FEDERATED_USER,
						connectionDto.getScCrdntlAccessType())) {
					dmuS3Entity.setScCrdntlAccessType(DmuConstants.AWS_FEDERATED_USER);
				}
			}
			dmuS3Repository.save(dmuS3Entity);
		});
	}

	public void populateDMUAuthenticationProperties(DmuConnectionDTO connectionDto) {
		Optional<DmuAuthenticationEntity> dmuAuthentication = authenticationRepository.findById(1L);
		log.info(" => dmuAuthentication " + dmuAuthentication);
		if (dmuAuthentication.isPresent()) {
			DmuAuthenticationEntity dmuAuthenticationObj = dmuAuthentication.get();
			connectionDto.setAuthenticationType(dmuAuthenticationObj.getAuthenticationType());
			connectionDto.setLdapCnctnFlag(dmuAuthenticationObj.getLdapCnctnFlag());
			connectionDto.setKerberosCnctnFlag(dmuAuthenticationObj.getKerberosCnctnFlag());
		}
	}

	public void populateDMUHdfsProperties(DmuConnectionDTO connectionDto) {
		Optional<DmuHdfsEntity> dmuHdfs = hdfsRepository.findById(1L);
		if (dmuHdfs.isPresent()) {
			DmuHdfsEntity dmuHdfsObj = dmuHdfs.get();
			if (DmuConstants.YES.equalsIgnoreCase(dmuHdfsObj.getHiveCnctnFlag())) {
				connectionDto.setHiveConnEnabled(true);
			}
			if (DmuConstants.YES.equalsIgnoreCase(dmuHdfsObj.getImpalaCnctnFlag())) {
				connectionDto.setImpalaConnEnabled(true);
			}
			if (DmuConstants.YES.equalsIgnoreCase(dmuHdfsObj.getSparkCnctnFlag())) {
				connectionDto.setSparkConnEnabled(true);
			}
			connectionDto.setHiveCnctnFlag(dmuHdfsObj.getHiveCnctnFlag());
			connectionDto.setHiveHostName(dmuHdfsObj.getHiveHostName());
			connectionDto.setHivePortNmbr(
					dmuHdfsObj.getHivePortNmbr() != null ? String.valueOf(dmuHdfsObj.getHivePortNmbr()) : "");

			connectionDto.setImpalaCnctnFlag(dmuHdfsObj.getImpalaCnctnFlag());
			connectionDto.setImpalaPortNmbr(
					dmuHdfsObj.getImpalaPortNmbr() != null ? String.valueOf(dmuHdfsObj.getImpalaPortNmbr()) : "");
			connectionDto.setImpalaHostName(dmuHdfsObj.getImpalaHostName());
			connectionDto.setHiveMsUri(dmuHdfsObj.getHiveMsUri());
			connectionDto.setSqlWhDir(dmuHdfsObj.getSqlWhDir());
			connectionDto.setImpalaCnctnFlag(dmuHdfsObj.getImpalaCnctnFlag());
			connectionDto.setSparkCnctnFlag(dmuHdfsObj.getSparkCnctnFlag());
		}
	}

	public void populateDMUS3Properties(DmuConnectionDTO connectionDto) {
		dmuS3Repository.findById(1L).ifPresent(dmuS3Obj -> {
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
		});
	}

	public void populateTGTOtherProperties(DmuConnectionDTO connectionDto) {
		tgtOtherPropRepository.findById(1L)
				.ifPresent(tgtOtherPropObj -> connectionDto.setTgtOtherPropDto(DmuTgtOtherPropDTO.builder()
						.parallelJobs(tgtOtherPropObj.getParallelJobs())
						.parallelUsrRqst(tgtOtherPropObj.getParallelUsrRqst())
						.tempHiveDB(tgtOtherPropObj.getTempHiveDB()).tempHdfsDir(tgtOtherPropObj.getTempHdfsDir())
						.tokenizationInd(tgtOtherPropObj.getTokenizationInd())
						.ptgyDirPath(tgtOtherPropObj.getPtgyDirPath()).hdfsEdgeNode(tgtOtherPropObj.getHdfsEdgeNode())
						.hdfsPemLocation(tgtOtherPropObj.getHdfsPemLocation())
						.hadoopInstallDir(tgtOtherPropObj.getHadoopInstallDir())
						.hdfsUserName(tgtOtherPropObj.getHdfsUserName()).build()));
	}

	public void populateTGTFormatProperties(DmuConnectionDTO connectionDto) {
		Optional<DmuTgtFormatEntity> tgtFormatProp = tgtFormatPropRepository.findById(1L);
		if (tgtFormatProp.isPresent()) {
			DmuTgtFormatEntity tgtFormatPropObj = tgtFormatProp.get();
			DmuTgtFormatPropTempDTO tgtFormatPropTempDto = DmuTgtFormatPropTempDTO.builder().build();
			if (StringUtils.isNotBlank(tgtFormatPropObj.getSrcFormatFlag())
					&& tgtFormatPropObj.getSrcFormatFlag().equalsIgnoreCase(DmuConstants.YES)) {
				tgtFormatPropTempDto.setFormatType(DmuConstants.SOURCE);
			} else if (StringUtils.isNotBlank(tgtFormatPropObj.getTextFormatFlag())
					&& tgtFormatPropObj.getTextFormatFlag().equalsIgnoreCase(DmuConstants.YES)) {
				tgtFormatPropTempDto.setFormatType(DmuConstants.TEXT);
			} else if (StringUtils.isNotBlank(tgtFormatPropObj.getSqncFormatFlag())
					&& tgtFormatPropObj.getSqncFormatFlag().equalsIgnoreCase(DmuConstants.YES)) {
				tgtFormatPropTempDto.setFormatType(DmuConstants.SEQUENCE);
			} else if (StringUtils.isNotBlank(tgtFormatPropObj.getRcFormatFlag())
					&& tgtFormatPropObj.getRcFormatFlag().equalsIgnoreCase(DmuConstants.YES)) {
				tgtFormatPropTempDto.setFormatType(DmuConstants.RECORD_COLUMNAR);
			} else if (StringUtils.isNotBlank(tgtFormatPropObj.getOrcFormatFlag())
					&& tgtFormatPropObj.getOrcFormatFlag().equalsIgnoreCase(DmuConstants.YES)) {
				tgtFormatPropTempDto.setFormatType(DmuConstants.ORC);
			} else if (StringUtils.isNotBlank(tgtFormatPropObj.getParquetFormatFlag())
					&& tgtFormatPropObj.getParquetFormatFlag().equalsIgnoreCase(DmuConstants.YES)) {
				tgtFormatPropTempDto.setFormatType(DmuConstants.PARQUET);
			} else if (StringUtils.isNotBlank(tgtFormatPropObj.getAvroFormatFlag())
					&& tgtFormatPropObj.getAvroFormatFlag().equalsIgnoreCase(DmuConstants.YES)) {
				tgtFormatPropTempDto.setFormatType(DmuConstants.AVRO);
			}

			if (StringUtils.isNotBlank(tgtFormatPropObj.getFieldDelimiter())) {
				tgtFormatPropTempDto.setFieldDelimiter(tgtFormatPropObj.getFieldDelimiter());
			}

			if (StringUtils.isNotBlank(tgtFormatPropObj.getUncmprsnFlag())
					&& tgtFormatPropObj.getUncmprsnFlag().equalsIgnoreCase(DmuConstants.YES)) {
				tgtFormatPropTempDto.setCompressionType(DmuConstants.UN_COMPRESSED);
			} else if (StringUtils.isNotBlank(tgtFormatPropObj.getGzipCmprsnFlag())
					&& tgtFormatPropObj.getGzipCmprsnFlag().equalsIgnoreCase(DmuConstants.YES)) {
				tgtFormatPropTempDto.setCompressionType(DmuConstants.GZIP);
			} else if (StringUtils.isNotBlank(tgtFormatPropObj.getSrcCmprsnFlag())
					&& tgtFormatPropObj.getSrcCmprsnFlag().equalsIgnoreCase(DmuConstants.YES)) {
				tgtFormatPropTempDto.setCompressionType(DmuConstants.SRC_COMPRESSION);
			}

			connectionDto.setTgtFormatPropTempDto(tgtFormatPropTempDto);
		}
	}

	public String getProperty(String propertyName) {
		return propertiesMap.get(propertyName);
	}

	public synchronized String buildS3MigrationUrl(DmuHistoryDetailEntity historyEntity,
			BasicSessionCredentials awsCredentials, String hdfsPath) {
		StringBuilder urlBuilder = new StringBuilder(800);
		if (DmuConstants.YES.equalsIgnoreCase(propertiesMap.get(DmuConstants.SRC_CMPRSN_FLAG))
				|| DmuConstants.YES.equalsIgnoreCase(propertiesMap.get(DmuConstants.UNCMPRSN_FLAG))) {
			urlBuilder.append(propertiesMap.get(DmuConstants.HADOOP_INSTALL_DIR));
			urlBuilder.append("/hadoop distcp ");
			urlBuilder.append(" -Dfs.s3a.access.key=");
			urlBuilder.append("\"");
			if (DmuConstants.DIRECT_LC.equalsIgnoreCase(propertiesMap.get(DmuConstants.CREDENTIAL_STRG_TYPE))) {
				urlBuilder.append(propertiesMap.get(DmuConstants.AWS_ACCESS_ID_LC));
			} else {
				urlBuilder.append(awsCredentials.getAWSAccessKeyId());
			}
			urlBuilder.append("\"");
			urlBuilder.append(" -Dfs.s3a.secret.key=");
			urlBuilder.append("\"");
			if (DmuConstants.DIRECT_LC.equalsIgnoreCase(propertiesMap.get(DmuConstants.CREDENTIAL_STRG_TYPE))) {
				urlBuilder.append(propertiesMap.get(DmuConstants.AWS_SECRET_KEY_LC));
			} else {
				urlBuilder.append(awsCredentials.getAWSSecretKey());
			}
			urlBuilder.append("\"");
			if (!DmuConstants.DIRECT_LC.equalsIgnoreCase(propertiesMap.get(DmuConstants.CREDENTIAL_STRG_TYPE))) {
				urlBuilder.append(" -Dfs.s3a.session.token=");
				urlBuilder.append("\"");
				urlBuilder.append(awsCredentials.getSessionToken());
				urlBuilder.append("\"");
			}
			urlBuilder.append(" ");
			urlBuilder.append(hdfsPath);
			urlBuilder.append("/* s3a://");
			urlBuilder.append(historyEntity.getTargetS3Bucket());
			urlBuilder.append(" ");
		} else if (DmuConstants.YES.equalsIgnoreCase(propertiesMap.get(DmuConstants.GZIP_CMPRSN_FLAG))) {
			urlBuilder.append(" mkdir ");
			urlBuilder.append(propertiesMap.get(DmuConstants.TEMP_HDFS_DIR));
			urlBuilder.append("/");
			urlBuilder.append(historyEntity.getDmuHIstoryDetailPK().getRequestNo());
			urlBuilder.append("-");
			urlBuilder.append(historyEntity.getTableName());
			urlBuilder.append(" ");
			urlBuilder.append(" gzip –rk  ");
			urlBuilder.append(hdfsPath);
			urlBuilder.append("/* ");
			urlBuilder.append(propertiesMap.get(DmuConstants.TEMP_HDFS_DIR));
			urlBuilder.append("/");
			urlBuilder.append(historyEntity.getDmuHIstoryDetailPK().getRequestNo());
			urlBuilder.append("-");
			urlBuilder.append(historyEntity.getTableName());
			urlBuilder.append(" ");
			urlBuilder.append(propertiesMap.get(DmuConstants.HADOOP_INSTALL_DIR));
			urlBuilder.append("/hadoop distcp -Dfs.s3a.aws.credentials.provider=");
			urlBuilder.append("\"");
			urlBuilder.append("org.apache.hadoop.fs.s3a.TemporaryAWSCredentialsProvider");
			urlBuilder.append("\"");
			urlBuilder.append(" -Dfs.s3a.access.key=");
			urlBuilder.append("\"");
			urlBuilder.append(awsCredentials.getAWSAccessKeyId());
			urlBuilder.append("\"");
			urlBuilder.append(" -Dfs.s3a.secret.key=");
			urlBuilder.append("\"");
			urlBuilder.append(awsCredentials.getAWSSecretKey());
			urlBuilder.append("\"");
			urlBuilder.append(" -Dfs.s3a.session.token=");
			urlBuilder.append("\"");
			urlBuilder.append(awsCredentials.getSessionToken());
			urlBuilder.append("\"");
			urlBuilder.append(" ");
			urlBuilder.append("/* s3a://");
			urlBuilder.append(historyEntity.getTargetS3Bucket());
			urlBuilder.append(" ");
			urlBuilder.append(hdfsPath);
			urlBuilder.append(" rm –r ");
			urlBuilder.append(propertiesMap.get(DmuConstants.TEMP_HDFS_DIR));
			urlBuilder.append(historyEntity.getDmuHIstoryDetailPK().getRequestNo());
			urlBuilder.append("-");
			urlBuilder.append(historyEntity.getTableName());
		}

		return urlBuilder.toString();
	}

	public synchronized String buildS3MigrationUrlForFilterCondition(DmuHistoryDetailEntity historyEntity,
			BasicSessionCredentials awsCredentials) {
		StringBuilder urlBuilder = new StringBuilder(800);
		if (DmuConstants.YES.equalsIgnoreCase(propertiesMap.get(DmuConstants.SRC_CMPRSN_FLAG))
				|| DmuConstants.YES.equalsIgnoreCase(propertiesMap.get(DmuConstants.UNCMPRSN_FLAG))) {

			urlBuilder.append(propertiesMap.get(DmuConstants.HADOOP_INSTALL_DIR));
			urlBuilder.append("/hadoop distcp ");
			urlBuilder.append(" -Dfs.s3a.access.key=");
			urlBuilder.append("\"");
			if (DmuConstants.DIRECT_LC.equalsIgnoreCase(propertiesMap.get(DmuConstants.CREDENTIAL_STRG_TYPE))) {
				urlBuilder.append(propertiesMap.get(DmuConstants.AWS_ACCESS_ID_LC));
			} else {
				urlBuilder.append(awsCredentials.getAWSAccessKeyId());
			}
			urlBuilder.append("\"");
			urlBuilder.append(" -Dfs.s3a.secret.key=");
			urlBuilder.append("\"");
			if (DmuConstants.DIRECT_LC.equalsIgnoreCase(propertiesMap.get(DmuConstants.CREDENTIAL_STRG_TYPE))) {
				urlBuilder.append(propertiesMap.get(DmuConstants.AWS_SECRET_KEY_LC));
			} else {
				urlBuilder.append(awsCredentials.getAWSSecretKey());
			}
			urlBuilder.append("\"");
			if (!DmuConstants.DIRECT_LC.equalsIgnoreCase(propertiesMap.get(DmuConstants.CREDENTIAL_STRG_TYPE))) {
				urlBuilder.append(" -Dfs.s3a.session.token=");
				urlBuilder.append("\"");
				urlBuilder.append(awsCredentials.getSessionToken());
				urlBuilder.append("\"");
			}
			urlBuilder.append(" ");
			urlBuilder.append(propertiesMap.get(DmuConstants.TEMP_HDFS_DIR));
			urlBuilder.append("/");
			urlBuilder.append(historyEntity.getDmuHIstoryDetailPK().getRequestNo());
			urlBuilder.append("-");
			urlBuilder.append(historyEntity.getTableName());
			urlBuilder.append(" ");
			urlBuilder.append("/* s3a://");
			urlBuilder.append(historyEntity.getTargetS3Bucket());
			urlBuilder.append(" ");
		} else if (DmuConstants.YES.equalsIgnoreCase(propertiesMap.get(DmuConstants.GZIP_CMPRSN_FLAG))) {

			urlBuilder.append(" gzip –rk  ");
			urlBuilder.append(propertiesMap.get(DmuConstants.TEMP_HDFS_DIR));
			urlBuilder.append("/");
			urlBuilder.append(historyEntity.getDmuHIstoryDetailPK().getRequestNo());
			urlBuilder.append("-");
			urlBuilder.append(historyEntity.getTableName());
			urlBuilder.append("/* ");

			urlBuilder.append(propertiesMap.get(DmuConstants.HADOOP_INSTALL_DIR));
			urlBuilder.append("/hadoop distcp ");
			urlBuilder.append(" -Dfs.s3a.access.key=");
			urlBuilder.append("\"");
			if (DmuConstants.DIRECT_LC.equalsIgnoreCase(propertiesMap.get(DmuConstants.CREDENTIAL_STRG_TYPE))) {
				urlBuilder.append(propertiesMap.get(DmuConstants.AWS_ACCESS_ID_LC));
			} else {
				urlBuilder.append(awsCredentials.getAWSAccessKeyId());
			}
			urlBuilder.append("\"");
			urlBuilder.append(" -Dfs.s3a.secret.key=");
			urlBuilder.append("\"");
			if (DmuConstants.DIRECT_LC.equalsIgnoreCase(propertiesMap.get(DmuConstants.CREDENTIAL_STRG_TYPE))) {
				urlBuilder.append(propertiesMap.get(DmuConstants.AWS_SECRET_KEY_LC));
			} else {
				urlBuilder.append(awsCredentials.getAWSSecretKey());
			}
			urlBuilder.append("\"");
			if (!DmuConstants.DIRECT_LC.equalsIgnoreCase(propertiesMap.get(DmuConstants.CREDENTIAL_STRG_TYPE))) {
				urlBuilder.append(" -Dfs.s3a.session.token=");
				urlBuilder.append("\"");
				urlBuilder.append(awsCredentials.getSessionToken());
				urlBuilder.append("\"");
			}
			urlBuilder.append(" ");

			urlBuilder.append(propertiesMap.get(DmuConstants.TEMP_HDFS_DIR));
			urlBuilder.append(historyEntity.getDmuHIstoryDetailPK().getRequestNo());
			urlBuilder.append("-");
			urlBuilder.append(historyEntity.getTableName());
			urlBuilder.append("/*gz s3a://");
			urlBuilder.append(historyEntity.getTargetS3Bucket());

			urlBuilder.append(" rm –r ");
			urlBuilder.append(propertiesMap.get(DmuConstants.TEMP_HDFS_DIR));
			urlBuilder.append(historyEntity.getDmuHIstoryDetailPK().getRequestNo());
			urlBuilder.append("-");
			urlBuilder.append(historyEntity.getTableName());
		}

		return urlBuilder.toString();
	}

}
