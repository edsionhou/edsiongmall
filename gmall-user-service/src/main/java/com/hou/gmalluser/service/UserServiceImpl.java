package com.hou.gmalluser.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.hou.gmall.bean.UmsMember;
import com.hou.gmall.bean.UmsMemberReceiveAddress;
import com.hou.gmall.service.UserService;
import com.hou.gmalluser.mapper.UmsMemberReceiveAddressMapper;
import com.hou.gmalluser.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.Id;
import java.util.List;


@Service //com.alibaba.dubbo.config.annotation.Service;
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
    public List<UmsMember> getAllUser() {
//        List<UmsMember> umsMemberList= userMapper.selectAllUsers();
//        List<UmsMember> umsMemberList = userMapper.selectAll();
        List<UmsMember> umsMembers = userMapper.selectAll();  //通用mapper
        return umsMembers;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        Example e = new Example(UmsMemberReceiveAddress.class);
//        e.createCriteria().andEqualTo("memberId",memberId);  //通过 memberid 来查询
        e.createCriteria().andEqualTo("memberId",memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(e);
        return umsMemberReceiveAddresses;
    }
}
