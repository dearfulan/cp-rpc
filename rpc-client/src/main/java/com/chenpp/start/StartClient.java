package com.chenpp.start;

import com.chenpp.api.IUserService;
import com.chenpp.domain.User;
import com.chenpp.proxy.RpcProxy;


/**
 * 2020/3/7
 * created by chenpp
 */
public class StartClient {

    public static void main(String[] args) {

        //由于rpc-server-api里只有实体类和接口类，想要实例化只能通过代理来实现
        IUserService userService = RpcProxy.getInstance(IUserService.class,"localhost",8080);
        User user = new User();
        user.setAge(12);
        user.setName("chenpp");
        userService.saveUser(user);
        User user1 = userService.getUserById(1);
        System.out.println(user1.getName()+","+user1.getAge());

    }
}
