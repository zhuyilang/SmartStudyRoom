package com.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.common.Result;
import com.studyroom.entity.Floor;
import com.studyroom.service.FloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "楼层管理", description = "楼层的增删改查")
@RestController
@RequestMapping("/api/admin/floor")
public class FloorController {

    @Autowired
    private FloorService floorService;

    @Operation(summary = "分页查询楼层")
    @GetMapping("/list")
    public Result<IPage<Floor>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long buildingId) {
        Page<Floor> page = new Page<>(current, size);
        return Result.success(floorService.pageFloor(page, buildingId));
    }

    @Operation(summary = "根据ID查询楼层")
    @GetMapping("/{id}")
    public Result<Floor> getById(@PathVariable Long id) {
        return Result.success(floorService.getById(id));
    }

    @Operation(summary = "新增楼层")
    @PostMapping
    public Result<?> add(@RequestBody Floor floor) {
        floorService.add(floor);
        return Result.success("新增成功");
    }

    @Operation(summary = "修改楼层")
    @PutMapping
    public Result<?> update(@RequestBody Floor floor) {
        floorService.update(floor);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除楼层")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        floorService.delete(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "根据楼栋ID查询楼层列表（下拉用）")
    @GetMapping("/list/{buildingId}")
    public Result<List<Floor>> listByBuilding(@PathVariable Long buildingId) {
        LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Floor::getBuildingId, buildingId);
        wrapper.orderByAsc(Floor::getFloorNumber);
        return Result.success(floorService.list(wrapper));
    }
}