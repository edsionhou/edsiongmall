package com.hou.gmall.service;

import com.hou.gmall.bean.UmsMember;
import com.hou.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;


public interface UserService {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    UmsMember toLogin(UmsMember umsMember);

    void addUserToken(String memberId, String token);

    UmsMember addOauthUser(UmsMember umsMember);

    UmsMember checkOauth2User(UmsMember umsMember);

    UmsMemberReceiveAddress getReceiveAddressByAddressId(String receiveAddressId);
}
