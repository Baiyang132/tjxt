package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.dto.LearningPlanDTO;
import com.tianji.learning.domain.po.LearningPlan;
import com.tianji.learning.domain.vo.LearningPlanVO;

/**
 * <p>
 * 学习计划 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2022-06-30
 */
public interface ILearningPlanService extends IService<LearningPlan> {

    /**
     * 创建学习计划
     *
     * @param dto 计划表单
     */
    void createMyPlan(LearningPlanDTO dto);

    /**
     * 修改学习计划
     *
     * @param dto 计划表单
     */
    void updateMyPlan(LearningPlanDTO dto);

    /**
     * 分页查询我的学习计划
     *
     * @param query 分页查询条件
     * @return 计划分页信息
     */
    PageDTO<LearningPlanVO> queryMyPlans(PageQuery query);

    /**
     * 处理学习进度，按天打卡更新本周学习天数
     *
     * @param lessonId 课表id
     */
    void handleLearningProgress(Long lessonId);

    /**
     * 每周一零点重置本周学习数据
     */
    void resetWeekData();
}
