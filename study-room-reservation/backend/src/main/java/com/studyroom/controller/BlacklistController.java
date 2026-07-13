package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.Blacklist;
import com.studyroom.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "黑名单管理", description = "黑名单查询与解除")
@RestController
@RequestMapping("/api/admin/blacklist")
public class BlacklistController {

    @Autowired
    private BlacklistService blacklistService;

    @Operation(summary = "获取黑名单列表")
    @GetMapping("/list")
    public Result<List<Blacklist>> list() {
        return Result.success(blacklistService.getActiveList());
    }

    @Operation(summary = "解除黑名单")
    @PostMapping("/remove/{userId}")
    public Result<?> removeBan(@PathVariable Long userId) {
        blacklistService.removeBan(userId);
        return Result.success("解除成功");
    }
}
