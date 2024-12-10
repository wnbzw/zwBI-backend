package com.zw.project.manager;

import com.google.common.util.concurrent.RateLimiter;
import com.zw.project.common.ErrorCode;
import com.zw.project.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Redis 限流器,专门提供 RedisLimiter 限流基础服务
 */
@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    public void doRateLimit(String key) {
        // 获取分布式限流对象
        // 1. 获取分布式限流对象
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 2. 执行限流操作
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        // 3. 返回结果
        //一个操作请求一个令牌
        boolean b = rateLimiter.tryAcquire(1);
        if (!b) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
