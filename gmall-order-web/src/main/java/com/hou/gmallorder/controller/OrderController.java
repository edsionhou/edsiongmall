package com.hou.gmallorder.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hou.gmall.bean.OmsCartItem;
import com.hou.gmall.bean.OmsOrder;
import com.hou.gmall.bean.OmsOrderItem;
import com.hou.gmall.bean.UmsMemberReceiveAddress;
import com.hou.gmall.service.CartService;
import com.hou.gmall.service.OrderService;
import com.hou.gmall.service.SkuService;
import com.hou.gmall.service.UserService;
import gmall.annotation.LoginRequired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

    @GetMapping("/toTrade")
    @LoginRequired(LoginNecessary = true)
    public String toTrade(HttpServletRequest request, ModelMap map) {  //点击 去结算，展示用户地址及购物车选中商品
        String memberId = (String) request.getAttribute("memberId");
        String username = (String) request.getAttribute("username");
        //1获取收件人地址列表
        List<UmsMemberReceiveAddress> receiveAddressByMemberId = userService.getReceiveAddressByMemberId(memberId);
        //2将购物车集合转为页面的结算清单
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            //每遍历一个购物车对象，就封装到 订单对象
            if (omsCartItem.getIsChecked().equals("1")) {
                OmsOrderItem o = new OmsOrderItem();
                o.setProductName(omsCartItem.getProductName());
                o.setProductPic(omsCartItem.getProductPic());
                o.setProductQuantity(omsCartItem.getQuantity());
                omsOrderItems.add(o);
            }
        }
        map.put("omsOrderItems", omsOrderItems);
        map.put("userAddressList", receiveAddressByMemberId);
        map.put("totalAmount", getTotalAmount(omsCartItems));
        //3生成交易码(一次性的)，为了提交订单时的校验
        String tradeCode = orderService.generateTradeCode(memberId); //存入redis
        map.put("tradeCode", tradeCode);
        return "trade";
    }

    @PostMapping("/submitOrder")
    @LoginRequired(LoginNecessary = true)  //点击提交订单  真正的结算
    public String submitOrder(String receiveAddressId, String tradeCode, BigDecimal totalAmount, HttpServletRequest request, ModelMap map) {
        String memberId = (String) request.getAttribute("memberId");
        String username = (String) request.getAttribute("username");
        //1.检查交易码
        String success = orderService.checkTradeCode(memberId, tradeCode);
        if (success.equals("success")) {
            //订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setConfirmStatus(1);
            omsOrder.setCreateTime(new Date());
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(username);
            omsOrder.setNote("快发货");
            String outTradeNumber = "gmall" + System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            outTradeNumber = outTradeNumber + sdf.format(new Date());  //生成 时间毫秒数 + sdf生成的订单号
            System.err.println(outTradeNumber);
            omsOrder.setOrderSn(outTradeNumber);  //外部订单号
            omsOrder.setPayAmount(totalAmount);  //订单总金额 其实应该从订单系统里调用
            omsOrder.setOrderType(1);
            UmsMemberReceiveAddress address = userService.getReceiveAddressByAddressId(receiveAddressId);
            omsOrder.setReceiverCity(address.getCity());
            omsOrder.setReceiverDetailAddress(address.getDetailAddress());
            omsOrder.setReceiverName(address.getName());
            omsOrder.setReceiverPhone(address.getPhoneNumber());
            omsOrder.setReceiverPostCode(address.getPostCode());
            omsOrder.setReceiverRegion(address.getRegion());
            omsOrder.setReceiverProvince(address.getProvince());
            //计算日期 calendar
            Calendar instance = Calendar.getInstance();
            instance.add(Calendar.DAY_OF_YEAR,1); //加一天后配送
            Date time = instance.getTime();
            omsOrder.setReceiveTime(time);
            omsOrder.setSourceType(0);
            omsOrder.setStatus(0);
            omsOrder.setTotalAmount(totalAmount);
            //2.根据用户ID获得要购买的商品列表（购物车）和总价   从redis /数据库获取，因为可能一个用户用多个浏览器操作web页面，每个页面展示的商品不同
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    //2.1获取被选中的商品
                    OmsOrderItem o = new OmsOrderItem();
                    //2.2验价
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if (b == false) {
                        return "tradeFailed";
                    }
                    //2.3验库存    远程调用库存系统
                    o.setProductName(omsCartItem.getProductName());
                    o.setProductPic(omsCartItem.getProductPic());
                    o.setProductQuantity(omsCartItem.getQuantity());
                    o.setProductCategoryId(omsCartItem.getProductCategoryId());
                    o.setProductPrice(omsCartItem.getPrice());
                    o.setRealAmount(omsCartItem.getTotalPrice());
                    o.setProductSkuCode("11111");
                    o.setProductSkuId(omsCartItem.getProductSkuId());
                    o.setProductId(omsCartItem.getProductId());
                    o.setProductSn("仓库对应的商品编号");  //在仓库中的 skuId 对应的编号
                    o.setOrderSn(outTradeNumber); //外部订单号，用来和其他系统交互，防止重复
                    omsOrderItems.add(o);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);

            //3.将订单和订单详情写入数据库 & 删除购物车中的对应商品
            orderService.saveOrder(omsOrder);
            //4.重定向到 支付业务
            return "redirect:http://payment.gmall.com:8030/index?outTradeNumber="+outTradeNumber+"&totalAmount="+totalAmount;

        } else {
            //5.
            return "tradeFailed";
        }
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalcount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            if (omsCartItem.getIsChecked().equals("1")) {
                BigDecimal totalPrice = omsCartItem.getTotalPrice();
//                System.out.println(totalPrice);//4183470.00
                totalcount = totalcount.add(totalPrice);  //这是神坑啊   totalcount.add(totalPrice);不管用 必须有 taotalcount引用
            }
        }
        return totalcount;
    }
}
