package com.example.couponcore.repository.redis.dto;

import com.example.couponcore.entity.Coupon;
import com.example.couponcore.entity.CouponType;
import com.example.couponcore.exception.CouponIssueException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;

import java.time.LocalDateTime;

import static com.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static com.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

public record CouponRedisEntity(
        Long id,
        CouponType couponType,
        Integer totalQuantity,
        // 쿠폰 소진 여부
        boolean availableIssueQuantity,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueStart,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueEnd
) {

    public CouponRedisEntity(Coupon coupon) {
        this(
                coupon.getId(),
                coupon.getCouponType(),
                coupon.getTotalQuantity(),
                coupon.availableIssueQuantity(),
                coupon.getDateIssueStart(),
                coupon.getDateIssueEnd()
        );
    }

    // 쿠폰 발급 기한에 대한 검증, 현재 시간 기준
    private boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    // 쿠폰 발급 검증
    // 1. 수량
    // 2. 기한
    public void checkIssuableCoupon() {
        if (!availableIssueQuantity) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "모든 발급 수량이 소진되었습니다. couponId : %s".formatted(id));
        }
        if (!availableIssueDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다. request : %s, issueStart: %s, issueEnd: %s".formatted(LocalDateTime.now(), dateIssueStart, dateIssueEnd));
        }
    }

}
