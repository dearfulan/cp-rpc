package com.chenpp.discovery;

import com.chenpp.config.ZKConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Description
 * @Author: chenpp
 * @Date: 2020/3/11 15:13
 */
@Component
public class ServiceDiscoveryImpl implements IServiceDiscovery {

    private Map<String,String> serviceMap = new HashMap<String,String>();

    private List<String> serviceAddresses;

    private CuratorFramework curatorFramework;


    {   // 通过curator连接zk
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(ZKConfig.ZK_CONNECTION)
                .sessionTimeoutMs(ZKConfig.SESSION_TIMEOUT)
                .retryPolicy(new ExponentialBackoffRetry(1000, 10)).build();
        //启动
        curatorFramework.start();
    }


    public String discover(String serviceName) {
        //根据serviceName获取对应的path
        String nodePath = ZKConfig.REGISTER_NAMESPACE + "/" + serviceName;
        try {
            //这里不考虑集群，一个服务只发布一个实例
            serviceAddresses = curatorFramework.getChildren().forPath(nodePath);
            addServiceAddress(serviceAddresses,serviceName);
            //动态发现服务节点变化，需要注册监听
            registerWatcher(nodePath,serviceName);
            System.out.println("获取服务:"+serviceName +"的服务地址:"+serviceMap.get(serviceName));
        } catch (Exception e) {
            throw new RuntimeException("服务发现获取子节点异常！", e);
        }
        return serviceMap.get(serviceName);
    }


    /**
     * 监听节点变化，更新serviceAddresses
     *
     * @param path
     */
    private void registerWatcher(final String path,final String serviceName) {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, path, true);

        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                serviceAddresses = curatorFramework.getChildren().forPath(path);
                addServiceAddress(serviceAddresses,serviceName);
                System.out.println("监听到节点:" + path + "变化为:" + serviceAddresses + "....");
            }
        });
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            throw new RuntimeException("监听节点变化异常！", e);
        }
    }

    private void addServiceAddress(List<String> serviceAddresses,String serviceName){
        if (!CollectionUtils.isEmpty(serviceAddresses)) {
            String serviceAddress = serviceAddresses.get(0);
            serviceMap.put(serviceName,serviceAddress);
        }
    }

}
