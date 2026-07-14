CREATE DATABASE IF NOT EXISTS study_room DEFAULT CHARACTER SET utf8mb4;
USE study_room;
/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : study_room

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 14/07/2026 21:00:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for blacklist
-- ----------------------------
DROP TABLE IF EXISTS `blacklist`;
CREATE TABLE `blacklist`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `miss_count` int NULL DEFAULT 1 COMMENT '累计爽约次数',
  `banned_until` datetime NULL DEFAULT NULL COMMENT '禁止预约截止时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `blacklist_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '黑名单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blacklist
-- ----------------------------

-- ----------------------------
-- Table structure for building
-- ----------------------------
DROP TABLE IF EXISTS `building`;
CREATE TABLE `building`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '楼栋ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '楼栋名称',
  `floor_count` int NOT NULL DEFAULT 0 COMMENT '楼层数',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '校区描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '上次更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `campus_id`(`campus_id` ASC) USING BTREE,
  CONSTRAINT `building_ibfk_1` FOREIGN KEY (`campus_id`) REFERENCES `campus` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '楼栋表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of building
-- ----------------------------
INSERT INTO `building` VALUES (1, 2, '学武楼', 5, '1号楼', '2026-07-14 20:56:14', NULL);
INSERT INTO `building` VALUES (2, 2, '文宣楼', 5, '4号楼', '2026-07-14 20:56:27', NULL);
INSERT INTO `building` VALUES (3, 1, '颂恩楼', 5, '主楼', '2026-07-14 20:56:53', NULL);
INSERT INTO `building` VALUES (4, 1, '祖营楼', 5, '', '2026-07-14 20:57:08', NULL);

-- ----------------------------
-- Table structure for campus
-- ----------------------------
DROP TABLE IF EXISTS `campus`;
CREATE TABLE `campus`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '校区ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '校区名称',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '校区地址',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '校区描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '上次更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '校区表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of campus
-- ----------------------------
INSERT INTO `campus` VALUES (1, '思明校区', '福建省厦门市思明区思明南路422号', '主校区', '2026-07-14 20:55:20', NULL);
INSERT INTO `campus` VALUES (2, '翔安校区', '福建省厦门市翔安区翔安南路4221号', '分校区', '2026-07-14 20:55:53', NULL);

-- ----------------------------
-- Table structure for floor
-- ----------------------------
DROP TABLE IF EXISTS `floor`;
CREATE TABLE `floor`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '楼层ID',
  `building_id` bigint NOT NULL COMMENT '所属楼栋',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '楼层名称',
  `floor_number` int NOT NULL COMMENT '楼层号',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '楼层描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '上次更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `building_id`(`building_id` ASC) USING BTREE,
  CONSTRAINT `floor_ibfk_1` FOREIGN KEY (`building_id`) REFERENCES `building` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '楼层表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of floor
-- ----------------------------
INSERT INTO `floor` VALUES (1, 1, '1层', 1, '', '2026-07-14 20:57:20', NULL);
INSERT INTO `floor` VALUES (2, 2, '4层', 4, '', '2026-07-14 20:57:32', NULL);
INSERT INTO `floor` VALUES (3, 4, '1层', 1, '', '2026-07-14 20:57:43', NULL);
INSERT INTO `floor` VALUES (4, 3, '3层', 3, '', '2026-07-14 20:57:57', NULL);

