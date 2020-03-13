package com.chenpp.api;

import com.chenpp.domain.User;

/**
 * 2020/3/7
 * created by chenpp
 * 定义接口，将rpc-server-api打包提供给client端使用，
 * client只要调用对应的接口方法就可以远程调用服务端的
 * 具体实现
 */
public interface IUserService {

    public void saveUser(User user);

    public User getUserById(Integer id);
}
