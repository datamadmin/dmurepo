package com.dataeconomy.migration.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.service.DmuConnectionService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/datamigration/connection")
@Api(value = "DataMigration Utility Connection System")
public class DmuConnectionController {

	@Autowired
	private DmuConnectionService connectionService;

	@RequestMapping("/validate")
	public boolean validateConnection(@RequestBody DmuConnectionDTO connectionDto) throws DataMigrationException {
		return connectionService.validateConnection(connectionDto);
	}

	@RequestMapping("/save")
	public boolean saveConnectionDetails(@RequestBody DmuConnectionDTO connectionDto) throws DataMigrationException {
		return connectionService.saveConnectionDetails(connectionDto);
	}

	@RequestMapping("/get")
	public DmuConnectionDTO getConnectionDetails() throws DataMigrationException {
		return connectionService.getConnectionDetails();
	}

}
