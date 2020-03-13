package com.chenpp.config;

/**
 * @Description
 * @Author: chenpp
 * @Date: 2020/3/10 19:01
 */
public interface ZKConfig {

     String ZK_CONNECTION = "127.0.0.1:2181";

     int SERVER_PORT = 8080;

     String REGISTER_NAMESPACE = "/cp-rpc";

     int SESSION_TIMEOUT = 5000;

     String DEFAULT_MSG = "OK";

}
