package com.tianji.learning.schedule;

import com.tianji.learning.service.ILearningPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 学习计划定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningPlanResetTask {

    private final ILearningPlanService planService;

    /**
     * 每周一零点重置本周学习数据
     */
    @Scheduled(cron = "0 0 0 * * MON")
    public void resetWeekData() {
        log.info("定时任务：重置学习计划本周数据");
        planService.resetWeekData();
    }
}
