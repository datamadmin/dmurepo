package com.dataeconomy.migration.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.model.DmuTgtFormatPropDTO;
import com.dataeconomy.migration.app.service.DmuTgtFormatPropService;

@RestController
@RequestMapping("/datamigration/tgtformatprop")
public class DmuTgtFormatPropController {

	@Autowired
	private DmuTgtFormatPropService tgtFormatPropService;

	@GetMapping("/all")
	public List<DmuTgtFormatPropDTO> getAllTGTFormatProp() {
		return tgtFormatPropService.getAllTGTFormatProp();
	}

	@GetMapping("/all/{requestNumber}")
	public DmuTgtFormatPropDTO getAllTGTFormatProp(@PathVariable("requestNumber") Long requestNumber) {
		return tgtFormatPropService.getAllTGTFormatProp(requestNumber);
	}

	@PostMapping("/save")
	public DmuTgtFormatPropDTO saveTGTFormat(@RequestBody DmuTgtFormatPropDTO tgtFormatPropDto) {
		return tgtFormatPropService.saveTGTFormat(tgtFormatPropDto);
	}
}
