package com.zw.project.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class RedisLimiterManagerTest {

    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Test
    void doRateLimit() throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            redisLimiterManager.doRateLimit("test");
            System.out.println("ok");
        }
        Thread.sleep(1000);
        for (int i = 0; i < 3; i++) {
            redisLimiterManager.doRateLimit("test");
            System.out.println("ok");
        }
    }
}