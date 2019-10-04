package com.dataeconomy.migration.app.mysql.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dataeconomy.migration.app.mysql.entity.DmuBasketTempEntity;

@Repository
public interface DmuBasketTempRepository extends JpaRepository<DmuBasketTempEntity, Long> {

	@Query("SELECT dmu FROM DmuBasketTempEntity dmu where dmu.userId = :userId ORDER BY userId.srNo, userId.labelName")
	List<DmuBasketTempEntity> getBasketDetailsByUserId(@Param("userId") String userId);

	@Query("SELECT dmu FROM DmuBasketTempEntity dmu where dmu.userId = :userId ORDER BY userId.srNo ASC")
	List<DmuBasketTempEntity> getBasketDetailsByRequestNo(@Param("userId") String userId);

	Long deleteByUserId(String userId);

	Long removeBySrNo(Long srNo);

	List<DmuBasketTempEntity> findAllByOrderBySrNoAsc();

}
