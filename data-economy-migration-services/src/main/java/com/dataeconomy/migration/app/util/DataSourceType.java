package com.dataeconomy.migration.app.util;

public enum DataSourceType {
	REGULAR("REGULAR"), LARGEQUERY("LARGEQUERY"), MEDIUMQUERY("MEDIUMQUERY"), SMALLQUERY("SMALLQUERY");

	private String dataSourceType;

	DataSourceType(String dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

	public String dataSourceType() {
		return dataSourceType;
	}

}
