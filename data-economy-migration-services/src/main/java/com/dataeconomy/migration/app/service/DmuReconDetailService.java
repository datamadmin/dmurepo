package com.dataeconomy.migration.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.model.DmuReconDetailDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuReconDetailEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuReconDetailsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuReconDetailService {

	@Autowired
	private DmuReconDetailsRepository dmuReconDetailRepository;

	@Timed
	@Transactional(readOnly = true)
	public List<DmuReconDetailDTO> getDMUReconDetailsList() {
		log.info(" DMUReconDetailService :: getDMUReconMainDetailsList ");
		try {
			List<DmuReconDetailEntity> reconDetailsList = dmuReconDetailRepository
					.findAll(Sort.by(Direction.ASC, "dmuHIstoryDetailPK.srNo"));
			return Optional.ofNullable(reconDetailsList).orElse(new ArrayList<>()).stream()
					.map(reconObj -> DmuReconDetailDTO.builder().srNo(reconObj.getDmuHIstoryDetailPK().getSrNo())
							.filterCondition(reconObj.getFilterCondition()).schemaName(reconObj.getSchemaName())
							.tableName(reconObj.getTableName()).targetS3Bucket(reconObj.getTargetS3Bucket())
							.incrementalFlag(reconObj.getIncrementalFlag())
							.incrementalColumn(reconObj.getIncrementalColumn()).sourceCount(reconObj.getSourceCount())
							.targetCount(reconObj.getTargetCount()).status(reconObj.getStatus()).build())
					.collect(Collectors.toList());
		} catch (Exception exception) {
			log.info(" Exception occured at DMUReconDetailService :: getDMUReconDetailsList {} ",
					ExceptionUtils.getStackTrace(exception));
			return Collections.emptyList();
		}
	}

	@Timed
	@Transactional(readOnly = true)
	public List<DmuReconDetailDTO> getReconDetailsBySearch(String requestNo) {
		log.info(" DMUReconDetailService :: getReconDetailsBySearch :: requestNo :: {} ", requestNo);
		try {
			List<DmuReconDetailEntity> reconDetailsEntityList = dmuReconDetailRepository
					.findByGivenRequestNo(requestNo);
			if (CollectionUtils.isNotEmpty(reconDetailsEntityList)) {
				return reconDetailsEntityList.stream().map(dmuReconDetail -> DmuReconDetailDTO.builder()
						.srNo(dmuReconDetail.getDmuHIstoryDetailPK().getSrNo())
						.filterCondition(dmuReconDetail.getFilterCondition()).schemaName(dmuReconDetail.getSchemaName())
						.tableName(dmuReconDetail.getTableName()).targetS3Bucket(dmuReconDetail.getTargetS3Bucket())
						.incrementalFlag(dmuReconDetail.getIncrementalFlag())
						.incrementalColumn(dmuReconDetail.getIncrementalColumn())
						.sourceCount(dmuReconDetail.getSourceCount()).targetCount(dmuReconDetail.getTargetCount())
						.status(dmuReconDetail.getStatus()).build()).collect(Collectors.toList());
			}
			return Collections.emptyList();
		} catch (Exception exception) {
			log.info(" Exception occured at DMUReconDetailService :: getReconDetailsBySearch {} ",
					ExceptionUtils.getStackTrace(exception));
			return Collections.emptyList();
		}
	}

}
