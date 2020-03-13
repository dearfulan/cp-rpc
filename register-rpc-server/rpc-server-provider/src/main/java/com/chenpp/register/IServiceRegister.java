package com.chenpp.register;

/**
 * @Description
 * @Author: chenpp
 * @Date: 2020/3/10 17:18
 * 注册接口
 */
public interface IServiceRegister {

    /**
     * 注册服务
     * @param serviceName 服务名称
     * @param serviceIp 服务IP
     * @param port 端口号
     */
    void register(String serviceName,String serviceIp,int port);
}
