package com.hou.gmallmanage;

import org.assertj.core.internal.cglib.asm.$ClassReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = {"com.hou.gmallmanage.mapper"})
public class GmallManageServiceApplication {
    public static void main(String[] args) {

        SpringApplication.run(GmallManageServiceApplication.class, args);

    }


}
