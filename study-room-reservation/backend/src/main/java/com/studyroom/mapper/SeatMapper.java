package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.Seat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeatMapper extends BaseMapper<Seat> {
}