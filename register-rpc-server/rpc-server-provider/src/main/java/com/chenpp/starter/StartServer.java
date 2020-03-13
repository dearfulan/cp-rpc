package com.chenpp.starter;

import com.chenpp.config.SpringConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 2020/3/7
 * created by chenpp
 * 启动类，启动rpc服务端
 */
public class StartServer {
    public static void main(String[] args) {
//        UserServiceImpl userService = new UserServiceImpl();
//        RpcServer rpcServer = new RpcServer();
//        rpcServer.register(userService,8080);
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        ((AnnotationConfigApplicationContext) applicationContext).start();
    }
}
