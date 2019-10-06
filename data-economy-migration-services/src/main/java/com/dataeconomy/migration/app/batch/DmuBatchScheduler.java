package com.dataeconomy.migration.app.batch;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.dataeconomy.migration.app.batch.listener.DmuItemReaderListener;
import com.dataeconomy.migration.app.batch.listener.DmuJobCompletionNotificationListener;
import com.dataeconomy.migration.app.batch.listener.DmuStepExecutionNotificationListener;
import com.dataeconomy.migration.app.batch.processor.DmuSchedulerProcessor;
import com.dataeconomy.migration.app.batch.writer.DmuSchedulerJdbcWriter;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtOtherPropEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryDetailRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryMainRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuTgtOtherPropRepository;
import com.dataeconomy.migration.app.util.DmuConstants;
import com.dataeconomy.migration.app.util.DmuServiceHelper;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class DmuBatchScheduler implements SchedulingConfigurer {

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	DmuTgtOtherPropRepository otherPropRepository;

	@Autowired
	DmuHistoryMainRepository historyMainRepository;

	@Autowired
	DmuHistoryDetailRepository historyDetailRepository;

	@Autowired
	DmuServiceHelper dmuServiceHelper;

	@Autowired
	Job job;

	@Scheduled(fixedDelay = 120000)
	public void performDataMigrationProcess() {
		Long noOfParallelusers = 0L;
		Long noOfParallelJobs = 0L;
		Optional<DmuTgtOtherPropEntity> otherPropEntityOpt = otherPropRepository.findById(1L);
		if (otherPropEntityOpt.isPresent()) {
			noOfParallelusers = otherPropEntityOpt.get().getParallelUsrRqst();
			noOfParallelJobs = otherPropEntityOpt.get().getParallelJobs();
		}
		long inProgressCount = historyMainRepository.getTaskDetailsCount(DmuConstants.IN_PROGRESS);
		log.info(" => DmuBatchScheduler : inProgressCount : {} , noOfParallelusers: {} , noOfParallelJobs : {} ",
				inProgressCount, noOfParallelusers, noOfParallelJobs);
		if (inProgressCount < noOfParallelusers) {
			long taskSubmittedCount = historyMainRepository.getTaskDetailsCount(DmuConstants.SUBMITTED);
			if (taskSubmittedCount > 0) {

				long limitCount = ((noOfParallelusers - inProgressCount) > taskSubmittedCount) ? taskSubmittedCount
						: (noOfParallelusers - inProgressCount);

				log.info(" DmuBatchScheduler : taskSubmittedCount : {} ", taskSubmittedCount);
				log.info(" DmuBatchScheduler : noOfUsers thread count  : {} ", limitCount);
				log.info("Job Started at : {} ", dateTimeFormatter.format(LocalDateTime.now()));
				Optional.ofNullable(
						historyMainRepository.findHistoryMainDetailsByStatusScheduler(DmuConstants.SUBMITTED))
						.ifPresent(
								entityList -> entityList.parallelStream().limit(limitCount).forEach(historyEntity -> {
									historyMainRepository.updateForRequestNo(historyEntity.getRequestNo(),
											DmuConstants.IN_PROGRESS);
									try {
										log.info("current thread {} executing the requestNo {} ",
												Thread.currentThread().getName(), historyEntity.getRequestNo());
										JobExecution jobExecution = jobLauncher.run(job, new JobParametersBuilder()
												.addString("requestNo", historyEntity.getRequestNo())
												.addLong("parallelJobs", getJobCount(historyEntity.getRequestNo()))
												.addString("name",
														historyEntity.getRequestNo() + " -" + historyEntity.getUserId())
												.addDate("date", new Date()).addLong("time", System.currentTimeMillis())
												.toJobParameters());
										log.info("Job started with name, time : {} , finished with time  {} ",
												jobExecution.getJobConfigurationName(), jobExecution.getCreateTime(),
												jobExecution.getEndTime());
									} catch (Exception e) {
										log.error(" => Exception occured at DmuBatchScheduler :: {} ",
												ExceptionUtils.getStackTrace(e));
										historyMainRepository.updateForRequestNo(historyEntity.getRequestNo(),
												DmuConstants.FAILED);
									}
								}));
			} else {
				log.info(" DmuBatchScheduler : no tasks submitted for scheduler ");
			}
		} else {
			log.info(
					" DmuBatchScheduler : inProgressCount : {} Job not executed due to numbers users requests limit exceeded => {} ",
					inProgressCount);
		}
	}

	@Bean
	public RepositoryMetadata repositoryMetadata() {
		return new DefaultRepositoryMetadata(DmuHistoryDetailRepository.class);
	}

	@Bean
	@StepScope
	public RepositoryItemReader<DmuHistoryDetailEntity> reader(@Value("#{jobParameters['requestNo']}") String requestNo,
			DmuHistoryDetailRepository historyDetailRepository) {
		log.info(" processing item reader for requestNo : {} ", requestNo);
		RepositoryItemReader<DmuHistoryDetailEntity> historyDetailsRepositoryReader = new RepositoryItemReader<>();
		historyDetailsRepositoryReader.setRepository(historyDetailRepository);
		historyDetailsRepositoryReader.setMethodName("findHistoryDetailsByRequestNoAndStatusListForBatch");
		List<Object> list = Lists.newArrayList();
		list.add(requestNo);
		list.add(DmuConstants.SUBMITTED);
		historyDetailsRepositoryReader.setArguments(list);
		HashMap<String, Sort.Direction> sorts = new HashMap<>(); 
		sorts.put("dmuHIstoryDetailPK.requestNo", Direction.ASC);
		int noOfThreads = Math.toIntExact(getJobCount(requestNo));
		log.info(" RepositoryItemReader processing with records count {} ", noOfThreads);
		historyDetailsRepositoryReader.setMaxItemCount(noOfThreads);
		historyDetailsRepositoryReader.setSort(sorts);
		return historyDetailsRepositoryReader;
	}

	@Bean
	public Step step1(StepBuilderFactory stepBuilderFactory, DmuSchedulerProcessor schedulerProcessor,
			DmuSchedulerJdbcWriter stepWriter, DmuStepExecutionNotificationListener stepListener,
			RepositoryItemReader<DmuHistoryDetailEntity> reader, TaskExecutor taskExecutor,
			DmuItemReaderListener dmuItemReaderListener) {
		return stepBuilderFactory.get("step1").<DmuHistoryDetailEntity, DmuHistoryDetailEntity>chunk(1).reader(reader)
				.processor(schedulerProcessor).chunk(1).writer(stepWriter).listener(stepListener)
				.listener(dmuItemReaderListener).taskExecutor(taskExecutor).throttleLimit(30).build();
	}

	@Bean
	public TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
		taskExecutor.setConcurrencyLimit(30);
		return taskExecutor;
	}

	@Bean
	public Job job(JobBuilderFactory jobBuilderFactory, Step s1,
			DmuJobCompletionNotificationListener jobCompletionListener) {
		return jobBuilderFactory.get("job").incrementer(new RunIdIncrementer()).start(s1)
				.listener(jobCompletionListener).build();
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setTaskScheduler(poolScheduler());
	}

	@Bean
	public TaskScheduler poolScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setThreadNamePrefix("DmuBatchScheduler-Async");
		scheduler.setPoolSize(30);
		return scheduler;
	}

	private synchronized Long getJobCount(String requestNo) {
		Long numberOfThreads = 0L;
		try {
			Long numberOfJobs = historyDetailRepository.findHistoryDetailsByRequestNoAndStatusAscOrder(requestNo,
					DmuConstants.SUBMITTED);
			log.info(" DmuBatchScheduler => getJobCount => numberOfJobs : {} ", numberOfJobs);
			if (numberOfJobs > 0) {
				Long inProgressJobs = historyDetailRepository.findHistoryDetailsByRequestNoAndStatus(requestNo,
						DmuConstants.IN_PROGRESS);
				Long parallelJobs = NumberUtils.toLong(dmuServiceHelper.getProperty(DmuConstants.PARALLEL_JOBS));
				log.info(
						" DmuBatchScheduler => getJobCount => numberOfJobs : {} , inProgressJobs : {} , parallelJobs : {}  ",
						numberOfJobs, inProgressJobs, parallelJobs);
				if (inProgressJobs < parallelJobs) {
					if ((parallelJobs - inProgressJobs) > numberOfJobs) {
						numberOfThreads = numberOfJobs;
					} else {
						numberOfThreads = (parallelJobs - inProgressJobs);
					}
				}
				log.info(
						" DmuBatchScheduler => getJobCount => numberOfThreads : {} to process the request for requestNo :: {} ",
						numberOfThreads, requestNo);
			}
			return numberOfThreads;
		} catch (Exception exception) {
			log.info(" Exception Occured at => DmuBatchScheduler => getJobCount => numberOfJobs : {} ",
					ExceptionUtils.getStackTrace(exception));
			return 0L;
		}
	}

}