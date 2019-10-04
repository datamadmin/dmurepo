package com.dataeconomy.migration.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmuReconDetailDTO {

	private String requestNo;

	private Long srNo;

	private Long userId;

	private String schemaName;

	private String tableName;

	private String filterCondition;

	private String targetS3Bucket;

	private String incrementalFlag;

	private String incrementalColumn;

	private Long sourceCount;

	private Long targetCount;

	private String status;

}
