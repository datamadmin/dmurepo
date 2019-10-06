package com.dataeconomy.migration.app.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataeconomy.migration.app.aop.Timed;
import com.dataeconomy.migration.app.connection.DmuHdfsConnectionService;
import com.dataeconomy.migration.app.exception.DataMigrationException;
import com.dataeconomy.migration.app.model.DmuBasketDTO;
import com.dataeconomy.migration.app.mysql.entity.DmuBasketTempEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuBasketTempId;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryDetailId;
import com.dataeconomy.migration.app.mysql.entity.DmuHistoryMainEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuPtgyTempEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuPtgyTempId;
import com.dataeconomy.migration.app.mysql.entity.DmuReconDetailEntity;
import com.dataeconomy.migration.app.mysql.entity.DmuReconMainentity;
import com.dataeconomy.migration.app.mysql.repository.DmuBasketTempRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryDetailRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuHistoryMainRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuPtgyRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuReconDetailsRepository;
import com.dataeconomy.migration.app.mysql.repository.DmuReconMainRepository;
import com.dataeconomy.migration.app.util.DmuConstants;
import com.google.common.collect.Lists;

@Service
public class DmuBasketService {

	@Autowired
	private DmuBasketTempRepository basketTempRepository;

	@Autowired
	private DmuPtgyRepository dmuPgtyRepository;

	@Autowired
	private DmuHdfsConnectionService hdfcConnectionService;

	@Autowired
	private DmuReconMainRepository reconMainRepository;

	@Autowired
	private DmuHistoryDetailRepository historyDetailRepository;

	@Autowired
	private DmuReconDetailsRepository dmuReconDetailRepository;

	@Autowired
	private DmuHistoryMainRepository dmuHistoryMainRepository;

	@Autowired
	private DmuPtgyRepository dmuPtgyRepository;

	@Timed
	@Transactional(readOnly = true)
	public List<DmuBasketDTO> getAllBasketDetails() {
		return Optional.ofNullable(basketTempRepository.findAll()).orElse(new ArrayList<>()).stream()
				.map(basketObj -> DmuBasketDTO.builder().srNo(basketObj.getDmuBasketTempId().getSrNo())
						.userId(basketObj.getDmuBasketTempId().getUserId()).schemaName(basketObj.getSchemaName())
						.tableName(basketObj.getTableName()).filterCondition(basketObj.getFilterCondition())
						.targetS3Bucket(basketObj.getTargetS3Bucket()).incrementalFlag(basketObj.getIncrementalFlag())
						.incrementalClmn(basketObj.getIncrementalClmn())
						.labelName(basketObj.getDmuBasketTempId().getLabelName()).build())
				.collect(Collectors.toList());
	}

	@Timed
	@Transactional(rollbackFor = Exception.class)
	public boolean saveBasketDetails(List<DmuBasketDTO> dmuBasketDtoList, String userName) {

		List<DmuBasketTempEntity> dmuEntityList = Optional.ofNullable(dmuBasketDtoList).orElse(new ArrayList<>())
				.stream().filter(DmuBasketDTO::isAddtoBasket)
				.map(dmuBasketDto -> DmuBasketTempEntity.builder()
						.dmuBasketTempId(DmuBasketTempId.builder().srNo(dmuBasketDto.getSrNo())
								.userId(dmuBasketDto.getUserId()).labelName(dmuBasketDto.getLabelName()).build())
						.schemaName(dmuBasketDto.getSchemaName()).tableName(dmuBasketDto.getTableName())
						.filterCondition(dmuBasketDto.getFilterCondition())
						.targetS3Bucket(dmuBasketDto.getTargetS3Bucket())
						.incrementalFlag(dmuBasketDto.getIncrementalFlag())
						.incrementalClmn(dmuBasketDto.getIncrementalClmn()).build())
				.collect(Collectors.toList());

		Optional.ofNullable(dmuEntityList).ifPresent(entities -> {
			basketTempRepository.saveAll(entities);
			dmuPgtyRepository.save(DmuPtgyTempEntity.builder()
					.id(DmuPtgyTempId.builder().userId(dmuBasketDtoList.get(0).getUserId())
							.labelName(dmuBasketDtoList.get(0).getLabelName()).build())
					.tknztnEnabled(dmuBasketDtoList.get(0).isTknztnEnabled() ? DmuConstants.YES : DmuConstants.NO)
					.tknztnFilePath(dmuBasketDtoList.get(0).getTknztnFilePath()).build());
		});
		return true;
	}

