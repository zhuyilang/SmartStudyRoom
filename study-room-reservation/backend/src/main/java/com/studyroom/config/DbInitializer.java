package com.studyroom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class DbInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DbInitializer.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== DB Init Start ===");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Execute init.sql (skip MySQL-specific statements for H2)
        ClassPathResource resource = new ClassPathResource("init.sql");
        if (!resource.exists()) {
            log.info("init.sql not found, skipping");
            return;
        }

        String sql;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Skip comments and MySQL-specific DDL
                if (trimmed.startsWith("--") || trimmed.isEmpty()
                        || trimmed.toUpperCase().startsWith("CREATE DATABASE")
                        || trimmed.toUpperCase().startsWith("USE ")
                        || trimmed.toUpperCase().startsWith("DROP DATABASE")
                        || trimmed.toUpperCase().startsWith("DELIMITER")
                        || trimmed.toUpperCase().startsWith("CREATE PROCEDURE")
                        || trimmed.toUpperCase().startsWith("CALL ")
                        || trimmed.toUpperCase().startsWith("DROP PROCEDURE")
                        || trimmed.toUpperCase().startsWith("SELECT '")) {
                    continue;
                }
                sb.append(line).append("\n");
            }
            sql = sb.toString();
        }

        // Fix SQL for H2: user→sys_user, strip MySQL-specific syntax
        sql = sql.replaceAll("(?i) COMMENT\\s+'[^']*'", "");
        sql = sql.replaceAll("(?i) COMMENT\\s+\"[^\"]*\"", "");
        // Replace table name 'user' → 'sys_user' (NOT column names like user_id)
        sql = sql.replaceAll("(?i)(?<![a-zA-Z])user(?![a-zA-Z_0-9])", "sys_user");

        String[] statements = sql.split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (trimmed.isEmpty()) continue;
            try {
                jdbcTemplate.execute(trimmed);
            } catch (Exception e) {
                log.debug("Skip (may already exist): {}", e.getMessage());
            }
        }

        // Auto-generate seats for each room (replaces MySQL stored procedure)
        java.util.List<java.util.Map<String, Object>> rooms = jdbcTemplate.queryForList(
                "SELECT id, total_rows, total_cols FROM room");
        for (java.util.Map<String, Object> room : rooms) {
            Long roomId = ((Number) room.get("id")).longValue();
            int rows = ((Number) room.get("total_rows")).intValue();
            int cols = ((Number) room.get("total_cols")).intValue();
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM seat WHERE room_id = ?", Integer.class, roomId);
            if (count != null && count > 0) {
                log.info("Room {} already has seats, skipping", roomId);
                continue;
            }
            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= cols; c++) {
                    jdbcTemplate.update(
                            "INSERT INTO seat (room_id, row_num, col_num, status) VALUES (?, ?, ?, 0)",
                            roomId, r, c);
                }
            }
            log.info("Room {} seats generated ({}x{})", roomId, rows, cols);
        }

        // Also execute data.sql for initial test data
        ClassPathResource dataResource = new ClassPathResource("data.sql");
        if (dataResource.exists()) {
            log.info("Executing data.sql...");
            String dataSql;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(dataResource.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) continue;
                    sb.append(line).append("\n");
                }
                dataSql = sb.toString();
            }
            String[] dataStatements = dataSql.split(";");
            for (String statement : dataStatements) {
                String trimmed = statement.trim();
                if (trimmed.isEmpty()) continue;
                try {
                    jdbcTemplate.execute(trimmed);
                } catch (Exception e) {
                    log.debug("Skip data insert: {}", e.getMessage());
                }
            }
        }

        log.info("=== DB Init Complete ===");
    }
}
