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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
        System.out.println("线程" + Thread.currentThread().getName() + "来了，获取到的skuJson为 " + skuJson);
//        if(StringUtils.isNoneBlank(skuJson)){
        if (skuJson != null /*&& !skuJson.equals("NOT_EXITS")*/) {
            //1.缓存有
            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        } else if ("NOT_EXITS".equals(skuJson)) {
            jedis.close();
            return null;
        } else {
            //2.缓存没有，   查询mysql   但存在缓存穿透  缓存击穿  缓存雪崩问题
            //设置分布式锁Lock  多个redis客户端，同时访问redis，普通锁无法做到同步
            String lockToken = UUID.randomUUID().toString();
            String OK = jedis.set("sku:" + skuId + ":lock", lockToken, "nx", "ex", 10);

            if (OK != null && OK.equals("OK")) {  //没有redis设置此 KEY，成功
                //3.设置锁成功，有权访问数据库 10s过期时间内
                pmsSkuInfo = getSkuByDBsId(skuId);
                if (pmsSkuInfo != null) {
                    /*同一个客户端，打开多个网页进行同一个请求，如果中间有阻塞，则会阻塞所有后续请求，
                    controller调用service方法，是多次调用，而不是多个调用！！！而不同浏览器则是多个调用
                    据我测试，controller serivice都是单例的，spring容器默认创建单例的，同一客户端，相当于同一对象多次调用，
                    不同客户端则是不同对象调用，所以不会阻塞*/
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    jedis.set(skuKey, JSON.toJSONString(pmsSkuInfo));

                } else {
                    //解决缓存穿透问题：防止多次该字符串请求  为skuKey赋值 空
                    jedis.setex(skuKey, 60 * 3, JSON.toJSONString("NOT_EXITS"));
                }
                //3.释放分布式锁
                System.out.println(Thread.currentThread().getName() + "获取到了锁");

                lockToken = jedis.get("sku:" + skuId + ":lock");
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                jedis.eval(script, Collections.singletonList("sku:" + skuId + ":lock"), Collections.singletonList(lockToken));
//                jedis.del("sku:" + skuId + ":lock");
            } else {
                //4.设置失败 自旋
                System.out.println("孤儿线程？--->" + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                getSkuById(skuId);  //孤儿线程？？ 没懂  个人感觉是 当前方法无法获取递归调用的结果  return了null
                jedis.close();
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

    @Override
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo skuInfo :
                pmsSkuInfos) {
            String skuId = skuInfo.getId();
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            skuInfo.setSkuAttrValueList(select);
        }

        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal price) {
        boolean b = false;
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        pmsSkuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        if (pmsSkuInfo != null) {
            BigDecimal price1 = pmsSkuInfo.getPrice();
           b =  price.compareTo(price1) == 0 ? true: false;
        }

        return b;
    }


}
