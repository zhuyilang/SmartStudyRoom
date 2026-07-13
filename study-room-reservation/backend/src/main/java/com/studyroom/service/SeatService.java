package com.studyroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;  // 加上这个 import
import com.studyroom.entity.Seat;

public interface SeatService extends IService<Seat> {  // 加上继承
    IPage<Seat> pageSeat(Page<Seat> page, Long floorId, Integer status, String type);
    Seat getById(Long id);
    void add(Seat seat);
    void update(Seat seat);
    void delete(Long id);
    void batchCreate(Long floorId, int count, String type);
}