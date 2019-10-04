package com.dataeconomy.migration.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.model.DmuHistoryDetailsDTO;
import com.dataeconomy.migration.app.service.DmuHistoryDetailsService;

@RestController
@RequestMapping("/datamigration/history")
public class DmuHistoryDataController {

	@Autowired
	private DmuHistoryDetailsService historyDetailService;

	@GetMapping("/all")
	public List<DmuHistoryDetailsDTO> getAllHistoryDetails() {
		return historyDetailService.getAllHistoryDetails();
	}

	@GetMapping("/getHistoryDetail")
	public List<DmuHistoryDetailsDTO> getAllHistoryDetails(@RequestParam("requestNumber") String requestNumber) {
		return historyDetailService.getAllHistoryDetailsByReq(requestNumber);
	}

}
