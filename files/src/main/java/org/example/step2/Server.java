package org.example.step2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务端 监听端口55533
 */
public class Server {
    private static final int port=55333;
    //线程池
    private static final ExecutorService executor=Executors.newFixedThreadPool(10);
    //模拟ack
    private static String ACK="helloworld";
    public static void main(String[] args) throws Exception {
        start();
    }

    private static void start(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port " + port + "...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Runnable messageTask = new MessageTask(clientSocket);
                executor.execute(messageTask);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class MessageTask implements Runnable{
        private Socket clientSocket;
        public MessageTask(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try ( InputStream inputStream=clientSocket.getInputStream();
                  OutputStream outputStream=clientSocket.getOutputStream();) {
                // 读取消息字符串
                byte[] bytes = new byte[1024];

                int len=inputStream.read(bytes);
                String command=new String(bytes,0,len);
                System.out.println("收到命令:"+command);

                //处理消息

                System.out.println("command=:"+command);
                if("shutdown".equals(command)){
                    //1.发送ack
                    outputStream.write(ACK.getBytes());
                    //2.收到server的ACK

                    int len2 = inputStream.read(bytes);
                    String ciphertext = new String(bytes, 0, len2);
                    System.out.println("ciphertext="+ciphertext);
                    String plaintext=deEncrypt(ciphertext,1);
                    //3.比较后发送应答
                    if(plaintext.equals(ACK)){
                        System.out.println("shutdown command received!");
                        System.out.println("机器10s后关机");
                        //Runtime.getRuntime().exec("shutdown -s -t 10");
                        outputStream.write("ok".getBytes());
                    }else{
                        System.out.println("Invalid ACK");
                    }
                }
                // 发送响应消息
                clientSocket.getOutputStream().write("OK".getBytes());
                clientSocket.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 密码解密
     * @param ciphertext
     * @param shift
     * @return
     * @throws Exception
     */
    private static String deEncrypt(String ciphertext,int shift) throws Exception {
        String plaintext="";
        for(int i=0;i<ciphertext.length();++i){
            char c=ciphertext.charAt(i);
            c=(char)((c-'a'-shift)%26+'a');
            plaintext+=c;
        }
        return plaintext;
    }
}

