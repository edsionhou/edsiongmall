package com.hou.gmall.service;

import com.hou.gmall.bean.PmsSearchParam;
import com.hou.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {

    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
