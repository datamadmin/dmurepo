package com.dataeconomy.migration.app.batch.listener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dataeconomy.migration.app.mysql.repository.DmuHistoryDetailRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryMainRepository;
import com.dataeconomy.migration.app.util.DmuConstants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DmuJobCompletionNotificationListener extends JobExecutionListenerSupport {

	private long startTime;

	@Autowired
	DmuHistoryMainRepository historyMainRepository;

	@Autowired
	DmuHistoryDetailRepository historyDetailRepository;

	@Override
	public synchronized void afterJob(JobExecution jobExecution) {
		log.info("Total time take in seconds : {} ",
				TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
		ExecutionContext executionContext = jobExecution.getExecutionContext();

		if (executionContext.containsKey(DmuConstants.REQUEST_NO)) {
			log.info(" => executionContext.getString() :: requestNo # {} ",
					executionContext.getString(DmuConstants.REQUEST_NO));
			String requestNo = executionContext.getString(DmuConstants.REQUEST_NO);
			historyMainRepository.findById(requestNo).ifPresent(historyMainEntity -> {
				historyMainEntity.setExctnCmpltTime(LocalDateTime.now());
				if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
					log.info("Job finished with status : {} , requestNo : {} ", jobExecution.getStatus(), requestNo);
					if (historyDetailRepository.findHistoryDetailsByRequestNoAndStatus(requestNo,
							DmuConstants.FAILED) > 0) {
						historyMainEntity.setStatus(DmuConstants.FAILED);
						historyMainRepository.save(historyMainEntity);
					} else {
						historyMainEntity.setStatus(DmuConstants.SUCCESS);
						historyMainRepository.save(historyMainEntity);
					}
				} else if (jobExecution.getStatus() == BatchStatus.FAILED) {
					log.info("Job finished with status : {} , requestNo : {} ", jobExecution.getStatus(), requestNo);
					historyMainEntity.setStatus(DmuConstants.FAILED);
					historyMainRepository.save(historyMainEntity);
					log.info("BATCH JOB FAILED WITH EXCEPTIONS");
					Optional.ofNullable(jobExecution.getAllFailureExceptions()).orElse(new ArrayList<>()).stream()
							.forEach(throwable -> log.error("exception : {} ", throwable.getLocalizedMessage()));
				}
			});
		}

	}

	@Override
	public synchronized void beforeJob(JobExecution jobExecution) {
		startTime = System.currentTimeMillis();
		log.info("  Job starts at : {} ", LocalDateTime.now());
		super.beforeJob(jobExecution);
	}

}
