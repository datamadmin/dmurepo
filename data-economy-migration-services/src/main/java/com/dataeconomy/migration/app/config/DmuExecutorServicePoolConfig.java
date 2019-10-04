package com.dataeconomy.migration.app.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class DmuExecutorServicePoolConfig {

	@Bean("cachedThreadPool")
	public ExecutorService cachedThreadPool() {
		return Executors.newCachedThreadPool();
	}

	@Bean("requestProcessorThread")
	public ExecutorService requestProcessorThread() {
		return Executors.newCachedThreadPool();
	}

}
