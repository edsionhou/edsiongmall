package com.hou.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    //读取配置文件中的redis的ip地址
    //此项目不是springboot项目，写了application.properties也没用， 到时候谁引入这个包，@Value加载谁的环境变量信息
    @Value("${spring.redis.host:disabled}")
    private String host ;
    @Value("${spring.redis.port:0}")
    private int port ;
    @Value("${spring.redis.database:0}")
    private int database;


    @Bean
    public RedisUtil getRedisUtil() {
        if (host.equals("disabled")) {
            return null;
        }
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initPool(host, port, database);
        return redisUtil;
    }
}
