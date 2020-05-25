package com.hou.gmallpayment.test;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class MQconsume1 {
    public static void main(String[] args) throws JMSException {
        ConnectionFactory connect = new ActiveMQConnectionFactory("admin","admin","tcp://192.168.199.240:61616");
        Session session = null;
        try {
            Connection connection = connect.createConnection();
            connection.start();
            /*
            第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            CLIENT_ACKNOWLEDGE 代表自动确认，如果没有 session.rollback(); 则自动确认，有的话 就回滚
            CLIENT_ACKNOWLEDGE  需要手动确认，相当于 true模式下的 commit，rollback
            SESSION_TRANSACTED  配合true，开启事务模式
             */
           session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            Destination testqueue = session.createQueue("TEST1");

            MessageConsumer consumer = session.createConsumer(testqueue);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    if (message instanceof TextMessage) {
                        try {
                            String text = ((TextMessage) message).getText();
                            System.out.println(text);
                            message.acknowledge();
                        } catch (JMSException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            });


//            System.in.read();
            consumer.close();
            session.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("回滚");
        }

    }
}

