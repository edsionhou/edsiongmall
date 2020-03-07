package gmalluser.service;

import gmalluser.bean.UmsMember;
import gmalluser.bean.UmsMemberReceiveAddress;
import gmalluser.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);
}
