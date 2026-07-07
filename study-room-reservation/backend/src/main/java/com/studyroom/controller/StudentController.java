package com.studyroom.controller;

import com.studyroom.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "学生端浏览", description = "自习室列表、详情与座位网格查询")
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Operation(summary = "获取自习室列表（含空闲座位数，支持按校区/楼栋筛选）")
    @GetMapping("/room/list")
    public Result<List<Map<String, Object>>> roomList(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String keyword) {

        StringBuilder sql = new StringBuilder(
                "SELECT r.id AS roomId, r.name AS roomName, b.name AS buildingName, c.name AS campusName, " +
                "r.total_rows AS totalRows, r.total_cols AS totalCols, " +
                "r.total_rows * r.total_cols AS totalSeats, " +
                "r.open_time AS openTime, r.close_time AS closeTime, " +
                "COALESCE((SELECT COUNT(*) FROM seat s WHERE s.room_id = r.id AND s.status = 0), 0) AS freeSeats " +
                "FROM room r " +
                "LEFT JOIN floor f ON r.floor_id = f.id " +
                "LEFT JOIN building b ON f.building_id = b.id " +
                "LEFT JOIN campus c ON b.campus_id = c.id WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (campusId != null) {
            sql.append("AND c.id = ? ");
            params.add(campusId);
        }
        if (buildingId != null) {
            sql.append("AND b.id = ? ");
            params.add(buildingId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND r.name LIKE ? ");
            params.add("%" + keyword + "%");
        }
        sql.append("ORDER BY r.id");

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        return Result.success(result);
    }

    @Operation(summary = "获取自习室详情")
    @GetMapping("/room/{id}")
    public Result<Map<String, Object>> roomDetail(@PathVariable Long id) {
        String sql = "SELECT r.id AS roomId, r.name AS roomName, b.name AS buildingName, c.name AS campusName, " +
                "r.total_rows AS totalRows, r.total_cols AS totalCols, " +
                "r.total_rows * r.total_cols AS totalSeats, " +
                "r.open_time AS openTime, r.close_time AS closeTime, r.description, " +
                "COALESCE((SELECT COUNT(*) FROM seat s WHERE s.room_id = r.id AND s.status = 0), 0) AS freeSeats " +
                "FROM room r " +
                "LEFT JOIN floor f ON r.floor_id = f.id " +
                "LEFT JOIN building b ON f.building_id = b.id " +
                "LEFT JOIN campus c ON b.campus_id = c.id " +
                "WHERE r.id = ?";

        Map<String, Object> result = jdbcTemplate.queryForMap(sql, id);
        return Result.success(result);
    }

    @Operation(summary = "获取指定房间的座位网格")
    @GetMapping("/seat/grid")
    public Result<Map<String, Object>> seatGrid(
            @RequestParam Long roomId,
            @RequestParam(required = false) String date) {

        String roomSql = "SELECT id, name, total_rows AS totalRows, total_cols AS totalCols FROM room WHERE id = ?";
        Map<String, Object> roomInfo = jdbcTemplate.queryForMap(roomSql, roomId);

        String seatSql = "SELECT row_num AS row, col_num AS col, status FROM seat " +
                "WHERE room_id = ? ORDER BY row_num, col_num";
        List<Map<String, Object>> seats = jdbcTemplate.queryForList(seatSql, roomId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("roomId", roomId);
        result.put("roomName", roomInfo.get("name"));
        result.put("rows", roomInfo.get("totalRows"));
        result.put("cols", roomInfo.get("totalCols"));
        result.put("seats", seats);

        return Result.success(result);
    }
}
