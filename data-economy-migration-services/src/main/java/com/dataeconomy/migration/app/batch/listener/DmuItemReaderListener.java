package com.dataeconomy.migration.app.batch.listener;

import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DmuItemReaderListener implements ItemReadListener<DmuHistoryDetailEntity> {

	@Override
	public void beforeRead() {
		log.info(" DmuItemReaderListener : beforeRead :: ");
	}

	@Override
	public void afterRead(DmuHistoryDetailEntity item) {
		log.info(" DmuItemReaderListener : beforeRead ::  requestNo {} , srNo {} ",
				item.getDmuHIstoryDetailPK().getRequestNo(), item.getDmuHIstoryDetailPK().getSrNo());

	}

	@Override
	public void onReadError(Exception ex) {
		log.info(" DmuItemReaderListener : onReadError ::  {} ", ex.getLocalizedMessage());
	}

}
