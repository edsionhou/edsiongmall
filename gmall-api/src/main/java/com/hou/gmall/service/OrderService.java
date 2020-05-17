package com.hou.gmall.service;

import com.hou.gmall.bean.OmsOrder;

import java.math.BigDecimal;

public interface OrderService {
    String generateTradeCode(String memberId);

    String checkTradeCode(String memberId,String tradeCode);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTradeNumber);

}
