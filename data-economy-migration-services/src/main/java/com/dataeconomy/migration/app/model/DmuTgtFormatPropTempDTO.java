package com.dataeconomy.migration.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmuTgtFormatPropTempDTO {
	String formatType;
	String compressionType;
	String fieldDelimiter;
}
