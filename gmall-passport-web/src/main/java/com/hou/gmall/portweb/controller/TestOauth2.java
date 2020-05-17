package com.hou.gmall.portweb.controller;

import gmall.util.HttpClientUtil;

import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {
    public static  void appluToWEIBO(){
        HttpClientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=2072389243&response_type=code&redirect_uri=http://passport.gmall.com:8020/vlogin");
    }

    public  static String applyAccess_token(String code){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id", "2072389243");
        paramMap.put("client_secret", "56d552f8ed09d65ed345601a041c3eb5");
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("redirect_uri", "http://passport.gmall.com:8020/vlogin");
        paramMap.put("code", code);
        String s3 = HttpClientUtil.doPost("https://api.weibo.com/oauth2/access_token?", paramMap);
        return s3; //返回了json
    }
    public static String queryUserInfo(String access_token){
        String s4 = "https://api.weibo.com/2/users/show.json?access_token="+access_token;
        return  s4;
    }

    public static void main(String[] args) {

       /* 1 .向微博请求  client-id = APP KEY  url = 授权回调页(授权成功后重定向至)：http://passport.gmall.com:8020/vlogin
        得到新浪的授权页*/

        String s1 = HttpClientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=2072389243&response_type=code&redirect_uri=http://passport.gmall.com:8020/vlogin");
        System.out.println(s1);

       /* 2.点击 授权，完成后 重定向到授权回调页 http://passport.gmall.com:8020/vlogin?code=02ad8b9c1687f925c1428eef0da18854
        附带了 code 9c324a4eb7ab72f31fc4ab0176ff00bb 每次都不同的*/
         /*
        3.  gmall通过授权码code 去交换access_token  必须是post请求
           client-id =APP KEY   client-secret=App Secret
           redirect-url=谷粒的登录页  code=新浪返回的code
        */
        String s2 = "https://api.weibo.com/oauth2/access_token?client_id=2072389243&client_secret=56d552f8ed09d65ed345601a041c3eb5&grant_type=authorization_code" +
                "&redirect_uri=http://passport.gmall.com:8020/vlogin&code=CODE";

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id", "2072389243");
        paramMap.put("client_secret", "56d552f8ed09d65ed345601a041c3eb5");
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("redirect_uri", "http://passport.gmall.com:8020/vlogin");
        paramMap.put("code", "9c324a4eb7ab72f31fc4ab0176ff00bb");
        String s3 = HttpClientUtil.doPost("https://api.weibo.com/oauth2/access_token?", paramMap);
        System.out.println(s3);
        /*
        {
            "access_token": "2.00mog8DGxGXPQC9462c13ae2xjBw8B",
            "remind_in": "157679999",
            "expires_in": 157679999,
            "uid": "5547725452",
            "isRealName": "true"
        }
         */
        /*
           4.根据微博的开发者API 网站 https://open.weibo.com/wiki/2/users/show中
           用access_token 查询用户信息
           返回了很多信息
         */
        String s4 = "https://api.weibo.com/2/users/show.json?access_token=2.00mog8DGxGXPQC9462c13ae2xjBw8B&uid=5547725452";
        String s = HttpClientUtil.doGet(s4);

    }
}
