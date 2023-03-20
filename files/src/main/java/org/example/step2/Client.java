package org.example.step2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.sql.Time;
import java.util.Scanner;

/**
 * 客户端 实现消息的发送
 */
public class Client {
    private static final int port=55333;
    private static final String host="127.0.0.1";
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket(host, port);
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        //1.发送关机命令
        String request = "shutdown";
        byte[] buffer = request.getBytes();
        out.write(buffer);
        //2.获得ack 加密后写回
        byte[] ack = new byte[1024];
        int len=in.read(ack);
        String plaintext=new String(ack,0,len);
        String ciphertext=encrypt(plaintext,1);
        out.write(ciphertext.getBytes());
        //3.再次获得ack
        byte[] response = new byte[1024];
        int rlen=in.read(response);
        String responseString=new  String(response,0,rlen);
        if("ok".equals(responseString)){
            System.out.println("Shutdown command sent successfully.");
        }else{
            System.out.println("Shutdown command failed to send.");
        }

        out.close();
        in.close();
        socket.close();
    }

    /**
     * 简单密码加密
     * @param plaintext
     * @param shift
     * @return
     * @throws Exception
     */
    private static String encrypt(String plaintext,Integer shift) throws Exception {
        String ciphertext="";
        for(int i=0;i<plaintext.length();++i){
            char c=plaintext.charAt(i);
            c=(char)((c-'a'+shift)%26+'a');
            ciphertext+=c;
        }
        return ciphertext;
    }
}
