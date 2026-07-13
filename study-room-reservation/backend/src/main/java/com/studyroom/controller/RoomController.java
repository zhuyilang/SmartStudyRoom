package com.studyroom.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.common.Result;
import com.studyroom.entity.Room;
import com.studyroom.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "自习室管理", description = "自习室的增删改查")
@RestController
@RequestMapping("/api/admin/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Operation(summary = "分页查询自习室")
    @GetMapping("/list")
    public Result<IPage<Room>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) String name) {
        Page<Room> page = new Page<>(current, size);
        return Result.success(roomService.pageRoom(page, floorId, name));
    }

    @Operation(summary = "根据ID查询自习室")
    @GetMapping("/{id}")
    public Result<Room> getById(@PathVariable Long id) {
        return Result.success(roomService.getById(id));
    }

    @Operation(summary = "新增自习室")
    @PostMapping
    public Result<?> add(@RequestBody Room room) {
        roomService.add(room);
        return Result.success("新增成功");
    }

    @Operation(summary = "修改自习室")
    @PutMapping
    public Result<?> update(@RequestBody Room room) {
        roomService.update(room);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除自习室")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        roomService.delete(id);
        return Result.success("删除成功");
    }
}