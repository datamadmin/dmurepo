package com.dataeconomy.migration.app.mysql.entity;

import java.time.LocalDateTime;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert(value = true)
@Entity
@Table(name = "DMU_RECON_MAIN")
public class DmuReconMainentity {

	@Id
	@Column(name = "REQUEST_NO", length = 100, nullable = false)
	private String requestNo;

	@Column(name = "USER_ID", length = 50, nullable = false)
	private String userId;

	@Column(name = "REQUESTED_TIME", nullable = false)
	private LocalDateTime requestedTime;

	@Column(name = "STATUS", length = 100, nullable = false)
	private String status;

	@Column(name = "REQUEST_TYPE", length = 100, nullable = true)
	private String requestType;

	@Column(name = "RECON_START_TIME", nullable = true)
	private LocalDateTime reconStartTime;

	@Column(name = "RECON_CMPLT_TIME", nullable = true)
	private LocalDateTime reconCmpltTime;

}
