package com.hou.gmallpayment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.hou.gmall.bean.OmsOrder;
import com.hou.gmall.bean.PaymentInfo;
import com.hou.gmall.service.OrderService;
import com.hou.gmall.service.PaymentService;
import com.hou.gmallpayment.config.AlipayConfig;
import gmall.annotation.LoginRequired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.applet.Main;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {
    @Resource
    AlipayClient alipayClient;  //1 获得初始化的AlipayClient

    @Autowired
    PaymentService paymentService;

    @Reference
    OrderService orderService;

    @GetMapping("/index")
    @LoginRequired(LoginNecessary = true)      //支付的主页面
    public String pay(String outTradeNumber, BigDecimal totalAmount, HttpServletRequest request, ModelMap map) {
        String memberId = (String) request.getAttribute("memberId");
        String username = (String) request.getAttribute("username");
        map.put("username", username);
        map.put("outTradeNumber", outTradeNumber);
        map.put("totalAmount", totalAmount);
        
        return "index";

    }


    @RequestMapping(value = "/alipay/submit", method = RequestMethod.POST)
    @LoginRequired(LoginNecessary = true)
//    @ResponseBody
    public String alipay(String outTradeNumber, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap) {
        //获得一个支付宝的客户端，它并不是一个链接 而是一个封装好http的表单请求
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest(); //2 创建API对应的request
        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", outTradeNumber);
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", 0.01);
        map.put("subject", "机械师测试用");
        String s = JSON.toJSONString(map);
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url); //在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent(s);
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();//调用SDK生成  扫码支付或登录支付表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //生成并保存用户信息
        //1.通过外部交易号 获取 订单Order
        OmsOrder order = orderService.getOrderByOutTradeNo(outTradeNumber);
        //2.保存交易信息到DB
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setOrderSn(outTradeNumber);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("此处为商品标题");
        paymentInfo.setTotalAmount(totalAmount);
        paymentService.savePaymentInfo(paymentInfo);


        //提交请求到支付宝
        System.out.println(form);

        //向MQ发送一个检查支付状态（支付服务消费）的延迟消息队列 用来检查是否支付成功了！
        paymentService.sendDelayPaymentResultCheckQueue(outTradeNumber,5);


//        return form;
//        我无法请求支付宝了 只能模拟成功
       modelMap.put("outTradeNumber",outTradeNumber);
        return "redirect:http://payment.gmall.com:8030/alipay/callback/return?sign=1&out_trade_no="+outTradeNumber;
    }

    @RequestMapping(value = "/mx/submit", method = RequestMethod.POST)
    @LoginRequired(LoginNecessary = true)
    public String mx() {
        return null;
    }

    @RequestMapping(value = "/alipay/callback/return", method = RequestMethod.GET)
    @LoginRequired(LoginNecessary = true)   //扫码支付成功后的  同步回调地址  我们没法测试异步
    @ResponseBody
    public String alipaycallbackreturn(HttpServletRequest request, ModelMap modelMap) {
        //1.从回调请求中获取支付宝的参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String app_id = request.getParameter("app_id");
        String out_trade_no = request.getParameter("out_trade_no");
        String total_amount = request.getParameter("total_amount");
        String trade_status = request.getParameter("trade_status");
        String subject = request.getParameter("subject");

        //2.通过支付宝的paramsMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法验签
        if (StringUtils.isNotBlank(sign)) {
            //验签成功
            String callback_content = request.getQueryString(); //支付宝返回的 callback_content
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setCallbackContent(callback_content);

            //更新用户支付状态
            try {
                /*
                由于 监听器和此处都会去根据支付宝的返回情况做出更新paymentinfo
                更新前 需要进行幂等性操作！
                 */
                PaymentInfo paymentInfoInDB = paymentService.selectPaymentInfo(out_trade_no);
                if (!paymentInfoInDB.getOrderSn().equals(out_trade_no)) {
                    paymentService.updatePaymentInfo(paymentInfo);
                }else{
                    //已被更新 无需继续
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //
        }

        //3.支付成功后，引起的系统服务-->订单服务-->库存服务-->物流
        /*
        利用MQ 发送支付成功消息
        分布式事务的一致性：消息发送给  paymentService.updatePaymentInfo(paymentInfo);
         执行成功，则继续；  执行失败，则@Transactional回滚，同时MQ重发
         */

      
        return "success";
    }
}
