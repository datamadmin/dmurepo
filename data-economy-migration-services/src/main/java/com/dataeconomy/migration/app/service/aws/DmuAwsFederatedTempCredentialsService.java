package com.dataeconomy.migration.app.service.aws;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetFederationTokenRequest;
import com.amazonaws.services.securitytoken.model.GetFederationTokenResult;
import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.util.DmuConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuAwsFederatedTempCredentialsService {

	public Optional<BasicSessionCredentials> getFederatedCredentials(DmuConnectionDTO connectionDto)
			throws DataMigrationException {
		log.info("called => AwsFederatedTempCredentialsService :: getFederatedCredentials :: federatedUser {} ",
				connectionDto.getFdrtdUserName());
		try {
			BasicAWSCredentials awsCredentials = new BasicAWSCredentials(connectionDto.getAwsAccessIdSc(),
					connectionDto.getAwsSecretKeySc());
			AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
					.withRegion(DmuConstants.CLIENT_REGION).build();

			GetFederationTokenRequest getFederationTokenRequest = new GetFederationTokenRequest();
			getFederationTokenRequest.setName(connectionDto.getFdrtdUserName());

			GetFederationTokenResult federationTokenResult = stsClient.getFederationToken(getFederationTokenRequest);
			Credentials sessionCredentials = federationTokenResult.getCredentials();

			BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
					sessionCredentials.getAccessKeyId(), sessionCredentials.getSecretAccessKey(),
					sessionCredentials.getSessionToken());
			log.info(
					"called => AwsFederatedTempCredentialsService :: getFederatedCredentials :: basicSessionCredentials {} ",
					Objects.toString(basicSessionCredentials,
							"Unable to generate credentials for federateduser " + connectionDto.getFdrtdUserName()));
			return Optional.of(basicSessionCredentials);
		} catch (Exception e) {
			log.error(
					"exception => Exception occured at AwsFederatedTempCredentialsService :: getFederatedCredentials {} ",
					ExceptionUtils.getStackTrace(e));
			throw new DataMigrationException("Invalid Connection Details for AWS Federated User Validation");
		}
	}

}
