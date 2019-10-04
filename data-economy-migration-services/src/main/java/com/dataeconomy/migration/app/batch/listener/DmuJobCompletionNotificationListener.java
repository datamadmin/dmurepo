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

import com.dataeconomy.migration.app.mysql.entity.DmuHistoryMainEntity;
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

	private static final String REQUEST_NO_UPDATED_WITH_STATUS = "requestNo : {} , updated with status : {} ";

	@Override
	public synchronized void afterJob(JobExecution jobExecution) {
		log.info("Total time take in seconds : {} ",
				TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
		ExecutionContext executionContext = jobExecution.getExecutionContext();

		if (executionContext.containsKey(DmuConstants.REQUEST_NO)) {
			log.info(" => executionContext.getString() :: requestNo # {} ",
					executionContext.getString(DmuConstants.REQUEST_NO));
			String requestNo = executionContext.getString(DmuConstants.REQUEST_NO);
			log.info(" BATCH JOB COMPLETED SUCCESSFULLY for REQUEST # {} ", requestNo);

			Optional<DmuHistoryMainEntity> historyEntityOpt = historyMainRepository.findById(requestNo);

			if (historyEntityOpt.isPresent()) {
				DmuHistoryMainEntity historyEntity = historyEntityOpt.get();
				historyEntity.setExctnCmpltTime(LocalDateTime.now());
				if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
					long submittedCount = historyDetailRepository.findHistoryDetailsByRequestNoAndStatus(requestNo,
							DmuConstants.SUBMITTED);
					log.info("Job finished with status : {} ", jobExecution.getStatus());
					if (historyDetailRepository.findHistoryDetailsByRequestNoAndStatus(requestNo,
							DmuConstants.FAILED) > 0) {
						log.info(REQUEST_NO_UPDATED_WITH_STATUS, requestNo, DmuConstants.FAILED);
						if (submittedCount > 0) {
							historyEntity.setStatus(DmuConstants.SUBMITTED);
							historyMainRepository.save(historyEntity);
						} else {
							historyEntity.setStatus(DmuConstants.FAILED);
							historyMainRepository.save(historyEntity);
						}
					} else {
						if (submittedCount > 0) {
							historyEntity.setStatus(DmuConstants.SUBMITTED);
							historyMainRepository.save(historyEntity);
						} else {
							historyEntity.setStatus(DmuConstants.SUCCESS);
							historyMainRepository.save(historyEntity);
						}
						log.info(REQUEST_NO_UPDATED_WITH_STATUS, historyEntity.getRequestNo(), DmuConstants.SUCCESS);
					}
				} else if (jobExecution.getStatus() == BatchStatus.FAILED) {
					log.info("Job finished with status : {} ", jobExecution.getStatus());
					log.info(REQUEST_NO_UPDATED_WITH_STATUS, historyEntity.getRequestNo(), DmuConstants.FAILED);
					historyEntity.setStatus(DmuConstants.FAILED);
					historyMainRepository.save(historyEntity);
					log.info("BATCH JOB FAILED WITH EXCEPTIONS");
					Optional.ofNullable(jobExecution.getAllFailureExceptions()).orElse(new ArrayList<>()).stream()
							.forEach(throwable -> log.error("exception : {} ", throwable.getLocalizedMessage()));
				}
			}
		}
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		startTime = System.currentTimeMillis();
		log.info("  Job starts at : {} ", LocalDateTime.now());
		super.beforeJob(jobExecution);
	}

}
