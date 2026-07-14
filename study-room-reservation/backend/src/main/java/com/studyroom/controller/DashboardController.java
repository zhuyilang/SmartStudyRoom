package com.studyroom.controller;

import com.studyroom.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "Dashboard")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Operation(summary = "Overall stats + week trend")
    @GetMapping("/overall")
    public Result<Map<String, Object>> overall() {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM campus) AS totalCampuses, "
                + "(SELECT COUNT(*) FROM building) AS totalBuildings, "
                + "(SELECT COUNT(*) FROM campus) AS totalCampuses, "
                + "(SELECT COUNT(*) FROM building) AS totalBuildings, "
                + "(SELECT COUNT(*) FROM room) AS totalRooms, "
                + "(SELECT COUNT(*) FROM seat) AS totalSeats, "
                + "(SELECT COUNT(*) FROM seat WHERE status = 1) AS occupiedSeats, "
                + "(SELECT COUNT(*) FROM seat WHERE status = 0) AS freeSeats, "
                + "(SELECT COUNT(*) FROM reservation WHERE status = 0) AS pendingReservations, "
                + "(SELECT COUNT(*) FROM reservation WHERE DATE(create_time) = CURRENT_DATE) AS todayReservations";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        long total = ((Number) result.get("totalSeats")).longValue();
        long occ = ((Number) result.get("occupiedSeats")).longValue();
        result.put("usageRate", total > 0 ? Math.round(occ * 1000.0 / total) / 10.0 : 0);

        // week trend
        List<Map<String, Object>> trend = jdbcTemplate.queryForList(
                "SELECT DATE(create_time) AS date, "
                + "COUNT(*) AS reservations, "
                + "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS checkins, "
                + "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS cancels "
                + "FROM reservation WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) "
                + "GROUP BY DATE(create_time) ORDER BY date");
        result.put("weekTrend", trend);
        return Result.success(result);
    }

    @Operation(summary = "Today stats + hourly + status dist")
    @GetMapping("/today")
    public Result<Map<String, Object>> today() {
        Map<String, Object> result = new LinkedHashMap<>();

        // summary
        Map<String, Object> summary = jdbcTemplate.queryForMap(
                "SELECT "
                + "(SELECT COUNT(*) FROM reservation WHERE DATE(create_time) = CURRENT_DATE) AS todayReservations, "
                + "(SELECT COUNT(*) FROM reservation WHERE DATE(sign_time) = CURRENT_DATE) AS todaySignIns, "
                + "(SELECT COUNT(*) FROM reservation WHERE status = 3 AND DATE(create_time) = CURRENT_DATE) AS todayNoShows");
        result.putAll(summary);

        // hourly data
        List<Map<String, Object>> hourly = jdbcTemplate.queryForList(
                "SELECT HOUR(create_time) AS hour, COUNT(*) AS count "
                + "FROM reservation WHERE DATE(create_time) = CURRENT_DATE "
                + "GROUP BY HOUR(create_time) ORDER BY hour");
        result.put("hourly", hourly);

        // hourly signins
        List<Map<String, Object>> hourlySignins = jdbcTemplate.queryForList(
                "SELECT HOUR(sign_time) AS hour, COUNT(*) AS count "
                + "FROM reservation WHERE DATE(sign_time) = CURRENT_DATE "
                + "GROUP BY HOUR(sign_time) ORDER BY hour");
        result.put("hourlySignins", hourlySignins);

        // hourly cancels
        List<Map<String, Object>> hourlyCancels = jdbcTemplate.queryForList(
                "SELECT HOUR(create_time) AS hour, COUNT(*) AS count "
                + "FROM reservation WHERE DATE(create_time) = CURRENT_DATE AND status = 2 "
                + "GROUP BY HOUR(create_time) ORDER BY hour");
        result.put("hourlyCancels", hourlyCancels);

        // status distribution: free + booked + signed_in
        Map<String, Object> dist = new LinkedHashMap<>();
        Integer freeSeats = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM seat WHERE status = 0", Integer.class);
        dist.put("0", freeSeats != null ? freeSeats : 0);
        Integer booked = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation WHERE status = 0", Integer.class);
        dist.put("1", booked != null ? booked : 0);
        Integer signedIn = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation WHERE status = 1", Integer.class);
        dist.put("2", signedIn != null ? signedIn : 0);
        result.put("statusDist", dist);
        return Result.success(result);
    }

    @Operation(summary = "Seat status per room")
    @GetMapping("/seatStatus")
    public Result<List<Map<String, Object>>> seatStatus() {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT r.id AS roomId, r.name AS roomName, "
                + "b.name AS buildingName, "
                + "r.row_count * r.col_count AS totalSeats, "
                + "COALESCE(SUM(CASE WHEN s.status = 1 THEN 1 ELSE 0 END), 0) AS occupiedSeats, "
                + "COALESCE(SUM(CASE WHEN s.status = 0 THEN 1 ELSE 0 END), 0) AS freeSeats "
                + "FROM room r "
                + "LEFT JOIN floor f ON r.floor_id = f.id "
                + "LEFT JOIN building b ON f.building_id = b.id "
                + "LEFT JOIN seat s ON s.room_id = r.id "
                + "GROUP BY r.id, r.name, b.name, r.row_count, r.col_count ORDER BY r.id");
        for (Map<String, Object> row : list) {
            long t = ((Number) row.get("totalSeats")).longValue();
            long o = ((Number) row.get("occupiedSeats")).longValue();
            row.put("rate", t > 0 ? Math.round(o * 1000.0 / t) / 10.0 : 0);
        }
        return Result.success(list);
    }

    @Operation(summary = "Heatmap data for a room")
    @GetMapping("/heatmap/{roomId}")
    public Result<Map<String, Object>> heatmap(@PathVariable Long roomId) {
        Map<String, Object> roomInfo = jdbcTemplate.queryForMap(
                "SELECT id, name, row_count AS `rows`, col_count AS `cols` FROM room WHERE id = ?", roomId);

        List<Map<String, Object>> seats = jdbcTemplate.queryForList(
                "SELECT row_num AS `row`, col_num AS `col`, status FROM seat "
                + "WHERE room_id = ? ORDER BY row_num, col_num", roomId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("roomId", roomId);
        result.put("roomName", roomInfo.get("name"));
        result.put("rows", roomInfo.get("rows"));
        result.put("cols", roomInfo.get("cols"));
        result.put("seats", seats);

        // 7-day aggregation per seat
        List<Map<String, Object>> daySeats = jdbcTemplate.queryForList(
                "SELECT s.row_num AS `row`, s.col_num AS `col`, "
                + "COUNT(r.id) AS usageCount "
                + "FROM seat s "
                + "LEFT JOIN reservation r ON s.id = r.seat_id "
                + "AND r.create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) "
                + "AND r.status IN (0,1,3,4) "
                + "WHERE s.room_id = ? "
                + "GROUP BY s.id, s.row_num, s.col_num ORDER BY s.row_num, s.col_num", roomId);
        result.put("daySeats", daySeats);

        // days labels
        List<Map<String, Object>> days = jdbcTemplate.queryForList(
                "SELECT DATE(r.create_time) AS date, COUNT(*) AS count "
                + "FROM reservation r WHERE r.room_id = ? "
                + "AND r.create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) "
                + "GROUP BY DATE(r.create_time) ORDER BY date", roomId);
        result.put("days", days);
        return Result.success(result);
    }
}
