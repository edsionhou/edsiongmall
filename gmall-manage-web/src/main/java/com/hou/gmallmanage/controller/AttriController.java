package com.hou.gmallmanage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hou.gmall.bean.PmsBaseAttrInfo;
import com.hou.gmall.bean.PmsBaseAttrValue;
import com.hou.gmall.bean.PmsBaseSaleAttr;
import com.hou.gmall.service.AttriService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@ResponseBody
@CrossOrigin
public class AttriController {

    @Reference
    AttriService attriService;

    @GetMapping("/attrInfoList")
    public List<PmsBaseAttrInfo> attrInfoList(@RequestParam String catalog3Id) {
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attriService.attrInfoLists(catalog3Id);
        return pmsBaseAttrInfos;
        //随便定义名字， 返回json格式，在前端都是 data数据，之前做的ajax请求中也是这么封装的result；
    }


    @RequestMapping(value = "/saveAttrInfo")
    //@RequestBody 接受的数据类型是 content-type:"application/json" ,传输的数据需要用JSON.stringify(data);
    public boolean saveAttrInfo(@RequestBody  PmsBaseAttrInfo pmsBaseAttrInfo){
        PmsBaseAttrInfo pmsBaseAttrInfo1 = attriService.saveAttrInfo(pmsBaseAttrInfo);
        //System.out.println(pmsBaseAttrInfo1);
        return true;
    }

    @PostMapping("/getAttrValueList")
    public List<PmsBaseAttrValue> getAttrValueList(@RequestParam String attrId){
        List<PmsBaseAttrValue> pmsBaseAttrValueList = attriService.getAttrValueList(attrId);
        return pmsBaseAttrValueList;
    }

    @PostMapping("/baseSaleAttrList")
    public  List<PmsBaseSaleAttr> baseSaleAttrList(){
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = attriService.baseSaleAttrList();
        return pmsBaseSaleAttrs;

    }
}
