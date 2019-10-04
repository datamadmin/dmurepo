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
public class DmuImpalaDataSourceService {

	@Value("${impala.connection.url}")
	public String impalaConnectionUrl;

	@Value("${impala.connection.unsecured.url}")
	public String impalaConnectionUnSecuredUrl;

	@Value("${impala.connection.ldap.url}")
	public String impalaConnectionLdapUrl;

	@Value("${impala.connection.kerberos.url}")
	public String impalaConnectionkerberosUrl;

	public Optional<String> getImpalaConnectionDetails(DmuConnectionDTO connectionDto) throws DataMigrationException {
		try {
			if (StringUtils.equalsIgnoreCase(connectionDto.getAuthenticationType(), DmuConstants.UNSECURED)
					|| StringUtils.equalsIgnoreCase(connectionDto.getAuthenticationType(), "UNSCRD")) {
				return Optional.ofNullable(MessageFormat.format(impalaConnectionUnSecuredUrl,
						connectionDto.getImpalaHostName(), String.valueOf(connectionDto.getImpalaPortNmbr())));
			} else if (StringUtils.equalsIgnoreCase(connectionDto.getAuthenticationType(), DmuConstants.SECURED)
					|| StringUtils.equalsIgnoreCase(connectionDto.getAuthenticationType(), "UNSCRD")) {
				if (StringUtils.equalsIgnoreCase(connectionDto.getAuthenticationType(), DmuConstants.LDAP)) {
					return Optional
							.ofNullable(MessageFormat.format(impalaConnectionLdapUrl, connectionDto.getImpalaHostName(),
									String.valueOf(connectionDto.getImpalaPortNmbr()), connectionDto.getLdapUserName(),
									connectionDto.getLdapDomain(), connectionDto.getLdapUserPassw()));
				} else if (StringUtils.equalsIgnoreCase(connectionDto.getAuthenticationType(), DmuConstants.KERBEROS)) {
					return Optional.ofNullable(MessageFormat.format(impalaConnectionkerberosUrl,
							connectionDto.getImpalaHostName(), String.valueOf(connectionDto.getImpalaPortNmbr()),
							connectionDto.getKerberosHostRealm(), connectionDto.getKerberosHostFqdn(),
							connectionDto.getKerberosServiceName()));
				} else {
					throw new DataMigrationException("Not a valid secured Imapla Authentication Details!");
				}
			} else {
				throw new DataMigrationException("Not a valid Imapla Authentication Details!");
			}
		} catch (Exception exception) {
			log.info(" Exception occured at ConnectionService :: getConnectionObject :: getImpalaConnectionDetails {} ",
					ExceptionUtils.getStackTrace(exception));
			throw new DataMigrationException("Invalid Connection Details for Imapla Validation");
		}
	}
}
