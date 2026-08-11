package com.tianji.learning.controller;

<<<<<<< Updated upstream

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.dto.LearningPlanDTO;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;
=======
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.vo.LearningLessonVO;
>>>>>>> Stashed changes
import com.tianji.learning.service.ILearningLessonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
<<<<<<< Updated upstream
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
=======
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
>>>>>>> Stashed changes

/**
 * <p>
 * 学生课程表 前端控制器
 * </p>
 *
 * @author 虎哥
<<<<<<< Updated upstream
 * @since 2022-12-02
 */
@RestController
@RequestMapping("/lessons")
@Api(tags = "我的课表相关接口")
@RequiredArgsConstructor
=======
 * @since 2022-06-30
 */
@Api(tags = "课表相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/lessons")
>>>>>>> Stashed changes
public class LearningLessonController {

    private final ILearningLessonService lessonService;

<<<<<<< Updated upstream
    @GetMapping("/page")
    @ApiOperation("分页查询我的课表")
=======
    @ApiOperation("分页查询我的课表")
    @GetMapping("/page")
>>>>>>> Stashed changes
    public PageDTO<LearningLessonVO> queryMyLessons(PageQuery query) {
        return lessonService.queryMyLessons(query);
    }

<<<<<<< Updated upstream
    @GetMapping("/now")
    @ApiOperation("查询我正在学习的课程")
    public LearningLessonVO queryMyCurrentLesson() {
        return lessonService.queryMyCurrentLesson();
    }

    @GetMapping("/{courseId}")
    @ApiOperation("查询指定课程信息")
    public LearningLessonVO queryLessonByCourseId(
            @ApiParam(value = "课程id" ,example = "1") @PathVariable("courseId") Long courseId) {
        return lessonService.queryLessonByCourseId(courseId);
    }

    @DeleteMapping("/{courseId}")
    @ApiOperation("删除指定课程信息")
    public void deleteCourseFromLesson(
            @ApiParam(value = "课程id" ,example = "1") @PathVariable("courseId") Long courseId) {
        lessonService.deleteCourseFromLesson(null, courseId);
=======
    @ApiOperation("查询我的当前课表信息")
    @GetMapping("/now")
    public LearningLessonVO queryMyCurrentLesson(
            @ApiParam("课程id") @RequestParam("courseId") Long courseId) {
        return lessonService.queryMyCurrentLesson(courseId);
>>>>>>> Stashed changes
    }

    @ApiOperation("统计课程学习人数")
    @GetMapping("/{courseId}/count")
    public Integer countLearningLessonByCourse(
<<<<<<< Updated upstream
            @ApiParam(value = "课程id" ,example = "1") @PathVariable("courseId") Long courseId){
        return lessonService.countLearningLessonByCourse(courseId);
    }

    @ApiOperation("校验当前课程是否已经报名")
    @GetMapping("/{courseId}/valid")
    public Long isLessonValid(
            @ApiParam(value = "课程id" ,example = "1") @PathVariable("courseId") Long courseId){
        return lessonService.isLessonValid(courseId);
    }

    @ApiOperation("创建学习计划")
    @PostMapping("/plans")
    public void createLearningPlans(@Valid @RequestBody LearningPlanDTO planDTO){
        lessonService.createLearningPlan(planDTO.getCourseId(), planDTO.getFreq());
    }

    @ApiOperation("查询我的学习计划")
    @GetMapping("/plans")
    public LearningPlanPageVO queryMyPlans(PageQuery query){
        return lessonService.queryMyPlans(query);
    }
=======
            @ApiParam("课程id") @PathVariable("courseId") Long courseId) {
        return lessonService.countLearningLessonByCourse(courseId);
    }

    @ApiOperation("校验当前用户是否可以学习当前课程")
    @GetMapping("/{courseId}/valid")
    public Long isLessonValid(
            @ApiParam("课程id") @PathVariable("courseId") Long courseId) {
        return lessonService.isLessonValid(courseId);
    }
>>>>>>> Stashed changes
}
