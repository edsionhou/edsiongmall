package com.hou.gmallmanage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hou.gmall.bean.PmsSkuInfo;
import com.hou.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@ResponseBody
@CrossOrigin
public class SkuController {

    @Reference
    SkuService skuService;

    @PostMapping("/saveSkuInfo")
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo) {

        skuService.saveSkuInfo(pmsSkuInfo);
        return "保存SKU成功";
    }
}
