package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.dto.LearningPlanDTO;
import com.tianji.learning.domain.vo.LearningPlanVO;
import com.tianji.learning.service.ILearningPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * 学习计划 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2022-06-30
 */
@Api(tags = "学习计划相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/my-plans")
public class LearningPlanController {

    private final ILearningPlanService planService;

    @ApiOperation("创建学习计划")
    @PostMapping
    public void createMyPlan(@Valid @RequestBody LearningPlanDTO dto) {
        planService.createMyPlan(dto);
    }

    @ApiOperation("修改学习计划")
    @PutMapping
    public void updateMyPlan(@Valid @RequestBody LearningPlanDTO dto) {
        planService.updateMyPlan(dto);
    }

    @ApiOperation("分页查询我的学习计划")
    @GetMapping("/page")
    public PageDTO<LearningPlanVO> queryMyPlans(PageQuery query) {
        return planService.queryMyPlans(query);
    }
}
