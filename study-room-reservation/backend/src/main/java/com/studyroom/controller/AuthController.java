package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.User;
import com.studyroom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Tag(name = "认证模块", description = "登录、注册、退出")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<User> login(@RequestBody User user, HttpSession session) {
        User loginUser = userService.login(user.getUsername(), user.getPassword(), session);
        return Result.success("登录成功", loginUser);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        userService.register(user);
        return Result.success("注册成功");
    }

    @Operation(summary = "退出登录")
    @GetMapping("/logout")
    public Result<?> logout(HttpSession session) {
        session.invalidate();
        return Result.success("已退出");
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/currentUser")
    public Result<User> currentUser(HttpSession session) {
        User user = userService.getCurrentUser(session);
        if (user == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(user);
    }
}
