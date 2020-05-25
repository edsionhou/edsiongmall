package com.hou.gmallorder.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hou.conf.ActiveMQUtil;
import com.hou.conf.RedisUtil;
import com.hou.gmall.bean.OmsCartItem;
import com.hou.gmall.bean.OmsOrder;
import com.hou.gmall.bean.OmsOrderItem;
import com.hou.gmall.service.CartService;
import com.hou.gmall.service.OrderService;
import com.hou.gmallorder.mapper.OmsOrderItemMapper;
import com.hou.gmallorder.mapper.OmsOrderMapper;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Reference
    CartService cartService;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public String generateTradeCode(String memberId) { //新建交易码 存入redis
        Jedis jedis = null;
        String tradeCode = "";
        try {
            jedis = redisUtil.getJedis();
            String tradeKey = "user:" + memberId + ":tradeCode";
           tradeCode = UUID.randomUUID().toString();
            jedis.setex(tradeKey, 60 * 60 * 2, tradeCode);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }


        return tradeCode;
    }

    @Override
    public String checkTradeCode(String memberId,String tradeCode) {
        Jedis jedis = null;
        RLock lock = null;
        try {
            lock = redissonClient.getLock("lock"+memberId);  //同时多个线程访问同个memberId 上锁
            lock.lock();
            jedis = redisUtil.getJedis();
            String tradeKey = "user:" + memberId + ":tradeCode";
            /*
            多个线程同时执行此方法时
            A获得 tradeKey时，执行del之前 时间片到期，BCDE线程都获得了tradeKey，
            A再删除已经晚了，所有线程都返回的success，存在线程安全问题。 需要redis分布式锁
            或者 lua脚本 原子对比删除
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Jedis jedis = jedisPool.getResource();
            Long eval = (Long) jedis.eval(script, Collections.singletonList(KEYS[1]),
                    Collections.singletonList(ARGV[1]));
             */
            String tradeCodeInCache = jedis.get(tradeKey);
            if (StringUtils.isNotBlank(tradeCodeInCache) && tradeCodeInCache.equals(tradeCode)) {
                jedis.del(tradeKey);
                return "success";
            }
            return "falied";
        } catch (Exception e) {
            e.printStackTrace();
            return "falied";
        } finally {
            jedis.close();
            lock.unlock();
        }

    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
         //保存订单 order  自动返回主键给 omsOrder
        omsOrderMapper.insertSelective(omsOrder);
        //保存订单详情 order-item
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(omsOrder.getId());
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //同时删除购物车的商品
            cartService.delItem(omsOrderItem.getProductSkuId()); //根据skuId删除

        }
    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNumber) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNumber);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    @Override
    public void updateOrderStatus(String outTradeNo) {  //更新订单状态
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        omsOrder.setStatus(1);
        Example e = new Example(OmsOrder.class);
        e.createCriteria().andEqualTo("orderSn", outTradeNo);
        omsOrderMapper.updateByExampleSelective(omsOrder, e);

        //发送一个订单已支付的队列，提供给库存消费
        OmsOrder omsOrderNew = omsOrderMapper.selectOne(omsOrder);
        /*
         发送信息到 消费队列
         */
        ORDER_PAY_QUEUE(omsOrderNew);
    }


    public void ORDER_PAY_QUEUE(OmsOrder omsOrder){   //订单已支付 ---> 库存消费
        Session session = null;
        Connection connection = null;
        try {
            ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);//开启MQ的事务
            Queue queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(queue);
            TextMessage textMessage = session.createTextMessage();

            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setOrderSn(omsOrder.getOrderSn());
            List<OmsOrderItem> select = omsOrderItemMapper.select(omsOrderItem);
            omsOrder.setOmsOrderItems(select);  //将omsorderitems 插入
            textMessage.setText(JSON.toJSONString(omsOrder));
            producer.send(textMessage);   //将omsorder转换为json数据发送


            session.commit(); //提交事务
            System.out.println("提交给库存消息发布成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                System.out.println("提交给库存异常，回滚");
                session.rollback();//消息回滚

            } catch (JMSException e) {
                e.printStackTrace();
            }
            throw new RuntimeException("自定义的错误");
        } finally {  //关闭资源
            try {
                session.close();
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
