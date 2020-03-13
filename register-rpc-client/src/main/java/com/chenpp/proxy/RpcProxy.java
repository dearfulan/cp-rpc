package com.chenpp.proxy;

import com.chenpp.discovery.IServiceDiscovery;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

/**
 * 2020/3/7
 * created by chenpp
 * 创建代理对象
 */
@Component
public class RpcProxy<T> {


    public static <T> T getInstance(Class<T> classInterface, String serviceName,IServiceDiscovery serviceDiscovery) {
        return (T) Proxy.newProxyInstance(classInterface.getClassLoader(), new Class[]{classInterface}, new RpcInvocationHandler(serviceName,serviceDiscovery));
    }
}
