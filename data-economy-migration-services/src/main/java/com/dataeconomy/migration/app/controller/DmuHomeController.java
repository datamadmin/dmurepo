package com.dataeconomy.migration.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.model.DmuReconAndRequestStatusDTO;
import com.dataeconomy.migration.app.service.DmuHomeService;

@RestController
@RequestMapping("/datamigration/home")
public class DmuHomeController {

	@Autowired
	private DmuHomeService homeService;

	@GetMapping("/status")
	public DmuReconAndRequestStatusDTO getRequestAndReconStatus() {
		return homeService.getRequestAndReconStatus();
	}

}
