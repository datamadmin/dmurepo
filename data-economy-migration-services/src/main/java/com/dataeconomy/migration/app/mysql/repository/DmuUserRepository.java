package com.dataeconomy.migration.app.mysql.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dataeconomy.migration.app.mysql.entity.DmuUsersEntity;

@Repository
public interface DmuUserRepository extends JpaRepository<DmuUsersEntity, String> {

	@Query(" SELECT dmu FROM DmuUsersEntity dmu where dmu.userName = :userName and dmu.password =:password ")
	public List<DmuUsersEntity> login(@Param("userName") String userName, @Param("password") String password);

	@Query(" SELECT dmu FROM DmuUsersEntity dmu where dmu.userName = :userName and dmu.emailid =:emailid ")
	public List<DmuUsersEntity> forgotPassword(@Param("userName") String userName, @Param("emailid") String emailid);

	@Query(" SELECT dmu FROM DmuUsersEntity dmu where dmu.userName = :userName")
	public List<DmuUsersEntity> checkUserExist(@Param("userName") String userName);

}
