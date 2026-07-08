package com.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.common.BusinessException;
import com.studyroom.entity.Building;
import com.studyroom.mapper.BuildingMapper;
import com.studyroom.service.BuildingService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements BuildingService {

    @Override
    public IPage<Building> pageBuilding(Page<Building> page, Long campusId, String name) {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        if (campusId != null) {
            wrapper.eq(Building::getCampusId, campusId);
        }
        if (StringUtils.hasText(name)) {
            wrapper.like(Building::getName, name);
        }
        wrapper.orderByDesc(Building::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Building getById(Long id) {
        Building building = super.getById(id);
        if (building == null) {
            throw new BusinessException("楼栋不存在");
        }
        return building;
    }

    @Override
    public void add(Building building) {
        building.setCreateTime(LocalDateTime.now());
        this.save(building);
    }

    @Override
    public void update(Building building) {
        building.setUpdateTime(LocalDateTime.now());
        this.updateById(building);
    }

    @Override
    public void delete(Long id) {
        // TODO: 检查该楼栋下是否有楼层，如果有则不允许删除
        this.removeById(id);
    }
}