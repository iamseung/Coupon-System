package com.example.couponcore.service;

import com.example.couponcore.entity.Coupon;
import com.example.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponCacheService {
    private final CouponIssueService couponIssueService;

    // 캐싱, 30분
    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(long couponId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);
        return new CouponRedisEntity(coupon);
    }

    // LocalCacheConfig Bean
    // 자동으로 데이터가 있으면 반환, 없으면 getCouponCache Redis 데이터 조회
    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity getCouponLocalCache(long couponId) {
        return proxy().getCouponCache(couponId);
    }

    // 캐시 업데이트
    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponCache(long couponId) {
        return getCouponCache(couponId);
    }

    // 캐시 업데이트
    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponLocalCache(long couponId) {
        return getCouponLocalCache(couponId);
    }

    // AopContext.currentProxy()를 사용해서 현재 실행 중인 메서드를 호출한 프록시 객체를 가져오게 설정
    private CouponCacheService proxy() {
        return ((CouponCacheService) AopContext.currentProxy());
    }
}
