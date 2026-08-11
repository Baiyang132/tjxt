package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
<<<<<<< Updated upstream
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.leanring.LearningRecordDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.exceptions.DbException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
=======
import com.tianji.api.dto.leanring.LearningRecordFormDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BooleanUtils;
import com.tianji.common.utils.UserContext;
>>>>>>> Stashed changes
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.SectionType;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
<<<<<<< Updated upstream
import com.tianji.learning.service.ILearningRecordService;
import com.tianji.learning.utils.LearningRecordDelayTaskHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

=======
import com.tianji.learning.service.ILearningPlanService;
import com.tianji.learning.service.ILearningRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
>>>>>>> Stashed changes
import java.util.List;

/**
 * <p>
 * 学习记录表 服务实现类
 * </p>
 *
 * @author 虎哥
<<<<<<< Updated upstream
 * @since 2022-12-10
 */
@Service
@RequiredArgsConstructor
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements ILearningRecordService {

    private final ILearningLessonService lessonService;

    private final CourseClient courseClient;

    private final LearningRecordDelayTaskHandler taskHandler;

    @Override
    public LearningLessonDTO queryLearningRecordByCourse(Long courseId) {
        // 1.获取登录用户
        Long userId = UserContext.getUser();
        // 2.查询课表
        LearningLesson lesson = lessonService.queryByUserAndCourseId(userId, courseId);
        // 3.查询学习记录
        // select * from xx where lesson_id = #{lessonId}
        List<LearningRecord> records = lambdaQuery().eq(LearningRecord::getLessonId, lesson.getId()).list();
        // 4.封装结果
        LearningLessonDTO dto = new LearningLessonDTO();
        dto.setId(lesson.getId());
        dto.setLatestSectionId(lesson.getLatestSectionId());
        dto.setRecords(BeanUtils.copyList(records, LearningRecordDTO.class));
        return dto;
    }

    @Override
    @Transactional
    public void addLearningRecord(LearningRecordFormDTO recordDTO) {
        // 1.获取登录用户
        Long userId = UserContext.getUser();
        // 2.处理学习记录
        boolean finished = false;
        if (recordDTO.getSectionType() == SectionType.VIDEO) {
            // 2.1.处理视频
            finished = handleVideoRecord(userId, recordDTO);
        }else{
            // 2.2.处理考试
            finished = handleExamRecord(userId, recordDTO);
        }
        if(!finished){
            // 没有新学完的小节，无需更新课表中的学习进度
            return;
        }
        // 3.处理课表数据
        handleLearningLessonsChanges(recordDTO);
    }

    private void handleLearningLessonsChanges(LearningRecordFormDTO recordDTO) {
        // 1.查询课表
        LearningLesson lesson = lessonService.getById(recordDTO.getLessonId());
        if (lesson == null) {
            throw new BizIllegalException("课程不存在，无法更新数据！");
        }
        // 2.判断是否有新的完成小节
        boolean allLearned = false;

            // 3.如果有新完成的小节，则需要查询课程数据
            CourseFullInfoDTO cInfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
            if (cInfo == null) {
                throw new BizIllegalException("课程不存在，无法更新数据！");
            }
            // 4.比较课程是否全部学完：已学习小节 >= 课程总小节
            allLearned = lesson.getLearnedSections() + 1 >= cInfo.getSectionNum();

        // 5.更新课表
        lessonService.lambdaUpdate()
                .set(lesson.getLearnedSections() == 0, LearningLesson::getStatus, LessonStatus.LEARNING.getValue())
                .set(allLearned, LearningLesson::getStatus, LessonStatus.FINISHED.getValue())
                .setSql("learned_sections = learned_sections + 1")
                .eq(LearningLesson::getId, lesson.getId())
                .update();
    }

    private boolean handleVideoRecord(Long userId, LearningRecordFormDTO recordDTO) {
        // 1.查询旧的学习记录
        LearningRecord old = queryOldRecord(recordDTO.getLessonId(), recordDTO.getSectionId());
        // 2.判断是否存在
        if (old == null) {
            // 3.不存在，则新增
            // 3.1.转换PO
            LearningRecord record = BeanUtils.copyBean(recordDTO, LearningRecord.class);
            // 3.2.填充数据
            record.setUserId(userId);
            // 3.3.写入数据库
            boolean success = save(record);
            if (!success) {
                throw new DbException("新增学习记录失败！");
            }
            return false;
        }
        // 4.存在，则更新
        // 4.1.判断是否是第一次完成
        boolean finished = !old.getFinished() && recordDTO.getMoment() * 2 >= recordDTO.getDuration();
        if(!finished){
            LearningRecord record = new LearningRecord();
            record.setLessonId(recordDTO.getLessonId());
            record.setSectionId(recordDTO.getSectionId());
            record.setMoment(recordDTO.getMoment());
            record.setId(old.getId());
            record.setFinished(old.getFinished());
            taskHandler.addLearningRecordTask(record);
            return false;
        }
        // 4.2.更新数据
        boolean success = lambdaUpdate()
                .set(LearningRecord::getMoment, recordDTO.getMoment())
                .set(LearningRecord::getFinished, true)
                .set(LearningRecord::getFinishTime, recordDTO.getCommitTime())
                .eq(LearningRecord::getId, old.getId())
                .update();
        if(!success){
            throw new DbException("更新学习记录失败！");
        }
        // 4.3.清理缓存
        taskHandler.cleanRecordCache(recordDTO.getLessonId(), recordDTO.getSectionId());
        return true;
    }

    private LearningRecord queryOldRecord(Long lessonId, Long sectionId) {
        // 1.查询缓存
        LearningRecord record = taskHandler.readRecordCache(lessonId, sectionId);
        // 2.如果命中，直接返回
        if (record != null) {
            return record;
        }
        // 3.未命中，查询数据库
        record = lambdaQuery()
                .eq(LearningRecord::getLessonId, lessonId)
                .eq(LearningRecord::getSectionId, sectionId)
                .one();
        // 4.写入缓存
        taskHandler.writeRecordCache(record);
        return record;
    }

    private boolean handleExamRecord(Long userId, LearningRecordFormDTO recordDTO) {
        // 1.转换DTO为PO
        LearningRecord record = BeanUtils.copyBean(recordDTO, LearningRecord.class);
        // 2.填充数据
        record.setUserId(userId);
        record.setFinished(true);
        record.setFinishTime(recordDTO.getCommitTime());
        // 3.写入数据库
        boolean success = save(record);
        if (!success) {
            throw new DbException("新增考试记录失败！");
        }
        return true;
=======
 * @since 2022-06-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord>
        implements ILearningRecordService {

    private final ILearningLessonService lessonService;
    private final ILearningPlanService planService;

    @Override
    @Transactional
    public void submitLearningRecord(LearningRecordFormDTO formDTO) {
        // 1.校验参数
        Long lessonId = formDTO.getLessonId();
        if (lessonId == null) {
            throw new BadRequestException("课表id不能为空");
        }
        Long sectionId = formDTO.getSectionId();
        if (sectionId == null) {
            throw new BadRequestException("小节id不能为空");
        }
        Long userId = UserContext.getUser();
        // 2.查询课表并校验
        LearningLesson lesson = lessonService.getById(lessonId);
        if (lesson == null) {
            throw new BadRequestException("课表不存在");
        }
        if (!lesson.getUserId().equals(userId)) {
            throw new BadRequestException("无权学习该课程");
        }
        if (lesson.getStatus() == LessonStatus.EXPIRED) {
            throw new BadRequestException("课程已过期，无法继续学习");
        }
        // 3.查询学习记录
        LearningRecord record = queryRecord(lessonId, sectionId);
        // 4.已完成的记录直接返回，避免重复处理
        if (record != null && BooleanUtils.isTrue(record.getFinished())) {
            return;
        }
        // 5.判断本次学习是否完成
        boolean finished = isFinished(formDTO, record);
        // 6.写入学习记录
        if (record == null) {
            // 6.1.新增记录
            LearningRecord newRecord = new LearningRecord();
            newRecord.setLessonId(lessonId);
            newRecord.setSectionId(sectionId);
            newRecord.setUserId(userId);
            newRecord.setMoment(formDTO.getMoment());
            newRecord.setFinished(finished);
            if (finished) {
                newRecord.setFinishTime(LocalDateTime.now());
            }
            try {
                save(newRecord);
            } catch (DuplicateKeyException e) {
                // 6.2.并发重复插入，降级为查询后处理
                record = queryRecord(lessonId, sectionId);
                if (record == null || BooleanUtils.isTrue(record.getFinished())) {
                    return;
                }
                if (finished) {
                    // 条件更新，防止并发下重复完成
                    finished = finishRecord(record, formDTO.getMoment());
                }
            }
        } else if (finished) {
            // 6.3.更新已有记录为完成，条件更新防止并发重复完成
            finished = finishRecord(record, formDTO.getMoment());
        }
        // 7.处理结果
        if (finished) {
            // 7.1.完成学习，更新课表学习进度
            updateLessonProgress(lesson, sectionId);
            // 7.2.更新学习计划（按天打卡）
            planService.handleLearningProgress(lessonId);
        } else if (record != null) {
            // 7.3.未完成，仅更新观看进度
            updateRecordMoment(record, formDTO.getMoment());
        }
    }

    @Override
    public List<LearningRecord> queryRecordsByLessonId(Long lessonId) {
        return lambdaQuery()
                .eq(LearningRecord::getLessonId, lessonId)
                .orderByAsc(LearningRecord::getCreateTime)
                .list();
    }

    /**
     * 根据课表和节id查询学习记录
     */
    private LearningRecord queryRecord(Long lessonId, Long sectionId) {
        return lambdaQuery()
                .eq(LearningRecord::getLessonId, lessonId)
                .eq(LearningRecord::getSectionId, sectionId)
                .one();
    }

    /**
     * 判断本次学习是否完成
     */
    private boolean isFinished(LearningRecordFormDTO formDTO, LearningRecord record) {
        Integer sectionType = formDTO.getSectionType();
        // 1.考试提交即完成
        if (sectionType != null && SectionType.EXAM.getValue() == sectionType) {
            return true;
        }
        // 2.视频：观看时长达到总时长视为完成
        Integer duration = formDTO.getDuration();
        Integer moment = formDTO.getMoment();
        if (duration != null && moment != null && moment >= duration) {
            return true;
        }
        // 3.进度没有变化，说明拖动进度条了，视为完成
        return record != null && moment != null && moment.equals(record.getMoment());
    }

    /**
     * 将学习记录置为完成，条件更新防止并发下重复完成
     *
     * @return 是否由本次请求完成
     */
    private boolean finishRecord(LearningRecord record, Integer moment) {
        return lambdaUpdate()
                .eq(LearningRecord::getId, record.getId())
                .eq(LearningRecord::getFinished, false)
                .set(LearningRecord::getFinished, true)
                .set(LearningRecord::getFinishTime, LocalDateTime.now())
                .set(LearningRecord::getMoment, moment)
                .update();
    }

    /**
     * 更新学习记录的观看进度
     */
    private void updateRecordMoment(LearningRecord record, Integer moment) {
        if (moment == null) {
            return;
        }
        lambdaUpdate()
                .eq(LearningRecord::getId, record.getId())
                .set(LearningRecord::getMoment, moment)
                .update();
    }

    /**
     * 更新课表学习进度：最近学习信息、已完成小节数、学习状态
     */
    private void updateLessonProgress(LearningLesson lesson, Long sectionId) {
        // 1.更新最近学习信息和已完成小节数
        lessonService.lambdaUpdate()
                .eq(LearningLesson::getId, lesson.getId())
                .setSql("learned_sections = learned_sections + 1")
                .set(LearningLesson::getLatestSectionId, sectionId)
                .set(LearningLesson::getLatestLearnTime, LocalDateTime.now())
                .update();
        // 2.学习状态从未学习变为学习中
        lessonService.lambdaUpdate()
                .eq(LearningLesson::getId, lesson.getId())
                .eq(LearningLesson::getStatus, LessonStatus.NOT_BEGIN)
                .set(LearningLesson::getStatus, LessonStatus.LEARNING)
                .update();
>>>>>>> Stashed changes
    }
}
