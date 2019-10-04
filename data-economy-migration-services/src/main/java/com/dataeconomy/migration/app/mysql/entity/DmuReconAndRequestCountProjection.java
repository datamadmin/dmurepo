package com.dataeconomy.migration.app.mysql.entity;

public class DmuReconAndRequestCountProjection {

	private String status;
	private Long count;

	public DmuReconAndRequestCountProjection(String status, Long count) {
		super();
		this.status = status;
		this.count = count;
	}

	public DmuReconAndRequestCountProjection() {
		super();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "ReconAndRequestCountProjection [status=" + status + ", count=" + count + "]";
	}

}
