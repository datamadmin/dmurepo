package com.dataeconomy.migration.app.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmuReconMainDTO {

	private String requestNo;

	private String userId;

	private LocalDateTime requestedTime;

	private String status;

	private String requestType;

	private LocalDateTime reconStartTime;

	private LocalDateTime reconCmpltTime;

}
