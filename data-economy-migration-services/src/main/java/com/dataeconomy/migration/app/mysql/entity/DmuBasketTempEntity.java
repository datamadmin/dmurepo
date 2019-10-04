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

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert(value = true)
@Table(name = "DMU_BASKET_TEMP")
public class DmuBasketTempEntity {

	@Id
	// @GeneratedValue
	@Column(name = "SR_NO", length = 11, nullable = false)
	private Long srNo;

	@Column(name = "USER_ID", length = 20, nullable = false)
	private String userId;

	@Column(name = "SCHEMA_NAME", length = 100, nullable = false)
	private String schemaName;

	@Column(name = "TABLE_NAME", length = 100, nullable = false)
	private String tableName;

	@Column(name = "FILTER_CONDITION", length = 500, nullable = true)
	private String filterCondition;

	@Column(name = "TARGET_S3_BUCKET", length = 500, nullable = true)
	private String targetS3Bucket;

	@Column(name = "INCREMENTAL_FLAG", length = 1, nullable = true)
	private String incrementalFlag;

	@Column(name = "INCREMENTAL_CLMN", length = 100, nullable = true)
	private String incrementalClmn;

	@Column(name = "LABEL_NAME", length = 50, nullable = true)
	private String labelName;

	@Column(name = "REQUEST_TYPE", length = 50, nullable = true)
	private String requestType;

}
