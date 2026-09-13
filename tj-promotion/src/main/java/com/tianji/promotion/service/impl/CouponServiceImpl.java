package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.dto.CouponIssueFormDTO;
import com.tianji.promotion.domain.enums.CouponStatus;
import com.tianji.promotion.domain.enums.ObtainType;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.ICouponService;
import com.tianji.promotion.service.IExchangeCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.tianji.promotion.domain.enums.CouponStatus.*;

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

    @Override
    public PageDTO<CouponPageVO> queryCouponByPage(CouponQuery query) {
        //1.获取优惠券的状态
        Integer status = query.getStatus();
        //获取优惠券的名称
        String name = query.getName();
        //获取优惠券的类型
        Integer type = query.getType();
        //2.分页查询
        Page<Coupon> page = lambdaQuery()
                .eq(type != null, Coupon::getType, type)
                .eq(status != null, Coupon::getStatus, status)
                .like(StringUtils.isNotBlank(name), Coupon::getName, name)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        //3.处理VO
        List<Coupon> records = page.getRecords();
        if (CollUtils.isEmpty(records))
            return PageDTO.empty(page);

        List<CouponPageVO> list = BeanUtils.copyList(records, CouponPageVO.class);

        return PageDTO.of(page, list);
    }

    private final IExchangeCodeService codeService;

    @Transactional
    @Override
    public void beginIssue(CouponIssueFormDTO dto) {
        // 1.查询优惠券
        Coupon coupon = getById(dto.getId());
        if (coupon == null) {
            throw new BadRequestException("优惠券不存在！");
        }
        // 2.判断优惠券状态，是否是暂停或待发放
        if(coupon.getStatus() != CouponStatus.DRAFT && coupon.getStatus() != PAUSE){
            throw new BizIllegalException("优惠券状态错误！");
        }
        // 3.判断是否是立刻发放
        LocalDateTime issueBeginTime = dto.getIssueBeginTime();
        LocalDateTime now = LocalDateTime.now();
        boolean isBegin = issueBeginTime == null || !issueBeginTime.isAfter(now);
        // 4.更新优惠券
        // 4.1.拷贝属性到PO
        Coupon c = BeanUtils.copyBean(dto, Coupon.class);
        // 4.2.更新状态
        if (isBegin) {
            c.setStatus(ISSUING);
            c.setIssueBeginTime(now);
        }else{
            c.setStatus(UN_ISSUE);
        }
        // 4.3.写入数据库
        updateById(c);

        // 5.判断是否需要生成兑换码，优惠券类型必须是兑换码，优惠券状态必须是待发放
        if(coupon.getObtainWay() == ObtainType.ISSUE && coupon.getStatus() == CouponStatus.DRAFT){
            coupon.setIssueEndTime(c.getIssueEndTime());
            codeService.asyncGenerateCode(coupon);
        }
    }

    @Transactional
    @Override
    public void updateCouponById(Long id, CouponFormDTO dto) {
        if (id == null||!id.equals(dto.getId())){
            throw new BadRequestException("优惠券id错误！");
        }
        Coupon coupon = this.getById(id);
        if (coupon == null){
            throw new BadRequestException("优惠券不存在！");
        }
        if (coupon.getStatus() != CouponStatus.DRAFT){
            throw new BizIllegalException("优惠券状态错误！");
        }
        Coupon po = BeanUtils.copyBean(dto, Coupon.class);
        updateById(po);
        if (coupon.getSpecific()){
            scopeService.remove(new QueryWrapper<CouponScope>().eq("coupon_id", id));
        }
        if (dto.getSpecific()){
            List<Long> scopes = dto.getScopes();
            if (CollUtils.isEmpty(scopes)){
                throw new BadRequestException("请选择优惠券的限定范围！");
            }
            List<CouponScope> list = scopes.stream()
                    .map(bizId->new CouponScope().setCouponId(coupon.getId()).setBizId(bizId).setType(1)).collect(Collectors.toList());
            scopeService.saveOrUpdateBatch(list);
        }
    }

    @Override
    public void deleteCouponById(Long id) {
        Coupon coupon = getById(id);
        if (coupon == null){
            throw new BadRequestException("优惠券不存在！");
        }
        if (coupon.getStatus() != CouponStatus.DRAFT){
            throw new BizIllegalException("优惠券状态错误！");
        }
        removeById(id);
        if (!coupon.getSpecific()) {
            return;
        }

        scopeService.remove(new QueryWrapper<CouponScope>().eq("coupon_id", id));
    }

    @Transactional
    @Override
    public void issueCoupons(List<Coupon> needIssueCouponList) {
        for (Coupon coupon : needIssueCouponList) {
            coupon.setStatus(CouponStatus.ISSUING);
        }
        updateBatchById(needIssueCouponList);
    }

    @Transactional
    @Override
    public void endIssueCoupons(List<Coupon> needEndIssueCoupon) {
        for (Coupon coupon : needEndIssueCoupon) {
            coupon.setStatus(CouponStatus.UN_ISSUE);
        }
        updateBatchById(needEndIssueCoupon);
    }
}





