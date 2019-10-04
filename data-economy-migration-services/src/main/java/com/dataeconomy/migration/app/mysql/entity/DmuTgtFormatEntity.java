package com.dataeconomy.migration.app.mysql.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@DynamicInsert(value = true)
@Table(name = "TGT_FORMAT_PROP")
@ApiModel(description = "All details about the format properties. ")
public class DmuTgtFormatEntity {

	@Id
	@Column(name = "SR_NO", length = 11, nullable = true)
	@ApiModelProperty(value = "1", notes = "The database generated sr No")
	private Long srNo;

	@Column(name = "TEXT_FORMAT_FLAG", length = 1, nullable = false)
	@ApiModelProperty(value = "Y", notes = "The database generated employee ID")
	private String textFormatFlag;

	@Column(name = "SRC_FORMAT_FLAG", length = 1, nullable = true)
	@ApiModelProperty(value = "N", notes = "The database generated employee ID")
	private String srcFormatFlag;

	@Column(name = "FIELD_DELIMITER", length = 1, nullable = true)
	@ApiModelProperty(value = "-", notes = "The database generated employee ID")
	private String fieldDelimiter;

	@Column(name = "SQNC_FORMAT_FLAG", length = 1, nullable = true)
	@ApiModelProperty(value = "N", notes = "The database generated employee ID")
	private String sqncFormatFlag;

	@Column(name = "RC_FORMAT_FLAG", length = 1, nullable = true)
	@ApiModelProperty(value = "N", notes = "The database generated employee ID")
	private String rcFormatFlag;

	@Column(name = "AVRO_FORMAT_FLAG", length = 1, nullable = true)
	@ApiModelProperty(value = "Y", notes = "The database generated employee ID")
	private String avroFormatFlag;

	@Column(name = "ORC_FORMAT_FLAG", length = 1, nullable = true)
	@ApiModelProperty(value = "Y", notes = "The database generated employee ID")
	private String orcFormatFlag;

	@Column(name = "PARQUET_FORMAT_FLAG", length = 1, nullable = true)
	@ApiModelProperty(value = "N", notes = "The database generated employee ID")
	private String parquetFormatFlag;

	@Column(name = "SRC_CMPRSN_FLAG", length = 1, nullable = true)
	@ApiModelProperty(notes = "The database generated employee ID")
	private String srcCmprsnFlag;

	@Column(name = "UNCMPRSN_FLAG", length = 1, nullable = true)
	@ApiModelProperty(value = "Y", notes = "The database generated employee ID")
	private String uncmprsnFlag;

	@Column(name = "GZIP_CMPRSN_FLAG", length = 1, nullable = true)
	@ApiModelProperty(value = "Y", notes = "The database generated employee ID")
	private String gzipCmprsnFlag;

}
