package com.dataeconomy.migration.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.mapper.DmuAuthenticationDetailsMapper;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.mysql.repository.DmuAuthenticationRepository;

@Service
public class DmuAuthenticationService {

	@Autowired
	private DmuAuthenticationRepository dmuAuthenticationRepository;

	@Autowired
	private DmuAuthenticationDetailsMapper mapper;

	@Timed
	@Transactional(readOnly = true)
	public DmuConnectionDTO getAllAuthenticationDetails() throws DataMigrationException {
		return mapper.toDto(dmuAuthenticationRepository.findById(1L)
				.orElseThrow(() -> new DataMigrationException("Authentication details not found")));
	}
}
