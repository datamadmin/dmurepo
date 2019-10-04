package com.dataeconomy.migration.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmuS3DTO {

	private Long srNo;

	private String credentialStrgType;

	private String awsAccessIdLc;

	private String awsSecretKeyLc;

	private String awsAccessIdSc;

	private String awsSecretKeySc;

	private String roleArn;

	private String principalArn;

	private String samlProviderArn;

	private String roleSesnName;

	private String policyArnMembers;

	private String externalId;

	private String fdrtdUserName;

	private String inlineSesnPolicy;

	private Long duration;

	private String ldapUserName;

	private String ldapUserPassw;

	private String ldapDomain;

	private String scCrdntlAccessType;

}
