package com.studyroom.controller;

import com.studyroom.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "Data Report")
@RestController
@RequestMapping("/api/admin/report")
public class ReportController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Operation(summary = "各自习室日均使用率")
    @GetMapping("/daily-avg-usage")
    public Result<List<Map<String, Object>>> dailyAvgUsage(@RequestParam(defaultValue = "1") int days) {
        String sql = "SELECT r.id, r.name AS roomName, "
                + "ROUND(COALESCE(used.total_used * 100.0 / NULLIF(r.row_count * r.col_count * ?, 0), 0), 1) AS avgDailyRate "
                + "FROM room r "
                + "LEFT JOIN ( "
                + "  SELECT room_id, COUNT(DISTINCT CONCAT(seat_id, '-', DATE(create_time))) AS total_used "
                + "  FROM reservation "
                + "  WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL ? DAY) "
                + "    AND status != 2 "
                + "  GROUP BY room_id "
                + ") used ON r.id = used.room_id "
                + "ORDER BY avgDailyRate DESC";
        return Result.success(jdbcTemplate.queryForList(sql, days, days));
    }

    @Operation(summary = "预约高峰时段")
    @GetMapping("/peak-hours")
    public Result<List<Map<String, Object>>> peakHours() {
        String sql = "SELECT HOUR(start_time) AS hour, COUNT(*) AS value "
                + "FROM reservation WHERE start_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) "
                + "GROUP BY HOUR(start_time) ORDER BY hour";
        return Result.success(jdbcTemplate.queryForList(sql));
    }

    @Operation(summary = "校区分布（按座位数）")
    @GetMapping("/campus-distribution")
    public Result<List<Map<String, Object>>> campusDistribution() {
        String sql = "SELECT c.name, "
                + "COALESCE(SUM(r.row_count * r.col_count), 0) AS value "
                + "FROM campus c "
                + "LEFT JOIN building b ON c.id = b.campus_id "
                + "LEFT JOIN floor f ON b.id = f.building_id "
                + "LEFT JOIN room r ON f.id = r.floor_id "
                + "GROUP BY c.id, c.name ORDER BY c.id";
        return Result.success(jdbcTemplate.queryForList(sql));
    }
}
