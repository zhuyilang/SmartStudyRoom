package com.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seat")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long floorId;
    private Long roomId;          // ← 加上这一行！
    private String seatNumber;
    private Integer status;
    private String type;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}