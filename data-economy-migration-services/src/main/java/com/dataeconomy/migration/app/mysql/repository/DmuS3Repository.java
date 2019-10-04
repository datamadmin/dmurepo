package com.dataeconomy.migration.app.mysql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dataeconomy.migration.app.mysql.entity.DmuS3Entity;

@Repository
public interface DmuS3Repository extends JpaRepository<DmuS3Entity, Long> {

}
