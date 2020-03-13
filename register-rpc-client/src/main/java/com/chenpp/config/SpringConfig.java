package com.chenpp.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 2020/3/7
 * created by chenpp
 * 扫描com.chenpp.discovery和com.chenpp.proxy包下的类，注入spring容器
 */
@Configuration
@ComponentScan({"com.chenpp"})
public class SpringConfig {

}
