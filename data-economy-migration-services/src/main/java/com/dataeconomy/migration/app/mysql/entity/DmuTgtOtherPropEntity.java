package com.dataeconomy.migration.app.mysql.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicInsert(value = true)
@Entity
@Table(name = "TGT_OTHER_PROP")
public class DmuTgtOtherPropEntity {

	@Id
	@Column(name = "SR_NO", length = 11, nullable = false)
	private Long srNo;

	@Column(name = "PARALLEL_JOBS", length = 11, nullable = true)
	private Long parallelJobs;

	@Column(name = "PARALLEL_USR_RQST", length = 11, nullable = true)
	private Long parallelUsrRqst;

	@Column(name = "TEMP_HIVE_DB", length = 50, nullable = true)
	private String tempHiveDB;

	@Column(name = "TEMP_HDFS_DIR", length = 200, nullable = true)
	private String tempHdfsDir;

	@Column(name = "TOKENIZATION_IND", length = 20, nullable = true)
	private String tokenizationInd;

	@Column(name = "PTGY_DIR_PATH", length = 200, nullable = true)
	private String ptgyDirPath;

	@Column(name = "HDFS_EDGE_NODE", length = 200, nullable = true)
	private String hdfsEdgeNode;

	@Column(name = "HDFS_USER_NAME", length = 50, nullable = true)
	private String hdfsUserName;

	@Column(name = "HDFS_PEM_LOCATION", length = 100, nullable = true)
	private String hdfsPemLocation;

	@Column(name = "HADOOP_INSTALL_DIR", length = 100, nullable = true)
	private String hadoopInstallDir;

}
