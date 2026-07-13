package com.studyroom.service.impl;

import com.studyroom.service.SeatStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeatStatusServiceImpl implements SeatStatusService {

    /**
     * Seat status constants:
     * 0 = free, 1 = occupied, 2 = maintenance
     */
    private static final int STATUS_FREE = 0;
    private static final int STATUS_OCCUPIED = 1;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void occupySeat(Long seatId) {
        jdbcTemplate.update("UPDATE seat SET status = ? WHERE id = ?", STATUS_OCCUPIED, seatId);
    }

    @Override
    public void releaseSeat(Long seatId) {
        jdbcTemplate.update("UPDATE seat SET status = ? WHERE id = ?", STATUS_FREE, seatId);
    }
}
