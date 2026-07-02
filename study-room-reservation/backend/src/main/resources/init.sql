-- ============================================
-- 校园自习室预约系统 - 数据库初始化脚本
-- 使用前请先创建数据库: CREATE DATABASE study_room;
-- ============================================

USE study_room;

-- 1. 用户表
DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（MD5加盐）',
    real_name   VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    role        VARCHAR(20)  NOT NULL DEFAULT 'STUDENT' COMMENT '角色: STUDENT/ADMIN',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) COMMENT '用户表';

-- 2. 校区表
DROP TABLE IF EXISTS campus;
CREATE TABLE campus (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '校区ID',
    name        VARCHAR(50) NOT NULL COMMENT '校区名称',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP
) COMMENT '校区表';

-- 3. 楼栋表
DROP TABLE IF EXISTS building;
CREATE TABLE building (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '楼栋ID',
    campus_id   BIGINT      NOT NULL COMMENT '所属校区',
    name        VARCHAR(100) NOT NULL COMMENT '楼栋名称',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (campus_id) REFERENCES campus(id)
) COMMENT '楼栋表';

-- 4. 楼层表
DROP TABLE IF EXISTS floor;
CREATE TABLE floor (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '楼层ID',
    building_id  BIGINT      NOT NULL COMMENT '所属楼栋',
    floor_number INT         NOT NULL COMMENT '楼层号',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (building_id) REFERENCES building(id)
) COMMENT '楼层表';

-- 5. 自习室表
DROP TABLE IF EXISTS room;
CREATE TABLE room (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自习室ID',
    floor_id    BIGINT      NOT NULL COMMENT '所属楼层',
    name        VARCHAR(100) NOT NULL COMMENT '自习室名称',
    total_rows  INT         NOT NULL DEFAULT 5 COMMENT '座位行数',
    total_cols  INT         NOT NULL DEFAULT 4 COMMENT '座位列数',
    open_time   TIME        DEFAULT '08:00:00' COMMENT '开放时间',
    close_time  TIME        DEFAULT '22:00:00' COMMENT '关闭时间',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (floor_id) REFERENCES floor(id)
) COMMENT '自习室表';

-- 6. 座位表
DROP TABLE IF EXISTS seat;
CREATE TABLE seat (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '座位ID',
    room_id     BIGINT      NOT NULL COMMENT '所属自习室',
    row_num     INT         NOT NULL COMMENT '行号',
    col_num     INT         NOT NULL COMMENT '列号',
    status      TINYINT     DEFAULT 0 COMMENT '状态: 0空闲 1已占用 2维修中',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES room(id)
) COMMENT '座位表';

-- 7. 预约表
DROP TABLE IF EXISTS reservation;
CREATE TABLE reservation (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预约ID',
    user_id     BIGINT      NOT NULL COMMENT '用户ID',
    seat_id     BIGINT      NOT NULL COMMENT '座位ID',
    room_id     BIGINT      NOT NULL COMMENT '自习室ID（冗余，方便查询）',
    start_time  DATETIME    NOT NULL COMMENT '预约开始时间',
    end_time    DATETIME    NOT NULL COMMENT '预约结束时间',
    status      TINYINT     DEFAULT 0 COMMENT '状态: 0已预约 1已签到 2已取消 3已超时 4已完成',
    sign_time   DATETIME    DEFAULT NULL COMMENT '签到时间',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (seat_id) REFERENCES seat(id),
    FOREIGN KEY (room_id) REFERENCES room(id)
) COMMENT '预约表';

-- 8. 黑名单表
DROP TABLE IF EXISTS blacklist;
CREATE TABLE blacklist (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id      BIGINT      NOT NULL UNIQUE COMMENT '用户ID',
    miss_count   INT         DEFAULT 1 COMMENT '累计爽约次数',
    banned_until DATETIME    DEFAULT NULL COMMENT '禁止预约截止时间',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
) COMMENT '黑名单表';

-- ============================================
-- 测试数据
-- ============================================

-- 管理员账号: admin / 123456
INSERT INTO user (username, password, real_name, role) VALUES
('admin', '0fefba348b72c84c950cf8d08af6d46e', '系统管理员', 'ADMIN');

-- 测试学生: 密码都是 123456
INSERT INTO user (username, password, real_name, role) VALUES
('zhangsan', '0fefba348b72c84c950cf8d08af6d46e', '张三', 'STUDENT'),
('lisi', '0fefba348b72c84c950cf8d08af6d46e', '李四', 'STUDENT'),
('wangwu', '0fefba348b72c84c950cf8d08af6d46e', '王五', 'STUDENT');

-- 测试校区
INSERT INTO campus (name) VALUES ('主校区'), ('东校区');

-- 测试楼栋
INSERT INTO building (campus_id, name) VALUES
(1, '图书馆'), (1, '第一教学楼'),
(2, '东区图书馆'), (2, '东区教学楼');

-- 测试楼层
INSERT INTO floor (building_id, floor_number) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2),
(3, 1), (3, 2),
(4, 1);

-- 测试自习室
INSERT INTO room (floor_id, name, total_rows, total_cols) VALUES
(1, '图书馆101', 5, 6),
(1, '图书馆102', 4, 5),
(2, '图书馆201', 6, 6),
(4, '一教101', 5, 4),
(4, '一教102', 4, 5);

-- 批量生成座位（存储过程）
DELIMITER //
CREATE PROCEDURE generate_seats()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_room_id BIGINT;
    DECLARE v_rows INT;
    DECLARE v_cols INT;
    DECLARE i INT;
    DECLARE j INT;
    DECLARE cur CURSOR FOR SELECT id, total_rows, total_cols FROM room;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_room_id, v_rows, v_cols;
        IF done THEN LEAVE read_loop; END IF;

        SET i = 1;
        WHILE i <= v_rows DO
            SET j = 1;
            WHILE j <= v_cols DO
                INSERT INTO seat (room_id, row_num, col_num, status) VALUES (v_room_id, i, j, 0);
                SET j = j + 1;
            END WHILE;
            SET i = i + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

CALL generate_seats();
DROP PROCEDURE IF EXISTS generate_seats;

SELECT '数据库初始化完成！' AS message;
