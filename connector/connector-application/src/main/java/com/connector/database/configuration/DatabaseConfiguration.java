package com.connector.database.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@PropertySource({ "classpath:application.properties" })
@Primary
public class DatabaseConfiguration {
	@Bean(name = "firstDataSource")
    @ConfigurationProperties("first.datasource")
    @Primary
    public HikariDataSource firstDataSource() {
        return (HikariDataSource) DataSourceBuilder.create().build();
    }

    @Bean(name = "secondDataSource")
    @ConfigurationProperties("second.datasource")
    public HikariDataSource secondDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name="firstTransactionManager")
    @Autowired
    @Primary
    DataSourceTransactionManager firstTransactionManager(@Qualifier ("firstDataSource") DataSource datasource1) {
        return new DataSourceTransactionManager(datasource1);
    }

    @Bean(name="secondTransactionManager")
    @Autowired
    DataSourceTransactionManager secondTransactionManager(@Qualifier("secondDataSource") DataSource datasource2) {
    	return new DataSourceTransactionManager(datasource2);
    }
}
