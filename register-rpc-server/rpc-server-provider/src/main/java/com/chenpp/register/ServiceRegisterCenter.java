package com.chenpp.register;

import com.chenpp.config.ZKConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author: chenpp
 * @Date: 2020/3/10 18:52
 */
@Component
public class ServiceRegisterCenter implements IServiceRegister {

    private CuratorFramework curatorFramework;

    {   // 通过curator连接zk
        curatorFramework = CuratorFrameworkFactory.builder().
                //定义连接串
                        connectString(ZKConfig.ZK_CONNECTION).
                // 定义session超时时间
                        sessionTimeoutMs(ZKConfig.SESSION_TIMEOUT).
                //定义重试策略
                        retryPolicy(new ExponentialBackoffRetry(1000, 10)).build();
        //启动
        curatorFramework.start();
    }

    //实现注册方法,将对应的服务名称和服务地址注册到zk上  serviceAddress--- ip : port
    public void register(String serviceName, String serviceIp, int port) {
        //注册相应的服务 注意 zk注册的节点名称需要以/开头
        String servicePath = ZKConfig.REGISTER_NAMESPACE + "/" + serviceName;
        try {
            //判断 /${registerPath}/${serviceName}节点是否存在，不存在则创建对应的持久节点
            if (curatorFramework.checkExists().forPath(servicePath) == null) {
                curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath, "0".getBytes());
            }
            //设置节点的value为对应的服务地址信息(临时节点)
            String serviceAddress = servicePath + "/" + serviceIp + ":" + port;
            String zkNode = curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(serviceAddress, "0".getBytes());
            System.out.println(serviceName + "服务,地址:" + serviceAddress + " 注册成功：" + zkNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