	@Timed
	@Transactional(rollbackFor = Exception.class)
	public boolean saveBasketDetailsAndPurge(List<DmuBasketDTO> dmuBasketDtoList, String userName)
			throws DataMigrationException {
		try {
			List<DmuHistoryMainEntity> historyMainList = new ArrayList<>();
			List<DmuReconMainentity> reconMainList = new ArrayList<>();
			List<DmuHistoryDetailEntity> historyDetailList = new ArrayList<>();
			List<DmuReconDetailEntity> reconDetailList = new ArrayList<>();

			Optional.ofNullable(dmuBasketDtoList).orElse(new ArrayList<>()).stream().filter(DmuBasketDTO::isAddtoBasket)
					.forEach(dmuBasketDto -> {

						historyMainList.add(DmuHistoryMainEntity.builder().userId(dmuBasketDto.getUserId())
								.requestNo(dmuBasketDto.getLabelName()).requestedTime(LocalDateTime.now())
								.status(DmuConstants.SUBMITTED).requestType(dmuBasketDto.getRequestType())
								.scriptGenCmpltTime(null).scriptGenCmpltTime(null)
								.requestNo(dmuBasketDto.getLabelName())
								.tknztnEnabled(dmuBasketDto.isTknztnEnabled() ? DmuConstants.YES : DmuConstants.NO)
								.tknztnFilePath(dmuBasketDto.getTknztnFilePath()).build());

						reconMainList.add(DmuReconMainentity.builder().userId(dmuBasketDto.getUserId())
								.requestedTime(LocalDateTime.now()).status(DmuConstants.NOT_STARTED)
								.requestType(dmuBasketDto.getRequestType()).requestNo(dmuBasketDto.getLabelName())
								.build());

						historyDetailList.add(DmuHistoryDetailEntity.builder()
								.dmuHIstoryDetailPK(DmuHistoryDetailId.builder().srNo(dmuBasketDto.getSrNo())
										.requestNo(dmuBasketDto.getLabelName()).build())
								.schemaName(dmuBasketDto.getSchemaName()).tableName(dmuBasketDto.getTableName())
								.filterCondition(dmuBasketDto.getFilterCondition())
								.targetS3Bucket(dmuBasketDto.getTargetS3Bucket())
								.incrementalFlag(dmuBasketDto.getIncrementalFlag())
								.incrementalClmn(dmuBasketDto.getIncrementalClmn()).status(DmuConstants.SUBMITTED)
								.build());

						reconDetailList.add(DmuReconDetailEntity.builder()
								.dmuHIstoryDetailPK(DmuHistoryDetailId.builder().srNo(dmuBasketDto.getSrNo())
										.requestNo(dmuBasketDto.getLabelName()).build())
								.schemaName(dmuBasketDto.getSchemaName()).tableName(dmuBasketDto.getTableName())
								.filterCondition(dmuBasketDto.getFilterCondition())
								.targetS3Bucket(dmuBasketDto.getTargetS3Bucket())
								.incrementalFlag(dmuBasketDto.getIncrementalFlag())
								.incrementalColumn(dmuBasketDto.getIncrementalClmn()).status(DmuConstants.NOT_STARTED)
								.build());

					});

			dmuReconDetailRepository.saveAll(reconDetailList);
			historyDetailRepository.saveAll(historyDetailList);
			reconMainRepository.saveAll(reconMainList);
			dmuHistoryMainRepository.saveAll(historyMainList);

			if (CollectionUtils.isNotEmpty(dmuBasketDtoList)) {
				dmuPgtyRepository.save(DmuPtgyTempEntity.builder()
						.id(DmuPtgyTempId.builder().userId(dmuBasketDtoList.get(0).getUserId())
								.labelName(dmuBasketDtoList.get(0).getLabelName()).build())
						.tknztnEnabled(dmuBasketDtoList.get(0).isTknztnEnabled() ? DmuConstants.YES : DmuConstants.NO)
						.tknztnFilePath(dmuBasketDtoList.get(0).getTknztnFilePath()).build());
			}
			basketTempRepository.deleteByDmuBasketTempIdUserId(userName);
			dmuPtgyRepository.deleteByRequestedUserName(userName);
			return true;
		} catch (Exception e) {
			throw new DataMigrationException("Unable to persist basket details ");
		}
	}

