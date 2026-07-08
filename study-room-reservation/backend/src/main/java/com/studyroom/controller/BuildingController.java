package com.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.common.Result;
import com.studyroom.entity.Building;
import com.studyroom.service.BuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "楼栋管理", description = "楼栋的增删改查")
@RestController
@RequestMapping("/api/v1/building")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    @Operation(summary = "分页查询楼栋")
    @GetMapping("/page")
    public Result<IPage<Building>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String name) {
        Page<Building> page = new Page<>(current, size);
        return Result.success(buildingService.pageBuilding(page, campusId, name));
    }

    @Operation(summary = "根据ID查询楼栋")
    @GetMapping("/{id}")
    public Result<Building> getById(@PathVariable Long id) {
        return Result.success(buildingService.getById(id));
    }

    @Operation(summary = "新增楼栋")
    @PostMapping
    public Result<?> add(@RequestBody Building building) {
        buildingService.add(building);
        return Result.success("新增成功");
    }

    @Operation(summary = "修改楼栋")
    @PutMapping
    public Result<?> update(@RequestBody Building building) {
        buildingService.update(building);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除楼栋")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "根据校区ID查询楼栋列表（下拉用）")
    @GetMapping("/list/{campusId}")
    public Result<List<Building>> listByCampus(@PathVariable Long campusId) {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Building::getCampusId, campusId);
        wrapper.eq(Building::getStatus, 1);
        wrapper.orderByAsc(Building::getName);
        return Result.success(buildingService.list(wrapper));
    }
}