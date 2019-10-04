package com.dataeconomy.migration.app.mysql.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class DmuPtgyTempId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "USER_ID", length = 20, nullable = false)
	private String userId;

	@Column(name = "LABEL_NAME", length = 50, nullable = false)
	private String labelName;

}
