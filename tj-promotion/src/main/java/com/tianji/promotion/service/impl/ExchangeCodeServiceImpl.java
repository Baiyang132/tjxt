package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.query.CodeQuery;
import com.tianji.promotion.domain.vo.ExchangeCodeVO;
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
    @Override
    @Async("generateExchangeCodeExecutor")
    public void asyncGenerateCode(Coupon coupon) {
        Integer totalNum = coupon.getTotalNum();
        Long result = serialOps.increment(totalNum);
        if (result == null) {
            return;
        }
        int maxSerialNum = result.intValue();
        List<ExchangeCode> list = new ArrayList<>(totalNum);
        for (int serialNum = maxSerialNum - totalNum + 1; serialNum <= maxSerialNum; serialNum++) {
            String code = CodeUtil.generateCode(serialNum, coupon.getId());
            ExchangeCode e = new ExchangeCode();
            e.setCode(code);
            e.setId(serialNum);
            e.setExchangeTargetId(coupon.getId());
            e.setExpiredTime(coupon.getIssueEndTime());
            list.add(e);
        }
        saveBatch(list);
        redisTemplate.opsForZSet().add(PromotionConstants.COUPON_RANGE_KEY, coupon.getId().toString(), maxSerialNum);
    }

    @Override
    public PageDTO<ExchangeCodeVO> queryExchangeCodeByPage(CodeQuery query) {
        Page<ExchangeCode> page = lambdaQuery()
                .eq(ExchangeCode::getExchangeTargetId, query.getCouponId())
                .eq(ExchangeCode::getStatus, query.getStatus())
                .page(query.toMpPage());
        return PageDTO.of(page, exchangeCode -> new ExchangeCodeVO(exchangeCode.getId(), exchangeCode.getCode()));
    }
}