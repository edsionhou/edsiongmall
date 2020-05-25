package com.hou.gmall.service;

import com.hou.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(PaymentInfo paymentInfo);

    void sendDelayPaymentResultCheckQueue(String outTradeNumber,int count);

    Map<String, Object> checkAlipayPayment(String outTradeNo);

    PaymentInfo selectPaymentInfo(String outTradeNo);
}
