package com.dataeconomy.migration.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.model.DmuHistoryDTO;
import com.dataeconomy.migration.app.service.DmuHistoryMainService;

@RestController
@RequestMapping("/datamigration/history/main")
public class DmuHistoryMainController {

	@Autowired
	private DmuHistoryMainService historyMainService;

	@GetMapping("/all")
	public List<DmuHistoryDTO> getAllHistoryDetails() {
		return historyMainService.getAllHistoryMainDetails();
	}
	@GetMapping("/byUserId")
	public List<DmuHistoryDTO> getAllHistoryDetailsByUserId(@RequestParam("userId") String userId) {
		System.out.println("***********"+userId);
		return historyMainService.getAllHistoryDetailsByUserId(userId);
	}
}
