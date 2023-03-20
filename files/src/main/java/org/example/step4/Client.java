package org.example.step4;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;

import javax.jms.*;
import javax.jms.Queue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Client {
    private static String username;
    private static List<String> users;

    private static List<String> groups;
    private static String basePath="D:\\codes\\java\\MiddleWareLab1\\src\\main\\java\\org\\example\\step4";

    private static String dbQueue="chat-db-queue";

//    private static Map<String,List<Message>> messageBox=new HashMap<>();
    public Client(){
        /**
         * 模拟数据库获取
         */
        users=new ArrayList<>();
        users.add("Tom");
        users.add("Jack");
        users.add("Mark");

        groups=new ArrayList<>();
        groups.add("group-a");
        groups.add("group-b");
    }
    public static void main(String[] args) throws JMSException, IOException {

        Client client=new Client();

        username="";
        // 创建连接和会话

        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();

        connection.setExceptionListener(new ExceptionListener() {
            @Override
            public void onException(JMSException e) {
                //处理异常
                System.out.println("error!");
            }
        });
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Scanner scanner=new Scanner(System.in);
        System.out.println("Please input your username:");
        username= scanner.nextLine();
        while(!users.contains(username)){
            System.out.println("No such user,please input again!");
            username=scanner.nextLine();
        }

        String command="";
        /**
         * 异步获取信息
         */
        for (String s:users){
            if(!s.equals(users)){
                ReceiveMessage(s,session);
            }
        }
        while(true){
            System.out.println("Please choose service:");
            System.out.println("sendmessage [user] [message]\\groupsend [groupname] [message]\\groupjoin [groupname]\\sendfile [user] filename...");
            command= scanner.nextLine();
            String[] arrs = command.split(" ");
            if(command.startsWith("sendmessage")){
                String user=arrs[1];
                String message=arrs[2];
                SendMessage(user,message,session);
            }else if(command.startsWith("groupsend")){
                String group=arrs[1];
                String message=arrs[2];
                if(!groups.contains(group)){
                    System.out.println("group doesn't exists");
                }else{
                    GroupSend(session,group,message);
                }
            }else if(command.startsWith("groupjoin")){
                String group=arrs[1];
                if(!groups.contains(group)){
                    System.out.println("group doesn't exists");
                }else{
                    BindTopic(session,group);
                }
            }else if(command.startsWith("sendfile")){
                String user=arrs[1];
                String filename=arrs[2];
                sendFile(filename,user,session);
            }
            else break;
        }


        // 关闭连接和会话
        session.close();
        connection.close();
    }

    /**
     * 发送消息 支持存储转发
     * @param user
     * @param message
     * @param session
     * @throws JMSException
     */
    public static void SendMessage(String user,String message,Session session) throws JMSException {
        Queue dbQueue = session.createQueue(Client.dbQueue);
        MessageProducer dbProducer = session.createProducer(dbQueue);
        TextMessage dbMessage = session.createTextMessage(message);
        dbMessage.setStringProperty("to",user);
        dbMessage.setStringProperty("from",username);
        dbProducer.send(dbMessage);

//        Destination destination = session.createQueue("chat-" + username + "-" + user);
//        MessageProducer producer = session.createProducer(destination);
//        TextMessage text = session.createTextMessage(message);
//        producer.send(text);
//        System.out.println(username+" Sent message: " + text.getText());
    }

    public static void ReceiveMessage(String user,Session session) throws JMSException {
        Destination destination = session.createQueue("chat-" + user + "-" + username);
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if(message instanceof TextMessage){
                    TextMessage textMessage=(TextMessage) message;
                    try {
                        System.out.println("Received from "+user+": "+textMessage.getText());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }else if(message instanceof BytesMessage){
                    //文件存储
                    BytesMessage bytesMessage=(BytesMessage)message;
                    try {
                        System.out.println("尝试保存");
                        String path = message.getStringProperty("filename");
                        byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                        bytesMessage.readBytes(bytes);
                        File file = new File(basePath+"\\received_file\\" + path);
                        Files.write(file.toPath(),bytes);
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        });
    }

    public static void BindTopic(Session session,String group) throws JMSException {
        Topic topic = session.createTopic("chat-group-" + group);
        MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try{
                    System.out.println("Received message from group "+group+" :"+((TextMessage)message).getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void GroupSend(Session session,String group,String text) throws JMSException {
        Topic topic = session.createTopic("chat-group-" + group);
        MessageProducer producer = session.createProducer(topic);
        TextMessage message = session.createTextMessage(text);
        producer.send(message);
    }

    public static void sendFile(String path,String user,Session session) throws JMSException, IOException {
        Queue queue = session.createQueue("chat-" + username + "-" + user);
        MessageProducer producer = session.createProducer(queue);
        File file = new File(basePath+"\\send_file\\" + path);
        byte[] bytes = Files.readAllBytes(file.toPath());

        BytesMessage message = session.createBytesMessage();
        message.setStringProperty("filename",path);
        message.writeBytes(bytes);
        producer.send(message);
    }

}
