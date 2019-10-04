package com.dataeconomy.migration.app.batch.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DmuSchedulerJdbcWriter implements ItemWriter<DmuHistoryDetailEntity> {

	private StepExecution stepExecution;

	@Override
	public synchronized void write(List<? extends DmuHistoryDetailEntity> items) throws Exception {
		Optional.ofNullable(items).orElse(new ArrayList<>()).stream().limit(1).forEach(item -> {
			log.info(" => sr No " + item.getDmuHIstoryDetailPK().getSrNo());
			stepExecution.getJobExecution().getExecutionContext().put("requestNo",
					item.getDmuHIstoryDetailPK().getRequestNo());
		});

	}

	@AfterStep
	public synchronized ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@BeforeStep
	public synchronized void saveStepExecution(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
	}
}
