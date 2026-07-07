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

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

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
    public Reservation reserve(Long userId, Long seatId, Long roomId,
                               String startTime, String endTime) {
        // 1. 检查黑名单
        if (blacklistService.isBanned(userId)) {
            throw new BusinessException("您因爽约已被限制预约，请联系管理员");
        }

        // 2. 检查每日预约次数
        int todayCount = reservationMapper.countTodayReservations(userId);
        if (todayCount >= MAX_DAILY_RESERVATIONS) {
            throw new BusinessException("今日预约次数已达上限（" + MAX_DAILY_RESERVATIONS + "次）");
        }

        // 3. 校验时间合法性（兼容 yyyy-MM-ddTHH:mm 和 yyyy-MM-ddTHH:mm:ss）
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        if (start.isBefore(LocalDateTime.now())) {
            throw new BusinessException("预约开始时间不能早于当前时间");
        }
        if (end.isBefore(start)) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new BusinessException("不允许跨天预约");
        }

        // 4. 校验自习室开放时间
        try {
            Map<String, Object> room = jdbcTemplate.queryForMap(
                    "SELECT open_time, close_time FROM room WHERE id = ?", roomId);
            Time openTime = (Time) room.get("open_time");
            Time closeTime = (Time) room.get("close_time");
            LocalTime startLocalTime = start.toLocalTime();
            LocalTime endLocalTime = end.toLocalTime();
            if (openTime != null && startLocalTime.isBefore(openTime.toLocalTime())) {
                throw new BusinessException("预约时间早于自习室开放时间（" + openTime + "）");
            }
            if (closeTime != null && endLocalTime.isAfter(closeTime.toLocalTime())) {
                throw new BusinessException("预约时间晚于自习室关闭时间（" + closeTime + "）");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // room not found or no time set, skip check
            log.debug("Room open time check skipped: {}", e.getMessage());
        }

        // 5. 检查时间段冲突
        int conflict = reservationMapper.findConflict(seatId, start, end);
        if (conflict > 0) {
            throw new BusinessException("该时段已被预约，请选择其他时间");
        }

        // 6. 创建预约
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setSeatId(seatId);
        reservation.setRoomId(roomId);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(Reservation.STATUS_BOOKED);
        reservation.setCreateTime(LocalDateTime.now());

        reservationMapper.insert(reservation);

        // 7. 更新座位状态为已占用
        seatStatusService.occupySeat(seatId);

        log.info("预约成功: userId={}, seatId={}, roomId={}, start={}, end={}",
                userId, seatId, roomId, startTime, endTime);
        return reservation;
    }

    @Override
    @Transactional
    public void cancel(Long reservationId, Long userId) {
        Reservation r = reservationMapper.selectById(reservationId);
        if (r == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!r.getUserId().equals(userId)) {
            throw new BusinessException("只能取消自己的预约");
        }
        if (r.getStatus() != Reservation.STATUS_BOOKED &&
            r.getStatus() != Reservation.STATUS_SIGNED_IN) {
            throw new BusinessException("当前状态不允许取消");
        }

        r.setStatus(Reservation.STATUS_CANCELLED);
        reservationMapper.updateById(r);

        // 释放座位
        seatStatusService.releaseSeat(r.getSeatId());

        log.info("预约已取消: id={}, userId={}", reservationId, userId);
    }

    @Override
    @Transactional
    public void signIn(Long reservationId, Long userId) {
        Reservation r = reservationMapper.selectById(reservationId);
        if (r == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!r.getUserId().equals(userId)) {
            throw new BusinessException("只能签到自己的预约");
        }
        if (r.getStatus() != Reservation.STATUS_BOOKED) {
            throw new BusinessException("预约状态异常，无法签到");
        }

        LocalDateTime now = LocalDateTime.now();
        // 签到窗口：startTime 前后15分钟
        if (now.isBefore(r.getStartTime().minusMinutes(15))) {
            throw new BusinessException("签到时间未到，请在预约开始前15分钟内签到");
        }
        if (now.isAfter(r.getStartTime().plusMinutes(15))) {
            throw new BusinessException("已超过签到时间（开始后15分钟），预约已自动释放");
        }

        r.setStatus(Reservation.STATUS_SIGNED_IN);
        r.setSignTime(now);
        reservationMapper.updateById(r);

        log.info("签到成功: id={}, userId={}", reservationId, userId);
    }

    @Override
    public List<Reservation> getMyReservations(Long userId, Integer status) {
        if (status != null) {
            return reservationMapper.findByUserIdAndStatus(userId, status);
        }
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

    /** Parse datetime string, handles both "yyyy-MM-ddTHH:mm" and "yyyy-MM-ddTHH:mm:ss" */
    private LocalDateTime parseDateTime(String dt) {
        if (dt == null || dt.isBlank()) {
            throw new BusinessException("时间不能为空");
        }
        try {
            return LocalDateTime.parse(dt);
        } catch (DateTimeParseException e) {
            // Try adding seconds
            if (dt.length() == 16 && dt.charAt(10) == 'T') {
                return LocalDateTime.parse(dt + ":00");
            }
            throw new BusinessException("时间格式不正确，请使用 yyyy-MM-ddTHH:mm 格式");
        }
    }
}
