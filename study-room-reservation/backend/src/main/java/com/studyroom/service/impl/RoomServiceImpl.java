package com.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.common.BusinessException;
import com.studyroom.entity.Room;
import com.studyroom.mapper.RoomMapper;
import com.studyroom.service.RoomService;
import com.studyroom.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Autowired
    private SeatService seatService;

    @Override
    public IPage<Room> pageRoom(Page<Room> page, Long floorId, String name) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        if (floorId != null) {
            wrapper.eq(Room::getFloorId, floorId);
        }
        if (StringUtils.hasText(name)) {
            wrapper.like(Room::getName, name);
        }
        wrapper.orderByDesc(Room::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Room getById(Long id) {
        Room room = super.getById(id);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }
        return room;
    }

    @Override
    public void add(Room room) {
        room.setCreateTime(LocalDateTime.now());
        this.save(room);

        // 自动生成座位（根据 rowCount × colCount）
        if (room.getRowCount() != null && room.getColCount() != null) {
            int totalSeats = room.getRowCount() * room.getColCount();
            // 调用 SeatService 批量生成座位，关联到该自习室
            // TODO: 需要修改 Seat 表，增加 roomId 字段（确认一下表中是否有 roomId）
            // 如果 Seat 表有 roomId，这里调用 seatService.batchCreateByRoom(room.getId(), totalSeats);
        }
    }

    @Override
    public void update(Room room) {
        room.setUpdateTime(LocalDateTime.now());
        this.updateById(room);
    }

    @Override
    public void delete(Long id) {
        // TODO: 检查该自习室下是否有座位，如果有则不允许删除
        this.removeById(id);
    }
}