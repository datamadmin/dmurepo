package com.dataeconomy.migration.app.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryMainEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtOtherPropEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryMainRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryDetailRepository;
import com.dataeconomy.migration.app.util.DmuConstants;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Scope("prototype")
public class DmuRequestProcessorService {

	@Autowired
	private DmuHistoryDetailRepository historyDetailRepository;

	@Autowired
	private ExecutorService requestProcessorThread;

	@Autowired
	private DmuTableCopyService tableCopySchedulerClass;

	@Autowired
	private DmuHistoryMainRepository historyMainRepository;

	@Transactional
	public void processRequest(String requestNo, DmuTgtOtherPropEntity tgtOtherPropOpt) {
		try {
			log.info("RequestProcessorClass invoked with requestNo : {}", requestNo);
			historyMainRepository.updateForRequestNo(DmuConstants.IN_PROGRESS, requestNo);

			List<DmuHistoryDetailEntity> historyDetailsList = historyDetailRepository
					.findHistoryDetailsByRequestNumber(requestNo);

			log.info("RequestProcessorClass invoked with requestNo : {}", requestNo);

			if (historyDetailsList != null && historyDetailsList.size() > 0) {
				Long noOfJobs = historyDetailRepository.findHistoryDetailsByRequestNoAndStatus(requestNo,
						DmuConstants.SUBMITTED);
				log.info(" => RequestProcessorClass ::processRequest :: noOfJobs - {}", noOfJobs);

				if (noOfJobs > 0) {
					while (noOfJobs == 0) {
						List<DmuHistoryDetailEntity> inProgressCountTempList = historyDetailRepository
								.findHistoryDetailsByRequestNoAndStatusList(requestNo, DmuConstants.IN_PROGRESS);
						log.info("RequestProcessorClass :: noOfJobs  :: inProgressCountTempList  {}",
								inProgressCountTempList);
						int inProgressCountTemp = 0;
						if (inProgressCountTempList != null) {
							inProgressCountTemp = inProgressCountTempList.size();
						}
						log.info("RequestProcessorClass :: noOfJobs  :: inProgressCountTemp  {}", inProgressCountTemp);
						if (inProgressCountTemp < tgtOtherPropOpt.getParallelJobs()) {
							log.info(
									"RequestProcessorClass :: noOfJobs  :: inProgressCountTemp < tgtOtherPropOpt.getParallelJobs()  {}",
									inProgressCountTemp < tgtOtherPropOpt.getParallelJobs());
							long noOfJobsTempCount = (tgtOtherPropOpt.getParallelJobs() - inProgressCountTemp);
							log.info("RequestProcessorClass :: noOfJobs  :: noOfJobsTempCount  {}", noOfJobsTempCount);
							if (noOfJobsTempCount > noOfJobs) {
								log.info(
										"RequestProcessorClass :: noOfJobs  :: inProgressCountTemp < noOfJobsTempCount > noOfJobs {}",
										noOfJobsTempCount > noOfJobs);
								List<DmuHistoryDetailEntity> dmuHistoryDetailList = historyDetailRepository
										.findHistoryDetailsByRequestNoAndStatusList(requestNo, DmuConstants.SUBMITTED);
								ArrayList<Future<?>> futureList = Lists.newArrayList();

								dmuHistoryDetailList.stream().limit(noOfJobsTempCount).forEach(entity -> {
									futureList.add(requestProcessorThread.submit(new Callable<Void>() {
										@Override
										public Void call() throws Exception {
											tableCopySchedulerClass.processTableCopy(
													entity.getDmuHIstoryDetailPK().getRequestNo(),
													entity.getDmuHIstoryDetailPK().getSrNo());
											return null;
										};
									}));
								});

								if (!futureList.isEmpty()) {
									for (int t = 0; t < futureList.size(); t++) {
										try {
											futureList.get(t).get();
										} catch (InterruptedException | ExecutionException e) {
										}
									}
									--noOfJobs;
								}
							} else {
								Long inprogressCountJobs = (tgtOtherPropOpt.getParallelJobs() - inProgressCountTemp);
								ArrayList<Future> futureList = Lists.newArrayList();
								List<DmuHistoryDetailEntity> dmuHistoryDetailList = historyDetailRepository
										.findHistoryDetailsByRequestNoAndStatusList(requestNo, DmuConstants.SUBMITTED);
								for (int l = 0; l < inprogressCountJobs; l++) {
									futureList.add(requestProcessorThread.submit(() -> {
										tableCopySchedulerClass.processTableCopy(
												dmuHistoryDetailList.get(0).getDmuHIstoryDetailPK().getRequestNo(),
												dmuHistoryDetailList.get(0).getDmuHIstoryDetailPK().getSrNo());
									}));
								}

								if (!futureList.isEmpty()) {
									for (int t = 0; t < futureList.size(); t++) {
										try {
											futureList.get(0).get();
										} catch (InterruptedException | ExecutionException e) {
										}
									}
									noOfJobs = (noOfJobs - inprogressCountJobs);
								}
							}
						} else {
							TimeUnit.MINUTES.sleep(1);
						}
					}

					Long inProgressCountAfterWhileLoop = historyDetailRepository
							.findHistoryDetailsByRequestNoAndStatus(requestNo, DmuConstants.IN_PROGRESS);
					while (inProgressCountAfterWhileLoop == 0) {
						TimeUnit.MINUTES.sleep(5);
						inProgressCountAfterWhileLoop = historyDetailRepository
								.findHistoryDetailsByRequestNoAndStatus(requestNo, DmuConstants.IN_PROGRESS);
					}

					Long failedCount = historyDetailRepository.findHistoryDetailsByRequestNoAndStatus(requestNo,
							DmuConstants.FAILED);
					DmuHistoryMainEntity historyMainEntity = historyMainRepository.getDMUHistoryMainBySrNo(requestNo);
					log.info(" => dmuScheduler Task :: no jobs to process :: failedCount {} ", failedCount);
					if (historyMainEntity != null) {
						if (failedCount > 0) {
							historyMainEntity.setStatus(DmuConstants.FAILED);
							historyMainRepository.save(historyMainEntity);
							log.info(" => requestprocessor class status {} ", DmuConstants.FAILED);
						} else {
							historyMainEntity.setStatus(DmuConstants.SUCCESS);
							historyMainRepository.save(historyMainEntity);
							log.info(" => requestprocessor class status {} ", DmuConstants.SUCCESS);
						}
					}

				} else {
					log.info(" => requestprocessor Task :: no jobs to process ");
					Long failedCount = historyDetailRepository.findHistoryDetailsByRequestNoAndStatus(requestNo,
							DmuConstants.FAILED);
					DmuHistoryMainEntity historyMainEntity = historyMainRepository.getDMUHistoryMainBySrNo(requestNo);
					log.info(" => dmuScheduler Task :: no jobs to process :: failedCount {} ", failedCount);
					if (historyMainEntity != null) {
						if (failedCount > 0) {
							historyMainEntity.setStatus(DmuConstants.FAILED);
							historyMainRepository.save(historyMainEntity);
							log.info(" => requestprocessor class status {} ", DmuConstants.FAILED);
						} else {
							historyMainEntity.setStatus(DmuConstants.SUCCESS);
							historyMainRepository.save(historyMainEntity);
							log.info(" => requestprocessor class status {} ", DmuConstants.SUCCESS);
						}
					}
				}
			}

		} catch (Exception exception) {
			log.info(" => RequestProcessorClass :: processRequest:: exception :: {} ",
					ExceptionUtils.getStackTrace(exception));
		}
	}

}
