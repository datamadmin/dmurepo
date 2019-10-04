package com.dataeconomy.migration.app.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmuUsersDTO {

	private String id;
	private String userRole;
	private String emailid;
	private String password;
	private String userName;
	private String createdBy;
	private LocalDateTime createdDate;
	private String updatedBy;
	private LocalDateTime updatedDate;
	private Boolean tokenization;
	private Integer basketCount;

}
