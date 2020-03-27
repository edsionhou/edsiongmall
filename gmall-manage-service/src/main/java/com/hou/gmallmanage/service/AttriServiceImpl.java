package com.hou.gmallmanage.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.hou.gmall.bean.PmsBaseAttrInfo;
import com.hou.gmall.bean.PmsBaseAttrValue;
import com.hou.gmall.bean.PmsBaseSaleAttr;
import com.hou.gmall.service.AttriService;
import com.hou.gmallmanage.mapper.PmsBaseAttrInfoMapper;
import com.hou.gmallmanage.mapper.PmsBaseAttrValueMapper;
import com.hou.gmallmanage.mapper.PmsBaseSaleAttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class AttriServiceImpl implements AttriService {

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    public List<PmsBaseAttrInfo> attrInfoLists(String catalog3Id){
        PmsBaseAttrInfo info = new PmsBaseAttrInfo();
        info.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(info);
        for (PmsBaseAttrInfo baseAttrInfo: pmsBaseAttrInfos) {
            //遍历每个PmsBaseAttrInfo   获取id
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue); //获取到attr id对应的 Value
           baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        }
        return pmsBaseAttrInfos;
    }

    public PmsBaseAttrInfo saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo){
        String id = pmsBaseAttrInfo.getId();
        if(id==null){
            //保存操作
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);

            //保存属性值
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue attrValue :
                    attrValueList) {
                attrValue.setAttrId(pmsBaseAttrInfo.getId());  //PmsBaseAttrValue 需要设置一个attr_idd 。getId使用的是info返回的主键值
                pmsBaseAttrValueMapper.insertSelective(attrValue);
            }

            //insertSelective 只给有值的字段赋值（会对传进来的值做非空判断）
            return pmsBaseAttrInfo;
        }else {
            //id不为空，修改操作  info
            Example e = new Example(PmsBaseAttrInfo.class);
            e.createCriteria().andEqualTo("id",id);
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,e);  //根据id 修改

            // 修改value
            PmsBaseAttrValue attrValueDel = new PmsBaseAttrValue();
            attrValueDel.setAttrId(id);
            pmsBaseAttrValueMapper.delete(attrValueDel);  //按照 attr_id 删除之前的 属性值
            for (PmsBaseAttrValue attrValueAdd : pmsBaseAttrInfo.getAttrValueList()) {
//                attrValueAdd.setId(null);
                attrValueAdd.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insert(attrValueAdd);  //再增加 修改过的
            }
            return pmsBaseAttrInfo;

        }

    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {  //按照attrId 获取 属性值集合
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return pmsBaseAttrValues;
    }


    public List<PmsBaseSaleAttr> baseSaleAttrList(){  //获取所有销售属性
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = pmsBaseSaleAttrMapper.selectAll();
        return pmsBaseSaleAttrs;
    }
}
