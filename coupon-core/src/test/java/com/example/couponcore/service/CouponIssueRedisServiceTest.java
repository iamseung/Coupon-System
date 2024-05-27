package com.example.couponcore.service;

import com.example.couponcore.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CouponIssueRedisServiceTest extends TestConfig {

    @Autowired
    CouponIssueRedisService sut;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void clear() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 존재하면 true 반환")
    void availableTotalIssueQuantity_1() {
        // Given
        int totalIssueQuantity = 10;
        long userId = 1;

        // When
        boolean result = sut.availableTotalIssueQuantity(totalIssueQuantity, userId);

        // Then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 존재하면 true 반환")
    void availableTotalIssueQuantity_2() {
        // Given
        int totalIssueQuantity = 10;
        long userId = 1;

        // When
        boolean result = sut.availableTotalIssueQuantity(totalIssueQuantity, userId);

        // Then
        Assertions.assertTrue(result);
    }
}