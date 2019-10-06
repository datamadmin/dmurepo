package com.dataeconomy.migration.app.mysql.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dataeconomy.migration.app.mysql.entity.DmuBasketTempEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuBasketTempId;

@Repository
public interface DmuBasketTempRepository extends JpaRepository<DmuBasketTempEntity, DmuBasketTempId> {

	@Query("SELECT dmu FROM DmuBasketTempEntity dmu where dmu.dmuBasketTempId.userId = :userId ORDER BY dmu.dmuBasketTempId.srNo, dmu.dmuBasketTempId.labelName")
	List<DmuBasketTempEntity> getBasketDetailsByUserId(@Param("userId") String userId);

	@Query("SELECT dmu FROM DmuBasketTempEntity dmu where dmu.dmuBasketTempId.userId = :userId ORDER BY dmu.dmuBasketTempId.srNo ASC")
	List<DmuBasketTempEntity> getBasketDetailsByRequestNo(@Param("userId") String userId);

	@Modifying
	@Query("DELETE FROM DmuBasketTempEntity dmu where dmu.dmuBasketTempId.userId = :userId")
	Integer deleteByDmuBasketTempIdUserId(String userId);

	@Query("DELETE FROM DmuBasketTempEntity dmu where dmu.dmuBasketTempId.srNo = :srNo")
	Integer removeByDmuBasketTempIdSrNo(Long srNo);

	@Query("SELECT dmu FROM DmuBasketTempEntity dmu where dmu.dmuBasketTempId.srNo = :srNo")
	List<DmuBasketTempEntity> findAllByOrderByDmuBasketTempIdSrNoAsc(Long srNo);

	@Query("SELECT dmu FROM DmuBasketTempEntity dmu where dmu.dmuBasketTempId.userId = :userId")
	List<DmuBasketTempEntity> findAllByOrderByDmuBasketTempIdUserIdAsc(String userId);

}
