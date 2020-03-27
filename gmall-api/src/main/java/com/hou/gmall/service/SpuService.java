package com.hou.gmall.service;

import com.hou.gmall.bean.PmsProductImage;
import com.hou.gmall.bean.PmsProductInfo;
import com.hou.gmall.bean.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> getSpuList(String catalog3Id);
    String saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> getProductSaleAttrList(String spuId);

    List<PmsProductImage> getSpuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String spuId,String skuId);
}
