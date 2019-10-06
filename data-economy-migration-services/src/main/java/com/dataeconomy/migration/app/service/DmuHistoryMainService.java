package com.dataeconomy.migration.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.model.DmuHistoryDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryMainEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryMainRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuHistoryMainService {

	@Autowired
	private DmuHistoryMainRepository historyMainRepository;

	@Timed
	@Transactional(readOnly = true)
	public List<DmuHistoryDTO> getAllHistoryMainDetails() {
		log.info(" HistoryMainService :: getAllHistoryMainDetails ");
		try {
			List<DmuHistoryMainEntity> dmuHistoryDetailOpt = historyMainRepository.findAll();
			return Optional.ofNullable(dmuHistoryDetailOpt).orElse(new ArrayList<>()).stream()
					.map(dmuHistoryDetailObj -> DmuHistoryDTO.builder().requestNo(dmuHistoryDetailObj.getRequestNo())
							.requestType(dmuHistoryDetailObj.getRequestType())
							.userId(dmuHistoryDetailObj.getUserId())
							.requestedTime(dmuHistoryDetailObj.getRequestedTime())
							.status(dmuHistoryDetailObj.getStatus()).requestType(dmuHistoryDetailObj.getRequestType())
							.scriptGenCmpltTime(dmuHistoryDetailObj.getScriptGenCmpltTime())
							.exctnCmpltTime(dmuHistoryDetailObj.getExctnCmpltTime())
							.tknztnEnabled(dmuHistoryDetailObj.getTknztnEnabled())
							.tknztnFilePath(dmuHistoryDetailObj.getTknztnFilePath()).build())
					.collect(Collectors.toList());
		} catch (Exception exception) {
			log.info(" Exception occured at HistoryDetailService :: getAllHistoryDetailsByReq {} ",
					ExceptionUtils.getStackTrace(exception));
			return Collections.emptyList();
		}
	}
	@Timed
	@Transactional(readOnly = true)
	public boolean checkLableExist(String lablename) {
		boolean existFlag = false;
		if(historyMainRepository.checkLableExist(lablename)!=null && historyMainRepository.checkLableExist(lablename).length()>0)
			existFlag= true;
		return existFlag;
	}
}
