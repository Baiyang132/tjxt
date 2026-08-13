-- =====================================================================
-- 天机学堂 - 学习中心数据库脚本
-- 数据库：tj_learning
-- =====================================================================

CREATE DATABASE IF NOT EXISTS tj_learning DEFAULT CHARACTER SET utf8mb4;
USE tj_learning;

-- ---------------------------------------------------------------------
-- 1. 课表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS learning_lesson;
CREATE TABLE learning_lesson (
  `id`                BIGINT      NOT NULL                 COMMENT '主键',
  `user_id`           BIGINT      NOT NULL                 COMMENT '用户id',
  `course_id`         BIGINT      NOT NULL                 COMMENT '课程id',
  `status`            TINYINT     NOT NULL DEFAULT 0       COMMENT '课程状态：0-未学习，1-学习中，2-已学完，3-已过期',
  `week_freq`         TINYINT     NOT NULL DEFAULT 0       COMMENT '学习计划每周学习天数',
  `plan_status`       TINYINT     NOT NULL DEFAULT 0       COMMENT '学习计划状态：0-没有计划，1-计划进行中',
  `learned_sections`  INT         NOT NULL DEFAULT 0       COMMENT '已学习的小节数',
  `latest_section_id` BIGINT      DEFAULT NULL             COMMENT '最近学习的小节id',
  `latest_learn_time` DATETIME    DEFAULT NULL             COMMENT '最近学习时间',
  `finish_time`       DATETIME    DEFAULT NULL             COMMENT '完成学习的时间',
  `create_time`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（报名时间）',
  `expire_time`       DATETIME    DEFAULT NULL             COMMENT '课程过期时间，NULL代表永久有效',
  `update_time`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`, `course_id`),
  KEY `idx_course_id` (`course_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课表';

-- ---------------------------------------------------------------------
-- 2. 学习记录
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS learning_record;
CREATE TABLE learning_record (
  `id`          BIGINT     NOT NULL               COMMENT '主键',
  `lesson_id`   BIGINT     NOT NULL               COMMENT '课表id',
  `section_id`  BIGINT     NOT NULL               COMMENT '小节id',
  `user_id`     BIGINT     NOT NULL               COMMENT '用户id',
  `moment`      INT        NOT NULL DEFAULT 0     COMMENT '视频的当前观看时长，单位秒',
  `finished`    TINYINT(1) NOT NULL DEFAULT 0     COMMENT '是否完成学习，默认false',
  `finish_time` DATETIME   DEFAULT NULL           COMMENT '完成学习的时间',
  `create_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lesson_section` (`lesson_id`, `section_id`),
  KEY `idx_lesson_id` (`lesson_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习记录表';

-- ---------------------------------------------------------------------
-- 3. 学习计划
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS learning_plan;
CREATE TABLE learning_plan (
  `id`            BIGINT      NOT NULL              COMMENT '主键',
  `user_id`       BIGINT      NOT NULL              COMMENT '用户id',
  `course_id`     BIGINT      NOT NULL              COMMENT '课程id',
  `week_freq`     TINYINT     NOT NULL DEFAULT 0    COMMENT '计划每周学习天数（1-7）',
  `plan_status`   TINYINT     NOT NULL DEFAULT 0    COMMENT '计划状态：0-没有计划，1-计划进行中',
  `week_learned`  TINYINT     NOT NULL DEFAULT 0    COMMENT '本周已学习天数',
  `week_plan`     TINYINT     NOT NULL DEFAULT 0    COMMENT '每周计划完成的小节数',
  `week_finished` TINYINT(1)  NOT NULL DEFAULT 0    COMMENT '本周计划是否达成',
  `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`, `course_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习计划表';
