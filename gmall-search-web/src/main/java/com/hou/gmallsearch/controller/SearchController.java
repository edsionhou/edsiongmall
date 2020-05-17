package com.hou.gmallsearch.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hou.gmall.bean.*;
import com.hou.gmall.service.AttriService;
import com.hou.gmall.service.SearchService;
import gmall.annotation.LoginRequired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

@Controller
public class SearchController {
    @Reference
    SearchService searchService;
    @Reference
    AttriService attriService;

    @GetMapping("/index")
    @LoginRequired(LoginNecessary = false)
    public String index() {
        return "index";
    }

    @GetMapping("/list.html")
    //通过ES，搜索关键字，展示所有的sku
    public String list(PmsSearchParam pmsSearchParam, ModelMap map) { //封装了catalog3Id  sku属性集合 关键字
        System.out.println("打印参数：" + pmsSearchParam);
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = searchService.list(pmsSearchParam);
        map.put("skuLsInfoList", pmsSearchSkuInfoList);

        //抽取所有的 pms_base_attr_value表的id，封装进Set(去重)，然后找出对应的value值，就是选中的平台属性
        Set<String> valueSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            for (PmsSkuAttrValue pmsSkuAttrValue : pmsSearchSkuInfo.getSkuAttrValueList()) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueSet.add(valueId);
            }
        }
        //根据PmsSkuAttrValue表的id 集合 获取所有的List<PmsBaseAttrValue>
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attriService.getAttrValueListByValueId(valueSet);
        map.put("attrList", pmsBaseAttrInfos);


        //对平台集合进一步处理，去掉当前条件中valueId所在的属性组，即 根据传入的属性对比 平台属性，有重复的 ，平台属性就去除掉
        //面包屑功能  不用查询数据库
        //合并两个功能

        String[] delValueIds = pmsSearchParam.getValueId();
        if (delValueIds != null) {
            //当前请求中包含属性参数(valueId),每个参数 生成一个面包屑
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
//            Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();//遍历平台属性，有和delValueIds重复的就减掉  这里有大问题，其实第一次循环就已经把iterator指针走到末尾了
            for (String delValueId : delValueIds) {
                PmsSearchCrumb crumb = new PmsSearchCrumb();
                crumb.setValueId(delValueId);
                crumb.setUrlParam(getUrlParam(pmsSearchParam, delValueId));

                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();//遍历平台属性，有和delValueIds重复的就减掉
                end:while (iterator.hasNext()) {
                    PmsBaseAttrInfo next = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = next.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String valueId = pmsBaseAttrValue.getId();
                        if (delValueId.equals(valueId)) {
                            //删除该属性值所在的属性组
                            //查找面包屑对应的valuename
                            crumb.setValueName(pmsBaseAttrValue.getValueName());
                            iterator.remove();
                            break end;
                        }
                    }
                }
                pmsSearchCrumbs.add(crumb);
            }
            map.put("attrValueSelectedList", pmsSearchCrumbs);
        }


        //根据valueId将属性列表查询出来 叠加
        String urlParam = getUrlParam(pmsSearchParam, null);
        map.put("urlParam", urlParam);
        String keyWord = pmsSearchParam.getKeyWord();
        map.put("keyword", keyWord);


        //调用搜索服务，返回结果
        return "list";
    }

    //根据param拼接url
    private String getUrlParam(PmsSearchParam pmsSearchParam, String delValueId) {
        String keyWord = pmsSearchParam.getKeyWord();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] valueIdArray = pmsSearchParam.getValueId();
//        List<PmsSkuAttrValue> skuAttrValueList = pmsSearchParam.getSkuAttrValueList();
        String urlParam = "";
        if (StringUtils.isNotBlank(keyWord)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyWord=" + keyWord;

        }
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;

        }
        if (valueIdArray != null) {

            /*for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {   前端传的是valueId，没有skuAttrValueList.valueId,所以这个没用
                if (StringUtils.isNotBlank(urlParam)){
                    urlParam =  urlParam + "&";
                }
                String valueId = pmsSkuAttrValue.getValueId();
                urlParam =  urlParam + "valueId="+valueId;
            }*/
//            System.out.println("delValueId" + delValueId);
            for (String valueId : valueIdArray) {
                /*if(delValueId!=null){   这个可变数组永远都不会是null delValueId[Ljava.lang.String;@5b1fe0ee 妈的可变数组怎么用？
                    continue;
                }*/
                if (valueId.equals(delValueId)) {
                    continue; //去除面包屑的valueId，形成点击面包屑，就请求减去此属性的请求
                }
                if (StringUtils.isNotBlank(urlParam)) {
                    urlParam = urlParam + "&";
                }
                urlParam = urlParam + "valueId=" + valueId;
            }
        }

        return urlParam;
    }


}
