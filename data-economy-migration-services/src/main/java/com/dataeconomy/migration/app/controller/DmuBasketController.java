package com.dataeconomy.migration.app.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.model.DmuBasketDTO;
import com.dataeconomy.migration.app.service.DmuBasketService;

@RestController
@RequestMapping("/datamigration/basket")
public class DmuBasketController {

	@Autowired
	DmuBasketService dmuBasketService;

	@GetMapping("/all")
	public List<DmuBasketDTO> getAllBasketDetails() {
		return dmuBasketService.getAllBasketDetails();
	}

	@PostMapping("/save")
	public boolean saveBasketDetailsTemp(@RequestBody List<DmuBasketDTO> dmuBasketDtoList,
			@RequestHeader("userId") String userId) throws DataMigrationException {
		return dmuBasketService.saveBasketDetails(dmuBasketDtoList, userId);
	}

	@PostMapping("/save/purge")
	public boolean saveBasketDetailsAndPurge(@RequestBody List<DmuBasketDTO> dmuBasketDtoList,
			@RequestHeader("userId") String userId) throws DataMigrationException {
		return dmuBasketService.saveBasketDetailsAndPurge(dmuBasketDtoList, userId);
	}

	@GetMapping("/clear")
	public boolean clearBasketDetails(@RequestParam("userId") String userId) throws DataMigrationException {
		return dmuBasketService.clearBasketDetails(userId);
	}

	@GetMapping("/all/{userId}")
	public List<DmuBasketDTO> saveBasketDetails(@PathVariable("userId") String userId) {
		return dmuBasketService.getBasketDetailsByUserId(userId);
	}

	@DeleteMapping("/delete")
	public boolean purgeBasketDetails(@RequestParam("userId") String userId) {
		return dmuBasketService.purgeBasketDetailsByUserId(userId);
	}

	@DeleteMapping("/delete/srNo/{srNo}")
	public boolean purgeBasketDetailsBySrNo(@PathVariable Optional<Long> srNo) {
		return dmuBasketService.purgeBasketDetailsBySrNo(srNo);
	}

	@GetMapping("/search")
	public List<DmuBasketDTO> getBasketDetailsBySearchParam(
			@RequestParam(value = "searchParam", required = true) String searchParam) throws DataMigrationException {
		return dmuBasketService.getBasketDetailsBySearchParam(searchParam);
	}
}
