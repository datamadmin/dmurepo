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
import com.dataeconomy.migration.app.model.DmuTgtOtherPropDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtOtherPropEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuTgtOtherPropRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuTgtOtherPropService {

	@Autowired
	private DmuTgtOtherPropRepository tgtOtherPropRepository;

	@Timed
	@Transactional(readOnly = true)
	public List<DmuTgtOtherPropDTO> getAllTGTOtherProp() {
		log.info(" TGTFormatPropService :: getAllTGTFormatProp {} ");
		try {
			List<DmuTgtOtherPropEntity> tgtOtherPropList = tgtOtherPropRepository.findAll();
			return Optional.ofNullable(tgtOtherPropList).orElse(new ArrayList<>()).stream()
					.map(tgtOtherPropEntity -> DmuTgtOtherPropDTO.builder().srNo(tgtOtherPropEntity.getSrNo())
							.parallelJobs(tgtOtherPropEntity.getParallelJobs())
							.tempHdfsDir(tgtOtherPropEntity.getHadoopInstallDir())
							.parallelUsrRqst(tgtOtherPropEntity.getParallelUsrRqst())
							.tempHiveDB(tgtOtherPropEntity.getTempHiveDB())
							.tempHdfsDir(tgtOtherPropEntity.getTempHdfsDir())
							.tokenizationInd(tgtOtherPropEntity.getTokenizationInd())
							.ptgyDirPath(tgtOtherPropEntity.getPtgyDirPath())
							.hdfsEdgeNode(tgtOtherPropEntity.getHdfsEdgeNode())
							.hadoopInstallDir(tgtOtherPropEntity.getHadoopInstallDir())
							.hdfsPemLocation(tgtOtherPropEntity.getHdfsPemLocation()).build())
					.collect(Collectors.toList());
		} catch (Exception exception) {
			log.info(" Exception occured at TGTOtherPropService :: getAllTGTOtherProp {} ",
					ExceptionUtils.getStackTrace(exception));
			return Collections.emptyList();
		}
	}

	@Timed
	@Transactional(readOnly = true)
	public DmuTgtOtherPropDTO getAllTGTOtherProp(Long requestNumber) {
		log.info(" TGTFormatPropService :: getAllTGTFormatProp {} ");
		try {
			Optional<DmuTgtOtherPropEntity> tgtOtherPropOpt = tgtOtherPropRepository.findById(requestNumber);
			if (tgtOtherPropOpt.isPresent()) {
				DmuTgtOtherPropEntity tgtOtherPropEntity = tgtOtherPropOpt.get();
				return DmuTgtOtherPropDTO.builder().srNo(tgtOtherPropEntity.getSrNo())
						.parallelJobs(tgtOtherPropEntity.getParallelJobs())
						.parallelUsrRqst(tgtOtherPropEntity.getParallelUsrRqst())
						.tempHiveDB(tgtOtherPropEntity.getTempHiveDB()).tempHdfsDir(tgtOtherPropEntity.getTempHdfsDir())
						.tokenizationInd(tgtOtherPropEntity.getTokenizationInd())
						.ptgyDirPath(tgtOtherPropEntity.getPtgyDirPath())
						.hdfsEdgeNode(tgtOtherPropEntity.getHdfsEdgeNode())
						.hdfsUserName(tgtOtherPropEntity.getHdfsUserName())
						.hadoopInstallDir(tgtOtherPropEntity.getHadoopInstallDir())
						.hdfsPemLocation(tgtOtherPropEntity.getHdfsPemLocation()).build();
			}
			return DmuTgtOtherPropDTO.builder().build();
		} catch (Exception exception) {
			log.info(" Exception occured at TGTOtherPropService :: getAllTGTOtherProp {} ",
					ExceptionUtils.getStackTrace(exception));
			return DmuTgtOtherPropDTO.builder().build();
		}
	}

	@Timed
	@Transactional
	public DmuTgtOtherPropDTO saveTGTOther(DmuTgtOtherPropDTO tgtOtherPropDto) {
		try {
			DmuTgtOtherPropEntity tgtOtherProp = DmuTgtOtherPropEntity.builder().srNo(tgtOtherPropDto.getSrNo())
					.parallelJobs(tgtOtherPropDto.getParallelJobs())
					.parallelUsrRqst(tgtOtherPropDto.getParallelUsrRqst()).tempHiveDB(tgtOtherPropDto.getTempHiveDB())
					.tempHdfsDir(tgtOtherPropDto.getTempHdfsDir()).tokenizationInd(tgtOtherPropDto.getTokenizationInd())
					.ptgyDirPath(tgtOtherPropDto.getPtgyDirPath()).hdfsEdgeNode(tgtOtherPropDto.getHdfsEdgeNode())
					.hdfsUserName(tgtOtherPropDto.getHdfsUserName()).tempHdfsDir(tgtOtherPropDto.getHadoopInstallDir())
					.hadoopInstallDir(tgtOtherPropDto.getHadoopInstallDir())
					.hdfsPemLocation(tgtOtherPropDto.getHdfsPemLocation()).build();
			tgtOtherPropRepository.save(tgtOtherProp);
			return tgtOtherPropDto;
		} catch (Exception exception) {
			log.info(" Exception occured at TGTOtherPropService :: getAllTGTOtherProp {} ",
					ExceptionUtils.getStackTrace(exception));
			return DmuTgtOtherPropDTO.builder().build();
		}
	}

}
