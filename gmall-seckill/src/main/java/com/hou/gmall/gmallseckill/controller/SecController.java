package com.hou.gmall.gmallseckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hou.conf.RedisUtil;
import com.hou.gmall.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

/**
 * @create 2020-05-25 22:00
 */
@Controller
public class SecController {

    @Reference
    CartService cartService;

    @Autowired
    RedisUtil redisUtil;

    @GetMapping("/kill")
    @ResponseBody
    public  String kill(){
        String memberId  = "1";
        Jedis jedis = redisUtil.getJedis();
        String s = jedis.get("106");
        System.out.println("当前剩余"+s);
        Long aLong = jedis.incrBy("106", -1); //这好像是原子操作
        jedis.close(); //重要！！！
        return "1";
    }
}
