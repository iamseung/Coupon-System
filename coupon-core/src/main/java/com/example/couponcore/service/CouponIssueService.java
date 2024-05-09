package com.example.couponcore.service;

import com.example.couponcore.entity.Coupon;
import com.example.couponcore.entity.CouponIssue;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.example.couponcore.repository.mysql.CouponIssueRepository;
import com.example.couponcore.repository.mysql.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponIssueService {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;

    public void issue(long couponId, long userId) {
        Coupon coupon = findCoupon(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
    }

    @Transactional(readOnly = true)
    public Coupon findCoupon(long couponId) {
        return couponJpaRepository.findById(couponId).orElseThrow(() -> {
           throw new CouponIssueException(ErrorCode.COUPON_NOT_EXIST, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId));
        });
    }

    public CouponIssue saveCouponIssue(long couponId, long userId) {
        checkAlreadyIssuance(couponId, userId);
        CouponIssue issue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();

        return couponIssueJpaRepository.save(issue);
    }

    public void checkAlreadyIssuance(long couponId, long userId) {
        CouponIssue issue = couponIssueRepository.findFirstCouponIssue(couponId, userId);

        if(issue != null) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. user_id : %s, coupon_id %s".formatted(userId, couponId));
        }
    }
}
