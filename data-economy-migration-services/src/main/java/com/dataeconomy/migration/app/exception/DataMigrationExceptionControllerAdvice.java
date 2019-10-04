package com.dataeconomy.migration.app.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DataMigrationExceptionControllerAdvice {

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseMessage handleAuthenticationException(AuthenticationException ex) {
		return ResponseMessage.builder().timestamp(LocalDateTime.now()).status(HttpStatus.NOT_FOUND.value())
				.message(ex.getLocalizedMessage()).build();
	}

	@ExceptionHandler(DataMigrationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseMessage handleDataMigrationException(DataMigrationException ex) {
		return ResponseMessage.builder().timestamp(LocalDateTime.now()).status(HttpStatus.BAD_REQUEST.value())
				.message(ex.getLocalizedMessage()).build();
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseMessage handleException(Exception ex) {
		return ResponseMessage.builder().timestamp(LocalDateTime.now()).status(HttpStatus.NOT_FOUND.value())
				.message(ex.getLocalizedMessage()).build();
	}

}
