package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.mapper.ExchangeCodeMapper;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.utils.CodeUtil;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 兑换码 服务实现类
 * </p>
 *
 * @author sxlnsm
 * @since 2026-06-24
 */
@Service
public class ExchangeCodeServiceImpl extends ServiceImpl<ExchangeCodeMapper, ExchangeCode> implements IExchangeCodeService {

    // Redis模板工具类，用于执行Redis操作
    private final StringRedisTemplate redisTemplate;

    // 绑定的值操作对象，专门用于操作优惠券序列号
    private final BoundValueOperations<String, String> serialOps;

    /**
     * 构造兑换码服务实现类，初始化Redis操作组件
     *
     * @param redisTemplate Redis模板工具类，用于操作Redis缓存
     */
    public ExchangeCodeServiceImpl(StringRedisTemplate redisTemplate) {
        // 将传入的Redis模板赋值给成员变量
        this.redisTemplate = redisTemplate;
        // 绑定到指定的Redis键，用于操作优惠券序列号
        this.serialOps = redisTemplate.boundValueOps(PromotionConstants.COUPON_CODE_SERIAL_KEY);
    }


    /**
     * 异步生成兑换码
     * 该方法通过异步方式批量生成兑换码，执行流程如下：
     * <p>
     * 从Redis获取自增序列号，确保分布式环境下的序列号唯一性
     * 根据序列号和优惠券ID生成兑换码
     * 批量保存兑换码到数据库
     * 将兑换码的最大序列号写入Redis缓存，用于后续查询和管理
     *
     * @param coupon 优惠券对象，包含发放数量、优惠券ID、发放结束时间等信息
     */
    // 重写接口方法
    @Override
    // 使用异步执行器异步执行该方法
    @Async("generateExchangeCodeExecutor")
    public void asyncGenerateCode(Coupon coupon) {
        // 从优惠券对象中获取需要生成的兑换码总数量
        Integer totalNum = coupon.getTotalNum();

        // 在Redis中对序列号进行原子自增操作，增加的数量等于需要生成的兑换码数量
        Long result = serialOps.increment(totalNum);
        // 如果自增操作返回null，说明操作失败，直接返回
        if (result == null) {
            return;
        }
        // 将Long类型的结果转换为int类型，得到最大序列号
        int maxSerialNum = result.intValue();

        // 创建ArrayList集合用于存储兑换码对象，初始容量设置为totalNum
        List<ExchangeCode> list = new ArrayList<>(totalNum);
        // 循环遍历从起始序列号到最大序列号的范围
        for (int serialNum = maxSerialNum - totalNum + 1; serialNum <= maxSerialNum; serialNum++) {
            // 调用编码工具类，根据序列号和优惠券ID生成唯一的兑换码
            String code = CodeUtil.generateCode(serialNum, coupon.getId());
            // 创建兑换码对象
            ExchangeCode e = new ExchangeCode();
            // 设置兑换码
            e.setCode(code);
            // 设置兑换码的ID为序列号
            e.setId(serialNum);
            // 设置兑换码关联的优惠券ID
            e.setExchangeTargetId(coupon.getId());
            // 设置兑换码的过期时间为优惠券发放结束时间
            e.setExpiredTime(coupon.getIssueEndTime());
            // 将兑换码对象添加到列表中
            list.add(e);
        }

        // 批量保存兑换码列表到数据库
        saveBatch(list);

        // 将优惠券ID和对应的最大序列号存入Redis的ZSet中，用于记录兑换码的范围
        redisTemplate.opsForZSet().add(PromotionConstants.COUPON_RANGE_KEY, coupon.getId().toString(), maxSerialNum);
    }
}