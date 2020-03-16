package com.hou.gmall.service;

import com.hou.gmall.bean.PmsProductInfo;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> getSpuList(String catalog3Id);
    String saveSpuInfo(PmsProductInfo pmsProductInfo);
}
