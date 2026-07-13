package com.studyroom;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.studyroom.mapper")
@EnableScheduling
public class StudyRoomApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyRoomApplication.class, args);
        System.out.println("========================================");
        System.out.println("  自习室预约系统启动成功！");
        System.out.println("  接口文档: http://localhost:8081/doc.html");
        System.out.println("========================================");
    }
}
