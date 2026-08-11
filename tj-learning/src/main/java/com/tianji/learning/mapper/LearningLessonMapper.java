package com.tianji.learning.mapper;

<<<<<<< Updated upstream
import com.tianji.learning.domain.po.LearningLesson;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
=======
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianji.learning.domain.po.LearningLesson;
>>>>>>> Stashed changes
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 学生课程表 Mapper 接口
 * </p>
 *
 * @author 虎哥
<<<<<<< Updated upstream
 * @since 2022-12-02
 */
public interface LearningLessonMapper extends BaseMapper<LearningLesson> {

    Integer queryTotalPlan(@Param("userId") Long userId);
=======
 * @since 2022-06-30
 */
public interface LearningLessonMapper extends BaseMapper<LearningLesson> {

    /**
     * 分页查询我的课表，未学习过的课表按报名时间兜底排序
     *
     * @param page   分页对象
     * @param userId 用户id
     * @return 课表分页数据
     */
    IPage<LearningLesson> queryMyLessonsPage(IPage<LearningLesson> page, @Param("userId") Long userId);
>>>>>>> Stashed changes
}
