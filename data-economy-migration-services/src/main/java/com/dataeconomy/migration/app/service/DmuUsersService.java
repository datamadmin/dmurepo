package com.dataeconomy.migration.app.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.model.DmuUsersDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuUsersEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuUserRepository;
import com.dataeconomy.migration.app.util.DmuMailUtilityService;
import com.dataeconomy.migration.app.util.DmuRandomUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuUsersService {

	@Autowired
	private DmuUserRepository userRepository;

	@Timed
	public List<DmuUsersDTO> getUsers() {
		log.info(" UserService : getUsers() ");
		try {
			List<DmuUsersEntity> usersList = userRepository.findAll();
			return Optional.ofNullable(usersList).orElse(new ArrayList<>()).stream().filter(Objects::nonNull)
					.map(user -> DmuUsersDTO.builder().emailid(user.getEmailid()).id(user.getId()).id(user.getId())
							.userRole(user.getUserRole()).userName(user.getUserName()).createdBy(user.getCreatedBy())
							.createdDate(user.getCreatedDate()).updatedBy(user.getUpdatedBy())
							.updatedDate(user.getUpdatedDate()).build())
					.collect(Collectors.toList());

		} catch (Exception exception) {
			return Collections.emptyList();
		}
	}

	@Timed
	public DmuUsersDTO getUser(String userName) {
		log.info(" UserService : getUser() ");
		try {
			Optional<DmuUsersEntity> dmuUsersOptional = userRepository.findById(userName);
			if (dmuUsersOptional.isPresent()) {
				DmuUsersEntity dmuUser = dmuUsersOptional.get();
				return DmuUsersDTO.builder().emailid(dmuUser.getEmailid()).id(dmuUser.getId()).id(dmuUser.getId())
						.userName(dmuUser.getUserName()).createdBy(dmuUser.getCreatedBy())
						.createdDate(dmuUser.getCreatedDate()).updatedBy(dmuUser.getUpdatedBy())
						.updatedDate(dmuUser.getUpdatedDate()).build();
			}
			return DmuUsersDTO.builder().build();
		} catch (Exception exception) {
			return DmuUsersDTO.builder().build();
		}
	}

	@Timed
	public boolean saveUser(DmuUsersDTO userDto) throws Exception {
		log.info(" UserService : saveUser() ");
		List<DmuUsersEntity> dmList = userRepository.checkUserExist(userDto.getUserName());
		if (dmList != null && dmList.size() > 0) {
			throw new Exception("User Already Exist!");
		}
		userDto.setCreatedBy("Admin");
		userDto.setCreatedDate(LocalDateTime.now());

		try {
			userDto.setPassword(DmuRandomUtil.getRandomPassword());
			try {
				DmuMailUtilityService.sendUseralert(userDto.getUserName(), userDto.getEmailid(), userDto.getPassword());
			} catch (Exception e) {
				e.printStackTrace();
			}
			userDto.setPassword(Base64.getEncoder().encodeToString(userDto.getPassword().getBytes()));
			userRepository.save(DmuUsersEntity.builder().emailid(userDto.getEmailid()).id(userDto.getId())
					.password(userDto.getPassword()).userName(userDto.getUserName()).userRole(userDto.getUserRole())
					.createdBy(userDto.getCreatedBy()).createdDate(userDto.getCreatedDate())
					.updatedBy(userDto.getUpdatedBy()).updatedDate(userDto.getUpdatedDate()).build());
			return true;
		} catch (Exception exception) {
			throw new DataMigrationException("Unable to create User Please Contact Admin");
		}
	}

	public boolean purgeUsers(String userId) {
		try {
			userRepository.deleteById(userId);
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	public DmuUsersEntity login(String userName, String password) {
		DmuUsersEntity udto = new DmuUsersEntity();
		try {
			List<DmuUsersEntity> dmList = userRepository.login(userName, password);
			if (CollectionUtils.isNotEmpty(dmList)) {
				udto = dmList.get(0);
			}
		} catch (Exception exception) {
		}
		return udto;
	}

	public boolean editUser(DmuUsersDTO userDto) {
		try {
			Optional<DmuUsersEntity> dmuUsersOpt = userRepository.findById(userDto.getId());
			if (dmuUsersOpt.isPresent()) {
				DmuUsersEntity dmuUsers = dmuUsersOpt.get();
				dmuUsers.setEmailid(userDto.getEmailid());
				//dmuUsers.setPassword(dmuUsersOpt.get(0).get );
				dmuUsers.setUserRole(userDto.getUserRole());
				dmuUsers.setId(userDto.getId());
				dmuUsers.setUpdatedBy("Admin");
				dmuUsers.setUpdatedDate(LocalDateTime.now());
				userRepository.save(dmuUsers);
				return true;
			}
			return false;
		} catch (Exception exception) {
			return false;
		}
	}

	public boolean resetPassword(String userId, String password) {
		try {
			Optional<DmuUsersEntity> dmuUsersOpt = userRepository.findById(userId);
			if (dmuUsersOpt.isPresent()) {
				DmuUsersEntity dmuUsers = dmuUsersOpt.get();
				dmuUsers.setPassword(password);
				userRepository.save(dmuUsers);
				return true;
			}
			return false;
		} catch (Exception exception) {
			return false;
		}
	}

	public boolean forgotPassword(String userName, String emailid) {
		try {
			List<DmuUsersEntity> dmList = userRepository.forgotPassword(userName, emailid);
			if (CollectionUtils.isNotEmpty(dmList)) {
				try {
					DmuMailUtilityService.senForgotPasswordalert(dmList.get(0).getUserName(),
							dmList.get(0).getEmailid(),
							new String(Base64.getDecoder().decode(dmList.get(0).getPassword())));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			} else {
				return false;
			}

		} catch (Exception exception) {
			return false;
		}
	}

}
