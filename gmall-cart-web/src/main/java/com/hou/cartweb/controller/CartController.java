package com.hou.cartweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hou.gmall.bean.OmsCartItem;
import com.hou.gmall.bean.PmsSkuInfo;
import com.hou.gmall.service.CartService;
import com.hou.gmall.service.SkuService;
import gmall.annotation.LoginRequired;
import gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

@Controller
public class CartController {
    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;


    @GetMapping("/toTrade")      //测试下 登录成功的演示
    @LoginRequired(LoginNecessary = true)
    public  String toTrade(HttpServletRequest request,HttpServletResponse response,ModelMap map){
        System.out.println("执行toTrade方法");
       String memberId = (String) request.getAttribute("memberId");
        String username = (String) request.getAttribute("username");

        return "toTradeTest";
    }

    @PostMapping("/checkCart") //修改选择状态时触发 onchange
    @LoginRequired(LoginNecessary = false)
    public String checkCart(String isChecked,String skuId,HttpServletRequest request,HttpServletResponse response,ModelMap map){

        System.out.println("ischecked: "+isChecked+" skuId:"+skuId);
        String memberId = (String) request.getAttribute("memberId");
        //调用服务，修改状态
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setProductSkuId(skuId);
        cartItem.setMemberId(memberId);
        cartItem.setIsChecked(isChecked);
        cartService.updateCartCheckState(cartItem);


        //将最新的数据从缓存中查出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        map.put("cartList",omsCartItems);
        //被勾选的商品 价格总和
        BigDecimal bb = getTotalAmount(omsCartItems);
        System.out.println("totalAmount "+bb);
        map.put("totalAmount",bb);

        return "cartListInner"; //返回 html内嵌页面 给 Ajax请求中
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalcount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            if(omsCartItem.getIsChecked().equals("1")) {
                BigDecimal totalPrice = omsCartItem.getTotalPrice();
//                System.out.println(totalPrice);//4183470.00
                totalcount = totalcount.add(totalPrice);  //这是神坑啊   totalcount.add(totalPrice);不管用 必须有 taotalcount引用
            }
        }
        return totalcount;
    }

    @GetMapping("/cartList") //去购物车结算  获取购物车集合
    @LoginRequired(LoginNecessary = false)
    public  String cartList(HttpServletRequest request, HttpServletResponse response,ModelMap map){

        String memberId = (String) request.getAttribute("memberId");
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        if(StringUtils.isNotBlank(memberId)){
            //用户已登录 查询redis中
            omsCartItems=  cartService.cartList(memberId);
        }else{
            //用户未登录，从cookie获取
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                //cookie存在购物车 数据
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        }
        for (OmsCartItem omsCartItem : omsCartItems) { //计算总价 price * quantity  BigDecimal 专业用来计算！！！
            omsCartItem.setTotalPrice(omsCartItem.getPrice()
                    .multiply(BigDecimal.valueOf(omsCartItem.getQuantity()) )
            );
        }

        map.put("cartList",omsCartItems);
        if(StringUtils.isNotBlank(memberId)){
            //被勾选的商品 价格总和
            BigDecimal bb = getTotalAmount(omsCartItems);
            map.put("totalAmount",bb);
        }

        return "cartList";
    }

    @PostMapping("/addToCart") //添加到购物车
    @LoginRequired(LoginNecessary = false)
    public String addToCart(String skuId, int num, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("skuID "+skuId+"  num： "+num);
        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);

        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductName(skuInfo.getSkuName()); //这里是spuname还是skuname呢？ 我们就用skuname来用了
//        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("1111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(num);

        //判断用户是否登录
        String memberId = (String) request.getAttribute("memberId");
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        if (StringUtils.isBlank(memberId)) {
            //1 用户没登录   把购物车们 放入cookie
//            List<OmsCartItem> omsCartItems = new ArrayList<>();

            //1.1查询cookie里原有的数据   true是中文转码
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isBlank(cartListCookie)) {
                // ！ cookies数组为空，即没有任何cookie
                omsCartItems.add(omsCartItem);
                CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 24 * 3, true);
            } else {
                //！ cookies 不为空
                //1.2 判断cookie是否存在于 cookies数组
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                boolean exit = if_cart_exit(omsCartItems, omsCartItem);
                if (exit) {
                    // 1.2.1，之前添加过cookie，更新
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            cartItem.setQuantity(cartItem.getQuantity() + omsCartItem.getQuantity());
                            cartItem.setPrice(cartItem.getPrice().add(omsCartItem.getPrice())); //BigDecimal的加法
                        }
                    }
                } else {
                    //1.2.2.未添加过cookie
                    omsCartItems.add(omsCartItem);
                }
                CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 24 * 3, true);

            }


        } else {
            //2 用户已登录 从DB获取
            OmsCartItem omsCartItemFromDB =  cartService.ifCartExistByUser(memberId,skuId);

            if(omsCartItemFromDB==null){
                //用户未添加过此商品  插入数据库
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("侯");
                cartService.addCart(omsCartItem);
            }else{
                //用户添加过该商品
                omsCartItemFromDB.setQuantity(omsCartItemFromDB.getQuantity()+omsCartItem.getQuantity());
                cartService.updateCart(omsCartItemFromDB);
            }

            //DB操作过后，同步缓存
            cartService.synchronizeCash(memberId);





        }

        return "redirect:/success.html";  //重定向  我靠 命名为success就不行？？？
    }

    private boolean if_cart_exit(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        for (OmsCartItem cartItem : omsCartItems) {
            // 对比skuId，对上就是true
            if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                return true;
            }

        }
        return false;
    }


}

