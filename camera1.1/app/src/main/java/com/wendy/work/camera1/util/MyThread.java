package com.wendy.work.camera1.util;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.wendy.work.camera1.activity.CameraActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
/**
 * Created by daiyunlei on 2015/8/26.
 */
class MyThread extends Thread{
    Socket socket=null;
    File file;
    private Handler handler;
    public MyThread(File file,Handler handler)
    {
        this.file=file;
        this.handler=handler;
    }
    @Override
    public void run() {
        Message message=new Message();
        message.what=0x11;
        Bundle bundle=new Bundle();
        bundle.clear();
        socket=new Socket();
        try {
            //socket.connect(new InetSocketAddress("172.20.52.110",8090));
            File serverInfo=new File(Environment.getExternalStorageDirectory() + "/Camera1/serverInfo.txt");
            BufferedReader bufferedReader=new BufferedReader(new FileReader(serverInfo));
            String ip=bufferedReader.readLine();
            int port=Integer.parseInt(bufferedReader.readLine());
            bufferedReader.close();
            socket.connect(new InetSocketAddress(ip, port));
            try {
                FileInputStream fileInputStream=new FileInputStream(file);
                System.out.println();
                DataInputStream dataInputStream=new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeLong(file.length());
                dataOutputStream.flush();
                byte[] bytes=new byte[1024*8];
                int length;
                while((length=fileInputStream.read(bytes,0,bytes.length))>0)
                {
                    dataOutputStream.write(bytes, 0, length);
                    dataOutputStream.flush();
                }
                String feedback = dataInputStream.readUTF();
                String feedbackHtml= InterpretationConvert.ConvertToHtml(feedback);
                bundle.putString("message",feedbackHtml);
                message.setData(bundle);
                handler.sendMessage(message);
                dataOutputStream.close();
                fileInputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("connect is failed");
        }
        File DirPath=new File(Environment.getExternalStorageDirectory() + "/Camera1");
        File[] files=DirPath.listFiles();
        if(files.length>10)
        {
            for(int i=0;i<files.length;i++)
            {
                if(!files[i].getName().equals("serverInfo.txt"))
                    files[i].delete();
            }
        }
    }
}