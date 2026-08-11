package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.learning.enums.PlanStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 学习计划表
 * </p>
 *
 * @author 虎哥
 * @since 2022-06-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("learning_plan")
public class LearningPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 学生id
     */
    private Long userId;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 计划每周学习天数（1-7）
     */
    private Integer weekFreq;

    /**
     * 计划状态，0-没有计划，1-计划进行中
     */
    private PlanStatus planStatus;

    /**
     * 本周已学习天数
     */
    private Integer weekLearned;

    /**
     * 每周计划完成的小节数
     */
    private Integer weekPlan;

    /**
     * 本周学习完成情况
     */
    private Boolean weekFinished;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
