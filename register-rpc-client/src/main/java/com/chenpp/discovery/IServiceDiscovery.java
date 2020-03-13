package com.chenpp.discovery;

/**
 * @Description
 * @Author: chenpp
 * @Date: 2020/3/11 15:13
 */
public interface IServiceDiscovery {

    /**
     * 根据服务名称获取服务的真实调用地址
     * @param serviceName
     * @return
     */
    String discover(String serviceName);
}
