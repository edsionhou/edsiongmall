package com.hou.cartservice.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hou.conf.RedisUtil;
import com.hou.gmall.bean.OmsCartItem;
import com.hou.gmall.service.CartService;
import com.hou.cartservice.mapper.OmsCartItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@ComponentScan("com.hou.conf")
public class CartServiceImpl implements CartService {
    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setMemberId(memberId);
        cartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem = omsCartItemMapper.selectOne(cartItem);
        return omsCartItem;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        if (omsCartItem.getMemberId() != null) {
            omsCartItemMapper.insertSelective(omsCartItem);
        }
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDB) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id", omsCartItemFromDB.getId());
        //        UPDATE oms_cart_item SET product_sku_id = ?,member_id = ?,is_checked = ? WHERE ( member_id = ? and product_sku_id = ? )
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDB, e);  //根据主键更新行
    }

    @Override
    public void synchronizeCash(String memberId) {
        OmsCartItem o = new OmsCartItem();
        o.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(o);
        //同步到Redis中
        Jedis jedis = redisUtil.getJedis();
        try {
            HashMap<String, String> map = new HashMap<>();
            for (OmsCartItem omsCartItem : omsCartItems) {
                //给redis中添加 toltalprice属性，否则每次 ajax请求后，页面的 总价 值就null了
                //所以说 DB中没有 totalprice属性真的好吗？
                omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(BigDecimal.valueOf(omsCartItem.getQuantity())));
                map.put(omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem));
            }
            jedis.del("user:" + memberId + ":cart");  //每次请求都先删除key 再添加 否则 DB删了 redis还存在
            jedis.hmset("user:" + memberId + ":cart", map); //key--skuId,value--购物车对象


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }

    @Override
    public List<OmsCartItem> cartList(String userId) {  //从redis中获取 购物车集合
        Jedis jedis = null;
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        try {
            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals("user:" + userId + ":cart"); //获取所有的value
            for (String hval : hvals) {
                OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItems.add(omsCartItem);
            }
            return omsCartItems;
        } catch (Exception e) {
            e.printStackTrace();
            //logService.addErrorlog( e.getMessage()) ;
            return null;
        } finally {
            jedis.close();
        }


    }

    @Override
    public void updateCartCheckState(OmsCartItem omsCartItem) { //更新 isChecked状态
        //1,修改DB
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem,e);

        //2.缓存同步
        synchronizeCash(omsCartItem.getMemberId());
    }

    @Override
    public void delItem(String productSkuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductSkuId(productSkuId);
        omsCartItemMapper.delete(omsCartItem);
    }
}
