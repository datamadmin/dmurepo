package com.dataeconomy.migration.app.connection;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.conn.service.DmuHiveDataSourceService;
import com.dataeconomy.migration.app.conn.service.DmuImpalaDataSourceService;
import com.dataeconomy.migration.app.conn.service.DmuSparkConnectionService;
import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuAuthenticationEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuHdfsEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuAuthenticationRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuHdfsRepository;
import com.dataeconomy.migration.app.util.DmuConstants;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuHdfsConnectionService {

	@Value("${hs2.datasource.jdbc-url}")
	public String hiveConnUrl;

	@Autowired
	private DmuHiveDataSourceService hiveConnectionService;

	@Autowired
	private DmuImpalaDataSourceService imaplaConnectionService;

	@Autowired
	private DmuSparkConnectionService sparkConnectionService;

	@Autowired
	private DmuConnectionPool dmuConnectionPool;

	@Autowired
	private DmuHdfsRepository hdfsRepository;

	@Autowired
	private DmuAuthenticationRepository authenticationRepository;

	private Map<String, DataSource> dataSourceMap = Collections.synchronizedMap(Maps.newHashMap());

	@PostConstruct
	public void initDataSourceConfig() {
		try {
			log.info(" initializing datasource connections while server start up ");
			Class.forName(DmuConstants.HIVE_DRIVER_CLASS_NAME).newInstance();
			Class.forName(DmuConstants.IMPALA_DRIVER_CLASS_NAME).newInstance();
			DmuConnectionDTO connectionDto = DmuConnectionDTO.builder().build();
			populateDMUAuthenticationProperties(connectionDto);
			populateDMUHdfsProperties(connectionDto);
			populateDataSourceConfigAtServerStarts(connectionDto);
		} catch (Exception exception) {
			log.error("Exception while creating datasources while server up {} ",
					ExceptionUtils.getStackTrace(exception));
		}
	}

	@Timed
	public void populateDataSourceConfig(DmuConnectionDTO connectionDto) throws DataMigrationException {
		if (StringUtils.equalsIgnoreCase(DmuConstants.HIVE, connectionDto.getConnectionType())) {
			Optional<String> hiveConnStringOpt = hiveConnectionService.getHiveConnectionDetails(connectionDto);
			if (hiveConnStringOpt.isPresent()) {
				String hiveConnString = hiveConnStringOpt.get();
				DataSource hiveDataSource = retrieveDataSource(DmuConstants.HIVE_CONN_POOL,
						DmuConstants.HIVE_DRIVER_CLASS_NAME, hiveConnString);
				dataSourceMap.put(DmuConstants.REGULAR, hiveDataSource);
				log.info(" ConnectionService :: validateConnection :: hiveConnString {}", hiveConnString);
			} else {
				throw new DataMigrationException("Invalid Connection Details for HIVE connection Validation ");
			}
		}
		if (StringUtils.equalsIgnoreCase(DmuConstants.IMPALA, connectionDto.getConnectionType())) {
			Optional<String> impalaConnStringOpt = imaplaConnectionService.getImpalaConnectionDetails(connectionDto);
			if (impalaConnStringOpt.isPresent()) {
				String impalaConnString = impalaConnStringOpt.get();
				DataSource impalaDataSource = retrieveDataSource(DmuConstants.IMPALA,
						DmuConstants.IMPALA_DRIVER_CLASS_NAME, impalaConnString);
				dataSourceMap.put(DmuConstants.LARGEQUERY, impalaDataSource);
				log.info(" ConnectionService :: validateConnection :: impalaConnString {}", impalaConnString);
			} else {
				throw new DataMigrationException("Invalid Connection Details for IMPALA connection Validation ");
			}
		}
		if (StringUtils.equalsIgnoreCase(DmuConstants.SPARK, connectionDto.getConnectionType())) {
			Optional<String> sparkConnStringOpt = sparkConnectionService.getSparkConnectionDetails(connectionDto);
			if (sparkConnStringOpt.isPresent()) {
				String sparkConnString = sparkConnStringOpt.get();
				log.info(" ConnectionService :: validateConnection :: sparkConnString {}", sparkConnString);
			} else {
				throw new DataMigrationException("Invalid Connection Details for SPARK connection Validation ");
			}
		}
	}

	public void populateDataSourceConfigAtServerStarts(DmuConnectionDTO connectionDto) throws DataMigrationException {
		if (connectionDto.isHiveConnEnabled()) {
			Optional<String> hiveConnStringOpt = hiveConnectionService.getHiveConnectionDetails(connectionDto);
			if (hiveConnStringOpt.isPresent()) {
				String hiveConnString = hiveConnStringOpt.get();
				DataSource hiveDataSource = retrieveDataSource(DmuConstants.HIVE_CONN_POOL,
						DmuConstants.HIVE_DRIVER_CLASS_NAME, hiveConnString);
				dataSourceMap.put(DmuConstants.REGULAR, hiveDataSource);
				log.info(" ConnectionService :: validateConnection :: hiveConnString {}", hiveConnString);
			} else {
				throw new DataMigrationException("Invalid Connection Details for HIVE connection Validation ");
			}
		}
		if (connectionDto.isImpalaConnEnabled()) {
			Optional<String> impalaConnStringOpt = imaplaConnectionService.getImpalaConnectionDetails(connectionDto);
			if (impalaConnStringOpt.isPresent()) {
				String impalaConnString = impalaConnStringOpt.get();
				// DataSource impalaDataSource = retrieveDataSource(DmuConstants.IMPALA,
				// DmuConstants.IMPALA_DRIVER_CLASS_NAME, impalaConnString);
				// dataSourceMap.put(DmuConstants.LARGEQUERY, impalaDataSource);
				log.info(" ConnectionService :: validateConnection :: impalaConnString {}", impalaConnString);
			} else {
				throw new DataMigrationException("Invalid Connection Details for IMPALA connection Validation ");
			}
		}
		if (connectionDto.isSparkConnEnabled()) {
			Optional<String> sparkConnStringOpt = sparkConnectionService.getSparkConnectionDetails(connectionDto);
			if (sparkConnStringOpt.isPresent()) {
				String sparkConnString = sparkConnStringOpt.get();
				log.info(" ConnectionService :: validateConnection :: sparkConnString {}", sparkConnString);
			} else {
				throw new DataMigrationException("Invalid Connection Details for SPARK connection Validation ");
			}
		}
	}

	private HikariDataSource retrieveDataSource(String connPoolName, String hiveDriverClassName, String hiveConnString)
			throws DataMigrationException {
		log.error(
				"called => HDFSConnectionService  :: retrieveDataSource :: connPoolName {} , hiveDriverClassName {} , hiveConnString {} ",
				connPoolName, hiveDriverClassName, hiveConnString);
		try {
			return dmuConnectionPool.getDataSourceFromConfig(connPoolName, hiveDriverClassName, hiveConnString);
		} catch (Exception exception) {
			log.error(
					"Exception occured at HDFSConnectionService  :: retrieveDataSource :: connPoolName {} , hiveDriverClassName {} , hiveConnString {} exception \n {} ",
					connPoolName, hiveDriverClassName, hiveConnString);
			throw new DataMigrationException("Unable to retrieve datasource object ");
		}
	}

	@Timed
	public DataSource getValidDataSource(String dataSourceType) throws DataMigrationException {
		log.error("called => HDFSConnectionService  :: getValidDataSource :: dataSourceType  {} ", dataSourceType);
		try {
			if (dataSourceMap.get(dataSourceType) != null) {
				return dataSourceMap.get(dataSourceType);
			} else {
				DataSource dataSource = dmuConnectionPool.getDataSourceFromConfig(DmuConstants.DEFAULT_HIVE_POOL,
						DmuConstants.HIVE_DRIVER_CLASS_NAME, hiveConnUrl);
				dataSourceMap.put(DmuConstants.REGULAR, dataSource);
				return dataSource;
			}
		} catch (Exception exception) {
			log.error("Exception while retrieving datasource for given type {} , {} ", dataSourceType,
					ExceptionUtils.getStackTrace(exception));
			throw new DataMigrationException(" DataSource config not found");
		}
	}

	@Timed
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

			connectionDto.setSqlWhDir(dmuHdfsObj.getSqlWhDir());
			connectionDto.setImpalaCnctnFlag(dmuHdfsObj.getImpalaCnctnFlag());
			connectionDto.setSparkCnctnFlag(dmuHdfsObj.getSparkCnctnFlag());
		}
	}

}
