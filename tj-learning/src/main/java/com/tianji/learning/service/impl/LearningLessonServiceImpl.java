package com.tianji.learning.service.impl;

<<<<<<< Updated upstream
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
=======
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
>>>>>>> Stashed changes
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
<<<<<<< Updated upstream
import com.tianji.api.dto.IdAndNumDTO;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.*;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;
import com.tianji.learning.domain.vo.LearningPlanVO;
=======
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.leanring.LearningRecordDTO;
import com.tianji.api.dto.trade.OrderBasicDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.domain.vo.LearningLessonVO;
>>>>>>> Stashed changes
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.PlanStatus;
import com.tianji.learning.mapper.LearningLessonMapper;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
<<<<<<< Updated upstream
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
=======
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
>>>>>>> Stashed changes
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 学生课程表 服务实现类
 * </p>
 *
 * @author 虎哥
<<<<<<< Updated upstream
 * @since 2022-12-02
 */
@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson> implements ILearningLessonService {
=======
 * @since 2022-06-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson>
        implements ILearningLessonService {
>>>>>>> Stashed changes

    private final CourseClient courseClient;
    private final CatalogueClient catalogueClient;
    private final LearningRecordMapper recordMapper;

    @Override
<<<<<<< Updated upstream
    @Transactional
    public void addUserLessons(Long userId, List<Long> courseIds) {
        // 1.查询课程有效期
        List<CourseSimpleInfoDTO> cInfoList = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(cInfoList)) {
            // 课程不存在，无法添加
            log.error("课程信息不存在，无法添加到课表");
            return;
        }
        // 2.循环遍历，处理LearningLesson数据
        List<LearningLesson> list = new ArrayList<>(cInfoList.size());
        for (CourseSimpleInfoDTO cInfo : cInfoList) {
            LearningLesson lesson = new LearningLesson();
            // 2.1.获取过期时间
            Integer validDuration = cInfo.getValidDuration();
            if (validDuration != null && validDuration > 0) {
                LocalDateTime now = LocalDateTime.now();
                lesson.setCreateTime(now);
                lesson.setExpireTime(now.plusMonths(validDuration));
            }
            // 2.2.填充userId和courseId
            lesson.setUserId(userId);
            lesson.setCourseId(cInfo.getId());
            list.add(lesson);
        }
        // 3.批量新增
        saveBatch(list);
=======
    public void addUserLessons(OrderBasicDTO order) {
        // 1.参数校验
        Long userId = order.getUserId();
        List<Long> courseIds = order.getCourseIds();
        if (userId == null || CollUtils.isEmpty(courseIds)) {
            return;
        }
        // 2.查询用户已存在的课表，过滤出需要新建的课程
        List<LearningLesson> exists = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .in(LearningLesson::getCourseId, courseIds)
                .list();
        List<Long> existCourseIds = exists.stream()
                .map(LearningLesson::getCourseId)
                .collect(Collectors.toList());
        List<Long> needCreateIds = courseIds.stream()
                .filter(courseId -> !existCourseIds.contains(courseId))
                .distinct()
                .collect(Collectors.toList());
        if (CollUtils.isEmpty(needCreateIds)) {
            return;
        }
        // 3.查询课程信息，用于计算过期时间
        LocalDateTime now = order.getFinishTime() == null ? LocalDateTime.now() : order.getFinishTime();
        Map<Long, CourseSimpleInfoDTO> courseMap = queryCourseSimpleInfoMap(needCreateIds);
        // 4.构建课表
        List<LearningLesson> lessons = new ArrayList<>(needCreateIds.size());
        for (Long courseId : needCreateIds) {
            LearningLesson lesson = new LearningLesson();
            lesson.setUserId(userId);
            lesson.setCourseId(courseId);
            lesson.setStatus(LessonStatus.NOT_BEGIN);
            lesson.setPlanStatus(PlanStatus.NO_PLAN);
            lesson.setWeekFreq(0);
            lesson.setLearnedSections(0);
            // 设置课程过期时间
            CourseSimpleInfoDTO courseInfo = courseMap.get(courseId);
            if (courseInfo != null && courseInfo.getValidDuration() != null && courseInfo.getValidDuration() > 0) {
                lesson.setExpireTime(now.plusMonths(courseInfo.getValidDuration()));
            }
            lessons.add(lesson);
        }
        // 5.批量保存，唯一键冲突时忽略，保证幂等
        try {
            saveBatch(lessons);
        } catch (DuplicateKeyException e) {
            log.debug("课表已存在，忽略重复创建，userId：{}", userId, e);
        }
>>>>>>> Stashed changes
    }

    @Override
    public PageDTO<LearningLessonVO> queryMyLessons(PageQuery query) {
<<<<<<< Updated upstream
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.分页查询
        // select * from learning_lesson where user_id = #{userId} order by latest_learn_time limit 0, 5
        Page<LearningLesson> page = lambdaQuery()
                .eq(LearningLesson::getUserId, userId) // where user_id = #{userId}
                .page(query.toMpPage("latest_learn_time", false));
=======
        // 1.获取当前用户
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        // 2.分页查询课表，按最近学习时间倒序，未学习过的按报名时间兜底
        Page<LearningLesson> page = (Page<LearningLesson>) baseMapper.queryMyLessonsPage(query.toMpPage(), userId);
>>>>>>> Stashed changes
        List<LearningLesson> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
<<<<<<< Updated upstream
        // 3.查询课程信息
        Map<Long, CourseSimpleInfoDTO> cMap = queryCourseSimpleInfoList(records);

        // 4.封装VO返回
        List<LearningLessonVO> list = new ArrayList<>(records.size());
        // 4.1.循环遍历，把LearningLesson转为VO
        for (LearningLesson r : records) {
            // 4.2.拷贝基础属性到vo
            LearningLessonVO vo = BeanUtils.copyBean(r, LearningLessonVO.class);
            // 4.3.获取课程信息，填充到vo
            CourseSimpleInfoDTO cInfo = cMap.get(r.getCourseId());
            vo.setCourseName(cInfo.getName());
            vo.setCourseCoverUrl(cInfo.getCoverUrl());
            vo.setSections(cInfo.getSectionNum());
            list.add(vo);
        }
        return PageDTO.of(page, list);
    }

    private Map<Long, CourseSimpleInfoDTO> queryCourseSimpleInfoList(List<LearningLesson> records) {
        // 3.1.获取课程id
        Set<Long> cIds = records.stream().map(LearningLesson::getCourseId).collect(Collectors.toSet());
        // 3.2.查询课程信息
        List<CourseSimpleInfoDTO> cInfoList = courseClient.getSimpleInfoList(cIds);
        if (CollUtils.isEmpty(cInfoList)) {
            // 课程不存在，无法添加
            throw new BadRequestException("课程信息不存在！");
        }
        // 3.3.把课程集合处理成Map，key是courseId，值是course本身
        Map<Long, CourseSimpleInfoDTO> cMap = cInfoList.stream()
                .collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
        return cMap;
    }

    @Override
    public LearningLessonVO queryMyCurrentLesson() {
        // 1.获取当前登录的用户
        Long userId = UserContext.getUser();
        // 2.查询正在学习的课程 select * from xx where user_id = #{userId} AND status = 1 order by latest_learn_time limit 1
        LearningLesson lesson = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getStatus, LessonStatus.LEARNING.getValue())
                .orderByDesc(LearningLesson::getLatestLearnTime)
                .last("limit 1")
                .one();
        if (lesson == null) {
            return null;
        }
        // 3.拷贝PO基础属性到VO
        LearningLessonVO vo = BeanUtils.copyBean(lesson, LearningLessonVO.class);
        // 4.查询课程信息
        CourseFullInfoDTO cInfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
        if (cInfo == null) {
            throw new BadRequestException("课程不存在");
        }
        vo.setCourseName(cInfo.getName());
        vo.setCourseCoverUrl(cInfo.getCoverUrl());
        vo.setSections(cInfo.getSectionNum());
        // 5.统计课表中的课程数量 select count(1) from xxx where user_id = #{userId}
        Integer courseAmount = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .count();
        vo.setCourseAmount(courseAmount);
        // 6.查询小节信息
        List<CataSimpleInfoDTO> cataInfos =
                catalogueClient.batchQueryCatalogue(CollUtils.singletonList(lesson.getLatestSectionId()));
        if (!CollUtils.isEmpty(cataInfos)) {
            CataSimpleInfoDTO cataInfo = cataInfos.get(0);
            vo.setLatestSectionName(cataInfo.getName());
            vo.setLatestSectionIndex(cataInfo.getCIndex());
        }
        return vo;
    }

    @Override
    public LearningLessonVO queryLessonByCourseId(Long courseId) {
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.查询课程信息 select * from xx where user_id = #{userId} AND course_id = #{courseId}
        LearningLesson lesson = getOne(buildUserIdAndCourseIdWrapper(userId, courseId));
        if (lesson == null) {
            return null;
        }
        // 3.处理VO
        return BeanUtils.copyBean(lesson, LearningLessonVO.class);
    }

    @Override
    public void deleteCourseFromLesson(Long userId, Long courseId) {
        // 1.获取当前登录用户
        if (userId == null) {
            userId = UserContext.getUser();
        }
        // 2.删除课程
        remove(buildUserIdAndCourseIdWrapper(userId, courseId));
    }

    @Override
    public Integer countLearningLessonByCourse(Long courseId) {
        // select count(1) from xx where course_id = #{cc} AND status in (0, 1, 2)
        return lambdaQuery()
                .eq(LearningLesson::getCourseId, courseId)
                .in(LearningLesson::getStatus,
                        LessonStatus.NOT_BEGIN.getValue(),
                        LessonStatus.LEARNING.getValue(),
                        LessonStatus.FINISHED.getValue())
                .count();
=======
        // 3.批量查询课程信息
        Set<Long> courseIds = records.stream()
                .map(LearningLesson::getCourseId)
                .collect(Collectors.toSet());
        Map<Long, CourseSimpleInfoDTO> courseMap = queryCourseSimpleInfoMap(courseIds);
        // 4.刷新课表状态并组装VO
        LocalDateTime now = LocalDateTime.now();
        List<LearningLesson> updateLessons = new ArrayList<>();
        List<LearningLessonVO> list = new ArrayList<>(records.size());
        for (LearningLesson record : records) {
            // 4.1.刷新状态（终态不再变化）
            if (refreshStatus(record, courseMap.get(record.getCourseId()), now)) {
                updateLessons.add(record);
            }
            // 4.2.转换VO并填充课程信息
            LearningLessonVO vo = BeanUtils.copyBean(record, LearningLessonVO.class);
            CourseSimpleInfoDTO courseInfo = courseMap.get(record.getCourseId());
            if (courseInfo != null) {
                vo.setCourseName(courseInfo.getName());
                vo.setCourseCoverUrl(courseInfo.getCoverUrl());
                vo.setSections(courseInfo.getSectionNum());
            }
            vo.setCourseAmount(Math.toIntExact(page.getTotal()));
            list.add(vo);
        }
        // 5.批量更新刷新后的状态
        if (CollUtils.isNotEmpty(updateLessons)) {
            updateBatchById(updateLessons);
        }
        // 6.填充最近学习小节信息
        fillLatestSectionName(list, records);
        return PageDTO.of(page, list);
    }

    @Override
    public Integer countLearningLessonByCourse(Long courseId) {
        return Math.toIntExact(lambdaQuery().eq(LearningLesson::getCourseId, courseId).count());
>>>>>>> Stashed changes
    }

    @Override
    public Long isLessonValid(Long courseId) {
<<<<<<< Updated upstream
        // 1.获取登录用户
=======
>>>>>>> Stashed changes
        Long userId = UserContext.getUser();
        if (userId == null) {
            return null;
        }
<<<<<<< Updated upstream
        // 2.查询课程
        LearningLesson lesson = getOne(buildUserIdAndCourseIdWrapper(userId, courseId));
        if (lesson == null) {
            return null;
        }
        return lesson.getId();
    }

    @Override
    public LearningLesson queryByUserAndCourseId(Long userId, Long courseId) {
        return getOne(buildUserIdAndCourseIdWrapper(userId, courseId));
    }

    @Override
    public void createLearningPlan(Long courseId, Integer freq) {
        // 1.获取当前登录的用户
        Long userId = UserContext.getUser();
        // 2.查询课表中的指定课程有关的数据
        LearningLesson lesson = queryByUserAndCourseId(userId, courseId);
        AssertUtils.isNotNull(lesson, "课程信息不存在！");
        // 3.修改数据
        LearningLesson l = new LearningLesson();
        l.setId(lesson.getId());
        l.setWeekFreq(freq);
        if(lesson.getPlanStatus() == PlanStatus.NO_PLAN) {
            l.setPlanStatus(PlanStatus.PLAN_RUNNING);
        }
        updateById(l);
    }

    @Override
    public LearningPlanPageVO queryMyPlans(PageQuery query) {
        LearningPlanPageVO result = new LearningPlanPageVO();
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.获取本周起始时间
        LocalDate now = LocalDate.now();
        LocalDateTime begin = DateUtils.getWeekBeginTime(now);
        LocalDateTime end = DateUtils.getWeekEndTime(now);
        // 3.查询总的统计数据
        // 3.1.本周总的已学习小节数量
        Integer weekFinished = recordMapper.selectCount(new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getFinished, true)
                .gt(LearningRecord::getFinishTime, begin)
                .lt(LearningRecord::getFinishTime, end)
        );
        result.setWeekFinished(weekFinished);
        // 3.2.本周总的计划学习小节数量
        Integer weekTotalPlan = getBaseMapper().queryTotalPlan(userId);
        result.setWeekTotalPlan(weekTotalPlan);
        // TODO 3.3.本周学习积分

        // 4.查询分页数据
        // 4.1.分页查询课表信息以及学习计划信息
        Page<LearningLesson> p = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getPlanStatus, PlanStatus.PLAN_RUNNING)
                .in(LearningLesson::getStatus, LessonStatus.NOT_BEGIN, LessonStatus.LEARNING)
                .page(query.toMpPage("latest_learn_time", false));
        List<LearningLesson> records = p.getRecords();
        if (CollUtils.isEmpty(records)) {
            return result.emptyPage(p);
        }
        // 4.2.查询课表对应的课程信息
        Map<Long, CourseSimpleInfoDTO> cMap = queryCourseSimpleInfoList(records);
        // 4.3.统计每一个课程本周已学习小节数量
        List<IdAndNumDTO> list = recordMapper.countLearnedSections(userId, begin, end);
        Map<Long, Integer> countMap = IdAndNumDTO.toMap(list);
        // 4.4.组装数据VO
        List<LearningPlanVO> voList = new ArrayList<>(records.size());
        for (LearningLesson r : records) {
            // 4.4.1.拷贝基础属性到vo
            LearningPlanVO vo = BeanUtils.copyBean(r, LearningPlanVO.class);
            // 4.4.2.填充课程详细信息
            CourseSimpleInfoDTO cInfo = cMap.get(r.getCourseId());
            if (cInfo != null) {
                vo.setCourseName(cInfo.getName());
                vo.setSections(cInfo.getSectionNum());
            }
            // 4.4.3.每个课程的本周已学习小节数量
            vo.setWeekLearnedSections(countMap.getOrDefault(r.getId(), 0));
            voList.add(vo);
        }
        return result.pageInfo(p.getTotal(), p.getPages(), voList);
    }

    private LambdaQueryWrapper<LearningLesson> buildUserIdAndCourseIdWrapper(Long userId, Long courseId) {
        LambdaQueryWrapper<LearningLesson> queryWrapper = new QueryWrapper<LearningLesson>()
                .lambda()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId);
        return queryWrapper;
