package com.dataeconomy.migration.app.mysql.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dataeconomy.migration.app.mysql.entity.DmuReconMainentity;
import com.dataeconomy.migration.app.mysql.entity.DmuReconAndRequestCountProjection;

@Repository
public interface DmuReconMainRepository extends JpaRepository<DmuReconMainentity, String> {

	@Query("select new com.dataeconomy.migration.app.mysql.entity.DmuReconAndRequestCountProjection(v.status , count(v) as cnt) from DmuReconMainentity v group by v.status")
	public List<DmuReconAndRequestCountProjection> findReconMainStatusCount();

}
