package com.example.couponcore.entity;

import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static com.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "coupons")
public class Coupon extends  AuditingFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false) private CouponType couponType;
    @Column(nullable = false) private String title;

    private Integer totalQuantity;

    @Column(nullable = false) private int issuedQuantity;
    @Column(nullable = false) private int discountAmount;
    @Column(nullable = false) private int minAvailableAmount;
    @Column(nullable = false) LocalDateTime dateIssueStart;
    @Column(nullable = false) LocalDateTime dateIssueEnd;

    // 쿠폰 발급 가능 여부 검증 1, 수량
    public boolean availableIssueQuantity() {
        // 무제한 쿠폰의 경우
        if(totalQuantity == null) {
            return true;
        }

        return totalQuantity > issuedQuantity;
    }

    // 쿠폰 발급 가능 여부 검증 2, 기한
    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    // 쿠폰 발급
    public void issue() {
        if (!availableIssueQuantity()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. total : %s, issued: %s".formatted(totalQuantity, issuedQuantity));
        }
        if (!availableIssueDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다. request : %s, issueStart: %s, issueEnd: %s".formatted(LocalDateTime.now(), dateIssueStart, dateIssueEnd));
        }

        issuedQuantity++;
    }
}
