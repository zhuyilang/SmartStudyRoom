package com.studyroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("floor")
public class Floor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long buildingId;
    private Integer floorNumber;
    private String name;
    private String description;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}