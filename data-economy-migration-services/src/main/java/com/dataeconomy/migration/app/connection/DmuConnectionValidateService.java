package com.dataeconomy.migration.app.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dataeconomy.migration.app.conn.service.DmuHiveDataSourceService;
import com.dataeconomy.migration.app.conn.service.DmuImpalaDataSourceService;
import com.dataeconomy.migration.app.conn.service.DmuSparkConnectionService;
import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.util.DmuConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuConnectionValidateService {

	@Autowired
	private DmuHiveDataSourceService hiveConnectionService;

	@Autowired
	private DmuImpalaDataSourceService imaplaConnectionService;

	@Autowired
	private DmuSparkConnectionService sparkConnectionService;

	public boolean validateConnectionDetails(DmuConnectionDTO connectionDto) throws DataMigrationException {
		if (StringUtils.equalsIgnoreCase(DmuConstants.HIVE, connectionDto.getConnectionType())
				|| connectionDto.isHiveConnEnabled()) {
			Optional<String> hiveConnStringOpt = hiveConnectionService.getHiveConnectionDetails(connectionDto);
			if (hiveConnStringOpt.isPresent()) {
				String hiveConnString = hiveConnStringOpt.get();
				log.info(" ConnectionService :: validateConnection :: hiveConnString {}", hiveConnString);
				if (!validateConnection(hiveConnString)) {
					throw new DataMigrationException("Invalid Hive Connection Details");
				}
				return true;
			} else {
				throw new DataMigrationException("Invalid Connection Details for HIVE connection Validation ");
			}
		}
		if (StringUtils.equalsIgnoreCase(DmuConstants.IMPALA, connectionDto.getConnectionType())
				|| connectionDto.isImpalaConnEnabled()) {
			Optional<String> impalaConnStringOpt = imaplaConnectionService.getImpalaConnectionDetails(connectionDto);
			if (impalaConnStringOpt.isPresent()) {
				String impalaConnString = impalaConnStringOpt.get();
				log.info(" ConnectionService :: validateConnection :: impalaConnString {}", impalaConnString);
				if (!validateConnection(impalaConnString)) {
					throw new DataMigrationException("Invalid Impala Connection Details");
				}
				return true;
			} else {
				throw new DataMigrationException("Invalid Connection Details for IMPALA connection Validation ");
			}
		}
		if (StringUtils.equalsIgnoreCase(DmuConstants.SPARK, connectionDto.getConnectionType())
				|| connectionDto.isSparkConnEnabled()) {
			Optional<String> sparkConnStringOpt = sparkConnectionService.getSparkConnectionDetails(connectionDto);
			if (sparkConnStringOpt.isPresent()) {
				String sparkConnString = sparkConnStringOpt.get();
				log.info(" ConnectionService :: validateConnection :: sparkConnString {}", sparkConnString);
				if (!validateConnection(sparkConnString)) {
					throw new DataMigrationException("Invalid Spark Connection Details");
				}
				return true;
			} else {
				throw new DataMigrationException("Invalid Connection Details for SPARK connection Validation ");
			}
		}
		throw new DataMigrationException("Invalid Connection Details");
	}

	private boolean validateConnection(String validateConnString) {
		try (Connection connection = DriverManager.getConnection(validateConnString);) {
			log.info(" ConnectionService :: connection validate successfully for url {}", validateConnString);
			return true;
		} catch (Exception exception) {
			log.error(" Exception occured at DMUConnectionValidationService :: validateConnection {} ",
					ExceptionUtils.getStackTrace(exception));
			return false;
		}
	}
}
