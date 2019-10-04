package com.dataeconomy.migration.app.demo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dataeconomy.migration.app.mysql.entity.DmuTgtOtherPropEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryMainRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuTgtOtherPropRepository;
import com.dataeconomy.migration.app.util.DmuConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DemoScheduler {

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	@Autowired
	private DmuHistoryMainRepository historyMainRepository;

	@Autowired
	private DemoRequestProcessor demorequestProcessor;

	@Autowired
	private DmuTgtOtherPropRepository propOtherRepository;

//	@Scheduled(cron = "* */2 * * * ?")
	//@Scheduled(fixedDelay = 180000)
	public void schedulerConfig() {
		try {
			log.info(" => dmuScheduler Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));

			DmuTgtOtherPropEntity tgtOtherPropOpt = propOtherRepository.findById(1L)
					.orElse(DmuTgtOtherPropEntity.builder().parallelUsrRqst(0L).parallelJobs(0L).build());

			Long taskInProgressCount = historyMainRepository.getTaskDetailsCount(DmuConstants.IN_PROGRESS);
			log.info(" => dmuScheduler Task :: current tasks in progress count  Time - {} - count {} ",
					dateTimeFormatter.format(LocalDateTime.now()), taskInProgressCount);

			if (taskInProgressCount == 0L) {
				Long tasksSubmittedCount = historyMainRepository.getTaskDetailsCount(DmuConstants.SUBMITTED);
				log.info(" => dmuScheduler Task ::tasksSubmittedCount from DMU_HISTORY_MAIN  {} ", tasksSubmittedCount);
				if (tasksSubmittedCount != 0L) {
					Optional.ofNullable(
							historyMainRepository.findHistoryMainDetailsByStatusScheduler(DmuConstants.SUBMITTED))
							.ifPresent(historyList -> {
//								historyList.parallelStream().forEach(historyEntity -> {
								historyList.parallelStream().forEach(historyEntity -> {
									log.info(
											" dmuTaskScheduler => created new thead with name  :: {} :: {} to process request No :: {} at time => ",
											Thread.currentThread().getName(),
											String.valueOf(Thread.currentThread().getId()),
											historyEntity.getRequestNo(),
											dateTimeFormatter.format(LocalDateTime.now()));
									log.info(" => dmuScheduler Task :: Thread {}  Execution Time - {}",
											Thread.currentThread().getName(),
											dateTimeFormatter.format(LocalDateTime.now()));
									demorequestProcessor.processRequest(historyEntity.getRequestNo(), tgtOtherPropOpt);
								});
							});
				}
			}
		} catch (Exception e) {
			log.error(" => DemoScheduler => schedulerConfig exception - {}", ExceptionUtils.getStackTrace(e));
		}
	}
}
