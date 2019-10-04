package com.dataeconomy.migration.app.demo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryMainEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtOtherPropEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryMainRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryDetailRepository;
import com.dataeconomy.migration.app.util.DmuConstants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DemoRequestProcessor {

	@Autowired
	private DmuHistoryDetailRepository historyDetailRepository;

	@Autowired
	private DemoTableCopyProcessor demoTableCopyProcessor;

	@Autowired
	private DmuHistoryMainRepository historyMainRepository;

	ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

//	@Transactional
	public void processRequest(String requestNo, DmuTgtOtherPropEntity tgtOtherPropOpt) {
		try {
			log.info(" updates history main table with staus => 'In Progress' for requestNo {} ", requestNo);
//			historyMainRepository.updateForRequestNo(requestNo, Constants.IN_PROGRESS);

			Optional<DmuHistoryMainEntity> dmuHistoryOpt = historyMainRepository.findById(requestNo);

			if (dmuHistoryOpt.isPresent()) {
				DmuHistoryMainEntity updated = dmuHistoryOpt.get();
				updated.setStatus(DmuConstants.IN_PROGRESS);
				historyMainRepository.save(updated);
			}

			Long numberOfJobs = historyDetailRepository.findHistoryDetailsByRequestNoAndStatusAscOrder(requestNo,
					DmuConstants.SUBMITTED);
			log.info(" DemoRequestProcessor >>  processRequest >> number fo jobs submitted  {} ", numberOfJobs);

			Long inprogressJobs = historyDetailRepository.findHistoryDetailsByRequestNoAndStatusAscOrder(requestNo,
					DmuConstants.IN_PROGRESS);
			log.info(" DemoRequestProcessor >>  processRequest >> number fo jobs inp rogress   {} ", inprogressJobs);

			if (numberOfJobs > 0) {
				log.info(" retrieving the records for requestNo {} from DMU_HISTORY_DTL {} ", requestNo);
				List<DmuHistoryDetailEntity> dmuHistoryDetailList = historyDetailRepository
						.findHistoryDetailsByRequestNoAndStatusList(requestNo, DmuConstants.SUBMITTED);

				if (CollectionUtils.isNotEmpty(dmuHistoryDetailList)) {
					log.info(" retrieved the records for requestNo {} from DMU_HISTORY_DTL {} with count {} ",
							dmuHistoryDetailList.size());
					dmuHistoryDetailList.parallelStream().limit(tgtOtherPropOpt.getParallelJobs())
							.forEach(historyDetailEntity -> {
								log.info("Thread : " + Thread.currentThread().getName() + ", value: " + requestNo
										+ " - " + historyDetailEntity.getDmuHIstoryDetailPK().getSrNo());
								demoTableCopyProcessor.processTableCopy(requestNo,
										historyDetailEntity.getDmuHIstoryDetailPK().getSrNo());
							});

					ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
					executor.scheduleAtFixedRate(new Runnable() {

						@Override
						public void run() {
							Long inProgressCount = historyDetailRepository
									.findHistoryDetailsByRequestNoAndStatus(requestNo, DmuConstants.IN_PROGRESS);
							log.info(" At ScheduledExecutorService =>>>>>>>>>>> inProgressCount :: ", inProgressCount);
							if (inProgressCount == 0) {
								Long failedCount = historyDetailRepository
										.findHistoryDetailsByRequestNoAndStatus(requestNo, DmuConstants.FAILED);
								DmuHistoryMainEntity historyMainEntity = historyMainRepository
										.getDMUHistoryMainBySrNo(requestNo);
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
								executor.shutdown();
							}
						}
					}, 0, 1, TimeUnit.MINUTES);
				}
			} else {
				log.info(" => requestprocessor Task :: no jobs to process ");
				Long failedCount = historyDetailRepository.findHistoryDetailsByRequestNoAndStatus(requestNo,
						DmuConstants.FAILED);
				if (failedCount > 0) {
					DmuHistoryMainEntity historyMainEntity = historyMainRepository.getDMUHistoryMainBySrNo(requestNo);
					log.info(" => dmuScheduler Task :: no jobs to process :: failedCount {} ", failedCount);
					historyMainEntity.setStatus(DmuConstants.FAILED);
					historyMainRepository.save(historyMainEntity);
					log.info(" => requestprocessor class status {} ", DmuConstants.FAILED);
				} else {
					DmuHistoryMainEntity historyMainEntity = historyMainRepository.getDMUHistoryMainBySrNo(requestNo);
					log.info(" => dmuScheduler Task :: no jobs to process :: failedCount {} ", failedCount);
					historyMainEntity.setStatus(DmuConstants.SUCCESS);
					historyMainRepository.save(historyMainEntity);
					log.info(" => requestprocessor class status {} ", DmuConstants.SUCCESS);
				}
			}
		} catch (Exception exceptin) {

		}
	}

}