package com.hou.gmall.service;

import com.hou.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem ifCartExistByUser(String memberId,String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItemFromDB);

    void synchronizeCash(String memberId);

    List<OmsCartItem> cartList(String userId);//从redis中获取 购物车集合

    void updateCartCheckState(OmsCartItem omsCartItem);

    void delItem(String productSkuId);
}
