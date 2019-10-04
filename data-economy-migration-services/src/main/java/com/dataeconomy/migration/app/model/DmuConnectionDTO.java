package com.dataeconomy.migration.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DmuConnectionDTO {

	String connectionGroup;

	boolean isHiveConnEnabled;

	boolean isImpalaConnEnabled;

	boolean isSparkConnEnabled;

	String connectionType;

	Long srNo;

	String domain;

	DmuTgtOtherPropDTO tgtOtherPropDto;

	DmuTgtFormatPropDTO tgtFormatPropDto;

	DmuTgtFormatPropTempDTO tgtFormatPropTempDto;

	String hiveCnctnFlag;

	String hiveHostName;

	String hivePortNmbr;

	String impalaCnctnFlag;

	String impalaHostName;

	String impalaPortNmbr;

	String sparkCnctnFlag;

	String sqlWhDir;

	String hiveMsUri;

	String credentialStrgType;

	String awsAccessIdLc;

	String awsSecretKeyLc;

	String awsAccessIdSc;

	String awsSecretKeySc;

	String roleArn;

	String principalArn;

	String samlProviderArn;

	String roleSesnName;

	String policyArnMembers;

	String externalId;

	String fdrtdUserName;

	String inlineSesnPolicy;

	Integer duration;

	String ldapUserName;

	String ldapUserPassw;

	String ldapDomain;

	String hdfsLdapUserName;

	String hdfsLdapUserPassw;

	String hdfsLdapDomain;

	String scCrdntlAccessType;

	String authenticationType;

	String ldapCnctnFlag;

	String kerberosHostRealm;

	String kerberosHostFqdn;

	String kerberosCnctnFlag;

	String textFormatFlag;

	String kerberosServiceName;

	String sslKeystorePath;

}
