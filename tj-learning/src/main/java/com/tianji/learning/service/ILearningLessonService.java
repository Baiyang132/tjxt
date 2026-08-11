package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
<<<<<<< Updated upstream
=======
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.trade.OrderBasicDTO;
>>>>>>> Stashed changes
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.vo.LearningLessonVO;
<<<<<<< Updated upstream
import com.tianji.learning.domain.vo.LearningPlanPageVO;

import java.util.List;
=======
>>>>>>> Stashed changes

/**
 * <p>
 * 学生课程表 服务类
 * </p>
 *
 * @author 虎哥
<<<<<<< Updated upstream
 * @since 2022-12-02
 */
public interface ILearningLessonService extends IService<LearningLesson> {

    void addUserLessons(Long userId, List<Long> courseIds);

    PageDTO<LearningLessonVO> queryMyLessons(PageQuery query);

    LearningLessonVO queryMyCurrentLesson();

    LearningLessonVO queryLessonByCourseId(Long courseId);

    void deleteCourseFromLesson(Long userId, Long courseId);

    Integer countLearningLessonByCourse(Long courseId);

    Long isLessonValid(Long courseId);

    LearningLesson queryByUserAndCourseId(Long userId, Long courseId);

    void createLearningPlan(Long courseId, Integer freq);

    LearningPlanPageVO queryMyPlans(PageQuery query);
=======
 * @since 2022-06-30
 */
public interface ILearningLessonService extends IService<LearningLesson> {

    /**
     * 根据订单信息创建课表
     *
     * @param order 订单信息（含用户id、课程id集合、完成时间）
     */
    void addUserLessons(OrderBasicDTO order);

    /**
     * 分页查询当前用户的课表
     *
     * @param query 分页查询条件
     * @return 课表分页信息
     */
    PageDTO<LearningLessonVO> queryMyLessons(PageQuery query);

    /**
     * 统计课程学习人数
     *
     * @param courseId 课程id
     * @return 学习人数
     */
    Integer countLearningLessonByCourse(Long courseId);

    /**
     * 校验当前用户是否可以学习当前课程
     *
     * @param courseId 课程id
     * @return 如果报名了返回课表id，否则返回null
     */
    Long isLessonValid(Long courseId);

    /**
     * 查询当前用户指定课程的学习进度
     *
     * @param courseId 课程id
     * @return 课表信息、学习记录及进度信息
     */
    LearningLessonDTO queryLearningRecordByCourse(Long courseId);

    /**
     * 查询当前用户指定课程的课表信息
     *
     * @param courseId 课程id
     * @return 课表信息
     */
    LearningLessonVO queryMyCurrentLesson(Long courseId);
>>>>>>> Stashed changes
}
