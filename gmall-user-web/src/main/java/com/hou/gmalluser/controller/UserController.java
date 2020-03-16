package com.hou.gmalluser.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hou.gmall.bean.UmsMember;
import com.hou.gmall.bean.UmsMemberReceiveAddress;
import com.hou.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@ResponseBody
public class UserController {

    @Reference //   使用dubbo协议，远程调用 dubbo-admin中看到的服务
    UserService userService;

    @GetMapping(value = "/hello")
    public String hello(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        System.out.println(requestURL);
        return "hello";
    }

    @RequestMapping(value = "/getAllUser", method = RequestMethod.GET)
    @ResponseBody
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMembers = userService.getAllUser();
        return umsMembers;
    }

    @GetMapping("/getAddr")
    @ResponseBody
    //RequestBody 接收前端传来的json类型的参数
    public  List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId){
        List<UmsMemberReceiveAddress> receiveAddressByMemberId = userService.getReceiveAddressByMemberId(memberId);
        System.out.println(receiveAddressByMemberId);
        return receiveAddressByMemberId;
    }


}
