package com.dataeconomy.migration.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmuTgtOtherPropDTO {

	private Long srNo;

	private Long parallelJobs;

	private Long parallelUsrRqst;

	private String tempHiveDB;

	private String tempHdfsDir;

	private String tokenizationInd;

	private String ptgyDirPath;

	private String hdfsEdgeNode;

	private String hdfsUserName;

	private String hdfsPemLocation;

	private String hadoopInstallDir;

}
