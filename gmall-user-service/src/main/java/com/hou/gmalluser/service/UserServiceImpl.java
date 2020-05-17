package com.hou.gmalluser.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hou.conf.RedisUtil;
import com.hou.gmall.bean.UmsMember;
import com.hou.gmall.bean.UmsMemberReceiveAddress;
import com.hou.gmall.service.UserService;
import com.hou.gmalluser.mapper.UmsMemberMapper;
import com.hou.gmalluser.mapper.UmsMemberReceiveAddressMapper;
import com.hou.gmalluser.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.Id;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service //com.alibaba.dubbo.config.annotation.Service;
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    UmsMemberMapper umsMemberMapper;

    @Autowired
    RedisUtil redisUtil;
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

    @Override
    public UmsMember toLogin(UmsMember umsMember) {
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            if(jedis!=null){
                //redis中根据 userinfo  生成hashmap key=用户名 value为用户信息
                List<String> userList = jedis.hmget("userinfo", "user:" + umsMember.getUsername());
                String userInfo = userList.get(0);
                if(StringUtils.isNotBlank(userInfo)){
                    UmsMember umsMemberInRedis = JSON.parseObject(userInfo, UmsMember.class);
                    if(umsMember.getPassword()==null || umsMember.getPassword().equals(umsMemberInRedis.getPassword())){
                        //密码正确
                        return umsMemberInRedis;
                    }else{
                        //密码错误
                        return null;
                    }
                }else{
                    //缓存没有 查数据库   我没设置redis过期时间，因为hmset不能设置。。。redis只能设置顶级key的过期时间
                    umsMember = umsMemberMapper.selectOne(umsMember);
                    if(umsMember!=null){
                        Map<String,String> map = new HashMap<>();
                        map.put("user:"+umsMember.getUsername(),JSON.toJSONString(umsMember));
                        jedis.hmset("userinfo",map);
                    }

                 return    umsMember == null ?  null : umsMember;
                }

            }else{
                //连接redis失败，查数据库
                umsMember = umsMemberMapper.selectOne(umsMember);
                return    umsMember == null ?  null : umsMember;
            }

        }catch(Exception e ){
            e.printStackTrace();
            return null;
        }finally {
            jedis.close();
        }
    }

    @Override
    public void addUserToken(String memberId, String token) { //token存入redis
       Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            jedis.setex("user:"+memberId+":token",60*60*2,token);
        }catch (Exception e){

        }finally {
            jedis.close();
        }

    }

    @Override
    public UmsMember addOauthUser(UmsMember umsMember) {  //保存社交用户
    umsMemberMapper.insertSelective(umsMember);
    System.out.println(umsMember.getId());
    return umsMember;
    }

    @Override
    public UmsMember checkOauth2User(UmsMember umsMember) {
        UmsMember umsMember1 = umsMemberMapper.selectOne(umsMember);
        System.out.println("执行了checkOauth2User");
        return umsMember1;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressByAddressId(String receiveAddressId) {
        UmsMemberReceiveAddress address = new UmsMemberReceiveAddress();
        address.setId(Long.parseLong(receiveAddressId));
        UmsMemberReceiveAddress address1 = umsMemberReceiveAddressMapper.selectOne(address);
        return address1;
    }
}
