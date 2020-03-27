package com.hou.gmallmanage.mapper;

import com.hou.gmall.bean.PmsSkuInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo> {

    List<PmsSkuInfo> selectSkuSaleAttrValueListBySpu(String productId);
}
