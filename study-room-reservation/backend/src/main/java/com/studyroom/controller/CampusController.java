package com.studyroom.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.common.Result;
import com.studyroom.entity.Campus;
import com.studyroom.service.CampusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "校区管理", description = "校区的增删改查")
@RestController
@RequestMapping("/api/v1/campus")
public class CampusController {

    @Autowired
    private CampusService campusService;

    @Operation(summary = "分页查询校区")
    @GetMapping("/page")
    public Result<IPage<Campus>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        Page<Campus> page = new Page<>(current, size);
        return Result.success(campusService.pageCampus(page, name));
    }

    @Operation(summary = "根据ID查询校区")
    @GetMapping("/{id}")
    public Result<Campus> getById(@PathVariable Long id) {
        return Result.success(campusService.getById(id));
    }

    @Operation(summary = "新增校区")
    @PostMapping
    public Result<?> add(@RequestBody Campus campus) {
        campusService.add(campus);
        return Result.success("新增成功");
    }

    @Operation(summary = "修改校区")
    @PutMapping
    public Result<?> update(@RequestBody Campus campus) {
        campusService.update(campus);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除校区")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        campusService.delete(id);
        return Result.success("删除成功");
    }
}