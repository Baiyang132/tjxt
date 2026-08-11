package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.BooleanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.LearningPlanDTO;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningPlan;
import com.tianji.learning.domain.vo.LearningPlanVO;
import com.tianji.learning.enums.PlanStatus;
import com.tianji.learning.mapper.LearningPlanMapper;
import com.tianji.learning.service.ILearningLessonService;
import com.tianji.learning.service.ILearningPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 学习计划 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-06-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningPlanServiceImpl extends ServiceImpl<LearningPlanMapper, LearningPlan>
        implements ILearningPlanService {

    /**
     * 按天打卡去重key，参数依次为：userId、courseId、日期
     */
    private static final String PLAN_SIGN_KEY_PREFIX = "learning:plan:sign:";

    private final ILearningLessonService lessonService;
    private final CourseClient courseClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public void createMyPlan(LearningPlanDTO dto) {
        // 1.校验并获取用户
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        Long courseId = dto.getCourseId();
        Integer weekFreq = dto.getWeekFreq();
        // 2.校验是否已报名该课程
        Long lessonId = lessonService.isLessonValid(courseId);
        if (lessonId == null) {
            throw new BadRequestException("请先报名课程");
        }
        // 3.查询课程章节数量，计算每周计划完成的学习天数
        Integer sectionNum = querySectionNum(courseId);
        int weekPlan = sectionNum == null ? 0 : (int) Math.ceil(sectionNum / 7.0) * weekFreq;
        // 4.保存学习计划
        LearningPlan plan = lambdaQuery()
                .eq(LearningPlan::getUserId, userId)
                .eq(LearningPlan::getCourseId, courseId)
                .one();
        if (plan == null) {
            // 4.1.新增计划
            plan = new LearningPlan();
            plan.setUserId(userId);
            plan.setCourseId(courseId);
            plan.setWeekFreq(weekFreq);
            plan.setPlanStatus(PlanStatus.PLAN_RUNNING);
            plan.setWeekPlan(weekPlan);
            plan.setWeekLearned(0);
            plan.setWeekFinished(false);
            save(plan);
        } else {
            // 4.2.更新计划
            plan.setWeekFreq(weekFreq);
            plan.setPlanStatus(PlanStatus.PLAN_RUNNING);
            plan.setWeekPlan(weekPlan);
            updateById(plan);
        }
        // 5.同步课表的计划状态
        lessonService.lambdaUpdate()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .set(LearningLesson::getPlanStatus, PlanStatus.PLAN_RUNNING)
                .set(LearningLesson::getWeekFreq, weekFreq)
                .update();
    }

    @Override
    @Transactional
    public void updateMyPlan(LearningPlanDTO dto) {
        // 1.校验并获取用户
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        Long courseId = dto.getCourseId();
        Integer weekFreq = dto.getWeekFreq();
        // 2.查询学习计划
        LearningPlan plan = lambdaQuery()
                .eq(LearningPlan::getUserId, userId)
                .eq(LearningPlan::getCourseId, courseId)
                .one();
        if (plan == null) {
            throw new BadRequestException("学习计划不存在");
        }
        // 3.更新计划
        Integer sectionNum = querySectionNum(courseId);
        int weekPlan = sectionNum == null ? 0 : (int) Math.ceil(sectionNum / 7.0) * weekFreq;
        plan.setWeekFreq(weekFreq);
        plan.setWeekPlan(weekPlan);
        updateById(plan);
        // 4.同步课表的学习频率
        lessonService.lambdaUpdate()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .set(LearningLesson::getWeekFreq, weekFreq)
                .update();
    }

    @Override
    public PageDTO<LearningPlanVO> queryMyPlans(PageQuery query) {
        // 1.获取用户
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        // 2.分页查询
        Page<LearningPlan> page = lambdaQuery()
                .eq(LearningPlan::getUserId, userId)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<LearningPlan> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        // 3.批量查询课程信息
        Set<Long> courseIds = records.stream()
                .map(LearningPlan::getCourseId)
                .collect(Collectors.toSet());
        Map<Long, CourseSimpleInfoDTO> courseMap = queryCourseSimpleInfoMap(courseIds);
        // 4.组装VO
        List<LearningPlanVO> list = new ArrayList<>(records.size());
        for (LearningPlan record : records) {
            LearningPlanVO vo = BeanUtils.copyBean(record, LearningPlanVO.class);
            CourseSimpleInfoDTO courseInfo = courseMap.get(record.getCourseId());
            if (courseInfo != null) {
                vo.setCourseName(courseInfo.getName());
                vo.setCourseCoverUrl(courseInfo.getCoverUrl());
                vo.setSections(courseInfo.getSectionNum());
            }
            list.add(vo);
        }
        return PageDTO.of(page, list);
    }

    @Override
    @Transactional
    public void handleLearningProgress(Long lessonId) {
        // 1.查询课表
        LearningLesson lesson = lessonService.getById(lessonId);
        if (lesson == null) {
            return;
        }
        // 2.查询学习计划
        LearningPlan plan = lambdaQuery()
                .eq(LearningPlan::getUserId, lesson.getUserId())
                .eq(LearningPlan::getCourseId, lesson.getCourseId())
                .one();
        // 3.没有运行中的计划则不做处理
        if (plan == null || plan.getPlanStatus() != PlanStatus.PLAN_RUNNING) {
            return;
        }
        // 4.按天打卡去重，当天已打卡不再累加
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String key = PLAN_SIGN_KEY_PREFIX + lesson.getUserId() + ":" + lesson.getCourseId() + ":" + date;
        LocalDateTime endOfWeek = LocalDate.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .atTime(23, 59, 59);
        Boolean firstSign = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.between(LocalDateTime.now(), endOfWeek));
        if (!BooleanUtils.isTrue(firstSign)) {
            // 当天已打卡
            return;
        }
        // 5.更新本周已学习天数
        int weekLearned = (plan.getWeekLearned() == null ? 0 : plan.getWeekLearned()) + 1;
        plan.setWeekLearned(weekLearned);
        if (weekLearned >= plan.getWeekFreq()) {
            plan.setWeekFinished(true);
        }
        updateById(plan);
    }

    @Override
    public void resetWeekData() {
        lambdaUpdate()
                .set(LearningPlan::getWeekLearned, 0)
                .set(LearningPlan::getWeekFinished, false)
                .update();
        log.info("学习计划本周数据已重置");
    }

    /**
     * 查询课程小节数量，失败时返回null
     */
    private Integer querySectionNum(Long courseId) {
        try {
            List<CourseSimpleInfoDTO> infos = courseClient.getSimpleInfoList(Collections.singleton(courseId));
            if (CollUtils.isEmpty(infos)) {
                return null;
            }
            return infos.get(0).getSectionNum();
        } catch (Exception e) {
            log.error("查询课程信息失败，courseId：{}", courseId, e);
            return null;
        }
    }

    /**
     * 批量查询课程简单信息，失败时返回空map
     */
    private Map<Long, CourseSimpleInfoDTO> queryCourseSimpleInfoMap(Set<Long> courseIds) {
        try {
            List<CourseSimpleInfoDTO> infos = courseClient.getSimpleInfoList(courseIds);
            if (CollUtils.isEmpty(infos)) {
                return Collections.emptyMap();
            }
            return infos.stream()
                    .collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
        } catch (Exception e) {
            log.error("查询课程信息失败，courseIds：{}", courseIds, e);
            return Collections.emptyMap();
        }
    }
}
