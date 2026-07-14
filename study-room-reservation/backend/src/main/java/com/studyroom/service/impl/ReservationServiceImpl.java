package com.studyroom.service.impl;

import com.studyroom.common.BusinessException;
import com.studyroom.entity.Reservation;
import com.studyroom.mapper.ReservationMapper;
import com.studyroom.service.BlacklistService;
import com.studyroom.service.ReservationService;
import com.studyroom.service.SeatStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @Autowired private ReservationMapper reservationMapper;
    @Autowired private BlacklistService blacklistService;
    @Autowired private SeatStatusService seatStatusService;
    @Autowired private JdbcTemplate jdbcTemplate;

    private static final int MAX_DAILY_RESERVATIONS = 3;

    @Override
    @Transactional
    public Reservation reserve(Long userId, Long seatId, Long roomId) {
        if (blacklistService.isBanned(userId)) {
            throw new BusinessException("blacklisted");
        }

        // one active reservation per user
        int activeCount = reservationMapper.countActiveByUser(userId);
        if (activeCount > 0) {
            throw new BusinessException("Already have active reservation, cancel first");
        }

        int todayCount = reservationMapper.countTodayReservations(userId);
        if (todayCount >= MAX_DAILY_RESERVATIONS) {
            throw new BusinessException("daily limit: " + MAX_DAILY_RESERVATIONS);
        }

        LocalDateTime start = LocalDateTime.now();
        // reservation held until user manually releases
        LocalDateTime end = start.plusYears(10);

        int conflict = reservationMapper.findConflict(seatId, start, end);
        if (conflict > 0) {
            throw new BusinessException("time conflict");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setSeatId(seatId);
        reservation.setRoomId(roomId);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(Reservation.STATUS_BOOKED);
        reservation.setCreateTime(LocalDateTime.now());

        reservationMapper.insert(reservation);
        seatStatusService.occupySeat(seatId);

        log.info("reserve success: userId={}, seatId={}, roomId={}, start={}, end={}",
                userId, seatId, roomId, start, end);
        return reservation;
    }

    @Override
    @Transactional
    public void cancel(Long reservationId, Long userId) {
        Reservation r = reservationMapper.selectById(reservationId);
        if (r == null) throw new BusinessException("not found");
        if (!r.getUserId().equals(userId)) throw new BusinessException("not your reservation");
        if (r.getStatus() != Reservation.STATUS_BOOKED) throw new BusinessException("cannot cancel");

        r.setStatus(Reservation.STATUS_CANCELLED);
        reservationMapper.updateById(r);
        seatStatusService.releaseSeat(r.getSeatId());
        log.info("cancelled: id={}, userId={}", reservationId, userId);
    }

    @Override
    @Transactional
    public void signIn(Long reservationId, Long userId) {
        Reservation r = reservationMapper.selectById(reservationId);
        if (r == null) throw new BusinessException("not found");
        if (!r.getUserId().equals(userId)) throw new BusinessException("not your reservation");
        if (r.getStatus() != Reservation.STATUS_BOOKED) throw new BusinessException("cannot sign in");

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(r.getCreateTime().plusMinutes(30))) {
            throw new BusinessException("sign-in window expired (30 min)");
        }

        r.setStatus(Reservation.STATUS_SIGNED_IN);
        r.setSignTime(now);
        reservationMapper.updateById(r);
        log.info("signed in: id={}, userId={}", reservationId, userId);
    }

    @Override
    @Transactional
    public void release(Long reservationId, Long userId) {
        Reservation r = reservationMapper.selectById(reservationId);
        if (r == null) throw new BusinessException("not found");
        if (!r.getUserId().equals(userId)) throw new BusinessException("not your reservation");
        if (r.getStatus() != Reservation.STATUS_SIGNED_IN) throw new BusinessException("can only release signed-in");

        r.setStatus(Reservation.STATUS_COMPLETED);
        reservationMapper.updateById(r);
        seatStatusService.releaseSeat(r.getSeatId());
        log.info("released: id={}, userId={}", reservationId, userId);
    }

    @Override
    public List<Reservation> getMyReservations(Long userId, Integer status) {
        if (status != null) return reservationMapper.findByUserIdAndStatus(userId, status);
        return reservationMapper.findByUserId(userId);
    }

    @Override
    public Reservation getById(Long reservationId) {
        return reservationMapper.selectById(reservationId);
    }

    @Override
    public List<Reservation> getActiveReservations() {
        return reservationMapper.findActiveReservations();
    }
}
