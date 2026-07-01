package com.studyroom.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeatReleaseTask {

    /**
     * 每5分钟检查一次超时未签到的预约，自动释放座位
     * TODO: 第二周由 A 完善，目前仅打印日志
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void releaseTimeoutSeats() {
        log.info("=== 定时任务：检查超时预约 ===");
        // TODO: 查询 reservation 表
        //   WHERE status = '已预约'
        //   AND start_time + 15分钟 < NOW()
        //   AND sign_time IS NULL
        // 将状态改为"已超时"，座位改为"空闲"，记录爽约次数
    }
}
