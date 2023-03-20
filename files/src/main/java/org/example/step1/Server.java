package org.example.step1;

import com.alibaba.fastjson2.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int port=55333;
    public static void startServer(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // 监听指定端口，等待连接请求
                Socket socket = serverSocket.accept();

                // 处理连接请求
                new Thread(() -> {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        // 读取消息字符串
                        String command = in.readLine();

                        //处理消息

                        System.out.println("command=:"+command);
                        executeCommand(command);
                        // 发送响应消息
                        socket.getOutputStream().write("OK".getBytes());
                        socket.getOutputStream().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    public static void executeCommand(String command) {
       if("shutdown".equals(command)){
           try {
               // 执行系统命令
               //Runtime.getRuntime().exec("shutdown -s -t 10");
               System.out.println("即将关机");
           } catch (Exception e) {
               e.printStackTrace();
           }
       }

    }

    public static void main(String[] args) throws IOException {
        startServer(port);
    }
}
