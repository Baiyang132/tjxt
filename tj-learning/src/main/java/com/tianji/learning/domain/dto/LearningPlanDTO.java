package com.tianji.learning.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
<<<<<<< Updated upstream
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "学习计划表单实体")
public class LearningPlanDTO {
    @NotNull
    @ApiModelProperty("课程表id")
    @Min(1)
    private Long courseId;
    @NotNull
    @Range(min = 1, max = 50)
    @ApiModelProperty("每周学习频率")
    private Integer freq;
=======

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 学习计划表单
 */
@Data
@ApiModel(description = "学习计划表单")
public class LearningPlanDTO {

    @ApiModelProperty("课程id")
    @NotNull(message = "课程id不能为空")
    private Long courseId;

    @ApiModelProperty("每周学习频率（学习天数，1-7）")
    @NotNull(message = "每周学习频率不能为空")
    @Min(value = 1, message = "每周学习频率不能小于1")
    @Max(value = 7, message = "每周学习频率不能大于7")
    private Integer weekFreq;
>>>>>>> Stashed changes
}
