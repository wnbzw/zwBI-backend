package com.zw.project.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zw.project.model.entity.Chart;
import com.zw.project.service.ChartService;
import com.zw.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


/*
 * 每日用户推荐缓存预热
 */
@Slf4j
@Component
public class PreCacheJob {
    @Resource
    private ChartService chartService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    //重点用户
    private List<Long> mainUserList = Arrays.asList(1l, 2l, 3l);

    //每天执行，预热推荐用户
    @Scheduled(cron = "0 6 11 * * *")
    public void doCacheChart(){

        //获取分布式锁
        RLock lock = redissonClient.getLock("zwbi:precachejob:docache:lock");
        try{
            //只有一个线程能获取到
            if (lock.tryLock(0,30000L,TimeUnit.MILLISECONDS)){
                System.out.println("getLock: "+Thread.currentThread().getId());
                for (Long userId: mainUserList){
                    QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("userId",userId);
                    queryWrapper.orderByDesc("createTime");
                    Page<Chart> chartPage = chartService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("zwbi:user:mychart:%s", userId);
                    ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
                    //写缓存
                    try {
                        valueOperations.set(redisKey,chartPage,300000, TimeUnit.MILLISECONDS);
                    }catch (Exception e){
                        log.error("redis set key error",e);
                    }
                }
            }
        }catch(InterruptedException e){
            log.error("doCacheRecommendUser error ",e);
        }finally {
            //只能释放自己的锁
            if (lock.isHeldByCurrentThread()){
                System.out.println("unlock: "+Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}