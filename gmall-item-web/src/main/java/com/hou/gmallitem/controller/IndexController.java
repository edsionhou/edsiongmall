package com.hou.gmallitem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class IndexController {

    @GetMapping("index")
    public String  indexh(ModelMap map) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(i);
        }
        map.put("hello","呵呵 尼玛死了！！");
        map.put("list",list);
        map.put("check",1);
        return "index";

    }
}
