package com.studyroom.controller;

import com.studyroom.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "实时看板", description = "自习室座位实时状态与统计数据")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Operation(summary = "获取所有自习室座位占用统计")
    @GetMapping("/seatStatus")
    public Result<List<Map<String, Object>>> seatStatus() {
        String sql = "SELECT r.id AS roomId, r.name AS roomName, " +
                "b.name AS buildingName, " +
                "r.row_count * r.col_count AS totalSeats, " +
                "COALESCE(SUM(CASE WHEN s.status = 1 THEN 1 ELSE 0 END), 0) AS occupiedSeats, " +
                "COALESCE(SUM(CASE WHEN s.status = 0 THEN 1 ELSE 0 END), 0) AS freeSeats " +
                "FROM room r " +
                "LEFT JOIN floor f ON r.floor_id = f.id " +
                "LEFT JOIN building b ON f.building_id = b.id " +
                "LEFT JOIN seat s ON s.room_id = r.id " +
                "GROUP BY r.id, r.name, b.name, r.row_count, r.col_count " +
                "ORDER BY r.id";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return Result.success(result);
    }

    @Operation(summary = "获取指定房间实时座位详情")
    @GetMapping("/room/{id}")
    public Result<Map<String, Object>> roomDetail(@PathVariable Long id) {
        String roomSql = "SELECT id, name, row_count AS rowCount, col_count AS colCount FROM room WHERE id = ?";
        Map<String, Object> roomInfo = jdbcTemplate.queryForMap(roomSql, id);

        String seatSql = "SELECT row_num AS `row`, col_num AS `col`, status FROM seat " +
                "WHERE room_id = ? ORDER BY row_num, col_num";
        List<Map<String, Object>> seats = jdbcTemplate.queryForList(seatSql, id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("roomId", id);
        result.put("roomName", roomInfo.get("name"));
        result.put("rows", roomInfo.get("rowCount"));
        result.put("cols", roomInfo.get("colCount"));
        result.put("seats", seats);

        return Result.success(result);
    }

    @Operation(summary = "整体统计（总数/使用率）")
    @GetMapping("/overall")
    public Result<Map<String, Object>> overall() {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM room) AS totalRooms, " +
                "(SELECT COUNT(*) FROM seat) AS totalSeats, " +
                "(SELECT COUNT(*) FROM seat WHERE status = 1) AS occupiedSeats, " +
                "(SELECT COUNT(*) FROM seat WHERE status = 0) AS freeSeats, " +
                "(SELECT COUNT(*) FROM reservation WHERE status = 0) AS pendingReservations";

        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        long total = ((Number) result.get("totalSeats")).longValue();
        long occupied = ((Number) result.get("occupiedSeats")).longValue();
        result.put("usageRate", total > 0 ? Math.round(occupied * 1000.0 / total) / 10.0 : 0);
        return Result.success(result);
    }

    @Operation(summary = "今日统计（预约/签到/爽约）")
    @GetMapping("/today")
    public Result<Map<String, Object>> today() {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM reservation WHERE DATE(create_time) = CURRENT_DATE) AS todayReservations, " +
                "(SELECT COUNT(*) FROM reservation WHERE DATE(sign_time) = CURRENT_DATE) AS todaySignIns, " +
                "(SELECT COUNT(*) FROM reservation WHERE status = 3 AND DATE(create_time) = CURRENT_DATE) AS todayNoShows";

        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        return Result.success(result);
    }
}
