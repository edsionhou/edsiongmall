package com.hou.gmall.service;

import com.hou.gmall.bean.UmsMember;
import com.hou.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;


public interface UserService {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);
}
