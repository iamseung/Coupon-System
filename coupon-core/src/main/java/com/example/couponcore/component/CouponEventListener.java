package com.example.couponcore.component;

import com.example.couponcore.entity.event.CouponIssueCompleteEvent;
import com.example.couponcore.service.CouponCacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class CouponEventListener {
    private final CouponCacheService couponCacheService;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    // Commit 된 이후, 데이터 베이스에 반영 이후
    // Redis & 로컬 캐시 Refresh
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void issueComplete(CouponIssueCompleteEvent event) {
        log.info("issue complete. cache refresh start couponId : %s".formatted(event.couponId()));
        couponCacheService.putCouponCache(event.couponId());
        couponCacheService.putCouponLocalCache(event.couponId());
        log.info("issue complete. cache refresh end couponId : %s".formatted(event.couponId()));
    }
}
