package com.chenpp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 2020/3/7
 * created by chenpp
 * 远程调用的服务端入口，使用socket监听
 */
public class RpcServer {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 注册服务实例，服务注册后，其他客户端通过接口调用就可以调用服务端的实现
     * */
    public void register(Object service ,int port)  {
        ServerSocket serverSocket = null;
        try {
            //创建socket
            serverSocket = new ServerSocket(port);
            while(true){
                //监听端口，是个阻塞的方法
                Socket socket = serverSocket.accept();
                //处理rpc请求，这里使用线程池来处理
                executor.submit(new HandleThread(service,socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
