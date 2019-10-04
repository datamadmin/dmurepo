package com.dataeconomy.migration.app.exception;

public class DataMigrationException extends Exception {

	private static final long serialVersionUID = 1L;

	public DataMigrationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DataMigrationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataMigrationException(String message) {
		super(message);
	}

	public DataMigrationException(Throwable cause) {
		super(cause);
	}

}
