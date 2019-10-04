package com.dataeconomy.migration.app.mapper;

import org.mapstruct.Mapper;

import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuAuthenticationEntity;

@Mapper(componentModel = "spring")
public interface DmuAuthenticationDetailsMapper {

	DmuConnectionDTO toDto(DmuAuthenticationEntity dmuAuthenticationEntity);

	DmuAuthenticationEntity toEntity(DmuConnectionDTO dmuConnectionDTO);

}
