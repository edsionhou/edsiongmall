package gmalluser.mapper;

import com.hou.gmall.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

//整合tk.mybatis的通用Mapper
public interface UserMapper extends Mapper<UmsMember> {
//    List<UmsMember> selectAllUsers();
}
