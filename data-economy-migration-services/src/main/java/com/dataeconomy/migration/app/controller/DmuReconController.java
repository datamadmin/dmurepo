package com.dataeconomy.migration.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.model.DmuReconMainDTO;
import com.dataeconomy.migration.app.service.DmuReconMainService;

@RestController
@RequestMapping("/datamigration/recon")
public class DmuReconController {

	@Autowired
	private DmuReconMainService dmuReconService;

	@GetMapping("/details/{requestNo}")
	public DmuReconMainDTO getReconDetailsBySearch(@PathVariable("requestNo") String requestNo) {
		return dmuReconService.getReconDetailsBySearch(requestNo);
	}

	@GetMapping("/all")
	public List<DmuReconMainDTO> getAllDatabases() {
		return dmuReconService.getDMUReconMainDetailsList();
	}
	@GetMapping("/byUserId")
	public List<DmuReconMainDTO> getAllDatabasesByUserId(@RequestParam("userId") String userId) {
		return dmuReconService.getAllDatabasesByUserId(userId);
	}
}
