package com.dataeconomy.migration.app.connection;

import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmuConnectionPool {

	public HikariDataSource getDataSourceFromConfig(String connectionPoolName, String driverClassName,
			String databaseUrl) {
		log.info(
				" ConnectionPool :: getDataSourceFromConfig =>    connectionPoolName {} ,  driverClassName {} , databaseUrl ",
				connectionPoolName, driverClassName, databaseUrl);

		HikariConfig jdbcConfig = new HikariConfig();
		jdbcConfig.setPoolName(connectionPoolName);
		jdbcConfig.setMaximumPoolSize(20);
		jdbcConfig.setMinimumIdle(5);
		jdbcConfig.setMaxLifetime(2000000);
		jdbcConfig.setConnectionTimeout(30000);
		jdbcConfig.setIdleTimeout(30000);
		jdbcConfig.setJdbcUrl(databaseUrl);
		jdbcConfig.setDriverClassName(driverClassName);

		jdbcConfig.addDataSourceProperty("cachePrepStmts", true);
		jdbcConfig.addDataSourceProperty("prepStmtCacheSize", 256);
		jdbcConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
		jdbcConfig.addDataSourceProperty("useServerPrepStmts", true);

		return new HikariDataSource(jdbcConfig);

	}

}
