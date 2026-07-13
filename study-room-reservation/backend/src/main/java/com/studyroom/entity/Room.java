package com.studyroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("room")
public class Room {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long floorId;
    private String name;
    private Integer rowCount;
    private Integer colCount;
    private String description;
    private Integer type;
    private Integer hasPower;
    private Integer hasNetwork;
    private Integer hasComputer;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}