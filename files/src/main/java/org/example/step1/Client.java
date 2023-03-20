package org.example.step1;

import com.alibaba.fastjson2.JSON;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final int port=55333;
    private static final String host="127.0.0.1";
    public static void sendMessage(String ip, int port, String message) throws IOException {


        // 建立Socket连接，向目标主机发送消息
        try (Socket socket = new Socket(ip, port)) {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.print(message);
            out.flush();
        }
    }

    public static void main(String[] args) throws IOException {
        // 发送一条消息
        String message = new String("shutdown");
        sendMessage(host,port, message);

    }
}
