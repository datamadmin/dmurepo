package com.dataeconomy.migration.app.service;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.model.DmuReconAndRequestStatusDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuReconAndRequestCountProjection;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryMainRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuReconMainRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DmuHomeService {

	@Autowired
	private DmuReconMainRepository dmuReconMainRepository;

	@Autowired
	private DmuHistoryMainRepository dmuHistoryMainRepository;

	@Timed
	@Transactional(readOnly = true)
	public DmuReconAndRequestStatusDTO getRequestAndReconStatus() {
		log.info(" HomeService :: getRequestAndReconStatus ");
		DmuReconAndRequestStatusDTO reconAndRequestStatusDto = new DmuReconAndRequestStatusDTO();
		try {

			List<DmuReconAndRequestCountProjection> reconMainCountList = dmuReconMainRepository
					.findReconMainStatusCount();
			List<DmuReconAndRequestCountProjection> reconHistoryMainCountList = dmuHistoryMainRepository
					.findReconHistoryStatusCount();

			if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(reconMainCountList)) {
				reconMainCountList.stream().forEach(reconMainObj -> {
					reconAndRequestStatusDto.getReconMainCount().put(reconMainObj.getStatus(), reconMainObj.getCount());
					reconAndRequestStatusDto.setReconMainTotalCount(
							reconAndRequestStatusDto.getReconMainTotalCount() + reconMainObj.getCount());
				});
			}

			if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(reconHistoryMainCountList)) {
				reconHistoryMainCountList.stream().forEach(reconMainObj -> {
					reconAndRequestStatusDto.getReconHistoryMainCount().put(reconMainObj.getStatus(),
							reconMainObj.getCount());
					reconAndRequestStatusDto.setReconHistoryMainTotalCount(
							reconAndRequestStatusDto.getReconHistoryMainTotalCount() + reconMainObj.getCount());
				});
			}

			log.info(" HomeService ::   getRequestAndReconStatus  :: all count :: {} ", reconAndRequestStatusDto);
			return reconAndRequestStatusDto;
		} catch (Exception exception) {
			log.info(" Exception occured at HomeService :: getRequestAndReconStatus {} ",
					ExceptionUtils.getStackTrace(exception));
			return reconAndRequestStatusDto;
		}

	}

}
