package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.ICouponService;
import com.tianji.promotion.mapper.CouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Administrator
* @description 针对表【coupon(优惠券的规则信息)】的数据库操作Service实现
* @createDate 2026-08-27 15:35:10
*/
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon>
    implements ICouponService {

    //注入优惠券作用范围服务
    private final ICouponScopeService scopeService;

    @Override
    public void saveCoupon(CouponFormDTO dto) {
        //1.保存优惠卷
        //1.1将传入的dto转成po
        Coupon coupon = BeanUtils.copyBean(dto, Coupon.class);

        //1.2保存po
        save(coupon);

        //判断有没有适用范围限定
        if (!dto.getSpecific()) {
            return;
        }

        //2.获取优惠卷的id
        Long couponId = coupon.getId();

        //3.获取传入的dto中具体的使用范围
        List<Long>scopes = dto.getScopes();
        if(CollUtils.isEmpty(scopes)){
            throw new BadRequestException("使用范围不能为空");
        }

        //4.转换成使用范围的po



    }
}




