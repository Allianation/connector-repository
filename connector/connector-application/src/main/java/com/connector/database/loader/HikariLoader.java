package com.connector.database.loader;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

@Component
public class HikariLoader implements ApplicationRunner {

	@Autowired
	@Qualifier("oracleDataSource")
	HikariDataSource oracleDataSource;

	@Autowired
	@Qualifier("sqlserverDataSource")
	HikariDataSource sqlserverDataSource;

	@Autowired
	public void run(ApplicationArguments args) throws SQLException {
		oracleDataSource.getConnection();
		sqlserverDataSource.getConnection();
	}
}
