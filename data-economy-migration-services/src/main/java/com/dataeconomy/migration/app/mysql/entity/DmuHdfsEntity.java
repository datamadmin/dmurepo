package com.dataeconomy.migration.app.mysql.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert(value = true)
@Entity
@Table(name = "DMU_HDFS")
public class DmuHdfsEntity {

	@Id
	@GeneratedValue
	@Column(name = "SR_NO", length = 11, nullable = false)
	private Long srNo;

	@Column(name = "HIVE_CNCTN_FLAG", length = 1, nullable = false)
	private String hiveCnctnFlag;

	@Column(name = "HIVE_HOST_NAME", length = 200, nullable = true)
	private String hiveHostName;

	@Column(columnDefinition = "bigint(20)", name = "HIVE_PORT_NMBR", length = 20, nullable = true)
	private Long hivePortNmbr;

	@Column(name = "IMPALA_CNCTN_FLAG", length = 1, nullable = false)
	private String impalaCnctnFlag;

	@Column(name = "IMPALA_HOST_NAME", length = 200, nullable = true)
	private String impalaHostName;

	@Column(columnDefinition = "bigint(20)", name = "IMPALA_PORT_NMBR", length = 20, nullable = true)
	private Long impalaPortNmbr;

	@Column(name = "SPARK_CNCTN_FLAG", length = 1, nullable = true)
	private String sparkCnctnFlag;

	@Column(name = "SQL_WH_DIR", length = 200, nullable = true)
	private String sqlWhDir;

	@Column(name = "HIVE_MS_URI", length = 200, nullable = true)
	private String hiveMsUri;

}
