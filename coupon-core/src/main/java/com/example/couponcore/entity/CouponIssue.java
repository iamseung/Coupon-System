package com.example.couponcore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "coupon_issues")
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
