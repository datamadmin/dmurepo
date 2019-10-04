package com.dataeconomy.migration.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.model.DmuTgtFormatPropDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuTgtFormatEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuTgtFormatPropRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuTgtFormatPropService {

	@Autowired
	private DmuTgtFormatPropRepository tgtFormatPropRepository;

	@Timed
	@Transactional(readOnly = true)
	public List<DmuTgtFormatPropDTO> getAllTGTFormatProp() {
		log.info(" TGTFormatPropService :: getAllTGTFormatProp {} ");
		try {
			List<DmuTgtFormatEntity> tgtFormatPropList = tgtFormatPropRepository.findAll();
			return Optional.ofNullable(tgtFormatPropList).orElse(new ArrayList<>()).stream()
					.map(tgtFormatPropEntity -> DmuTgtFormatPropDTO.builder().srNo(tgtFormatPropEntity.getSrNo())
							.srcFormatFlag(tgtFormatPropEntity.getSrcFormatFlag())
							.textFormatFlag(tgtFormatPropEntity.getTextFormatFlag())
							.fieldDelimiter(tgtFormatPropEntity.getFieldDelimiter())
							.sqncFormatFlag(tgtFormatPropEntity.getSqncFormatFlag())
							.rcFormatFlag(tgtFormatPropEntity.getRcFormatFlag())
							.avroFormatFlag(tgtFormatPropEntity.getAvroFormatFlag())
							.orcFormatFlag(tgtFormatPropEntity.getOrcFormatFlag())
							.parquetFormatFlag(tgtFormatPropEntity.getParquetFormatFlag())
							.srcCmprsnFlag(tgtFormatPropEntity.getSrcCmprsnFlag())
							.uncmprsnFlag(tgtFormatPropEntity.getUncmprsnFlag())
							.gzipCmprsnFlag(tgtFormatPropEntity.getGzipCmprsnFlag()).build())
					.collect(Collectors.toList());

		} catch (Exception exception) {
			log.info(" Exception occured at TGTFormatPropRepository :: getAllTGTFormatProp {} ",
					ExceptionUtils.getStackTrace(exception));
			return Collections.emptyList();
		}
	}

	@Timed
	public DmuTgtFormatPropDTO saveTGTFormat(DmuTgtFormatPropDTO tgtFormatPropDto) {
		log.info(" TGTFormatPropService :: saveTGTFormat {} ", ObjectUtils.toString(tgtFormatPropDto));
		try {
			DmuTgtFormatEntity tgtFormatPropEntity = DmuTgtFormatEntity.builder().srNo(tgtFormatPropDto.getSrNo())
					.srcFormatFlag(tgtFormatPropDto.getSrcFormatFlag())
					.textFormatFlag(tgtFormatPropDto.getTextFormatFlag())
					.fieldDelimiter(tgtFormatPropDto.getFieldDelimiter())
					.sqncFormatFlag(tgtFormatPropDto.getSqncFormatFlag())
					.rcFormatFlag(tgtFormatPropDto.getRcFormatFlag())
					.avroFormatFlag(tgtFormatPropDto.getAvroFormatFlag())
					.orcFormatFlag(tgtFormatPropDto.getOrcFormatFlag())
					.parquetFormatFlag(tgtFormatPropDto.getParquetFormatFlag())
					.srcCmprsnFlag(tgtFormatPropDto.getSrcCmprsnFlag()).uncmprsnFlag(tgtFormatPropDto.getUncmprsnFlag())
					.gzipCmprsnFlag(tgtFormatPropDto.getGzipCmprsnFlag()).build();
			tgtFormatPropRepository.save(tgtFormatPropEntity);
		} catch (Exception exception) {
			log.info(" Exception occured at TGTFormatPropRepository :: saveTGTFormat {} ",
					ExceptionUtils.getStackTrace(exception));
		}

		return null;
	}

	@Timed
	@Transactional(readOnly = true)
	public DmuTgtFormatPropDTO getAllTGTFormatProp(Long requestNumber) {
		log.info(" TGTFormatPropService :: getAllTGTFormatProp  :: requestNumber :: {} ", requestNumber);
		try {
			Optional<DmuTgtFormatEntity> tgtFormatPropOpt = tgtFormatPropRepository.findById(requestNumber);
			if (tgtFormatPropOpt.isPresent()) {
				DmuTgtFormatEntity tgtFormatPropEntity = tgtFormatPropOpt.get();
				return DmuTgtFormatPropDTO.builder().srNo(tgtFormatPropEntity.getSrNo())
						.srcFormatFlag(tgtFormatPropEntity.getSrcFormatFlag())
						.textFormatFlag(tgtFormatPropEntity.getTextFormatFlag())
						.fieldDelimiter(tgtFormatPropEntity.getFieldDelimiter())
						.sqncFormatFlag(tgtFormatPropEntity.getSqncFormatFlag())
						.rcFormatFlag(tgtFormatPropEntity.getRcFormatFlag())
						.avroFormatFlag(tgtFormatPropEntity.getAvroFormatFlag())
						.orcFormatFlag(tgtFormatPropEntity.getOrcFormatFlag())
						.parquetFormatFlag(tgtFormatPropEntity.getParquetFormatFlag())
						.srcCmprsnFlag(tgtFormatPropEntity.getSrcCmprsnFlag())
						.uncmprsnFlag(tgtFormatPropEntity.getUncmprsnFlag())
						.gzipCmprsnFlag(tgtFormatPropEntity.getGzipCmprsnFlag()).build();
			}
			return DmuTgtFormatPropDTO.builder().build();
		} catch (Exception exception) {
			log.info(
					" Exception occured at TGTFormatPropRepository :: getAllTGTFormatProp :: requestNumber :: {} :: exception => {} ",
					requestNumber, ExceptionUtils.getStackTrace(exception));
			return DmuTgtFormatPropDTO.builder().build();
		}
	}

}
