package com.hou.gmall.service;


import com.hou.gmall.bean.PmsBaseAttrInfo;
import com.hou.gmall.bean.PmsBaseAttrValue;
import com.hou.gmall.bean.PmsBaseSaleAttr;

import java.util.List;

public interface AttriService {
   List<PmsBaseAttrInfo> attrInfoLists(String catalog3Id);
   PmsBaseAttrInfo saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    List<PmsBaseSaleAttr> baseSaleAttrList();   //获取所有的 销售属性
}
