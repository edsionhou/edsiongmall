package com.hou.gmallpayment.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.hou.conf.ActiveMQUtil;
import com.hou.gmall.bean.PaymentInfo;
import com.hou.gmall.service.PaymentService;
import com.hou.gmallpayment.mapper.PaymentInfoMapper;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {   //支付成功后更新用户支付状态
        Example e = new Example(PaymentInfo.class);
        e.createCriteria().andEqualTo("orderSn");
        paymentInfoMapper.updateByExampleSelective(paymentInfo,e);

        //更新支付信息后 发送信息到消息队列
        PAYHMENT_SUCCESS_QUEUE(paymentInfo);
    }

    @Override
    public void sendDelayPaymentResultCheckQueue(String outTradeNumber,int count) {  //延迟检查  DelayQueue  PAYMENT_CHECK_QUEUE
         Session session = null;
        Connection connection = null;
        try {
            ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);//开启MQ的事务
            Queue queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(queue);
            MapMessage mapMessage = new ActiveMQMapMessage();  //hashmap结构的信息
            mapMessage.setString("outTradeNo",outTradeNumber);
            mapMessage.setInt("count",count);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 3 * 1000);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 2 * 1000);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
            producer.send(mapMessage);

            session.commit(); //提交事务
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                System.out.println("检查PAYMENT_CHECK_QUEUE异常，回滚");
                session.rollback();//消息回滚

            } catch (JMSException e) {
                e.printStackTrace();
            }
            /*
             我认为很重要！ 抛出异常，试 上面调用的 updatePaymentInfo 一起回滚
             */
            throw new RuntimeException("自定义的错误PAYMENT_CHECK_QUEUE");
        } finally {  //关闭资源
            try {
                session.close();
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> checkAlipayPayment(String outTradeNo) { //向alipay查询支付情况  使用阿里sdk
        AlipayTradeQueryRequest requestMap = new AlipayTradeQueryRequest();
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        String s = JSON.toJSONString(map);
        requestMap.setBizContent(s);
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(requestMap);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        Map<String,Object> resultMap = new HashMap<>();
        if(response.isSuccess()){
            System.out.println("交易结束，调用成功");
            resultMap.put("out_trade_no", response.getOutTradeNo());
            resultMap.put("trade_no", response.getTradeNo());
            resultMap.put("trade_status", response.getTradeStatus());
        } else {
            System.out.println("有可能交易未创建，调用失败 resultMap--> "+resultMap);
        }

        return resultMap;
    }

    @Override
    public PaymentInfo selectPaymentInfo(String outTradeNo) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(outTradeNo);
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfo);
        return paymentInfo1;
    }


    public void PAYHMENT_SUCCESS_QUEUE(PaymentInfo paymentInfo){  //把支付信息发送给消息队列，通知 订单业务 PAYHMENT_SUCCESS_QUEUE
        Session session = null;
        Connection connection = null;
        try {
            ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);//开启MQ的事务
            Queue queue = session.createQueue("PAYHMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(queue);
            MapMessage mapMessage = new ActiveMQMapMessage();  //hashmap结构的信息
            mapMessage.setString("outTradeNo",paymentInfo.getOrderSn());
            producer.send(mapMessage);

            session.commit(); //提交事务
            System.out.println("消息发布成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                System.out.println("支付异常，回滚");
                session.rollback();//消息回滚

            } catch (JMSException e) {
                e.printStackTrace();
            }
            /*
             我认为很重要！ 抛出异常，试 上面调用的 updatePaymentInfo 一起回滚
             */
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