=======
        LearningLesson lesson = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        return lesson == null ? null : lesson.getId();
    }

    @Override
    public LearningLessonDTO queryLearningRecordByCourse(Long courseId) {
        // 1.查询课表
        Long userId = UserContext.getUser();
        if (userId == null) {
            return null;
        }
        LearningLesson lesson = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if (lesson == null) {
            // 没有报名该课程（免费试看），返回null
            return null;
        }
        // 2.查询学习记录，按最近学习时间倒序（第一条即最近学习的小节记录）
        List<LearningRecord> records = recordMapper.selectList(Wrappers.<LearningRecord>lambdaQuery()
                .eq(LearningRecord::getLessonId, lesson.getId())
                .orderByDesc(LearningRecord::getUpdateTime));
        // 3.组装DTO
        LearningLessonDTO dto = new LearningLessonDTO();
        dto.setId(lesson.getId());
        dto.setLatestSectionId(lesson.getLatestSectionId());
        dto.setRecords(BeanUtils.copyList(records, LearningRecordDTO.class));
        return dto;
    }

    @Override
    public LearningLessonVO queryMyCurrentLesson(Long courseId) {
        // 1.查询课表
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        LearningLesson lesson = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if (lesson == null) {
            return null;
        }
        // 2.转换VO
        LearningLessonVO vo = BeanUtils.copyBean(lesson, LearningLessonVO.class);
        // 3.填充课程信息
        Map<Long, CourseSimpleInfoDTO> courseMap = queryCourseSimpleInfoMap(Collections.singleton(courseId));
        CourseSimpleInfoDTO courseInfo = courseMap.get(courseId);
        if (courseInfo != null) {
            vo.setCourseName(courseInfo.getName());
            vo.setCourseCoverUrl(courseInfo.getCoverUrl());
            vo.setSections(courseInfo.getSectionNum());
        }
        // 4.填充最近学习小节信息
        Long latestSectionId = lesson.getLatestSectionId();
        if (latestSectionId != null) {
            Map<Long, CataSimpleInfoDTO> cataMap = queryCatalogueMap(Collections.singletonList(latestSectionId));
            CataSimpleInfoDTO cata = cataMap.get(latestSectionId);
            if (cata != null) {
                vo.setLatestSectionName(cata.getName());
                vo.setLatestSectionIndex(cata.getCIndex());
            }
        }
        return vo;
    }

    /**
     * 刷新课表状态：过期改为已过期，学完改为已学完（终态不再变化）
     *
     * @param lesson 课表
     * @param courseInfo 课程信息
     * @param now 当前时间
     * @return 状态是否发生变化
     */
    private boolean refreshStatus(LearningLesson lesson, CourseSimpleInfoDTO courseInfo, LocalDateTime now) {
        LessonStatus status = lesson.getStatus();
        // 终态不再变化
        if (status == LessonStatus.FINISHED || status == LessonStatus.EXPIRED) {
            return false;
        }
        // 判断是否过期
        if (lesson.getExpireTime() != null && lesson.getExpireTime().isBefore(now)) {
            lesson.setStatus(LessonStatus.EXPIRED);
            return true;
        }
        // 判断是否学完
        if (courseInfo != null && courseInfo.getSectionNum() != null
                && lesson.getLearnedSections() != null
                && lesson.getLearnedSections() >= courseInfo.getSectionNum()) {
            lesson.setStatus(LessonStatus.FINISHED);
            return true;
        }
        return false;
    }

    /**
     * 填充最近学习小节名称和序号
     *
     * @param list    课表VO集合
     * @param records 课表PO集合，与list顺序一致
     */
    private void fillLatestSectionName(List<LearningLessonVO> list, List<LearningLesson> records) {
        List<Long> sectionIds = records.stream()
                .map(LearningLesson::getLatestSectionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtils.isEmpty(sectionIds)) {
            return;
        }
        Map<Long, CataSimpleInfoDTO> cataMap = queryCatalogueMap(sectionIds);
        for (int i = 0; i < records.size(); i++) {
            Long sectionId = records.get(i).getLatestSectionId();
            if (sectionId == null) {
                continue;
            }
            CataSimpleInfoDTO cata = cataMap.get(sectionId);
            if (cata != null) {
                LearningLessonVO vo = list.get(i);
                vo.setLatestSectionName(cata.getName());
                vo.setLatestSectionIndex(cata.getCIndex());
            }
        }
    }

    /**
     * 批量查询课程简单信息，失败时返回空map（course服务不可用时不影响课表查询）
     *
     * @param courseIds 课程id集合
     * @return 课程id和课程信息映射
     */
    private Map<Long, CourseSimpleInfoDTO> queryCourseSimpleInfoMap(Iterable<Long> courseIds) {
        try {
            List<CourseSimpleInfoDTO> courseInfos = courseClient.getSimpleInfoList(courseIds);
            if (CollUtils.isEmpty(courseInfos)) {
                return Collections.emptyMap();
            }
            return courseInfos.stream()
                    .collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
        } catch (Exception e) {
            log.error("查询课程信息失败，courseIds：{}", courseIds, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 批量查询目录信息，失败时返回空map（course服务不可用时不影响课表查询）
     *
     * @param cataIds 目录id集合
     * @return 目录id和目录信息映射
     */
    private Map<Long, CataSimpleInfoDTO> queryCatalogueMap(List<Long> cataIds) {
        try {
            List<CataSimpleInfoDTO> catas = catalogueClient.batchQueryCatalogue(cataIds);
            if (CollUtils.isEmpty(catas)) {
                return Collections.emptyMap();
            }
            return catas.stream()
                    .collect(Collectors.toMap(CataSimpleInfoDTO::getId, c -> c));
        } catch (Exception e) {
            log.error("查询目录信息失败，cataIds：{}", cataIds, e);
            return Collections.emptyMap();
        }
>>>>>>> Stashed changes
    }
}
