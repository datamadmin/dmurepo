package com.dataeconomy.migration.app.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.connection.DmuHdfsConnectionService;
import com.dataeconomy.migration.app.model.DmuBasketDTO;
import com.dataeconomy.migration.app.model.DmuHistoryDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryMainEntity;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryMainRepository;
import com.dataeconomy.migration.app.util.DmuConstants;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuRequestService {

	@Autowired
	private DmuHistoryMainRepository dmuHistoryMainRepository;

	@Autowired
	private DmuHdfsConnectionService hdfcConnectionService;

	Random r = new Random();

	@Timed
	public boolean saveRequest(DmuHistoryDTO historyMainDto) {
		log.info("DMURequestService :: saveRequest :: historyMainDto {} ",
				ObjectUtils.toString(historyMainDto, "Invalid details"));
		try {
			DmuHistoryMainEntity dmuHistoryMain = DmuHistoryMainEntity.builder()
					.requestNo(historyMainDto.getRequestNo()).status(historyMainDto.getStatus())
					.requestedTime(LocalDateTime.now()).requestType(historyMainDto.getRequestType())
					.tknztnEnabled(historyMainDto.getTknztnEnabled()).tknztnFilePath(historyMainDto.getTknztnFilePath())
					.build();
			dmuHistoryMainRepository.save(dmuHistoryMain);
			return true;
		} catch (Exception exception) {
			log.error(" Exception occured at DMURequestService :: saveRequest :: {} ",
					ExceptionUtils.getStackTrace(exception));
			return false;
		}
	}

	@Timed
	@Transactional(readOnly = true)
	public List<String> getAllRequestDatabases() {
		log.info(" invoked =>  RequestService :: getAllRequestDatabases ");
		try {
			return new JdbcTemplate(hdfcConnectionService.getValidDataSource(DmuConstants.REGULAR))
					.query("SHOW DATABASES", new ResultSetExtractor<List<String>>() {

						@Override
						public List<String> extractData(ResultSet rs) throws SQLException {
							List<String> databaseList = new ArrayList<>();
							while (rs.next()) {
								databaseList.add(rs.getString(1));
							}
							return databaseList;
						}
					});
		} catch (Exception exception) {
			log.info(" Exception occured at RequestService :: getAllRequestDatabases {} ",
					ExceptionUtils.getStackTrace(exception));
			return Collections.emptyList();
		}
	}

	@Timed
	@Transactional(readOnly = true)
	public List<DmuBasketDTO> getAllTablesForGivenDatabase(String databaseName) {
		log.info(" invoked =>  RequestService :: getAllTablesForGivenDatabase  :: {} ", databaseName);
		try {
			DataSource dataSource = hdfcConnectionService.getValidDataSource(DmuConstants.REGULAR);
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.execute("USE " + databaseName);
			return jdbcTemplate.query(" SHOW TABLES", new ResultSetExtractor<List<DmuBasketDTO>>() {

				@Override
				public List<DmuBasketDTO> extractData(ResultSet rs) throws SQLException {
					List<DmuBasketDTO> dmuBasketDtoList = Lists.newArrayList();
					Long value = 0L;
					while (rs.next()) {
						dmuBasketDtoList.add(
								DmuBasketDTO.builder().srNo(++value).schemaName(databaseName).tableName(rs.getString(1))
										.filterCondition(null).targetS3Bucket(databaseName + "/" + rs.getString(1))
										.incrementalFlag(DmuConstants.NO).incrementalClmn(null).build());
					}
					return dmuBasketDtoList;
				}
			});
		} catch (Exception exception) {
			log.error(" Exception occured at RequestService :: getAllTablesForGivenDatabase {} ",
					ExceptionUtils.getStackTrace(exception));
			return Collections.emptyList();
		}
	}
}
