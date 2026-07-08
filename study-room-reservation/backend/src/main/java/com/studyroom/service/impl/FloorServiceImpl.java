package com.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.common.BusinessException;
import com.studyroom.entity.Floor;
import com.studyroom.mapper.FloorMapper;
import com.studyroom.service.FloorService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FloorServiceImpl extends ServiceImpl<FloorMapper, Floor> implements FloorService {

    @Override
    public IPage<Floor> pageFloor(Page<Floor> page, Long buildingId) {
        LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
        if (buildingId != null) {
            wrapper.eq(Floor::getBuildingId, buildingId);
        }
        wrapper.orderByAsc(Floor::getFloorNumber);
        return this.page(page, wrapper);
    }

    @Override
    public Floor getById(Long id) {
        Floor floor = super.getById(id);
        if (floor == null) {
            throw new BusinessException("楼层不存在");
        }
        return floor;
    }

    @Override
    public void add(Floor floor) {
        floor.setCreateTime(LocalDateTime.now());
        this.save(floor);
    }

    @Override
    public void update(Floor floor) {
        floor.setUpdateTime(LocalDateTime.now());
        this.updateById(floor);
    }

    @Override
    public void delete(Long id) {
        // TODO: 检查该楼层下是否有座位，如果有则不允许删除
        this.removeById(id);
    }
}