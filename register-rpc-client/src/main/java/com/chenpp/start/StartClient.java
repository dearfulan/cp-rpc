package com.chenpp.start;

import com.chenpp.api.IUserService;
import com.chenpp.config.SpringConfig;
import com.chenpp.discovery.IServiceDiscovery;
import com.chenpp.discovery.ServiceDiscoveryImpl;
import com.chenpp.domain.User;
import com.chenpp.proxy.RpcProxy;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


/**
 * 2020/3/7
 * created by chenpp
 */
public class StartClient {

    public static void main(String[] args) {

        //先通过服务名从注册中心获取对应的信息

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        ((AnnotationConfigApplicationContext) applicationContext).start();

        IServiceDiscovery serviceDiscovery = (ServiceDiscoveryImpl) applicationContext.getBean(IServiceDiscovery.class);
        //由于rpc-server-api里只有实体类和接口类，想要实例化只能通过代理来实现
        IUserService userService = RpcProxy.getInstance(IUserService.class,"userService",serviceDiscovery);
        User user = new User();
        user.setAge(12);
        user.setName("chenpp");
        userService.saveUser(user);
        User user1 = userService.getUserById(1);
        System.out.println("执行getUserById获得返回值:"+user.getName()+","+user1.getAge());

    }
}
