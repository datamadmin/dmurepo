package com.dataeconomy.migration.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.model.DmuReconDetailDTO;
import com.dataeconomy.migration.app.service.DmuReconDetailService;

@RestController
@RequestMapping("/datamigration/recon/detail")
public class DmuReconDetailController {

	@Autowired
	private DmuReconDetailService dmuReconDetailService;

	@GetMapping("/details/{requestNo}")
	public List<DmuReconDetailDTO> getReconDetailsBySearch(@PathVariable("requestNo") String requestNo) {
		return dmuReconDetailService.getReconDetailsBySearch(requestNo);
	}

	@GetMapping("/all")
	public List<DmuReconDetailDTO> getAllDatabases() {
		return dmuReconDetailService.getDMUReconDetailsList();
	}
}
