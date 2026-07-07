package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.Blacklist;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface BlacklistMapper extends BaseMapper<Blacklist> {

    /** Find active ban for a user */
    @Select("SELECT * FROM blacklist WHERE user_id = #{userId} AND banned_until > CURRENT_TIMESTAMP")
    Blacklist findActiveBan(@Param("userId") Long userId);

    /** Increment miss count */
    @Update("UPDATE blacklist SET miss_count = miss_count + 1 WHERE user_id = #{userId}")
    int incrementMissCount(@Param("userId") Long userId);
}
