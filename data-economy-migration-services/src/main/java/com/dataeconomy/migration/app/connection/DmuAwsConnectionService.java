package com.dataeconomy.migration.app.connection;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.BasicSessionCredentials;
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
public class DmuAwsConnectionService {

	@Autowired
	private DmuAwsFederatedTempCredentialsService awsFederatedTempCredentialsService;

	@Autowired
	private DmuAwsLongTermCredentialsService awsLongTermAwsCredentialsService;

	@Autowired
	private DmuAwsAssumeRoleCredentialsService awsAssumeRoleCredentialsService;

	@Autowired
	private DmuAwsAssumeRoleWithSAMLCredentialsService awsAssumeRoleWithSAMLCredentialsService;

	private BasicSessionCredentials basicSessionCredentials;

	@Autowired
	private DmuServiceHelper dmuHelpserService;

	@PostConstruct
	public void populateAWSCredentials() {
		try {
			log.info(
					" called => AWSConnectionService ::  populateAWSCredentials creating AWS credentials and connectio details while server running");

			DmuConnectionDTO connectionDto = DmuConnectionDTO.builder().build();
			dmuHelpserService.populateDMUS3Properties(connectionDto);

			basicSessionCredentials = retrieveAWSCredentials(connectionDto)
					.orElseThrow(() -> new DataMigrationException("Invalid AWS credentials "));

		} catch (Exception exception) {
			log.error(" Exception occured at AWSConnectionService :: populateAWSCredentials ::  {} ",
					ExceptionUtils.getStackTrace(exception));
		}
	}

	private Optional<BasicSessionCredentials> retrieveAWSCredentials(DmuConnectionDTO connectionDto)
			throws DataMigrationException {
		switch (connectionDto.getConnectionType()) {
		case DmuConstants.DIRECT_LC:
			return awsLongTermAwsCredentialsService.validateLongTermAWSCredentials(connectionDto);
		case DmuConstants.DIRECT_SC:
			switch (connectionDto.getScCrdntlAccessType()) {
			case DmuConstants.ASSUME:
				return awsAssumeRoleCredentialsService.getAwsAssumeRoleRequestCredentials(connectionDto);
			case DmuConstants.ASSUME_SAML:
				return awsAssumeRoleWithSAMLCredentialsService
						.getAwsAssumeRoleRequestWithSAMLCredentials(connectionDto);
			case DmuConstants.AWS_FEDERATED_USER:
				return awsFederatedTempCredentialsService.getFederatedCredentials(connectionDto);
			default:
				throw new DataMigrationException("Invalid Connection Details for AWS  crdentials ");
			}
		default:
			throw new DataMigrationException("Invalid Connection Details for AWS crdentials ");
		}
	}

	public synchronized BasicSessionCredentials getBasicSessionCredentials() {
		return basicSessionCredentials;
	}

	public synchronized void setBasicSessionCredentials(BasicSessionCredentials basicSessionCredentials) {
		this.basicSessionCredentials = basicSessionCredentials;
	}

}
