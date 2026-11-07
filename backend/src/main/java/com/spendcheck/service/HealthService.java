package com.spendcheck.service;

import com.spendcheck.dto.HealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Service
public class HealthService {

    private static final Logger log = LoggerFactory.getLogger(HealthService.class);

    private final DataSource dataSource;

    public HealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HealthResponse checkHealth() {
        String dbStatus = getDatabaseStatus();
        String javaVersion = System.getProperty("java.version");

        return new HealthResponse(
                "UP",
                "SpendCheck Backend API",
                dbStatus,
                javaVersion
        );
    }

    private String getDatabaseStatus() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion() + " (Connected)";
        } catch (SQLException e) {
            log.warn("Database connection check failed: {}", e.getMessage());
            return "Disconnected: " + e.getMessage();
        }
    }
}
