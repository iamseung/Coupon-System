package com.example.couponapi.controller;

import com.example.couponapi.controller.dto.CouponIssueRequestDto;
import com.example.couponapi.service.CouponIssueRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CouponIssueController {
    private final CouponIssueRequestService couponIssueRequestService;

    @PostMapping("/v1/issue")
    public boolean issueV1(@RequestBody CouponIssueRequestDto body) {
        // 성공한다면 true 를 반환
        couponIssueRequestService.issueRequestV1(body);
        return true;
    }
}