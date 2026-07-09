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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Autowired
    private SeatService seatService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
            int rows = room.getRowCount();
            int cols = room.getColCount();
            // 使用 JDBC 批量插入座位（row_num, col_num）
            String sql = "INSERT INTO seat (room_id, row_num, col_num, status) VALUES (?, ?, ?, 0)";
            List<Object[]> batchArgs = new ArrayList<>();
            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= cols; c++) {
                    batchArgs.add(new Object[]{room.getId(), r, c});
                }
            }
            jdbcTemplate.batchUpdate(sql, batchArgs);
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