package com.dataeconomy.migration.app.mysql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dataeconomy.migration.app.mysql.entity.DmuPtgyTempId;
import com.dataeconomy.migration.app.mysql.entity.DmuPtgyTempEntity;

@Repository
public interface DmuPtgyRepository extends JpaRepository<DmuPtgyTempEntity, DmuPtgyTempId> {

	@Modifying
	@Query("DELETE from DmuPtgyTempEntity pgty where pgty.id.userId = :userId")
	void deleteByRequestedUserName(@Param("userId") String userName);

}
