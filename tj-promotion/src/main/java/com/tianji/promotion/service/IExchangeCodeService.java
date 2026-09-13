package com.tianji.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.query.CodeQuery;
import com.tianji.promotion.domain.vo.ExchangeCodeVO;

import javax.validation.Valid;

/**
* @author Administrator
* @description 针对表【exchange_code(兑换码)】的数据库操作Service
* @createDate 2026-08-27 15:35:10
*/
public interface IExchangeCodeService extends IService<ExchangeCode> {
    void asyncGenerateCode(Coupon coupon);

    PageDTO<ExchangeCodeVO> queryExchangeCodeByPage(@Valid CodeQuery query);
}