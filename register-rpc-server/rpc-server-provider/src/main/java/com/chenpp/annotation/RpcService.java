package com.chenpp.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 2020/3/7
 * created by chenpp
 * 引入Component注解，加了RpcService注解的类都会被Spring容器管理
 * 我们会把加了RpcService注解的服务都注册到Zookeeper上
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {

    Class interfaceClass() ;

    String serviceName () ;
}
