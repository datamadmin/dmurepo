package com.dataeconomy.migration.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.model.DmuConnectionDTO;
import com.dataeconomy.migration.app.service.DmuAuthenticationService;

@RestController
@RequestMapping("/datamigration/authentication")
public class DmuAuthenticationController {

	@Autowired
	private DmuAuthenticationService authenticationService;

	@GetMapping("/all")
	public DmuConnectionDTO getAllAuthenticationDetails() throws DataMigrationException {
		return authenticationService.getAllAuthenticationDetails();
	}

}
