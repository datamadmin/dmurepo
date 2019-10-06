package com.dataeconomy.migration.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.mapper.DmuHistoryDetailsMapper;
import com.dataeconomy.migration.app.model.DmuHistoryDetailsDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryDetailRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuHistoryDetailsService {

	@Autowired
	private DmuHistoryDetailRepository historyDetailRepository;

	@Autowired
	private DmuHistoryDetailsMapper mapper;

	@Timed
	@Transactional(readOnly = true)
	public List<DmuHistoryDetailsDTO> getAllHistoryDetailsByReq(String requestNumber) {
		log.info(" HistoryDetailService :: getAllHistoryDetailsByReq {} ",
				Objects.toString(requestNumber, "Invalid requestNumber"));
		try {
			List<DmuHistoryDetailEntity> dmuHistoryDetailListOpt = historyDetailRepository
					.findHistoryDetailsByRequestNumber(requestNumber);
			log.info(" HistoryDetailService :: getAllHistoryDetailsByReq dmuHistoryDetailListOpt :: {} ",
					Objects.toString(dmuHistoryDetailListOpt, "Empty resultset"));
			return dmuHistoryDetailListOpt.stream()
					.map(dmuHistoryDetailEntity -> mapper.dmuHistoryDetailEntityToHistoryDTO(dmuHistoryDetailEntity))
					.collect(Collectors.toList());
		} catch (Exception exception) {
			log.info(" Exception occured at HistoryDetailService :: getAllHistoryDetailsByReq {} ",
					ExceptionUtils.getStackTrace(exception));
			return Collections.emptyList();
		}
	}

	@Timed
	@Transactional(readOnly = true)
	public List<DmuHistoryDetailsDTO> getAllHistoryDetails() {
		log.info(" ConnectionService :: getAllHistoryDetails ");
		try {
			List<DmuHistoryDetailEntity> detailsList = historyDetailRepository.findAll();
			return Optional.ofNullable(detailsList).orElse(new ArrayList<>()).stream()
					.map(dmuHistoryDetailObj -> DmuHistoryDetailsDTO.builder()
							.srNo(dmuHistoryDetailObj.getDmuHIstoryDetailPK().getSrNo())
							.schemaName(dmuHistoryDetailObj.getSchemaName())
							.tableName(dmuHistoryDetailObj.getTableName())
							.filterCondition(dmuHistoryDetailObj.getFilterCondition())
							.targetS3Bucket(dmuHistoryDetailObj.getTargetS3Bucket())
							.incrementalFlag(dmuHistoryDetailObj.getIncrementalFlag())
							.incrementalClmn(dmuHistoryDetailObj.getIncrementalClmn())
							.status(dmuHistoryDetailObj.getStatus()).build())
					.collect(Collectors.toList());
		} catch (Exception exception) {
			log.info(" Exception occured at HistoryDetailService :: getAllHistoryDetailsByReq {} ",
					ExceptionUtils.getStackTrace(exception));
			return Collections.emptyList();
		}
	}
}
