package com.chenpp.proxy;

import java.lang.reflect.Proxy;

/**
 * 2020/3/7
 * created by chenpp
 * 创建代理对象
 */
public class RpcProxy<T> {

    public static <T> T getInstance(Class<T> classInterface, String host, int port) {
        return (T) Proxy.newProxyInstance(classInterface.getClassLoader(), new Class[]{classInterface}, new RpcInvocationHandler(host, port));
    }
}
