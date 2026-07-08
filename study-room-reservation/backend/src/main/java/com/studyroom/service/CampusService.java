package com.studyroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.entity.Campus;

public interface CampusService {
    IPage<Campus> pageCampus(Page<Campus> page, String name);
    Campus getById(Long id);
    void add(Campus campus);
    void update(Campus campus);
    void delete(Long id);
}