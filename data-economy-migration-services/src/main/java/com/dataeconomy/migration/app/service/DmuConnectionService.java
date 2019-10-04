package com.dataeconomy.migration.app.service;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.connection.DmuAwsConnectionService;
import com.dataeconomy.migration.app.connection.DmuConnectionValidateService;
import com.dataeconomy.migration.app.connection.DmuHdfsConnectionService;
import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.service.aws.DmuAwsAssumeRoleCredentialsService;
import com.dataeconomy.migration.app.service.aws.DmuAwsAssumeRoleWithSAMLCredentialsService;
import com.dataeconomy.migration.app.service.aws.DmuAwsFederatedTempCredentialsService;
import com.dataeconomy.migration.app.service.aws.DmuAwsLongTermCredentialsService;
import com.dataeconomy.migration.app.util.DmuConstants;
import com.dataeconomy.migration.app.util.DmuServiceHelper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuConnectionService {

	@Value("${hs2.datasource.driver-class-name: com.cloudera.hive.jdbc41.HS2Driver}")
	public String hs2Driver;

	@Value("${hs2.validate.url}")
	public String hs2DriverUrl;

	@Autowired
	private DmuAwsFederatedTempCredentialsService awsFederatedTempCredentialsService;

	@Autowired
	private DmuAwsLongTermCredentialsService awsLongTermAwsCredentialsService;

	@Autowired
	private DmuAwsAssumeRoleCredentialsService awsAssumeRoleCredentialsService;

	@Autowired
	private DmuAwsAssumeRoleWithSAMLCredentialsService awsAssumeRoleWithSAMLCredentialsService;

	@Autowired
	private DmuConnectionValidateService dmuConnectionValidationService;

	@Autowired
	private DmuAwsConnectionService awsConnectionService;

	@Autowired
	DmuHdfsConnectionService hdfsConnectionService;

	@Autowired
	DmuTgtOtherPropService tgtOtherPropService;

	@Autowired
	DmuServiceHelper dmuHelperService;

	@Transactional
	@Timed
	public boolean validateConnection(DmuConnectionDTO connectionDto) throws DataMigrationException {
		try {
			log.info(" ConnectionService :: validateConnection :: connectionDto {}",
					ObjectUtils.toString(connectionDto));
			if (DmuConstants.AWS_TO_S3.equalsIgnoreCase(connectionDto.getConnectionGroup())) {
				if (StringUtils.equalsIgnoreCase(DmuConstants.DIRECT_LC, connectionDto.getConnectionType())) {
					awsLongTermAwsCredentialsService.validateLongTermAWSCredentials(connectionDto);
					return true;
				} else if (StringUtils.equalsIgnoreCase(DmuConstants.DIRECT_SC, connectionDto.getConnectionType())) {
					if (StringUtils.equalsIgnoreCase(connectionDto.getScCrdntlAccessType(), DmuConstants.ASSUME)) {
						awsAssumeRoleCredentialsService.getAwsAssumeRoleRequestCredentials(connectionDto);
						return true;
					} else if (StringUtils.equalsIgnoreCase(connectionDto.getScCrdntlAccessType(),
							DmuConstants.ASSUME_SAML)) {
						awsAssumeRoleWithSAMLCredentialsService
								.getAwsAssumeRoleRequestWithSAMLCredentials(connectionDto);
						return true;
					} else if (StringUtils.equalsIgnoreCase(DmuConstants.AWS_FEDERATED_USER,
							connectionDto.getScCrdntlAccessType())) {
						awsFederatedTempCredentialsService.getFederatedCredentials(connectionDto);
						return true;
					} else {
						throw new DataMigrationException("Invalid Connection Details for AWS Shortterm Validation ");
					}
				}
			} else if (DmuConstants.HDFS.equalsIgnoreCase(connectionDto.getConnectionGroup())) {
				return dmuConnectionValidationService.validateConnectionDetails(connectionDto);
			} else {
				throw new DataMigrationException("Invalid Connection Details for AWS/HDFS save details ");
			}
			return false;
		} catch (Exception exception) {
			log.info(" Exception occured at ConnectionService :: getConnectionObject :: validateConnection {} ",
					ExceptionUtils.getStackTrace(exception));
			throw new DataMigrationException(exception.getMessage());
		}

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Timed
	public boolean saveConnectionDetails(DmuConnectionDTO connectionDto) throws DataMigrationException {
		try {
			if (DmuConstants.AWS_TO_S3.equalsIgnoreCase(connectionDto.getConnectionGroup())) {
				dmuHelperService.saveDMUS3Properties(connectionDto);
				awsConnectionService.populateAWSCredentials();
			}
			if (DmuConstants.HDFS.equalsIgnoreCase(connectionDto.getConnectionGroup())) {
				dmuHelperService.saveDMUHdfsEntityProperties(connectionDto);
				hdfsConnectionService.initDataSourceConfig();
			}
			if (DmuConstants.TARGET_FILE_PROPS.equalsIgnoreCase(connectionDto.getConnectionGroup())) {
				dmuHelperService.saveTGTFormatProperties(connectionDto);
			}
			if (DmuConstants.OTHER_PROPS.equalsIgnoreCase(connectionDto.getConnectionGroup())) {
				dmuHelperService.saveTGTOtherProperties(connectionDto);
			}
			dmuHelperService.init();
		} catch (Exception exception) {
			log.info(" Exception occured at ConnectionService :: saveConnectionDetails ::   {} ",
					ExceptionUtils.getStackTrace(exception));
			throw new DataMigrationException("Exception while saving connection properties ");
		}
		return true;
	}

	@Timed
	@Transactional(readOnly = true)
	public DmuConnectionDTO getConnectionDetails() throws DataMigrationException {
		DmuConnectionDTO connectionDto = DmuConnectionDTO.builder().build();
		try {
			dmuHelperService.populateDMUHdfsProperties(connectionDto);
			dmuHelperService.populateDMUS3Properties(connectionDto);
			dmuHelperService.populateDMUAuthenticationProperties(connectionDto);
			dmuHelperService.populateTGTFormatProperties(connectionDto);
			dmuHelperService.populateTGTOtherProperties(connectionDto);
			return connectionDto;
		} catch (Exception exception) {
			log.info(" Exception occured at ConnectionService :: getConnectionDetails {} ",
					ExceptionUtils.getStackTrace(exception));
			throw new DataMigrationException("Exception occured while retrieving connection details ");
		}
	}

}