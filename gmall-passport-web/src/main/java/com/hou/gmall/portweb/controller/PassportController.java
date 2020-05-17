package com.hou.gmall.portweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hou.gmall.bean.UmsMember;
import com.hou.gmall.service.UserService;
import gmall.util.HttpClientUtil;
import gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {
    @Reference
    UserService userService;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(@RequestParam(value = "ReturnUrl", required = false) String returnUrl, ModelMap map) {
        if (StringUtils.isNotBlank(returnUrl)) {
            map.put("ReturnUrl", returnUrl);
        }
        return "index";

    }

    @PostMapping("/login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        String token = "";
        //调用用户服务， 验证用户名和密码
        System.out.println("访问passport。login");
        UmsMember umsMemberLogin = userService.toLogin(umsMember);

        if (umsMemberLogin != null) {
            //1.登录成功 通过JWT制作 token
            Map<String, Object> map = new HashMap<>();
            String memberId = umsMemberLogin.getId();
            String username = umsMemberLogin.getUsername();
            map.put("memberId", memberId);
            map.put("username", username);
            String remoteAddr = request.getHeader("x-forwarded-for");//可以从nginx代理中获取真正的 请求地址
            if (StringUtils.isBlank(remoteAddr)) {
                remoteAddr = request.getRemoteAddr(); // 从request请求中获取远端请求IP ，如果是nginx负载均衡 那其实不是真正的IP
            }
            //2.jwt算法 key--map---盐值   目前我们没进行加密
            token = JwtUtil.encode("2020gmall", map, remoteAddr);

            //3.将token存入redis一份
            userService.addUserToken(memberId, token);
        } else {
            //2.登录失败
            token = "failed";
        }
        return token; //返回的是string，所以需要前端用text接受
        /*
        @responseBody注解的作用是将controller的方法返回的对象通过适当的转换器转换为指定的格式之后，写入到response对象的body区，通常用来返回JSON数据或者是XML
        效果等同于如下代码：
        　　@RequestMapping("/login")
        　　public void login(User user, HttpServletResponse response){
        　　　　response.getWriter.write(JSONObject.fromObject(user).toString());
        　　}
         */
    }

    @GetMapping("/verify")
    @ResponseBody
    public String verify(String token, String currentIP) { //核实; 查对; 核准; 证明; 证实;  Token!
        //通过JWT校验token真假
        Map<String, Object> map = new HashMap<>();
        /*
           拦截器中发送了HttpClientUtil.doGet("http://passport.gmall.com:8020/verify?token=" + token+"&currentIP"+remoteAddr)
           进行认证，实际上 从此request中获取的IP，是 拦截器所在项目的IP，而不是客户端的IP，所以我们无法从request中获取到客户的IP，
           只能在拦截器中多一个参数，currentIP 代表客户端请求时的IP
         */
        Map<String, Object> decode = JwtUtil.decode(token, "2020gmall", currentIP);
        if (decode != null) {
            map.put("status", "success");
            map.put("memberId", decode.get("memberId"));
            map.put("username", decode.get("username"));
        } else {
            map.put("status", "falied");
        }


        return JSON.toJSONString(map);
    }

    @GetMapping("/vlogin")
    public String vLogin(String code, ModelMap map, HttpServletRequest request) {  //新浪微博登录
        //1.授权码code 换取access_token
//        Map<String, String> paramMap = new HashMap<>();
//        paramMap.put("client_id", "2072389243");
//        paramMap.put("client_secret", "56d552f8ed09d65ed345601a041c3eb5");
//        paramMap.put("grant_type", "authorization_code");
//        paramMap.put("redirect_uri", "http://passport.gmall.com:8020/vlogin");
//        paramMap.put("code", code);
//        String s3 = HttpClientUtil.doPost("https://api.weibo.com/oauth2/access_token?", paramMap);
        Map<String, String> map22 = new HashMap<>();
        String s3 = HttpClientUtil.doPost("https://api.weibo.com/oauth2/access_token?client_id=2072389243" +
                "&client_secret=56d552f8ed09d65ed345601a041c3eb5&grant_type=authorization_code" +
                "&redirect_uri=http://passport.gmall.com:8020/vlogin" +
                "&code=" + code, map22);
        //S3可能返回的是 错误的信息，需要处理
        Map<String, String> parseObject = JSON.parseObject(s3, Map.class);  //把access_token解析出来
        if(StringUtils.isBlank(parseObject.get("error")) ){
            // 没有返回正常的 access_token 重定向到登录页面
            return "redirect:/login";

        }
        String access_token = parseObject.get("access_token");
        String source_uid = parseObject.get("uid");
        String user_infos = HttpClientUtil.doGet("https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + source_uid);
        Map<String, String> userMap = JSON.parseObject(user_infos, Map.class);

        //2.将用户信息保存至数据库，类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType(2);
//        umsMember.setAccessCode(code); 不能加这个把 每次都不同的
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid(Long.parseLong(source_uid));
        umsMember.setUsername(userMap.get("screen_name")); //新闻社
        umsMember.setNickname("呵呵");
        umsMember.setCity(userMap.get("location"));

        UmsMember umsMemberCheck = userService.checkOauth2User(umsMember);
        if (umsMemberCheck == null) {
            umsMemberCheck = userService.addOauthUser(umsMember);//自动生成了主键返回 是在userserviceimpl那个对象Ums对象返回的 不是这里切记！！！
        }

        //3.生成JWT的token，并重定向至首页，携带该token
        String memberId = umsMemberCheck.getId();
        String username = umsMemberCheck.getUsername();
        System.out.println();
        map.put("memberId", memberId);
        map.put("username", username);
        String remoteAddr = request.getHeader("x-forwarded-for");//可以从nginx代理中获取真正的 请求地址
        if (StringUtils.isBlank(remoteAddr)) {
            remoteAddr = request.getRemoteAddr(); // 从request请求中获取远端请求IP ，如果是nginx负载均衡 那其实不是真正的IP
        }
        //2.jwt算法 key--map---盐值   目前我们没进行加密
        String token = JwtUtil.encode("2020gmall", map, remoteAddr);
        //3.将token存入redis一份
        userService.addUserToken(memberId, token);

        return "redirect:http://search.gmall.com:8011/index?&token=" + token;
    }

}
