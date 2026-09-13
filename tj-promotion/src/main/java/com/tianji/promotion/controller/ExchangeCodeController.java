package com.tianji.promotion.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.query.CodeQuery;
import com.tianji.promotion.domain.vo.ExchangeCodeVO;
import com.tianji.promotion.service.IExchangeCodeService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author Administrator
 * @description 针对表【exchange_code(兑换码)】的数据库操作Service实现
 * @createDate 2026-08-27 15:35:10
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/codes")
public class ExchangeCodeController {

    private final IExchangeCodeService codeService;

    @ApiOperation("分也查询兑换码")
    @GetMapping("/page")
    public PageDTO<ExchangeCodeVO> queryExchangeCodeByPage(@Valid CodeQuery query) {
        return codeService.queryExchangeCodeByPage(query);
    }
}




