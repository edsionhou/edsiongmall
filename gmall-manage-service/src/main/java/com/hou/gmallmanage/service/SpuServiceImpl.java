package com.hou.gmallmanage.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.hou.gmall.bean.*;
import com.hou.gmall.service.SpuService;
import com.hou.gmallmanage.mapper.PmsProductImageMapper;
import com.hou.gmallmanage.mapper.PmsProductInfoMapper;
import com.hou.gmallmanage.mapper.PmsProductSaleAttrMapper;
import com.hou.gmallmanage.mapper.PmsProductSaleAttrValueMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    PmsProductImageMapper pmsProductImageMapper;
    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    public List<PmsProductInfo> getSpuList(String catalog3Id) {  //根据3级目录 查询 SPU 集合
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(pmsProductInfo);
        return pmsProductInfos;
    }

    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {  //保存 新建的 SPU信息
        //Pms_Product_Info 信息插入
        int insert = pmsProductInfoMapper.insertSelective(pmsProductInfo);
        //Product_image 插入
        List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
        String productInfoId = pmsProductInfo.getId();
        for (PmsProductImage image : spuImageList) {
            image.setProductId(productInfoId);
            pmsProductImageMapper.insertSelective(image);
        }

        //pms_product_sale_attr
        List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
        for (PmsProductSaleAttr saleAttr :
                spuSaleAttrList) {
            saleAttr.setProductId(productInfoId);
            pmsProductSaleAttrMapper.insertSelective(saleAttr);

            //pms_product_sale_attr_value
            List<PmsProductSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue attrvalue :
                    spuSaleAttrValueList) {
                attrvalue.setProductId(productInfoId);
                pmsProductSaleAttrValueMapper.insertSelective(attrvalue);
            }
        }


        return "success";
    }

    @Override
    public List<PmsProductSaleAttr> getProductSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);//获取到 销售属性集合
        //获取 销售属性值
        for (PmsProductSaleAttr pmsProductSaleAttr1 : pmsProductSaleAttrs) {
            PmsProductSaleAttrValue saleAttrValue = new PmsProductSaleAttrValue();
            saleAttrValue.setProductId(pmsProductSaleAttr1.getProductId());
            saleAttrValue.setSaleAttrId(pmsProductSaleAttr1.getSaleAttrId());
            List<PmsProductSaleAttrValue> select = pmsProductSaleAttrValueMapper.select(saleAttrValue);
            pmsProductSaleAttr1.setSpuSaleAttrValueList(select);
        }
        //销售属性 是厂家需要多增加的自定义属性吗？  pms_base_attr_info 是 页面提供的基础属性
        return pmsProductSaleAttrs;
    }

    @Override
    public List<PmsProductImage> getSpuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> pmsProductImages = pmsProductImageMapper.select(pmsProductImage);
        return pmsProductImages;
    }


    @Override
    //获取所有的 spu销售属性 by SkuId，其中包含了 一个sku的所有兄弟姐妹，并且有默认的sku属性被选择了
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String spuId, String skuId) { //spuId即product_id
       /* PmsProductSaleAttr productSaleAttr = new PmsProductSaleAttr();
        productSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> productSaleAttrs = pmsProductSaleAttrMapper.select(productSaleAttr);
        for (PmsProductSaleAttr  pmsProductSaleAttr: productSaleAttrs) {
            PmsProductSaleAttrValue productSaleAttrValue = new PmsProductSaleAttrValue();
            productSaleAttrValue.setProductId(spuId);
            productSaleAttrValue.setSaleAttrId(pmsProductSaleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> productSaleAttrValues = pmsProductSaleAttrValueMapper.select(productSaleAttrValue);
            pmsProductSaleAttr.setSpuSaleAttrValueList(productSaleAttrValues);
        }*/
        List<PmsProductSaleAttr> productSaleAttrs = pmsProductSaleAttrMapper.selectSpuSaleAttrListBySku(spuId, skuId);
        return productSaleAttrs;
    }


}
