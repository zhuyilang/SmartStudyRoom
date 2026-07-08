package com.studyroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;  // 加上这个 import
import com.studyroom.entity.Floor;

public interface FloorService extends IService<Floor> {  // 加上继承
    IPage<Floor> pageFloor(Page<Floor> page, Long buildingId);
    Floor getById(Long id);
    void add(Floor floor);
    void update(Floor floor);
    void delete(Long id);
}