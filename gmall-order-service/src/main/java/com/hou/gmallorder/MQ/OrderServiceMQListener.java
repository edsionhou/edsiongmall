package com.hou.gmallorder.MQ;

import com.hou.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderServiceMQListener {

    @Autowired
    OrderService orderService;

    /*
       此监听器会持续监听在PAYHMENT_SUCCESS_QUEUE上，一有消息就执行updateOrderProcessStatus方法
      quque的消费者会有2个，因为进行了持久化，就会自动生成一个sesssionId，其实这个不会消费的
       @JmsListener 应该是默认开启了事务，  无异常 consumer就不会回滚 ，有异常自动回滚
        String a = null;
        a.length();
        设置 空指针后， 重复消费6次 共7次后消息 出队，进入死信队列。
     */
    @JmsListener(destination="PAYHMENT_SUCCESS_QUEUE",containerFactory="jmsQueueListener")
    public  void updateOrderProcessStatus(MapMessage message) throws JMSException {
        String outTradeNo = message.getString("outTradeNo");
//        String a = null;
//        a.length();

        //更新订单状态业务 从0到1
        System.out.println(outTradeNo);

        /*
        updateOrderStatus中调用sql异常，则会触发mysql的回滚
        异常抛出到这里，触发MQ的回滚，并重新消费，连续7次失败后，进入死信队列
         */
        orderService.updateOrderStatus(outTradeNo);

    }
}
