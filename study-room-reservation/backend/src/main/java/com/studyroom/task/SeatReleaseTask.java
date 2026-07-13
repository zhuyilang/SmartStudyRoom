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

    /** Auto-release overdue reservations */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void releaseTimeoutSeats() {
        log.info("=== Check overdue reservations ===");

        String sql = "SELECT * FROM reservation WHERE status = ? " +
                     "AND create_time < ? AND sign_time IS NULL";
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(30);

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
                jdbcTemplate.update(
                        "UPDATE reservation SET status = ? WHERE id = ?",
                        Reservation.STATUS_TIMEOUT, r.getId());

                seatStatusService.releaseSeat(r.getSeatId());

                blacklistService.recordMiss(r.getUserId());

                released++;
                log.info("Released: reservationId={}, userId={}, seatId={}",
                        r.getId(), r.getUserId(), r.getSeatId());
            }

            if (released > 0) {
                log.info("Released {} overdue reservations", released);
            }
        } catch (Exception e) {
            log.error("Failed to release overdue seats", e);
        }
    }
}