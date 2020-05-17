package com.hou.gmallsearch;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class crontroller {

    @RequestMapping("/hello")
    public String helo(){
        System.out.println("请求来了");
        return "index";
    }
}
