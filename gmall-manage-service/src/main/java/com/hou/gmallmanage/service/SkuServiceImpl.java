package com.hou.gmallmanage.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hou.conf.RedisUtil;
import com.hou.gmall.bean.PmsSkuAttrValue;
import com.hou.gmall.bean.PmsSkuImage;
import com.hou.gmall.bean.PmsSkuInfo;
import com.hou.gmall.bean.PmsSkuSaleAttrValue;
import com.hou.gmall.service.SkuService;
import com.hou.gmallmanage.mapper.PmsSkuAttrValueMapper;
import com.hou.gmallmanage.mapper.PmsSkuImageMapper;
import com.hou.gmallmanage.mapper.PmsSkuInfoMapper;
import com.hou.gmallmanage.mapper.PmsSkuSaleAttrValueMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service

public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //插入sku_info
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());  //把spuid转为 productid
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);

        //插入平台属性关联
        for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuInfo.getSkuAttrValueList()) {
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        //插入销售属性关联
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuInfo.getSkuSaleAttrValueList()) {
            pmsSkuSaleAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        //插入skuimages
        for (PmsSkuImage pmsSkuImage : pmsSkuInfo.getSkuImageList()) {
            pmsSkuImage.setProductImgId(pmsSkuImage.getSpuImgId());
            pmsSkuImage.setSkuId(pmsSkuInfo.getId());
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }
        return "success";
    }

    @Override
    //通过sku主键获取 SKU
    public PmsSkuInfo getSkuById(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //连接缓存
        Jedis jedis = redisUtil.getJedis();

        //查询缓存
        String skuKey = "sku:" + skuId + "info";
        String skuJson = jedis.get(skuKey);  //从redis中获取 JSON
//        if(StringUtils.isNoneBlank(skuJson)){
        // =  skuJson!=null && !skuJson.equals("")
        if (skuJson != null && !skuJson.equals("")) {
            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        } else {
            //缓存没有，查询mysql   但存在缓存穿透  缓存击穿  缓存雪崩问题
            //设置分布式锁  多个redis客户端，同时访问redis，普通锁无法做到同步
            String OK = jedis.set(skuKey, "1", "nx", "ex", 20);

            if (OK != null && OK.equals("OK")) {  //没有redis设置此 KEY，成功
                //设置锁成功，有权访问数据库 20s过期时间内
                pmsSkuInfo = getSkuByDBsId(skuId);
                if (pmsSkuInfo != null) {
                    jedis.set(skuKey, JSON.toJSONString(pmsSkuInfo));
                } else {
                    //解决缓存穿透问题：防止多次该字符串请求  为skuKey赋值 空
                    jedis.setex(skuKey, 60 * 3, JSON.toJSONString(""));
                }
            } else {
                //设置失败 自旋
                System.out.println("孤儿线程？--->"+Thread.currentThread().getName());
//                getSkuById(skuId);  //孤儿线程？？ 没懂  个人感觉是 当前方法无法获取递归调用的结果  return了null
                return getSkuById(skuId);
            }

        }
        //mysql返回结果存入redis
        jedis.close();
        return pmsSkuInfo;
    }

    public PmsSkuInfo getSkuByDBsId(String skuId) {
        PmsSkuInfo pmsSkuInfo = pmsSkuInfoMapper.selectByPrimaryKey(skuId);
        //获取图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        pmsSkuInfo.setSkuImageList(pmsSkuImages);
        return pmsSkuInfo;
    }


    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }


}
