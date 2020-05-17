package com.hou.gmall.bean;

import java.io.Serializable;
import java.util.List;

public class PmsSearchParam implements Serializable {
    private String catalog3Id;  //自动关联出查找的分类ID
    private List<PmsSkuAttrValue> skuAttrValueList;  //查找时过滤选项（如果勾选了的话）
    private String keyWord;   //查找的关键词   (商品名称(展示/查询) 5 商品描述(展示/查询) 2 商品价格(展示/查询))
    private String[] valueId;   //居然可以接收前端传的 多个valueId 组成数组

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public List<PmsSkuAttrValue> getSkuAttrValueList() {
        return skuAttrValueList;
    }

    public void setSkuAttrValueList(List<PmsSkuAttrValue> skuAttrValueList) {
        this.skuAttrValueList = skuAttrValueList;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyword) {
        this.keyWord = keyword;
    }

    public String[] getValueId() {
        return valueId;
    }

    public void setValueId(String[] valueId) {
        this.valueId = valueId;
    }

    @Override
    public String toString() {
        return "PmsSearchParam{" +
                "catalog3Id='" + catalog3Id + '\'' +
                ", skuAttrValueList=" + skuAttrValueList +
                ", keyWord='" + keyWord + '\'' +
                '}';
    }
}
