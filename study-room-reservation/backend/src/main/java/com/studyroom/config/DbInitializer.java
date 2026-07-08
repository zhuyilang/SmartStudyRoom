package com.studyroom.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DbInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== 开始初始化数据库 ===");

        // 1. 尝试MySQL方式：确保数据库存在（来自"新建文件夹(2)"版本）
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS study_room DEFAULT CHARACTER SET utf8mb4");
            stmt.execute("USE study_room");
            log.info("数据库 study_room 已确认");
        } catch (Exception e) {
            log.warn("创建数据库失败（可能已有连接）: {}", e.getMessage());
        }

        // 2. 用 JdbcTemplate 执行建表SQL
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        ClassPathResource resource = new ClassPathResource("init.sql");
        String sql;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.startsWith("USE") || trimmed.isEmpty()
                        || trimmed.startsWith("DELIMITER") || trimmed.startsWith("CREATE PROCEDURE")
                        || trimmed.startsWith("CALL") || trimmed.startsWith("DROP PROCEDURE")
                        || trimmed.startsWith("SELECT '")) {
                    continue;
                }
                sb.append(line).append("\n");
            }
            sql = sb.toString();
        }

        String[] statements = sql.split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (trimmed.isEmpty()) continue;
            try {
                jdbcTemplate.execute(trimmed);
            } catch (Exception e) {
                log.warn("跳过（可能已存在）: {}", e.getMessage());
            }
        }

        // 3. 自动生成座位
        try {
            List<Map<String, Object>> rooms = jdbcTemplate.queryForList(
                    "SELECT id, total_rows, total_cols FROM room");
            for (Map<String, Object> room : rooms) {
                Long roomId = ((Number) room.get("id")).longValue();
                int rows = ((Number) room.get("total_rows")).intValue();
                int cols = ((Number) room.get("total_cols")).intValue();

                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM seat WHERE room_id = ?", Integer.class, roomId);
                if (count != null && count > 0) {
                    log.info("自习室 {} 已有座位，跳过", roomId);
                    continue;
                }

                for (int r = 1; r <= rows; r++) {
                    for (int c = 1; c <= cols; c++) {
                        jdbcTemplate.update(
                                "INSERT INTO seat (room_id, row_num, col_num, status) VALUES (?, ?, ?, 0)",
                                roomId, r, c);
                    }
                }
                log.info("自习室 {} 座位已生成 ({}x{})", roomId, rows, cols);
            }
        } catch (Exception e) {
            log.info("座位自动生成跳过（room表可能不存在或格式不同）: {}", e.getMessage());
        }

        log.info("=== 数据库初始化完成 ===");
    }
}
