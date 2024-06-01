package com.example.couponcore.service;

import com.example.couponcore.component.DistributeLockExecutor;
import com.example.couponcore.entity.Coupon;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.repository.redis.dto.CouponIssueRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.couponcore.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV1 {
    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DistributeLockExecutor distributeLockExecutor;

    public void issue(long couponId, long userId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);

        // Redis 에서 요청 큐의 수 검증
        if(!couponIssueRedisService.availableTotalIssueQuantity(coupon.getTotalQuantity(), couponId)) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과했습니다.. couponId : %s, userId : %s".formatted(couponId, userId));
        }

        /*
        Redis 의 Command 자체는 Single Thread 를 처리가 되기 때문에 동시성 이슈가 없지만
        로직에서 Redis 를 사용하는 과정이 분리가 되어 있기 때문에 동시성 이슈가 발생할 수 있다!
        그렇기에 동시성 제어 사용
         */
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            // 중복 발급 검증
            if(!couponIssueRedisService.availableUserIssueQuantity(couponId, userId)) {
                throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급 요청이 처리됐습니다. couponId : %s, userId : %s".formatted(couponId, userId));
            };

            // 발급 일자 조회
            if(!coupon.availableIssueDate()) {
                throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다. couponId : %s, userId : %s".formatted(couponId, userId));
            }

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
            redisRepository.rPush(getIssueRequestQueueKey(), String.valueOf(userId));
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input : %s".formatted(issueRequest));
        }
    }
}
