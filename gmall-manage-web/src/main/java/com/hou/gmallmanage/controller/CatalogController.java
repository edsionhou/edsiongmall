package com.hou.gmallmanage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hou.gmall.bean.PmsBaseCatalog1;
import com.hou.gmall.bean.PmsBaseCatalog2;
import com.hou.gmall.bean.PmsBaseCatalog3;
import com.hou.gmall.service.CatalogService;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@CrossOrigin    //EC7120: [CORS] 原点“http://localhost:8888”未在“http://localhost:8005/getCatalog1”的 cross-origin
            // 资源的 Access-Control-Allow-Origin response header 中找到“http://localhost:8888”。 跨域问题
@ResponseBody
public class CatalogController {

    @Reference
    CatalogService catalogService;



    @PostMapping("/getCatalog1")
    public List<PmsBaseCatalog1> getCatalog1(){
        System.out.println("呵呵");
        List<PmsBaseCatalog1> catalog1 = catalogService.getCatalog1();
        System.out.println(catalog1);
        return catalog1;
    }

    @PostMapping("/getCatalog2")
    public List<PmsBaseCatalog2> getCatalog2(@RequestParam String catalog1Id ){
        System.out.println("呵呵");
        List<PmsBaseCatalog2> catalog2 = catalogService.getCatalog2(catalog1Id);
        System.out.println(catalog2);
        return catalog2;
    }

    @PostMapping("/getCatalog3")
    public List<PmsBaseCatalog3> getCatalog3(@RequestParam String catalog2Id ){
        System.out.println("呵呵");
        List<PmsBaseCatalog3> catalog3 = catalogService.getCatalog3(catalog2Id);
        System.out.println(catalog3);
        return catalog3;
    }




}
