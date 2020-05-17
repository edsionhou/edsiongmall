package com.hou.gmallpayment.test;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class consumeTopic {
    public static void main(String[] args) {
        ConnectionFactory connect = new ActiveMQConnectionFactory("admin","admin","tcp://192.168.199.240:61616");
        Session session = null;
        try {
            Connection connection = connect.createConnection();
            connection.setClientID("消费者1");
            connection.start();
            /*
            第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            CLIENT_ACKNOWLEDGE 代表自动确认，如果没有 session.rollback(); 则自动确认，有的话 就回滚
            CLIENT_ACKNOWLEDGE  需要手动确认，相当于 true模式下的 commit，rollback
            SESSION_TRANSACTED  配合true，开启事务模式

            quque和topic差别： 前者在 broker中持久化 是默认的
                            后者需要在接收者进行持久化 需要指定客户端
             */
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            Topic topic1 = session.createTopic("topic1");
//            MessageConsumer consumer = session.createConsumer(topic1);
            TopicSubscriber durableSubscriber = session.createDurableSubscriber(topic1, "DurableSubscriber1");
            durableSubscriber.setMessageListener(new MessageListener() {
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
            System.in.read();
            durableSubscriber.close();
            session.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("回滚");
        }
    }
}
