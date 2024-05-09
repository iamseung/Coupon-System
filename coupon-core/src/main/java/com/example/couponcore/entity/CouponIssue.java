package com.example.couponcore.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Builder
@Entity
@Table(name = "coupon_issue")
public class CouponIssue extends AuditingFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long couponId;
    @Column(nullable = false) private Long userId;

    @CreatedDate
    @Column(nullable = false) private LocalDateTime dateIssued;

    private LocalDateTime dateUsed;
}
