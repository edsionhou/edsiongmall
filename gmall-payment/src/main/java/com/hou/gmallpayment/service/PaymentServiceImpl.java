package com.hou.gmallpayment.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.hou.conf.ActiveMQUtil;
import com.hou.gmall.bean.PaymentInfo;
import com.hou.gmall.service.PaymentService;
import com.hou.gmallpayment.mapper.PaymentInfoMapper;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {
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

            Example e = new Example(PaymentInfo.class);
            e.createCriteria().andEqualTo("orderSn");
            paymentInfoMapper.updateByExampleSelective(paymentInfo,e);
//        String a = null;
//        a.length();
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
