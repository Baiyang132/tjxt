package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
<<<<<<< Updated upstream
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
import com.tianji.learning.domain.po.LearningRecord;

=======
import com.tianji.api.dto.leanring.LearningRecordFormDTO;
import com.tianji.learning.domain.po.LearningRecord;

import java.util.List;

>>>>>>> Stashed changes
/**
 * <p>
 * 学习记录表 服务类
 * </p>
 *
 * @author 虎哥
<<<<<<< Updated upstream
 * @since 2022-12-10
 */
public interface ILearningRecordService extends IService<LearningRecord> {

    LearningLessonDTO queryLearningRecordByCourse(Long courseId);

    void addLearningRecord(LearningRecordFormDTO formDTO);
=======
 * @since 2022-06-30
 */
public interface ILearningRecordService extends IService<LearningRecord> {

    /**
     * 提交学习记录，更新学习进度
     *
     * @param formDTO 学习记录表单
     */
    void submitLearningRecord(LearningRecordFormDTO formDTO);

    /**
     * 根据课表id查询学习记录，按学习时间排序
     *
     * @param lessonId 课表id
     * @return 学习记录列表
     */
    List<LearningRecord> queryRecordsByLessonId(Long lessonId);
>>>>>>> Stashed changes
}
