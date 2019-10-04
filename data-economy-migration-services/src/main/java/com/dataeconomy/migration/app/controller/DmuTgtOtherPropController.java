package com.dataeconomy.migration.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.model.DmuTgtOtherPropDTO;
import com.dataeconomy.migration.app.service.DmuTgtOtherPropService;

@RestController
@RequestMapping("/datamigration/tgtotherprop")
public class DmuTgtOtherPropController {

	@Autowired
	DmuTgtOtherPropService tgtOtherPropService;

	@GetMapping("/all")
	public List<DmuTgtOtherPropDTO> getAllTGTOtherProp() {
		return tgtOtherPropService.getAllTGTOtherProp();
	}

	@GetMapping("/all/{requestNumber}")
	public DmuTgtOtherPropDTO getAllTGTOtherProp(@PathVariable("requestNumber") Long requestNumber) {
		return tgtOtherPropService.getAllTGTOtherProp(requestNumber);
	}

	@PostMapping("/save")
	public DmuTgtOtherPropDTO saveTGTOther(@RequestBody DmuTgtOtherPropDTO tgtOtherPropDto) {
		return tgtOtherPropService.saveTGTOther(tgtOtherPropDto);
	}
}
