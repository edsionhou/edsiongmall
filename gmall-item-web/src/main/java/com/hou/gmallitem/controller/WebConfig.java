package com.hou.gmallitem.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig  extends WebMvcConfigurerAdapter {
    @Override
    //很奇怪 同名之后 ，每次都走serevlet的hehe.html 不走我的静态资源  所以建议不要搞同名的
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
}
