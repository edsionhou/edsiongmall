package com.hou.gmallitem.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hou.gmall.bean.PmsProductSaleAttr;
import com.hou.gmall.bean.PmsProductSaleAttrValue;
import com.hou.gmall.bean.PmsSkuInfo;
import com.hou.gmall.bean.PmsSkuSaleAttrValue;
import com.hou.gmall.service.SkuService;
import com.hou.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@Controller
@CrossOrigin
public class ItemController {
    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;


    @GetMapping("/{skuId}.html")
    public String skuIdhtml(@PathVariable("skuId") String skuId, ModelMap map) {
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);
        //SKU对象
        map.put("skuInfo", pmsSkuInfo);

        //销售属性列表   spuSaleAttrListCheckBySku
//        String spuId = pmsSkuInfo.getSpuId();   大错特错！！！
        String spuId = pmsSkuInfo.getProductId();
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(spuId, skuId);
        map.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);

        //查询当前 sku的其他sku 形成的hashmap集合
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());

        HashMap<String,String> skuSaleAttrHash = new HashMap<>();
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            String k = "";
            String v = skuInfo.getId();
            for (PmsSkuSaleAttrValue skuSaleAttrValue: skuSaleAttrValueList) {
                //获取sale attr value Id
                k = k + skuSaleAttrValue.getSaleAttrValueId()+"|";
            }
            skuSaleAttrHash.put(k,v);
        }

        //将 skuSaleAttrHash 这个哈希表放进页面  使用fastJson
        //前端无法接收hashmap吗。。。？？  没太懂 List怎么可以，难道是因为list我进行了th:each 获取
        // map 我采用的是 jquery的操作，所以转为json
        String skuSaleAttrValueJsonStr = JSON.toJSONString(skuSaleAttrHash);
        map.put("skuSaleAttrHashJsonStr",skuSaleAttrValueJsonStr);
        System.out.println(skuSaleAttrValueJsonStr);
        return "item";
    }
}
