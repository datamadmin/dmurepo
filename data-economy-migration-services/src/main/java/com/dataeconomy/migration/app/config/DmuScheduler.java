package com.dataeconomy.migration.app.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.dataeconomy.migration.app.mysql.entity.DmuHistoryMainEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtOtherPropEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryMainRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuTgtOtherPropRepository;
import com.dataeconomy.migration.app.scheduler.DmuRequestProcessorService;
import com.dataeconomy.migration.app.util.DmuConstants;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

//@Component
@Slf4j
public class DmuScheduler {

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	@Autowired
	private DmuTgtOtherPropRepository propOtherRepository;

	@Autowired
	private DmuHistoryMainRepository historyMainRepository;

	@Autowired
	private DmuRequestProcessorService requestProcessorClass;

	@Autowired
	private ExecutorService cachedThreadPool;

	// @Scheduled(cron = "* */5 * * * ?")
	public void dmuScheduler() {
		try {
			log.info(" => dmuScheduler Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
			DmuTgtOtherPropEntity tgtOtherPropOpt = propOtherRepository.findById(1L)
					.orElse(DmuTgtOtherPropEntity.builder().parallelUsrRqst(0L).parallelJobs(0L).build());

			Long taskInProgressCount = historyMainRepository.getTaskDetailsCount(DmuConstants.IN_PROGRESS);

			log.info(" => dmuScheduler Task :: taskInProgressCount - {}", taskInProgressCount);
			log.info(" => dmuScheduler Task :: parallelUsersRequest - {}", tgtOtherPropOpt.getParallelUsrRqst());
			log.info(" => dmuScheduler Task :: parallelJobsRequest - {}", tgtOtherPropOpt.getParallelJobs());

			if (taskInProgressCount <= tgtOtherPropOpt.getParallelUsrRqst()) {
				Long taskSubmittedCount = historyMainRepository.getTaskDetailsCount(DmuConstants.SUBMITTED);
				log.info(" => dmuScheduler Task :: taskSubmittedCount - {}", taskSubmittedCount);
				if (taskSubmittedCount != 0) {
					if ((tgtOtherPropOpt.getParallelUsrRqst() - taskInProgressCount) > taskSubmittedCount) {
						log.info(
								" => parallel user requests greater than submitted count  - taskSubmittedCount :: {}  ",
								taskSubmittedCount);
						List<DmuHistoryMainEntity> historyMainList = historyMainRepository
								.findHistoryMainDetailsByStatus(DmuConstants.STATUS_LIST);
						if (CollectionUtils.isNotEmpty(historyMainList)) {
							ArrayList<Future<?>> futureList = Lists.newArrayList();
							historyMainList.stream().limit(taskSubmittedCount).forEach(entity -> {
								futureList.add(cachedThreadPool.submit(new Callable<Object>() {
									@Override
									public Object call() throws Exception {
										requestProcessorClass.processRequest(entity.getRequestNo(), tgtOtherPropOpt);
										return "Result of " + entity.getRequestNo();
									};
								}));
							});

							if (!futureList.isEmpty()) {
								for (int t = 0; t < futureList.size(); t++) {
									try {
										futureList.get(t).get();
									} catch (InterruptedException | ExecutionException e) {
										log.error(" => InterruptedException at DMUScheduler ::  - {}",
												taskSubmittedCount);
									}
								}
							}
						}
					} else {
						long count = (tgtOtherPropOpt.getParallelUsrRqst() - taskInProgressCount);
						log.info(" => parallel user requests less than submitted count  - {} ", count);
						List<DmuHistoryMainEntity> historyMainList = historyMainRepository
								.findHistoryMainDetailsByStatus(DmuConstants.STATUS_LIST);
						if (historyMainList != null && historyMainList.size() > 0) {
							ArrayList<Future<?>> futureList = Lists.newArrayList();
							historyMainList.stream().limit(count).forEach(entity -> {
								futureList.add(cachedThreadPool.submit(new Callable<String>() {
									@Override
									public String call() throws Exception {
										requestProcessorClass.processRequest(entity.getRequestNo(), tgtOtherPropOpt);
										return "Result of " + entity.getRequestNo();
									};
								}));
							});

							if (!futureList.isEmpty()) {
								for (int t = 0; t < futureList.size(); t++) {
									try {
										futureList.get(t).get();
									} catch (InterruptedException | ExecutionException e) {
										log.error(" => InterruptedException at DMUScheduler ::  - {}",
												taskSubmittedCount);
									}
								}
							}
						}
					}
				} else {
					log.info(" => There are no submitted records exiting the scheduler - {}", taskSubmittedCount);
				}
			} else if (taskInProgressCount < tgtOtherPropOpt.getParallelUsrRqst()) {
				log.info(" => taskInProgressCount > parallelUserRequest -> so terminated the execution - {}");
			}
		} catch (Exception e) {
			log.error(" => taskInProgressCount > parallelUserRequest -> so terminated the execution - {}",
					ExceptionUtils.getStackTrace(e));
		}
	}

}