package com.hou.cartweb.controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 *  读取 web-util下的拦截器 ，加入容器中
 */
@Component
@ComponentScan(basePackages = {"gmall.util"})
public class config {
}
