package com.chenpp.starter;

import com.chenpp.impl.UserServiceImpl;
import com.chenpp.server.RpcServer;

/**
 * 2020/3/7
 * created by chenpp
 * 启动类，启动rpc服务端
 */
public class StartServer {
    public static void main(String[] args) {
        UserServiceImpl userService = new UserServiceImpl();
        RpcServer rpcServer = new RpcServer();
        rpcServer.register(userService,8080);
    }
}
