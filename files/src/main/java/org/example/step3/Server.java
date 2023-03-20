package org.example.step3;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;

public class Server {
    public static void main(String[] args) throws JMSException, IOException {
        // 创建连接工厂
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        // 创建连接
        Connection connection = connectionFactory.createConnection();

        // 启动连接
        connection.start();

        // 创建会话
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // 创建Topic
        Topic topic = session.createTopic("shutdown-topic");

        // 创建消息消费者
        MessageConsumer consumer = session.createConsumer(topic);

        // 接收消息
        consumer.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                try {
                    // 处理消息
                    String text=((TextMessage)message).getText();
                    System.out.println("Received:"+text);
                    if("shutdown".equals(text)){
                        System.out.println("机器即将关机");
//                        Runtime.getRuntime().exec("shutdown -s -t 10");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 等待消息
        System.in.read();

        // 关闭连接
        connection.close();
    }
}
