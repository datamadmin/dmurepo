package com.dataeconomy.migration.app.mapper;

import org.mapstruct.Mapper;

import com.dataeconomy.migration.app.model.DmuHistoryDetailsDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;

@Mapper(componentModel = "spring")
public interface DmuHistoryDetailsMapper {

	DmuHistoryDetailsDTO dmuHistoryDetailEntityToHistoryDTO(DmuHistoryDetailEntity dmuHistoryDetailEntity);

	DmuHistoryDetailEntity historyDTOToEmployeeEntity(DmuHistoryDetailsDTO dmuHistoryDetailsDTO);

}
