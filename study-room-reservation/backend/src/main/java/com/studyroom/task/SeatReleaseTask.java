package com.studyroom.task;

import com.studyroom.entity.Reservation;
import com.studyroom.service.BlacklistService;
import com.studyroom.service.SeatStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SeatReleaseTask {

    private static final Logger log = LoggerFactory.getLogger(SeatReleaseTask.class);

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private BlacklistService blacklistService;
    @Autowired private SeatStatusService seatStatusService;

    /**
     * 每5分钟检查一次超时未签到的预约，自动释放座位并记录爽约
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void releaseTimeoutSeats() {
        log.info("=== 定时任务：检查超时预约 ===");

        // 查询所有已预约但超过开始时间15分钟仍未签到的记录
        String sql = "SELECT * FROM reservation WHERE status = ? " +
                     "AND start_time < ? AND sign_time IS NULL";
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(15);

        try {
            List<Reservation> timeoutList = jdbcTemplate.query(sql,
                    new Object[]{Reservation.STATUS_BOOKED, deadline},
                    (rs, rowNum) -> {
                        Reservation r = new Reservation();
                        r.setId(rs.getLong("id"));
                        r.setUserId(rs.getLong("user_id"));
                        r.setSeatId(rs.getLong("seat_id"));
                        r.setStatus(rs.getInt("status"));
                        return r;
                    });

            int released = 0;
            for (Reservation r : timeoutList) {
                // 更新预约状态为超时
                jdbcTemplate.update(
                        "UPDATE reservation SET status = ? WHERE id = ?",
                        Reservation.STATUS_TIMEOUT, r.getId());

                // 释放座位
                seatStatusService.releaseSeat(r.getSeatId());

                // 记录爽约
                blacklistService.recordMiss(r.getUserId());

                released++;
                log.info("超时释放: reservationId={}, userId={}, seatId={}",
                        r.getId(), r.getUserId(), r.getSeatId());
            }

            if (released > 0) {
                log.info("本次释放 {} 个超时预约", released);
            }
        } catch (Exception e) {
            log.error("释放超时座位失败", e);
        }
    }
}
