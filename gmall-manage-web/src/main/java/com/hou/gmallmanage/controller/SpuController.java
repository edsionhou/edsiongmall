package com.hou.gmallmanage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hou.gmall.bean.PmsProductInfo;
import com.hou.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
@ResponseBody
public class SpuController {

    @Reference
    SpuService spuService;

    @GetMapping("/spuList")
    public List<PmsProductInfo>  spuList(@RequestParam("catalog3Id") String catalog3Id){
        List<PmsProductInfo> spuList = spuService.getSpuList(catalog3Id);
        return spuList;
    }

    @PostMapping("/saveSpuInfo")
    //必须@RequestBody 接收json数据 否则无法自动封装
    public  String  saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        String saveSpuInfo = spuService.saveSpuInfo(pmsProductInfo);
        return saveSpuInfo;
    }

    @PostMapping("/fileUpload")
    //前端提交的数据：  file: (binary)
    public String fileUpload(@RequestParam("file")  MultipartFile multipartFile){
        //将图片 或者 音视频 上传到分布式文件系统


        //将图片的存储路径返回给页面
        return "success";
    }
}
