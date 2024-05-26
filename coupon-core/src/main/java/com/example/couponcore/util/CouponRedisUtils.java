package com.example.couponcore.util;

public class CouponRedisUtils {
    public static String getIssueRequestKey(long couponId) {
        return "issue.request.couponId=%s".formatted(couponId);
    }
}
