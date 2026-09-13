package com.tianji.promotion.handler;


import com.tianji.common.utils.CollUtils;
import com.tianji.promotion.domain.enums.CouponStatus;
import com.tianji.promotion.domain.po.Coupon;

import com.tianji.promotion.service.ICouponService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueTask {

    private final ICouponService couponService;

    @XxlJob("beginCouponIssueJob")
    public void beginCouponIssueJob(){
        log.info("正在执行优惠券发放任务");
        //1.查询所有复合条件的优惠券，状态为为发放或者开始发放时间小于当前时间
        List<Coupon> needIssueCoupon = couponService.lambdaQuery()
                .eq(Coupon::getStatus, CouponStatus.UN_ISSUE)
                .le(Coupon::getIssueBeginTime, LocalDateTime.now())
                .list();
        //2.判断是否有优惠券需要发放
        if (CollUtils.isEmpty(needIssueCoupon)) {
            log.info("没有需要发放的优惠券");
            return;
        }
        //3.批量发放复合条件的优惠券
        log.info("正在发放优惠券，数量为{}", needIssueCoupon.size());
        couponService.issueCoupons(needIssueCoupon);
    }

    @XxlJob("endCouponIssueJob")
    public void endCouponIssueJob(){
        log.info("正在执行优惠券结束发放任务");
        List<Coupon> needEndIssueCoupon = couponService.lambdaQuery()
                .eq(Coupon::getStatus, CouponStatus.ISSUING)
                .le(Coupon::getIssueEndTime, LocalDateTime.now())
                .list();
        if (CollUtils.isEmpty(needEndIssueCoupon)) {
            log.info("没有需要结束发放的优惠券");
            return;
        }
        log.info("正在结束发放优惠券，数量为{}", needEndIssueCoupon.size());
        couponService.endIssueCoupons(needEndIssueCoupon);
    }

}