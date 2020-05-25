package com.hou.gmallpayment;

import com.hou.conf.ActiveMQUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.ConnectionFactory;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallPaymentApplicationTests {
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Test
    public   void contextLoads() {
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        System.out.println(connectionFactory);
    }

}
