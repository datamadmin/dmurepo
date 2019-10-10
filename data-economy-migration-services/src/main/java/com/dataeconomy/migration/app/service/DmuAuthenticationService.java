package com.dataeconomy.migration.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.mapper.DmuAuthenticationDetailsMapper;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.mysql.repository.DmuAuthenticationRepository;
import com.dataeconomy.migration.app.util.DmuConstants;

@Service
public class DmuAuthenticationService {

	@Autowired
	private DmuAuthenticationRepository dmuAuthenticationRepository;

	@Autowired
	private DmuAuthenticationDetailsMapper mapper;

	@Timed
	@Transactional(readOnly = true)
	public DmuConnectionDTO getAllAuthenticationDetails() throws DataMigrationException {
		return mapper.toDto(dmuAuthenticationRepository.findById(1L)
				.orElseThrow(() -> new DataMigrationException("Authentication details not found")));
	}

	public void saveAuthenticationDetails(DmuConnectionDTO dmuConnectionDTO) {
		dmuAuthenticationRepository.findById(1L).ifPresent(authenticationEntity -> {

			authenticationEntity.setLdapUserName(null);
			authenticationEntity.setLdapPassword(null);
			authenticationEntity.setLdapDomainName(null);
			authenticationEntity.setKerberosHostRealm(null);
			authenticationEntity.setKerberosHostFqdn(null);
			authenticationEntity.setKerberosServiceName(null);
			authenticationEntity.setSslKeystorePath(null);

			if (DmuConstants.SCRD.equalsIgnoreCase(dmuConnectionDTO.getAuthenticationType())) {
				authenticationEntity.setAuthenticationType(dmuConnectionDTO.getAuthenticationType());
				if (DmuConstants.LDAP.equalsIgnoreCase(dmuConnectionDTO.getCredentialStrgType())) {
					authenticationEntity.setLdapUserName(dmuConnectionDTO.getHdfsLdapUserName());
					authenticationEntity.setLdapPassword(dmuConnectionDTO.getHdfsLdapUserPassw());
					authenticationEntity.setLdapDomainName(dmuConnectionDTO.getHdfsLdapDomain());
					authenticationEntity.setLdapCnctnFlag(DmuConstants.YES);
					authenticationEntity.setKerberosCnctnFlag(DmuConstants.NO);
				} else {
					authenticationEntity.setKerberosHostRealm(dmuConnectionDTO.getKerberosHostRealm());
					authenticationEntity.setKerberosHostFqdn(dmuConnectionDTO.getKerberosHostFqdn());
					authenticationEntity.setKerberosServiceName(dmuConnectionDTO.getKerberosServiceName());
					authenticationEntity.setSslKeystorePath(dmuConnectionDTO.getSslKeystorePath());
					authenticationEntity.setLdapCnctnFlag(DmuConstants.NO);
					authenticationEntity.setKerberosCnctnFlag(DmuConstants.YES);
				}
			} else {
				authenticationEntity.setAuthenticationType(dmuConnectionDTO.getAuthenticationType());
			}
			dmuAuthenticationRepository.save(authenticationEntity);
		});
	}
}