-- ----------------------------
-- Table structure for reservation
-- ----------------------------
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `seat_id` bigint NOT NULL COMMENT '座位ID',
  `room_id` bigint NOT NULL COMMENT '自习室ID（冗余，方便查询）',
  `start_time` datetime NOT NULL COMMENT '预约开始时间',
  `end_time` datetime NOT NULL COMMENT '预约结束时间',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0已预约 1已签到 2已取消 3已超时 4已完成',
  `sign_time` datetime NULL DEFAULT NULL COMMENT '签到时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `seat_id`(`seat_id` ASC) USING BTREE,
  INDEX `room_id`(`room_id` ASC) USING BTREE,
  CONSTRAINT `reservation_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `reservation_ibfk_2` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `reservation_ibfk_3` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reservation
-- ----------------------------

-- ----------------------------
-- Table structure for room
-- ----------------------------
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room`  (
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自习室描述',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自习室ID',
  `floor_id` bigint NOT NULL COMMENT '所属楼层',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '自习室名称',
  `row_count` int NOT NULL DEFAULT 5 COMMENT '座位行数',
  `col_count` int NOT NULL DEFAULT 4 COMMENT '座位列数',
  `type` int NOT NULL DEFAULT 0 COMMENT '0静音1讨论',
  `has_power` int NOT NULL DEFAULT 1 COMMENT '有电源',
  `has_network` int NOT NULL DEFAULT 1 COMMENT '有网络',
  `has_computer` int NOT NULL DEFAULT 0 COMMENT '有电脑',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `floor_id`(`floor_id` ASC) USING BTREE,
  CONSTRAINT `room_ibfk_1` FOREIGN KEY (`floor_id`) REFERENCES `floor` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '自习室表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of room
-- ----------------------------
INSERT INTO `room` VALUES ('', 1, 1, '101', 5, 6, 2, 1, 0, 1, '2026-07-14 20:58:07', NULL);
INSERT INTO `room` VALUES ('', 2, 2, '402', 4, 4, 1, 0, 1, 1, '2026-07-14 20:58:24', NULL);
INSERT INTO `room` VALUES ('', 3, 3, '101', 9, 8, 2, 1, 1, 1, '2026-07-14 20:58:36', NULL);
INSERT INTO `room` VALUES ('', 4, 4, '301', 5, 6, 1, 1, 1, 0, '2026-07-14 20:58:50', NULL);

-- ----------------------------
-- Table structure for seat
-- ----------------------------
DROP TABLE IF EXISTS `seat`;
CREATE TABLE `seat`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '座位ID',
  `room_id` bigint NOT NULL COMMENT '所属自习室',
  `row_num` int NOT NULL COMMENT '行号',
  `col_num` int NOT NULL COMMENT '列号',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0空闲 1已占用 2维修中',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `room_id`(`room_id` ASC) USING BTREE,
  CONSTRAINT `seat_ibfk_1` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 149 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '座位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of seat
-- ----------------------------
INSERT INTO `seat` VALUES (1, 1, 1, 1, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (2, 1, 1, 2, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (3, 1, 1, 3, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (4, 1, 1, 4, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (5, 1, 1, 5, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (6, 1, 1, 6, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (7, 1, 2, 1, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (8, 1, 2, 2, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (9, 1, 2, 3, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (10, 1, 2, 4, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (11, 1, 2, 5, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (12, 1, 2, 6, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (13, 1, 3, 1, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (14, 1, 3, 2, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (15, 1, 3, 3, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (16, 1, 3, 4, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (17, 1, 3, 5, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (18, 1, 3, 6, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (19, 1, 4, 1, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (20, 1, 4, 2, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (21, 1, 4, 3, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (22, 1, 4, 4, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (23, 1, 4, 5, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (24, 1, 4, 6, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (25, 1, 5, 1, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (26, 1, 5, 2, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (27, 1, 5, 3, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (28, 1, 5, 4, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (29, 1, 5, 5, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (30, 1, 5, 6, 0, '2026-07-14 20:58:07');
INSERT INTO `seat` VALUES (31, 2, 1, 1, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (32, 2, 1, 2, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (33, 2, 1, 3, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (34, 2, 1, 4, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (35, 2, 2, 1, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (36, 2, 2, 2, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (37, 2, 2, 3, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (38, 2, 2, 4, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (39, 2, 3, 1, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (40, 2, 3, 2, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (41, 2, 3, 3, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (42, 2, 3, 4, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (43, 2, 4, 1, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (44, 2, 4, 2, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (45, 2, 4, 3, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (46, 2, 4, 4, 0, '2026-07-14 20:58:24');
INSERT INTO `seat` VALUES (47, 3, 1, 1, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (48, 3, 1, 2, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (49, 3, 1, 3, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (50, 3, 1, 4, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (51, 3, 1, 5, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (52, 3, 1, 6, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (53, 3, 1, 7, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (54, 3, 1, 8, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (55, 3, 2, 1, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (56, 3, 2, 2, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (57, 3, 2, 3, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (58, 3, 2, 4, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (59, 3, 2, 5, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (60, 3, 2, 6, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (61, 3, 2, 7, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (62, 3, 2, 8, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (63, 3, 3, 1, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (64, 3, 3, 2, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (65, 3, 3, 3, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (66, 3, 3, 4, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (67, 3, 3, 5, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (68, 3, 3, 6, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (69, 3, 3, 7, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (70, 3, 3, 8, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (71, 3, 4, 1, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (72, 3, 4, 2, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (73, 3, 4, 3, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (74, 3, 4, 4, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (75, 3, 4, 5, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (76, 3, 4, 6, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (77, 3, 4, 7, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (78, 3, 4, 8, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (79, 3, 5, 1, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (80, 3, 5, 2, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (81, 3, 5, 3, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (82, 3, 5, 4, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (83, 3, 5, 5, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (84, 3, 5, 6, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (85, 3, 5, 7, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (86, 3, 5, 8, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (87, 3, 6, 1, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (88, 3, 6, 2, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (89, 3, 6, 3, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (90, 3, 6, 4, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (91, 3, 6, 5, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (92, 3, 6, 6, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (93, 3, 6, 7, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (94, 3, 6, 8, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (95, 3, 7, 1, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (96, 3, 7, 2, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (97, 3, 7, 3, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (98, 3, 7, 4, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (99, 3, 7, 5, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (100, 3, 7, 6, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (101, 3, 7, 7, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (102, 3, 7, 8, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (103, 3, 8, 1, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (104, 3, 8, 2, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (105, 3, 8, 3, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (106, 3, 8, 4, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (107, 3, 8, 5, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (108, 3, 8, 6, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (109, 3, 8, 7, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (110, 3, 8, 8, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (111, 3, 9, 1, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (112, 3, 9, 2, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (113, 3, 9, 3, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (114, 3, 9, 4, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (115, 3, 9, 5, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (116, 3, 9, 6, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (117, 3, 9, 7, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (118, 3, 9, 8, 0, '2026-07-14 20:58:36');
INSERT INTO `seat` VALUES (119, 4, 1, 1, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (120, 4, 1, 2, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (121, 4, 1, 3, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (122, 4, 1, 4, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (123, 4, 1, 5, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (124, 4, 1, 6, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (125, 4, 2, 1, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (126, 4, 2, 2, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (127, 4, 2, 3, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (128, 4, 2, 4, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (129, 4, 2, 5, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (130, 4, 2, 6, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (131, 4, 3, 1, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (132, 4, 3, 2, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (133, 4, 3, 3, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (134, 4, 3, 4, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (135, 4, 3, 5, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (136, 4, 3, 6, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (137, 4, 4, 1, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (138, 4, 4, 2, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (139, 4, 4, 3, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (140, 4, 4, 4, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (141, 4, 4, 5, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (142, 4, 4, 6, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (143, 4, 5, 1, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (144, 4, 5, 2, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (145, 4, 5, 3, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (146, 4, 5, 4, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (147, 4, 5, 5, 0, '2026-07-14 20:58:50');
INSERT INTO `seat` VALUES (148, 4, 5, 6, 0, '2026-07-14 20:58:50');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（MD5加盐）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'STUDENT' COMMENT '角色: STUDENT/ADMIN',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '0fefba348b72c84c950cf8d08af6d46e', '系统管理员', 'ADMIN', '2026-07-14 20:54:09');

SET FOREIGN_KEY_CHECKS = 1;
