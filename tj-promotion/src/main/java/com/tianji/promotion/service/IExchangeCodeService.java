package com.tianji.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;

/**
* @author Administrator
* @description 针对表【exchange_code(兑换码)】的数据库操作Service
* @createDate 2026-08-27 15:35:10
*/
public interface IExchangeCodeService extends IService<ExchangeCode> {
    void asyncGenerateCode(Coupon coupon);
}