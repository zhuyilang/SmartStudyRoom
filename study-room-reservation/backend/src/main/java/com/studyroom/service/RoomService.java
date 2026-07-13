package com.studyroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.entity.Room;

public interface RoomService {
    IPage<Room> pageRoom(Page<Room> page, Long floorId, String name);
    Room getById(Long id);
    void add(Room room);
    void update(Room room);
    void delete(Long id);
}