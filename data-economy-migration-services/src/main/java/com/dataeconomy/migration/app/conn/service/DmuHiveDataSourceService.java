package com.dataeconomy.migration.app.conn.service;

import java.text.MessageFormat;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.util.DmuConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuHiveDataSourceService {

	@Value("${hive.connection.url}")
	public String hiveConnectionUrl;

	@Value("${hive.connection.unsecured.url}")
	public String hiveConnectionUnSecuredUrl;

	@Value("${hive.connection.ldap.url}")
	public String hiveConnectionLdapUrl;

	@Value("${hive.connection.ldap.domain.url}")
	public String hiveConnectionLdapDomainUrl;

	@Value("${hive.connection.kerberos.url}")
	public String hiveConnectionkerberosUrl;

	public Optional<String> getHiveConnectionDetails(DmuConnectionDTO connectionDto) throws DataMigrationException {
		try {
			if (StringUtils.equalsIgnoreCase(connectionDto.getAuthenticationType(), DmuConstants.UNSECURED)
					|| StringUtils.equalsIgnoreCase(connectionDto.getAuthenticationType(), "UNSCRD")) {
				log.info(" HiveConnectionService :: getHiveConnectionDetails :: UNSECURED : getAuthenticationType {} ",
						connectionDto.getAuthenticationType());
				return Optional.ofNullable(MessageFormat.format(hiveConnectionUnSecuredUrl,
						connectionDto.getHiveHostName(), String.valueOf(connectionDto.getHivePortNmbr())));
			} else if (StringUtils.equalsIgnoreCase(connectionDto.getAuthenticationType(), DmuConstants.SECURED)
					|| StringUtils.equalsIgnoreCase(connectionDto.getAuthenticationType(), "UNSCRD")) {
				log.info(
						" HiveConnectionService :: getHiveConnectionDetails :: SECURED : getAuthenticationType {} , getCredentialStrgType {}",
						connectionDto.getAuthenticationType(), connectionDto.getCredentialStrgType());
				if (StringUtils.equalsIgnoreCase(connectionDto.getCredentialStrgType(), DmuConstants.LDAP)) {
					if (StringUtils.isNotBlank(connectionDto.getDomain())) {
						return Optional.ofNullable(MessageFormat.format(hiveConnectionLdapDomainUrl,
								connectionDto.getHiveHostName(), String.valueOf(connectionDto.getHivePortNmbr()),
								connectionDto.getLdapUserName(), connectionDto.getLdapDomain(),
								connectionDto.getLdapUserPassw()));
					} else {
						return Optional.ofNullable(MessageFormat.format(hiveConnectionLdapUrl,
								connectionDto.getHiveHostName(), String.valueOf(connectionDto.getHivePortNmbr()),
								connectionDto.getLdapUserName(), connectionDto.getLdapUserPassw()));
					}
				} else if (StringUtils.equalsIgnoreCase(connectionDto.getCredentialStrgType(), DmuConstants.KERBEROS)) {
					return Optional.ofNullable(MessageFormat.format(hiveConnectionkerberosUrl,
							connectionDto.getHiveHostName(), String.valueOf(connectionDto.getHivePortNmbr()),
							connectionDto.getKerberosHostRealm(), connectionDto.getKerberosHostFqdn(),
							connectionDto.getKerberosServiceName()));
				} else {
					log.info(" HiveConnectionService :: getHiveConnectionDetails :: invalid credentials");
					throw new DataMigrationException("Not a valid Hive Validation Details!");
				}
			} else {
				log.info(" HiveConnectionService :: getHiveConnectionDetails :: invalid credentials");
				throw new DataMigrationException("Not a valid Hive Validation Details!");
			}
		} catch (Exception exception) {
			log.info(" Exception occured at ConnectionService :: getConnectionObject :: getImpalaConnectionDetails {} ",
					ExceptionUtils.getStackTrace(exception));
			throw new DataMigrationException("Invalid Connection Details for Hive Validation");
		}
	}

}
