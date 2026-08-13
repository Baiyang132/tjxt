package com.tianji.learning.utils;

import com.tianji.common.utils.JsonUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.classify.ClassifierSupport;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningRecordDelayTaskHandler {

    //注入RedisTemplate
    private final StringRedisTemplate redisTemplate;
    //缓存键的模版
    private final static String RECORD_KEY_TEMPLATE = "learning:record:{}";
    //创建延时队列
    private final DelayQueue<DelayTask<RecordTaskData>> queue = new DelayQueue<>();

    //注入学习记录的Mapper(不注入学习记录的Service,为了避免两个类循环以来，因为需要在学习记录的Service中使用当前类)
    private final LearningRecordMapper recordMapper;
    //注入当前正在学习的课程Service
    private final ILearningLessonService lessonService;

    //延迟任务处理开关
    private static volatile  boolean begin = true;


    //在 Spring 容器实例化并注入依赖后，自动执行该方法。这确保了应用启动时，延迟任务处理器就能立即开始工作，处理用户的学习记录
    @PostConstruct
    public void init(){
        //使用 CompletableFuture.runAsync() 异步执行 handleDelayTask 方法：
        //非阻塞执行：在后台线程池中运行延迟任务处理逻辑
        //避免主线程阻塞：因为 handleDelayTask 是无限循环，异步执行可确保应用正常启动
        //提升性能：不占用主线程资源，实现并发处理
        CompletableFuture.runAsync(this::handleDelayTask);
    }

    @PreDestroy
    public void destroy(){
        begin = false;
        log.debug("延迟任务停止执行！");
    }


    //4.处理延迟队列任务
    public void handleDelayTask(){
        while (begin){
            try {
                //1.获取延迟队列任务
                DelayTask<RecordTaskData> delayTask = queue.take();
                //2.获取任务数据
                RecordTaskData taskData = delayTask.getData();
                //3.查询缓存
                LearningRecord record = readRecordCache(taskData.lessonId, taskData.sectionId);
                if (record == null){
                    //缓存数据不存在，避免循环中断，继续下一次循环
                    continue;
                }
                //4.比较数据，moment值
                if (!Objects.equals(taskData.getMoment(), record.getMoment())){
                    //如果不一致，说明用户在观看，放弃旧数据
                    continue;
                }
                //5.如果一致，说明用户已经停止观看，持久化播放进度到数据库
                //更新学习记录的moment
                record.setFinished(null);//缓存中的finished有可能没有数据库的新
                recordMapper.updateById(record);
                //更新课程表的学习信息
                LearningLesson learningLesson = new LearningLesson();
                learningLesson.setId(taskData.lessonId);
                learningLesson.setLatestSectionId(taskData.sectionId);
                learningLesson.setLatestLearnTime(LocalDateTime.now());
                lessonService.updateById(learningLesson);

            } catch (InterruptedException e) {
                log.error("处理延迟队列任务异常", e);
            }
        }
    }

    //3.清除缓存数据
    public void cleanRecordCache(Long lessonId, Long sectionId){
        //获取key
        String key = StringUtils.format(RECORD_KEY_TEMPLATE, lessonId);
        //删除数据
        redisTemplate.opsForHash().delete(key,sectionId.toString());
    }

    //2.从缓存中读取数据
    public LearningRecord readRecordCache(Long lessonId, Long sectionId){
        try {
            //1.获取缓存键
            //得到redis的key
            String key = StringUtils.format(RECORD_KEY_TEMPLATE, lessonId);
            //读取数据
            Object cacheData = redisTemplate.opsForHash().get(key, sectionId.toString());
            if (cacheData == null) {
                return null;
            }
            //2.数据检查及转换
            return JsonUtils.toBean(cacheData.toString(), LearningRecord.class);
        }catch (Exception e){
            log.error("从缓存中读取数据异常", e);
            return null;
        }
    }


    //1.添加学习任务到缓存，并添加延迟任务到延迟队列
    public void addLearningRecordTask(LearningRecord  record){
        //1.添加数据到Redis缓存
        writeRecordCache(record);
        //2.提交延迟任务到延迟队列 DelayQueue
        //转换延迟队列任务数据
        RecordTaskData recordTaskData = new RecordTaskData(record);
        //创建延迟队列任务
        DelayTask<RecordTaskData> delayTask = new DelayTask<>(recordTaskData, Duration.ofSeconds(20));
        //添加延迟队列任务
        queue.add(delayTask);

    }

    //添加数据到Redis缓存
    public void writeRecordCache(LearningRecord record) {
        log.debug("更新学习记录的缓存数据");
        try {
            //1.数据转换,转换成json
            String json = JsonUtils.toJsonStr(new RecordCacheData(record));
            //2.写入Redis
            String key = StringUtils.format(RECORD_KEY_TEMPLATE, record.getLessonId());
            redisTemplate.opsForHash().put(key, record.getSectionId().toString(), json);
            //3.添加缓存过期时间
            redisTemplate.expire(key, Duration.ofMinutes(1));
        } catch (Exception e) {
            log.error("更新学习记录缓存异常", e);
        }
        log.debug("更新学习记录的缓存数据成功");
    }

    @Data
    @NoArgsConstructor
    // 缓存数据类
    private static class RecordCacheData{
        // 小节id
        private Long id;
        // 播放时长
        private Integer moment;
        // 是否完成
        private Boolean finished;

        // 构造方法转换数据
        public RecordCacheData(LearningRecord record) {
            this.id = record.getId();
            this.moment = record.getMoment();
            this.finished = record.getFinished();
        }
    }

    //延迟队列任务类
    @Data
    @NoArgsConstructor
    private static class RecordTaskData{
        // 课程id
        private Long lessonId;
        // 小节id
        private Long sectionId;
        // 播放时长
        private Integer moment;

        public RecordTaskData(LearningRecord record) {
            this.lessonId = record.getLessonId();
            this.sectionId = record.getSectionId();
            this.moment = record.getMoment();
        }
    }
}
