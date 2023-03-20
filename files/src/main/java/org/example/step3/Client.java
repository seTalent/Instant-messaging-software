package org.example.step3;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Client {
    public static void main(String[] args) throws JMSException {
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

        // 创建消息生产者
        MessageProducer producer = session.createProducer(topic);

        // 创建消息
        TextMessage message = session.createTextMessage("shutdown");

        // 发送消息
        producer.send(message);

        // 关闭连接
        connection.close();
    }
}
