package com.hou.gmallmanage.service;


import com.alibaba.dubbo.config.annotation.Service;
import com.hou.gmall.bean.PmsBaseCatalog1;
import com.hou.gmall.bean.PmsBaseCatalog2;
import com.hou.gmall.bean.PmsBaseCatalog3;
import com.hou.gmall.service.CatalogService;
import com.hou.gmallmanage.mapper.PmsBaseCatalog1Mapper;
import com.hou.gmallmanage.mapper.PmsBaseCatalog2Mapper;
import com.hou.gmallmanage.mapper.PmsBaseCatalog3Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    PmsBaseCatalog1Mapper catalog1Mapper;
    @Autowired
    PmsBaseCatalog2Mapper catalog2Mapper;
    @Autowired
    PmsBaseCatalog3Mapper catalog3Mapper;

    public List<PmsBaseCatalog1> getCatalog1(){
        List<PmsBaseCatalog1> pmsBaseCatalog1s = catalog1Mapper.selectAll();
        return pmsBaseCatalog1s;
    }

    public List<PmsBaseCatalog2> getCatalog2(String catalogId){
       PmsBaseCatalog2 catalog2 = new PmsBaseCatalog2();
       catalog2.setCatalog1Id(catalogId);
        List<PmsBaseCatalog2> select = catalog2Mapper.select(catalog2);
        return select;
    }

    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id){
        PmsBaseCatalog3 catalog3 = new PmsBaseCatalog3();
        catalog3.setCatalog2Id(catalog2Id);
        List<PmsBaseCatalog3> select = catalog3Mapper.select(catalog3);
        return select;
    }



}
