package com.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("floor")
public class Floor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long buildingId;
    private Integer floorNumber;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}