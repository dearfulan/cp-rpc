package com.chenpp.impl;

import com.chenpp.api.IUserService;
import com.chenpp.domain.User;

/**
 * 2020/3/7
 * created by chenpp
 * 在Pom引入rpc-server-api的依赖，实现接口
 */
public class UserServiceImpl implements IUserService {

    public void saveUser(User user) {
        System.out.println("保存User对象:" + user.getName() + "," + user.getAge());
    }

    public User getUserById(Integer id) {
        User user = new User();
        user.setId(1);
        user.setName("Caroline");
        user.setAge(26);
        return user;
    }
}
