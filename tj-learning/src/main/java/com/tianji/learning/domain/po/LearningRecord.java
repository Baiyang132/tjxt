package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 学习记录表
 * </p>
 *
 * @author 虎哥
<<<<<<< Updated upstream
 * @since 2022-12-10
=======
 * @since 2022-06-30
>>>>>>> Stashed changes
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("learning_record")
public class LearningRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
<<<<<<< Updated upstream
     * 学习记录的id
=======
     * 学习记录id
>>>>>>> Stashed changes
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
<<<<<<< Updated upstream
     * 对应课表的id
=======
     * 课表id
>>>>>>> Stashed changes
     */
    private Long lessonId;

    /**
<<<<<<< Updated upstream
     * 对应小节的id
=======
     * 小节id
>>>>>>> Stashed changes
     */
    private Long sectionId;

    /**
<<<<<<< Updated upstream
     * 用户id
=======
     * 学生id
>>>>>>> Stashed changes
     */
    private Long userId;

    /**
<<<<<<< Updated upstream
     * 视频的当前观看时间点，单位秒
=======
     * 视频的当前观看时长，单位秒
>>>>>>> Stashed changes
     */
    private Integer moment;

    /**
     * 是否完成学习，默认false
     */
    private Boolean finished;

    /**
<<<<<<< Updated upstream
     * 第一次观看时间
     */
    private LocalDateTime createTime;

    /**
=======
>>>>>>> Stashed changes
     * 完成学习的时间
     */
    private LocalDateTime finishTime;

    /**
<<<<<<< Updated upstream
     * 更新时间（最近一次观看时间）
     */
    private LocalDateTime updateTime;


=======
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
>>>>>>> Stashed changes
}
