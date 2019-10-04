package com.dataeconomy.migration.app.model;

import java.util.Map;

import com.google.common.collect.Maps;

public class DmuReconAndRequestStatusDTO {

	Long reconMainTotalCount = 0L;
	Long reconHistoryMainTotalCount = 0L;
	Map<String, Long> reconMainCount = Maps.newLinkedHashMap();
	Map<String, Long> reconHistoryMainCount = Maps.newLinkedHashMap();

	public Map<String, Long> getReconMainCount() {
		return reconMainCount;
	}

	public void setReconMainCount(Map<String, Long> reconMainCount) {
		this.reconMainCount = reconMainCount;
	}

	public Map<String, Long> getReconHistoryMainCount() {
		return reconHistoryMainCount;
	}

	public void setReconHistoryMainCount(Map<String, Long> reconHistoryMainCount) {
		this.reconHistoryMainCount = reconHistoryMainCount;
	}

	@Override
	public String toString() {
		return "ReconAndRequestStatusDto [reconMainTotalCount=" + reconMainTotalCount + ", reconHistoryMainTotalCount="
				+ reconHistoryMainTotalCount + ", reconMainCount=" + reconMainCount + ", reconHistoryMainCount="
				+ reconHistoryMainCount + "]";
	}

	public Long getReconMainTotalCount() {
		return reconMainTotalCount;
	}

	public void setReconMainTotalCount(Long reconMainTotalCount) {
		this.reconMainTotalCount = reconMainTotalCount;
	}

	public Long getReconHistoryMainTotalCount() {
		return reconHistoryMainTotalCount;
	}

	public void setReconHistoryMainTotalCount(Long reconHistoryMainTotalCount) {
		this.reconHistoryMainTotalCount = reconHistoryMainTotalCount;
	}

	public DmuReconAndRequestStatusDTO() {
		super();
	}

}
