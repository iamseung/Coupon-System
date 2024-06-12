package com.example.couponcore.service;

import com.example.couponcore.component.DistributeLockExecutor;
import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.repository.redis.dto.CouponRedisEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV2 {
    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DistributeLockExecutor distributeLockExecutor;
    private final CouponCacheService couponCacheService;

    public void issue(long couponId, long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponLocalCache(couponId);
        coupon.checkIssuableCoupon();
        // 위에서 쿠폰 수량에 대한 검증을 선행으로 작업하기에 Redis 로 가는 부하를 줄일 수 있다.
        issueRequest(couponId, userId, coupon.totalQuantity());
    }

    /*
    1. 쿠폰 발급 수량 제어, totalQuantity > redisRepository.sCard(key);
    2. 중복 발급 요청 제어, !redisRepository.sIsMember(key, String.valueOf(userId);
    3. 쿠폰 발급 요청 저장, redisRepository.sAdd
    4. 쿠폰 발급 큐 적재, redisRepository.rPush

    4가지 단계를 스크립트로 실행
     */
    private void issueRequest(long couponId, long userId, Integer totalIssuedQuantity) {
        if(totalIssuedQuantity == null) {
            redisRepository.issueRequest(couponId, userId, Integer.MAX_VALUE);
        }

        redisRepository.issueRequest(couponId, userId, totalIssuedQuantity);
    }
}
