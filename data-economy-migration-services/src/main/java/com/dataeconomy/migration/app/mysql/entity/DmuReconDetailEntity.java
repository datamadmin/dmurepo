package com.dataeconomy.migration.app.mysql.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@DynamicInsert(value = true)
@Table(name = "DMU_RECON_DTL")
public class DmuReconDetailEntity {

	@EmbeddedId
	private DmuHistoryDetailId dmuHIstoryDetailPK;

	@Column(name = "SCHEMA_NAME", nullable = false)
	private String schemaName;

	@Column(name = "TABLE_NAME", length = 500, nullable = false)
	private String tableName;

	@Column(name = "FILTER_CONDITION", length = 500, nullable = true)
	private String filterCondition;

	@Column(name = "TARGET_S3_BUCKET", length = 500, nullable = true)
	private String targetS3Bucket;

	@Column(name = "INCREMENTAL_FLAG", length = 1, nullable = true)
	private String incrementalFlag;

	@Column(name = "INCREMENTAL_CLMN", length = 100, nullable = true)
	private String incrementalColumn;

	@Column(name = "SOURCE_COUNT", length = 1, nullable = true)
	private Long sourceCount;

	@Column(name = "TARGET_COUNT", length = 1, nullable = true)
	private Long targetCount;

	@Column(name = "STATUS", length = 100, nullable = true)
	private String status;

}
