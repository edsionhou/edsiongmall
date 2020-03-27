package com.hou.gmallmanage.mapper;

import com.hou.gmall.bean.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {


    List<PmsProductSaleAttr> selectSpuSaleAttrListBySku(@Param("productId") String spuId, @Param("skuId") String skuId);
}
