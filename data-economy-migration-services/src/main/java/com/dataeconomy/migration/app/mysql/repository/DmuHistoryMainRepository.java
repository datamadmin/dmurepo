package com.dataeconomy.migration.app.mysql.repository;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.mysql.entity.DmuHistoryMainEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuReconAndRequestCountProjection;

@Repository
public interface DmuHistoryMainRepository extends JpaRepository<DmuHistoryMainEntity, String> {

	@Query("select history from DmuHistoryMainEntity history where history.status in :ids ORDER BY history.requestedTime DESC")
	List<DmuHistoryMainEntity> findHistoryMainDetailsByStatus(@Param("ids") List<String> inventoryIdList);

	@Query("select history from DmuHistoryMainEntity history where history.status = :status ORDER BY history.requestedTime ASC")
	List<DmuHistoryMainEntity> findHistoryMainDetailsByStatusScheduler(@Param("status") String status);

	@Query("select new com.dataeconomy.migration.app.mysql.entity.DmuReconAndRequestCountProjection(v.status , count(v) as cnt) from DmuHistoryMainEntity v group by v.status")
	public List<DmuReconAndRequestCountProjection> findReconHistoryStatusCount(); 

	@Query("SELECT COUNT(u.status) FROM DmuHistoryMainEntity u WHERE u.status=:statusValue")
	Long getTaskDetailsCount(@Param("statusValue") String statusValue);

	@Modifying
	@Query(" UPDATE DmuHistoryMainEntity u SET u.status= :status WHERE u.requestNo = :requestNo")
	@Transactional
	void updateForRequestNo(@Param("requestNo") String requestNo, @Param("status") String status);
	
	@Query(" SELECT u FROM DmuHistoryMainEntity u  WHERE u.requestNo = :requestNo")
	DmuHistoryMainEntity getDMUHistoryMainBySrNo(@Param("requestNo") String requestNo);

	@Query("select history from DmuHistoryMainEntity history where status = :status AND requestNo = :requestNo")
	List<DmuHistoryMainEntity> findHistoryMainDetailsByStatusAndRequestNo(@Param("status") String status,
			@Param("requestNo") String requestNo);
	
	@Query(" SELECT u.requestNo FROM DmuHistoryMainEntity u  WHERE u.requestNo = :lablename")
	String checkLableExist(@Param("lablename") String lablename);

}
