package com.hou.gmallpayment.MQ;

import com.hou.gmall.bean.PaymentInfo;
import com.hou.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentMQListener {
    @Autowired
    PaymentService paymentService;

    @JmsListener(destination="PAYMENT_CHECK_QUEUE",containerFactory="jmsQueueListener")
    public void consumePaymentCheckResult(MapMessage message) throws JMSException {  //定时检查 用户支付是否成功
        String outTradeNo = message.getString("outTradeNo");
        int count = message.getInt("count");

        //调用paymentService的支付宝检查接口，定时检查 用户支付是否成功
        System.out.println("延迟检查，调用支付检查的接口服务");
//        Map<String, Object> result = paymentService.checkAlipayPayment(outTradeNo);
        Map<String, Object> result = new HashMap<>();
        result.put("trade_status","TRADE_SUCCESS");
        if (result==null || result.isEmpty()) {
            //继续发送延迟检查任务，计算延迟时间
            System.out.println("未支付，继续发送延迟检查");
            if (count > 0) {
                count--;
                paymentService.sendDelayPaymentResultCheckQueue(outTradeNo,count);
            }else{
                System.out.println("结束检查");
            }

            return;
        }
        String  trade_status = (String) result.get("trade_status");  //一般来说未支付都是 WAIT_BUYER_PAY
        //根据查询到的支付状态，判断是否进行下一次的延迟任务 或者 成功后的后续任务
        if ("TRADE_SUCCESS".equals(trade_status)) {
            //支付成功，更新支付 发送支付队列
            System.out.println("支付成功，调用支付服务，修改支付信息和发送支付成功队列");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(outTradeNo);
//            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setPaymentStatus("已支付");
//            paymentInfo.setCallbackContent(callback_content);
            paymentInfo.setCallbackTime(new Date());

            //更新前 需要进行幂等性操作！
            PaymentInfo paymentInfoInDB = paymentService.selectPaymentInfo(outTradeNo);
            if (paymentInfoInDB.getOrderSn().equals(outTradeNo) && paymentInfoInDB.getPaymentStatus().equals("已支付")) {
                return; //已被更新 无需继续
            }
            paymentService.updatePaymentInfo(paymentInfo);

        }else{
            //继续发送延迟检查任务，计算延迟时间
            System.out.println("未支付，继续发送延迟检查");
            if (count > 0) {
                count--;
                paymentService.sendDelayPaymentResultCheckQueue(outTradeNo,count);
            }else{
                System.out.println("结束检查");
            }

        }


    }


}
