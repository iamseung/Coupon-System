package com.example.couponapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // 부하 테스리
    @GetMapping("/hello")
    public String hello() {
        return "hello!";
    }
}
