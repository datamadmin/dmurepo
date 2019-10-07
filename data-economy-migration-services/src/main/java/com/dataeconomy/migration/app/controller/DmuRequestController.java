package com.dataeconomy.migration.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.model.DmuBasketDTO;
import com.dataeconomy.migration.app.model.DmuHistoryDTO;
import com.dataeconomy.migration.app.service.DmuHistoryMainService;
import com.dataeconomy.migration.app.service.DmuRequestService;

@RestController
@RequestMapping("/datamigration/request")
public class DmuRequestController {

	@Autowired
	private DmuRequestService dmuRequestService;

	@Autowired
	private DmuHistoryMainService historyMainService;


	@PostMapping("/save")
	public boolean saveRequest(@RequestBody DmuHistoryDTO historyMainDto) {
		return dmuRequestService.saveRequest(historyMainDto);
	}

	@GetMapping("/all")
	public List<String> getAllDatabases() {
		return dmuRequestService.getAllRequestDatabases();
	}

	@GetMapping("/all/{databaseName}")
	public List<DmuBasketDTO> getAllTablesForGivenDatabase(@PathVariable(name = "databaseName") String databaseName) {
		return dmuRequestService.getAllTablesForGivenDatabase(databaseName);
	}
	@GetMapping("/checkLableExist")
	public boolean checkLableExist(@RequestParam("lableName") String lableName) throws Exception {
		System.out.println("**lableName***"+lableName);
		boolean existFlasg = historyMainService.checkLableExist(lableName);
		if (existFlasg) {
			throw new Exception("Same Lable Name Already Exist!");
		}
		else
		{
			return true;
		}
	}


}
