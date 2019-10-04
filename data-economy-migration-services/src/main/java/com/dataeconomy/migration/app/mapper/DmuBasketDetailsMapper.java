package com.dataeconomy.migration.app.mapper;

import org.mapstruct.Mapper;

import com.dataeconomy.migration.app.model.DmuBasketDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuBasketTempEntity;

@Mapper(componentModel = "spring")
public interface DmuBasketDetailsMapper {

	DmuBasketDTO toDto(DmuBasketTempEntity dmuAuthenticationEntity);

//	@Mappings({ @Mapping(source = "requestType.value", target = "SUBMIT") })
	DmuBasketTempEntity toEntity(DmuBasketDTO dmuConnectionDTO);
}
