package com.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.common.BusinessException;
import com.studyroom.entity.Seat;
import com.studyroom.mapper.SeatMapper;
import com.studyroom.service.SeatService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements SeatService {

    @Override
    public IPage<Seat> pageSeat(Page<Seat> page, Long floorId, Integer status, String type) {
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        if (floorId != null) {
            wrapper.eq(Seat::getFloorId, floorId);
        }
        if (status != null) {
            wrapper.eq(Seat::getStatus, status);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Seat::getType, type);
        }
        wrapper.orderByAsc(Seat::getSeatNumber);
        return this.page(page, wrapper);
    }

    @Override
    public Seat getById(Long id) {
        Seat seat = super.getById(id);
        if (seat == null) {
            throw new BusinessException("座位不存在");
        }
        return seat;
    }

    @Override
    public void add(Seat seat) {
        seat.setCreateTime(LocalDateTime.now());
        this.save(seat);
    }

    @Override
    public void update(Seat seat) {
        seat.setUpdateTime(LocalDateTime.now());
        this.updateById(seat);
    }

    @Override
    public void delete(Long id) {
        // TODO: 检查该座位是否有未结束的预约（等C同学接口好了再补）
        this.removeById(id);
    }

    @Override
    public void batchCreate(Long floorId, int count, String type) {
        // 检查该楼层下已存在的座位数
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Seat::getFloorId, floorId);
        long existCount = this.count(wrapper);

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Seat seat = new Seat();
            seat.setFloorId(floorId);
            // 编号从现有数量+1开始
            int number = (int) existCount + i;
            seat.setSeatNumber(String.format("%02d", number));
            seat.setStatus(0); // 0空闲
            seat.setType(type != null ? type : "普通");
            seat.setCreateTime(LocalDateTime.now());
            seats.add(seat);
        }
        this.saveBatch(seats);
    }
}