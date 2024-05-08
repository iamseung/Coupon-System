package com.example.couponcore.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "coupon")
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
    public boolean availableIssuedQuantity() {
        // 무제한 쿠폰의 경우
        if(totalQuantity == null) {
            return true;
        }

        return totalQuantity > issuedQuantity;
    }

    // 쿠폰 발급 가능 여부 검증 2, 기한
    public boolean availableIssuedDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    // 쿠폰 발급
    public void issue() {
        if (!availableIssuedQuantity()) {
            throw new RuntimeException("수량 검증");
        }

        if (!availableIssuedDate()) {
            throw new RuntimeException("기한 검증");
        }

        issuedQuantity++;
    }
}