	@Timed
	@Transactional(readOnly = true)
	public List<DmuBasketDTO> getBasketDetailsByUserId(String userId) {
		return Optional.ofNullable(basketTempRepository.findAllByOrderByDmuBasketTempIdUserIdAsc(userId))
				.orElse(new ArrayList<>()).stream()
				.map(basketObj -> DmuBasketDTO.builder().srNo(basketObj.getDmuBasketTempId().getSrNo())
						.userId(basketObj.getDmuBasketTempId().getUserId()).schemaName(basketObj.getSchemaName())
						.tableName(basketObj.getTableName()).filterCondition(basketObj.getFilterCondition())
						.targetS3Bucket(basketObj.getTargetS3Bucket()).incrementalFlag(basketObj.getIncrementalFlag())
						.incrementalClmn(basketObj.getIncrementalClmn())
						.labelName(basketObj.getDmuBasketTempId().getLabelName()).build())
				.collect(Collectors.toList());

	}

	@Transactional
	public boolean purgeBasketDetailsByUserId(String userId) {
		basketTempRepository.deleteByDmuBasketTempIdUserId(userId);
		dmuPtgyRepository.deleteByRequestedUserName(userId);
		return true;
	}

	@Timed
	@Transactional(readOnly = true)
	public List<DmuBasketDTO> getBasketDetailsBySearchParam(String searchParam) throws DataMigrationException {
		return new JdbcTemplate(hdfcConnectionService.getValidDataSource(DmuConstants.REGULAR))
				.query("USE " + searchParam + "; SHOW TABLES;", new ResultSetExtractor<List<DmuBasketDTO>>() {

					@Override
					public List<DmuBasketDTO> extractData(ResultSet rs) throws SQLException {
						List<DmuBasketDTO> dmuBasketDtoList = Lists.newArrayList();
						Long value = 0L;
						while (rs.next()) {
							dmuBasketDtoList.add(DmuBasketDTO.builder().srNo(++value).schemaName(rs.getString(1))
									.tableName(rs.getString(1)).filterCondition(null)
									.targetS3Bucket(searchParam + "/" + rs.getString(1))
									.incrementalFlag(DmuConstants.NO).incrementalClmn(null).build());
						}
						return dmuBasketDtoList;
					}
				});
	}

	@Timed
	@Transactional(rollbackFor = Exception.class)
	public boolean purgeBasketDetailsBySrNo(Optional<Long> srNo) {
		srNo.ifPresent(deleteBySrNo -> basketTempRepository.removeByDmuBasketTempIdSrNo(deleteBySrNo));
		return true;
	}

	@Timed
	@Transactional(rollbackFor = Exception.class)
	public boolean clearBasketDetails(String userName) {
		basketTempRepository.deleteByDmuBasketTempIdUserId(userName);
		dmuPtgyRepository.deleteByRequestedUserName(userName);
		return true;
	}
}