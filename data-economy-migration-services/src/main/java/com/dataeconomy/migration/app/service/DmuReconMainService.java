package com.dataeconomy.migration.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.model.DmuReconMainDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuReconMainentity;
import com.dataeconomy.migration.app.mysql.repository.DmuReconMainRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuReconMainService {

	@Autowired
	DmuReconMainRepository dmuReconMainRepository;

	@Timed
	@Transactional(readOnly = true)
	public List<DmuReconMainDTO> getDMUReconMainDetailsList() {
		log.info(" DMUReconMainService :: getDMUReconMainDetailsList ");
		try {
			List<DmuReconMainentity> reconDetailsList = dmuReconMainRepository
					.findAll(Sort.by(Direction.ASC, "requestedTime"));
			return Optional.ofNullable(reconDetailsList).orElse(new ArrayList<>()).stream()
					.map(reconObj -> DmuReconMainDTO.builder().requestNo(reconObj.getRequestNo())
							.userId(reconObj.getUserId()).requestedTime(reconObj.getRequestedTime())
							.status(reconObj.getStatus()).requestType(reconObj.getRequestType())
							.reconStartTime(reconObj.getReconStartTime()).reconCmpltTime(reconObj.getReconCmpltTime())
							.build())
					.collect(Collectors.toList());
		} catch (Exception exception) {
			log.info(" Exception occured at DMUReconMainService :: getDMUReconMainDetailsList {} ",
					ExceptionUtils.getStackTrace(exception));
			return Collections.emptyList();
		}
	}
	@Timed
	@Transactional(readOnly = true)
	public List<DmuReconMainDTO> getAllDatabasesByUserId(String userId) {
		log.info(" DMUReconMainService :: getDMUReconMainDetailsList ");
		try {
			List<DmuReconMainentity> reconDetailsList = dmuReconMainRepository.getAllDatabasesByUserId(userId);
			return Optional.ofNullable(reconDetailsList).orElse(new ArrayList<>()).stream()
					.map(reconObj -> DmuReconMainDTO.builder().requestNo(reconObj.getRequestNo())
							.userId(reconObj.getUserId()).requestedTime(reconObj.getRequestedTime())
							.status(reconObj.getStatus()).requestType(reconObj.getRequestType())
							.reconStartTime(reconObj.getReconStartTime()).reconCmpltTime(reconObj.getReconCmpltTime())
							.build())
					.collect(Collectors.toList());
		} catch (Exception exception) {
			log.info(" Exception occured at DMUReconMainService :: getDMUReconMainDetailsList {} ",
					ExceptionUtils.getStackTrace(exception));
			return Collections.emptyList();
		}
	}

	@Timed
	@Transactional(readOnly = true)
	public DmuReconMainDTO getReconDetailsBySearch(String requestNo) {
		log.info(" DMUReconMainService :: getReconDetailsBySearch ");
		try {
			Optional<DmuReconMainentity> reconDetailsEntity = dmuReconMainRepository.findById(requestNo);
			if (reconDetailsEntity.isPresent()) {
				DmuReconMainentity dmuReconMain = reconDetailsEntity.get();
				return DmuReconMainDTO.builder().requestNo(dmuReconMain.getRequestNo()).userId(dmuReconMain.getUserId())
						.requestedTime(dmuReconMain.getRequestedTime()).status(dmuReconMain.getStatus())
						.requestType(dmuReconMain.getRequestType()).reconStartTime(dmuReconMain.getReconStartTime())
						.reconCmpltTime(dmuReconMain.getReconCmpltTime()).build();
			}
			return DmuReconMainDTO.builder().build();
		} catch (Exception exception) {
			log.info(" Exception occured at DMUReconMainService :: getReconDetailsBySearch {} ",
					ExceptionUtils.getStackTrace(exception));
			return DmuReconMainDTO.builder().build();
		}
	}

}
