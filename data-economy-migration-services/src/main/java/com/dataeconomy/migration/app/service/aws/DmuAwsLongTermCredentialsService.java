package com.dataeconomy.migration.app.service.aws;

import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;
import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.util.DmuConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuAwsLongTermCredentialsService {

	public Optional<BasicSessionCredentials> validateLongTermAWSCredentials(DmuConnectionDTO connectionDto)
			throws DataMigrationException {
		log.info(" DMULongTermCredentialsService :: validateLongTermAWSCredentials  ");
		try {
			BasicAWSCredentials awsCredentials = new BasicAWSCredentials(connectionDto.getAwsAccessIdLc(),
					connectionDto.getAwsSecretKeyLc());
			AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
					.withRegion(DmuConstants.CLIENT_REGION).build();
			GetSessionTokenRequest sessionTokenRequest = new GetSessionTokenRequest();
			sessionTokenRequest.setDurationSeconds(900);
			GetSessionTokenResult sessionTokenResult;
			sessionTokenResult = stsClient.getSessionToken(sessionTokenRequest);
			Credentials sessionCreds = sessionTokenResult.getCredentials();
			log.info(" DMULongTermCredentialsService :: validateLongTermAWSCredentials  :: accessKey :: {} ",
					sessionCreds.getAccessKeyId());
			log.info(" DMULongTermCredentialsService :: validateLongTermAWSCredentials  :: secretKey :: {} ",
					sessionCreds.getSecretAccessKey());
			log.info(" DMULongTermCredentialsService :: validateLongTermAWSCredentials  :: token :: {} ",
					sessionCreds.getSessionToken());
			BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(sessionCreds.getAccessKeyId(),
					sessionCreds.getSecretAccessKey(), sessionCreds.getSessionToken());
			return Optional.ofNullable(basicSessionCredentials);
		} catch (Exception exception) {
			log.info(" Exception occured at DMULongTermCredentialsService :: validateLongTermAWSCredentials :: {}  ",
					ExceptionUtils.getStackTrace(exception));
			throw new DataMigrationException("Invalid Connection Details for AWS Longterm Validation ");
		}
	}

}
