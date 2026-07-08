package com.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("room")
public class Room {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long floorId;        // 所属楼层ID
    private String name;         // 自习室名称（如：301教室、电子阅览室）
    private Integer rowCount;    // 行数
    private Integer colCount;    // 列数
    private Integer status;      // 状态：0停用 1启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}