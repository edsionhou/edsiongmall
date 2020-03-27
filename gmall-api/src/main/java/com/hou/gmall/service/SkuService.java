package com.hou.gmall.service;

import com.hou.gmall.bean.PmsSkuInfo;

import java.util.List;

public interface SkuService {
    String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuById(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);
}
