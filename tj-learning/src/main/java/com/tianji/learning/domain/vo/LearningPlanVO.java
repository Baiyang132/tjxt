package com.tianji.learning.domain.vo;

<<<<<<< Updated upstream
=======
import com.tianji.learning.enums.PlanStatus;
>>>>>>> Stashed changes
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

<<<<<<< Updated upstream
@Data
@ApiModel(description = "课程计划信息")
public class LearningPlanVO {

    @ApiModelProperty("主键lessonId")
=======
/**
 * 学习计划信息
 */
@Data
@ApiModel(description = "学习计划信息")
public class LearningPlanVO {

    @ApiModelProperty("计划id")
>>>>>>> Stashed changes
    private Long id;

    @ApiModelProperty("课程id")
    private Long courseId;

    @ApiModelProperty("课程名称")
    private String courseName;

<<<<<<< Updated upstream
    @ApiModelProperty("每周计划学习章节数")
    private Integer weekFreq;
=======
    @ApiModelProperty("课程封面")
    private String courseCoverUrl;
>>>>>>> Stashed changes

    @ApiModelProperty("课程章节数量")
    private Integer sections;

<<<<<<< Updated upstream
    @ApiModelProperty("本周已学习章节数")
    private Integer weekLearnedSections;

    @ApiModelProperty("总已学习章节数")
    private Integer learnedSections;

    @ApiModelProperty("最近一次学习时间")
    private LocalDateTime latestLearnTime;
=======
    @ApiModelProperty("每周学习频率（学习天数）")
    private Integer weekFreq;

    @ApiModelProperty("本周已学习天数")
    private Integer weekLearned;

    @ApiModelProperty("每周计划完成的学习天数")
    private Integer weekPlan;

    @ApiModelProperty("本周计划是否达成")
    private Boolean weekFinished;

    @ApiModelProperty("学习计划状态，0-没有计划，1-计划进行中")
    private PlanStatus planStatus;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
>>>>>>> Stashed changes
}
