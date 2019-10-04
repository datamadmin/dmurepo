package com.dataeconomy.migration.app.mysql.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailId;
import com.dataeconomy.migration.app.mysql.entity.DmuReconDetailEntity;

@Repository
public interface DmuReconDetailsRepository extends JpaRepository<DmuReconDetailEntity, DmuHistoryDetailId> {

	@Query("SELECT d FROM DmuReconDetailEntity d WHERE d.dmuHIstoryDetailPK.requestNo = :requestNo ORDER BY d.dmuHIstoryDetailPK.srNo ASC")
	List<DmuReconDetailEntity> findByGivenRequestNo(@Param("requestNo") String requestNo);

}
