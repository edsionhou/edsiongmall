package com.hou.gmallmanage.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.hou.gmall.bean.PmsProductInfo;
import com.hou.gmall.service.SpuService;
import com.hou.gmallmanage.mapper.PmsProductInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;

    public List<PmsProductInfo> getSpuList(String catalog3Id){  //根据3级目录 查询 SPU 集合
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(pmsProductInfo);
        return pmsProductInfos;
    }

    public  String saveSpuInfo(PmsProductInfo pmsProductInfo){  //保存 新建的 SPU信息
        int insert = pmsProductInfoMapper.insertSelective(pmsProductInfo);
        return "success";
    }
}
