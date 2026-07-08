package com.studyroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;  // 加上这个
import com.studyroom.entity.Building;

public interface BuildingService extends IService<Building> {  // 继承 IService
    IPage<Building> pageBuilding(Page<Building> page, Long campusId, String name);
    Building getById(Long id);
    void add(Building building);
    void update(Building building);
    void delete(Long id);
}