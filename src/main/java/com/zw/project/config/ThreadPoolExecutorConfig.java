package com.zw.project.config;

import org.ehcache.xml.model.ThreadPoolsType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {

            //初始化线程数为1
            private int count = 1;

            @Override
            //每当线程池需要创建新线程时,就会调用newThread方法
            public Thread newThread(Runnable r) {
                //创建一个线程
                Thread thread = new Thread(r);
                //设置线程名称
                thread.setName("线程" + count++);
                return thread;
            }
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,4,100, TimeUnit.SECONDS
                ,new ArrayBlockingQueue<>(4),threadFactory);
        return threadPoolExecutor;
    }
}
