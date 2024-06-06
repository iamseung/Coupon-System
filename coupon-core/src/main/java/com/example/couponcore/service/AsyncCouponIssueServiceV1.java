package com.example.couponcore.service;

import com.example.couponcore.component.DistributeLockExecutor;
import com.example.couponcore.entity.Coupon;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.repository.redis.dto.CouponIssueRequest;
import com.example.couponcore.repository.redis.dto.CouponRedisEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.example.couponcore.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@Slf4j
@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV1 {
    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DistributeLockExecutor distributeLockExecutor;
    private final CouponCacheService couponCacheService;

    public void issue(long couponId, long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        // 발급 일자 조회
        coupon.checkIssuableCoupon();

        /*
        Redis 의 Command 자체는 Single Thread 를 처리가 되기 때문에 동시성 이슈가 없지만
        로직에서 Redis 를 사용하는 과정이 분리가 되어 있기 때문에 동시성 이슈가 발생할 수 있다!
        그렇기에 동시성 제어 사용
         */
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId);
            issueRequest(couponId, userId);
        });
    }

    // Redis 에 요청 적재, 발급 요청
    private void issueRequest(long couponId, long userId) {
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);

        try {
            // 직렬화
            String value = objectMapper.writeValueAsString(issueRequest);
            redisRepository.sADD(getIssueRequestKey(couponId), String.valueOf(userId));
            // 쿠폰 발급 대기열 관리 List, Queue 에 삽입
            redisRepository.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input : %s".formatted(issueRequest));
        }
    }
}
