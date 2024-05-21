package com.example.couponcore.component;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class DistributeLockExecutor {
    private final RedissonClient redissonClient;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public void execute(String lockName, long waitMilliSecond, long leaseMilliSecond, Runnable logic) {
        RLock lock = redissonClient.getLock(lockName);

        try {
            // tryLock을 사용하여 지정된 시간 동안 락을 시도.
            // waitMilliSecond 동안 락을 획득하지 못하면 false를 반환.
            boolean isLocked = lock.tryLock(waitMilliSecond, leaseMilliSecond, TimeUnit.MILLISECONDS);

            // 동시성 이슈 방지
            if(!isLocked) {
                throw new IllegalStateException("[" + lockName + "] lock 획득 실패");
            }

            logic.run();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 현재 스레드가 락을 보유하고 있다면 락을 해제.
            if(lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
