package com.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.common.Result;
import com.studyroom.entity.Seat;
import com.studyroom.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Tag(name = "座位管理", description = "座位的增删改查 + 批量生成")
@RestController
@RequestMapping("/api/v1/seat")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @Operation(summary = "分页查询座位")
    @GetMapping("/page")
    public Result<IPage<Seat>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type) {
        Page<Seat> page = new Page<>(current, size);
        return Result.success(seatService.pageSeat(page, floorId, status, type));
    }

    @Operation(summary = "根据ID查询座位")
    @GetMapping("/{id}")
    public Result<Seat> getById(@PathVariable Long id) {
        return Result.success(seatService.getById(id));
    }

    @Operation(summary = "新增座位")
    @PostMapping
    public Result<?> add(@RequestBody Seat seat) {
        seatService.add(seat);
        return Result.success("新增成功");
    }

    @Operation(summary = "修改座位")
    @PutMapping
    public Result<?> update(@RequestBody Seat seat) {
        seatService.update(seat);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除座位")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        seatService.delete(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量生成座位")
    @PostMapping("/batch")
    public Result<?> batchCreate(@RequestBody Map<String, Object> params) {
        Long floorId = Long.valueOf(params.get("floorId").toString());
        Integer count = Integer.valueOf(params.get("count").toString());
        String type = params.get("type") != null ? params.get("type").toString() : "普通";
        seatService.batchCreate(floorId, count, type);
        return Result.success("批量生成 " + count + " 个座位成功");
    }

    @Operation(summary = "获取座位网格数据")
    @GetMapping("/grid/{roomId}")
    public Result<List<List<Seat>>> getGrid(@PathVariable Long roomId) {
        // 查询该自习室的所有座位
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Seat::getRoomId, roomId);
        wrapper.orderByAsc(Seat::getSeatNumber);
        List<Seat> seats = seatService.list(wrapper);

        // TODO: 按行排列成二维数组
        // 需要 Room 的 rowCount 和 colCount
        // 这里先返回一维列表，前端自己处理
        return Result.success(Collections.singletonList(seats));
    }

    @Operation(summary = "修改座位状态（管理员）")
    @PutMapping("/status")
    public Result<?> updateStatus(@RequestBody Map<String, Object> params) {
        Long seatId = Long.valueOf(params.get("seatId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());

        Seat seat = seatService.getById(seatId);
        if (seat == null) {
            return Result.error(404, "座位不存在");
        }
        seat.setStatus(status);
        seatService.updateById(seat);
        return Result.success("修改成功");
    }
}