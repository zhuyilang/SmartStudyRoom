package com.studyroom.controller;

import com.studyroom.common.BusinessException;
import com.studyroom.common.Result;
import com.studyroom.entity.Reservation;
import com.studyroom.entity.User;
import com.studyroom.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Tag(name = "预约管理", description = "座位预约、签到、取消")
@RestController
@RequestMapping("/api/student/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Operation(summary = "创建预约")
    @PostMapping("/reserve")
    public Result<Reservation> reserve(@RequestBody Map<String, Object> body,
                                        HttpSession session) {
        Long userId = getUserId(session);
        Long seatId = Long.valueOf(body.get("seatId").toString());
        Long roomId = Long.valueOf(body.get("roomId").toString());

        Reservation r = reservationService.reserve(userId, seatId, roomId);
        return Result.success("预约成功", r);
    }

    @Operation(summary = "取消预约")
    @PostMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id, HttpSession session) {
        Long userId = getUserId(session);
        reservationService.cancel(id, userId);
        return Result.success("取消成功");
    }

    @Operation(summary = "签到")
    @PostMapping("/signIn/{id}")
    public Result<?> signIn(@PathVariable Long id, HttpSession session) {
        Long userId = getUserId(session);
        reservationService.signIn(id, userId);
        return Result.success("签到成功");
    }

    @Operation(summary = "释放座位")
    @PostMapping("/release/{id}")
    public Result<?> release(@PathVariable Long id, HttpSession session) {
        Long userId = getUserId(session);
        reservationService.release(id, userId);
        return Result.success("座位已释放");
    }

    @Operation(summary = "我的预约列表")
    @GetMapping("/my")
    public Result<?> myReservations(@RequestParam(required = false) Integer status,
                                     HttpSession session) {
        Long userId = getUserId(session);
        return Result.success(reservationService.getMyReservations(userId, status));
    }

    private Long getUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user.getId();
    }
}
