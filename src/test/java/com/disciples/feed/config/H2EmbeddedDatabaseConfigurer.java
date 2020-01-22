package com.disciples.feed.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseConfigurer;

final class H2EmbeddedDatabaseConfigurer implements EmbeddedDatabaseConfigurer {
	
	private static final Logger logger = LoggerFactory.getLogger(H2EmbeddedDatabaseConfigurer.class);
	private static H2EmbeddedDatabaseConfigurer instance;

	public static synchronized H2EmbeddedDatabaseConfigurer getInstance() {
		if (instance == null) {
			instance = new H2EmbeddedDatabaseConfigurer();
		}
		return instance;
	}

	@Override
	public void configureConnectionProperties(ConnectionProperties properties, String databaseName) {
		properties.setDriverClass(org.h2.Driver.class);
		//disable MVCC
		properties.setUrl(String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false;MVCC=false", databaseName));
		properties.setUsername("sa");
		properties.setPassword("");
	}

	@Override
	public void shutdown(DataSource dataSource, String databaseName) {
		Connection con = null;
		try {
			con = dataSource.getConnection();
			con.createStatement().execute("SHUTDOWN");
		} catch (SQLException ex) {
			logger.warn("Could not shut down embedded database");
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Throwable ex) {
					logger.debug("Could not close JDBC Connection on shutdown", ex);
				}
			}
		}
	}
	
}
