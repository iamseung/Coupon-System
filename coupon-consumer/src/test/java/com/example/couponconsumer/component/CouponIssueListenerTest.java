package com.example.couponconsumer.component;

import com.example.couponconsumer.TestConfig;
import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.service.CouponIssueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Import(CouponIssueListener.class)
class CouponIssueListenerTest extends TestConfig {
    @Autowired
    CouponIssueListener sut;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    RedisRepository redisRepository;

    @MockBean
    CouponIssueService couponIssueService;

    @BeforeEach
    void clear() {
        // Set<String> keys = redisTemplate.keys("*");
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @Test
    @DisplayName("쿠폰 발급 큐에 처리 대상이 없으면 발급을 하지 않는다.")
    void issue_1() throws JsonProcessingException {
        // When
        // 큐를 기반으로 발급 요청
    	sut.issue();
        // Then
    	verify(couponIssueService, never()).issue(anyLong(), anyLong());
    }

    @Test
    @DisplayName("쿠폰 발급 큐에 처리 대상이 있으면 발급")
    void issue_2() throws JsonProcessingException {
        // Given
        long couponId = 1, userId = 1;
        int totalQuantity = Integer.MAX_VALUE;
        redisRepository.issueRequest(couponId, userId, totalQuantity);

        // When, 큐를 기반으로 발급 요청
        sut.issue();
        // Then, times(1) -> 호출
        verify(couponIssueService, times(1)).issue(anyLong(), anyLong());
    }

    @Test
    @DisplayName("쿠폰 발급 요청 순서에 맞게 처리")
    void issue_3() throws JsonProcessingException {
        // Given
        long couponId = 1, userId1 = 1, userId2 = 2, userId3 = 3;
        int totalQuantity = Integer.MAX_VALUE;
        redisRepository.issueRequest(couponId, userId1, totalQuantity);
        redisRepository.issueRequest(couponId, userId2, totalQuantity);
        redisRepository.issueRequest(couponId, userId3, totalQuantity);

        // When, 큐를 기반으로 발급 요청
        sut.issue();
        // Then, inOrder 객체는 couponIssueService 객체에 대한 메소드 호출 순서를 추적하기 위해 생성.
        InOrder inOrder = inOrder(couponIssueService);
        inOrder.verify(couponIssueService, times(1)).issue(couponId, userId1);
        inOrder.verify(couponIssueService, times(1)).issue(couponId, userId2);
        inOrder.verify(couponIssueService, times(1)).issue(couponId, userId3);
    }
}