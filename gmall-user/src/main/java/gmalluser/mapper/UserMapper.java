package gmalluser.mapper;

import gmalluser.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

//整合tk.mybatis的通用Mapper
public interface UserMapper extends Mapper<UmsMember> {
//    List<UmsMember> selectAllUsers();
}
