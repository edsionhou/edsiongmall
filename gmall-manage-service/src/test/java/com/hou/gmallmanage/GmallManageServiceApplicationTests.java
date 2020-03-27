package com.hou.gmallmanage;


import com.hou.conf.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageServiceApplicationTests {
    @Autowired
    RedisUtil redisUtil;
    @Test
    public void contextLoads() {
        System.out.println(redisUtil.getJedis());
    }

    @Test
    public void contextLoads1() {
        System.out.println("1");
        try {
            System.out.println("线程："+Thread.currentThread().getName()+"准备睡眠");
            Thread.sleep(3000);
            System.out.println("线程："+Thread.currentThread().getName()+"醒了");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        contextLoads1();
    }


}