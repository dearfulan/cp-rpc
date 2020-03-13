package com.chenpp.config;

import com.chenpp.server.v2.SpringRpcServer;
import com.chenpp.server.v3.ZkRpcServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * 2020/3/7
 * created by chenpp
 * 扫描com.chenpp.impl包下的类，注入spring容器
 */
@Component
@ComponentScan({"com.chenpp.impl","com.chenpp.register"})
public class SpringConfig {

//    @Bean
    public SpringRpcServer springRpcServer(){
        //这里直接写死启动的端口 : 8080
        return new SpringRpcServer(8080);
    }

    @Bean
    public ZkRpcServer zkRpcServer(){
        return new ZkRpcServer();
    }
}
