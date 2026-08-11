package com.tianji.learning.controller;

<<<<<<< Updated upstream

import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
=======
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.leanring.LearningRecordFormDTO;
import com.tianji.learning.service.ILearningLessonService;
>>>>>>> Stashed changes
import com.tianji.learning.service.ILearningRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
<<<<<<< Updated upstream
import org.springframework.web.bind.annotation.*;
=======
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
>>>>>>> Stashed changes

/**
 * <p>
 * 学习记录表 前端控制器
 * </p>
 *
 * @author 虎哥
<<<<<<< Updated upstream
 * @since 2022-12-10
 */
@RestController
@RequestMapping("/learning-records")
@Api(tags = "学习记录的相关接口")
@RequiredArgsConstructor
public class LearningRecordController {

    private final ILearningRecordService recordService;

    @ApiOperation("查询指定课程的学习记录")
    @GetMapping("/course/{courseId}")
    public LearningLessonDTO queryLearningRecordByCourse(
            @ApiParam(value = "课程id", example = "2") @PathVariable("courseId") Long courseId){
        return recordService.queryLearningRecordByCourse(courseId);
    }

    @ApiOperation("提交学习记录")
    @PostMapping
    public void addLearningRecord(@RequestBody LearningRecordFormDTO formDTO){
        recordService.addLearningRecord(formDTO);
=======
 * @since 2022-06-30
 */
@Api(tags = "学习记录相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/learning-records")
public class LearningRecordController {

    private final ILearningLessonService lessonService;
    private final ILearningRecordService recordService;

    @ApiOperation("查询当前用户指定课程的学习进度")
    @GetMapping("/course/{courseId}")
    public LearningLessonDTO queryLearningRecordByCourse(
            @ApiParam("课程id") @PathVariable("courseId") Long courseId) {
        return lessonService.queryLearningRecordByCourse(courseId);
    }

    @ApiOperation("提交学习记录，更新学习进度")
    @PostMapping
    public void submitLearningRecord(@Valid @RequestBody LearningRecordFormDTO formDTO) {
        recordService.submitLearningRecord(formDTO);
>>>>>>> Stashed changes
    }
}
