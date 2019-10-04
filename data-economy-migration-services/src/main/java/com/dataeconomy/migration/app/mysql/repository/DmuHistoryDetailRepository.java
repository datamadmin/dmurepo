package com.dataeconomy.migration.app.mysql.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailId;

@Repository
public interface DmuHistoryDetailRepository extends JpaRepository<DmuHistoryDetailEntity, DmuHistoryDetailId> {

	@Query("SELECT detail from DmuHistoryDetailEntity detail where detail.dmuHIstoryDetailPK.requestNo = :requestNo")
	List<DmuHistoryDetailEntity> findHistoryDetailsByRequestNumber(@Param("requestNo") String requestNo);

	@Query("SELECT detail from DmuHistoryDetailEntity detail where detail.dmuHIstoryDetailPK.requestNo = :requestNo AND detail.dmuHIstoryDetailPK.srNo = :srNo")
	List<DmuHistoryDetailEntity> findHistoryDetailsByRequestNumberAndSrNo(@Param("requestNo") String requestNo,
			@Param("srNo") Long srNo);

	@Query("SELECT count(detail) from DmuHistoryDetailEntity detail where detail.dmuHIstoryDetailPK.requestNo = :requestNo AND detail.status = :status")
	Long findHistoryDetailsByRequestNoAndStatus(@Param("requestNo") String requestNo, @Param("status") String status);

	@Query("SELECT count(detail) from DmuHistoryDetailEntity detail where detail.dmuHIstoryDetailPK.requestNo = :requestNo AND detail.status = :status ORDER BY detail.dmuHIstoryDetailPK.srNo  ASC")
	Long findHistoryDetailsByRequestNoAndStatusAscOrder(@Param("requestNo") String requestNo,
			@Param("status") String status);

	@Query("SELECT detail from DmuHistoryDetailEntity detail where detail.dmuHIstoryDetailPK.requestNo = :requestNo AND detail.status = :status ORDER BY detail.dmuHIstoryDetailPK.requestNo ASC")
	List<DmuHistoryDetailEntity> findHistoryDetailsByRequestNoAndStatusList(@Param("requestNo") String requestNo,
			@Param("status") String status);

	@Query("SELECT detail from DmuHistoryDetailEntity detail where detail.dmuHIstoryDetailPK.requestNo = :requestNo AND detail.status = :status ORDER BY detail.dmuHIstoryDetailPK.requestNo ASC")
	public Page<DmuHistoryDetailEntity> findHistoryDetailsByRequestNoAndStatusListForBatch(
			@Param("requestNo") String requestNo, @Param("status") String status, Pageable pageable);

	@Modifying
	@Transactional
	@Query("UPDATE  DmuHistoryDetailEntity detail SET detail.status= :status where detail.dmuHIstoryDetailPK.requestNo = :requestNo AND detail.status = :status")
	void updateByRequestNoAndSrNo(@Param("requestNo") String requestNo, @Param("status") String status);

	@Modifying
	@Transactional
	@Query("UPDATE  DmuHistoryDetailEntity detail SET detail.status= :status where detail.dmuHIstoryDetailPK.requestNo = :requestNo AND detail.dmuHIstoryDetailPK.srNo = :srNo AND detail.status = :status")
	void updateByRequestNoAndSrNoAndStatus(@Param("requestNo") String requestNo, @Param("srNo") Long srNo,
			@Param("status") String status);

	@Modifying
	@Transactional
	@Query(" UPDATE DmuHistoryDetailEntity u SET u.status='In Progress' WHERE u.dmuHIstoryDetailPK.requestNo = :requestNo AND u.dmuHIstoryDetailPK.srNo = :srNo")
	void updateForRequestNo(@Param("requestNo") String requestNo, @Param("srNo") Long srNo);

}
