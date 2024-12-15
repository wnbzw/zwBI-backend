package com.zw.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/*
 * RedisTemplate配置
 * 自定义序列化
 */
@Configuration
public class RedisTemplateConfig {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        // 设置key的序列化方式,String 类型
        redisTemplate.setKeySerializer(RedisSerializer.string());
        return redisTemplate;
    }
}