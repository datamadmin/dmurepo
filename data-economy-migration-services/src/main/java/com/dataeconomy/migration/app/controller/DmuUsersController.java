package com.dataeconomy.migration.app.controller;

import java.util.Base64;
import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dataeconomy.migration.app.model.DmuTgtOtherPropDTO;
import com.dataeconomy.migration.app.model.DmuUsersDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuUsersEntity;
import com.dataeconomy.migration.app.service.DmuBasketService;
import com.dataeconomy.migration.app.service.DmuTgtOtherPropService;
import com.dataeconomy.migration.app.service.DmuUsersService;

@RestController
@RequestMapping("/datamigration/users")
public class DmuUsersController {

	@Autowired
	private DmuUsersService userService;

	@Autowired
	DmuTgtOtherPropService tgtOtherPropService;

	@Autowired
	DmuBasketService dmuBasketService;

	@GetMapping("/all")
	public List<DmuUsersDTO> getUsers() {
		return userService.getUsers();
	}

	@GetMapping("getUser/{userId}")
	public DmuUsersDTO getUsers(@PathParam("userId") String userId) {
		return userService.getUser(userId);
	}

	@PostMapping("/save")
	public boolean saveUser(@RequestBody DmuUsersDTO userDto) throws Exception {
		return userService.saveUser(userDto);
	}

	@PostMapping("/edit")
	public boolean editUser(@RequestBody DmuUsersDTO userDto) {
		return userService.editUser(userDto);
	}

	@GetMapping("/delete")
	public boolean purgeUsers(@RequestParam("userId") String userId) {
		return userService.purgeUsers(userId);
	}

	@GetMapping("/login")
	public DmuUsersEntity login(@RequestParam("userName") String userName, @RequestParam("password") String password) {
		DmuUsersEntity dm = new DmuUsersEntity();
		List<DmuTgtOtherPropDTO> tgoProList = tgtOtherPropService.getAllTGTOtherProp();
		dm = userService.login(userName, Base64.getEncoder().encodeToString(password.getBytes()));
		if (tgoProList != null && tgoProList.size() > 0
				&& tgoProList.get(0).getTokenizationInd().equalsIgnoreCase("Y")) {
			dm.setTokenization(true);
		} else {
			dm.setTokenization(false);
		}
		if (dm.getId() != null && dmuBasketService.getBasketDetailsByUserId(dm.getId()) != null
				&& dmuBasketService.getBasketDetailsByUserId(dm.getId()).size() > 0) {
			dm.setBasketCount(dmuBasketService.getBasketDetailsByUserId(dm.getId()).size());
		}
		return dm;
	}

	@GetMapping("/resetPassword")
	public boolean resetPassword(@RequestParam("id") String userId, @RequestParam("password") String password) {
		return userService.resetPassword(userId, Base64.getEncoder().encodeToString(password.getBytes()));
	}

	@GetMapping("/forgotPassword")
	public boolean forgotPassword(@RequestParam("userName") String userName, @RequestParam("emailid") String emailid) {
		return userService.forgotPassword(userName, emailid);
	}
}
