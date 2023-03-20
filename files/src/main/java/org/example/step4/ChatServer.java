package org.example.step4;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ChatServer implements Runnable {
    private String brokerUrl = "tcp://localhost:61616";
    private String chatQueueName = "chat-db-queue";
    private Connection dbConnection;
    private Session session;
    private MessageProducer producer;

    public ChatServer() {
        try {
            // 创建数据库连接
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat", "root", "123456");

            // 创建连接工厂
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);

            // 创建连接
            javax.jms.Connection connection = connectionFactory.createConnection();
            connection.start();

            // 创建会话
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        } catch (SQLException | JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // 创建Consumer
            MessageConsumer consumer = session.createConsumer(session.createQueue(chatQueueName));

            // 注册MessageListener
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        String from = message.getStringProperty("from");
                        String to = message.getStringProperty("to");

                        // 将消息保存到数据库
                        String text = ((TextMessage) message).getText();
                        PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO chatlog (message) VALUES (?)");
                        statement.setString(1, text);
                        statement.executeUpdate();
                        producer=session.createProducer(session.createQueue("chat-"+from+"-"+to));
                        // 转发消息给其他客户端
                        System.out.println("Server send message to the queue: "+"chat-"+from+"-"+to);
                        producer.send(message);
                    } catch (SQLException | JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        new Thread(server).start();
    }
}
