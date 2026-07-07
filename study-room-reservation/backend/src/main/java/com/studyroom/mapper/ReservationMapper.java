package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.Reservation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationMapper extends BaseMapper<Reservation> {

    /** 统计用户今日预约次数（status: 0已预约/1已签到） */
    @Select("SELECT COUNT(*) FROM reservation WHERE user_id = #{userId} " +
            "AND status IN (0, 1) AND DATE(create_time) = CURRENT_DATE")
    int countTodayReservations(@Param("userId") Long userId);

    /** 检查座位在指定时间段内是否有冲突预约 */
    @Select("SELECT COUNT(*) FROM reservation WHERE seat_id = #{seatId} " +
            "AND status IN (0, 1) " +
            "AND NOT (end_time <= #{startTime} OR start_time >= #{endTime})")
    int findConflict(@Param("seatId") Long seatId,
                     @Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime);

    /** 查询所有活跃预约 */
    @Select("SELECT * FROM reservation WHERE status IN (0, 1) ORDER BY create_time DESC")
    List<Reservation> findActiveReservations();

    /** 按用户ID查询预约列表 */
    @Select("SELECT * FROM reservation WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Reservation> findByUserId(@Param("userId") Long userId);

    /** 按用户ID和状态筛选预约 */
    @Select("<script>SELECT * FROM reservation WHERE user_id = #{userId} " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "ORDER BY create_time DESC</script>")
    List<Reservation> findByUserIdAndStatus(@Param("userId") Long userId,
                                             @Param("status") Integer status);
}
