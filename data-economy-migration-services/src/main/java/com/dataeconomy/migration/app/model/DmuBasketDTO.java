package com.dataeconomy.migration.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmuBasketDTO {

	private Long srNo;

	private String userId;

	private String schemaName;

	private String tableName;

	private String filterCondition;

	private String targetS3Bucket;

	private String incrementalFlag;

	private String incrementalClmn;

	private String labelName;

	private boolean tknztnEnabled;

	private String tknztnFilePath;

	private String requestType;

	private boolean addtoBasket;
}
