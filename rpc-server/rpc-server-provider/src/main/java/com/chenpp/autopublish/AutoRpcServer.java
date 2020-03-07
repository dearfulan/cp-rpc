package com.chenpp.autopublish;

import com.chenpp.annotation.RegisterService;
import com.chenpp.server.HandleThread;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import sun.swing.StringUIClientPropertyKey;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 实现了InitializingBean接口，那么会在对应的AutoRpcServer实例化之后调用afterPropertiesSet方法
 * 而实现了ApplicationContextAware接口，当spring容器初始化的时候，会自动的将ApplicationContext注入进来，
 * 使得当前bean可以获得ApplicationContext上下文
 * */
@Component
public class AutoRpcServer implements ApplicationContextAware, InitializingBean {

    private int port;

    public AutoRpcServer(int port){
        this.port = port;
    }

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    //key 为对应的接口类名，valeu 为具体的实例
    private Map<String,Object> map = new HashMap<String, Object>();

    public void afterPropertiesSet() throws Exception {
        ServerSocket serverSocket = null;
        try {
            //创建socket
            serverSocket = new ServerSocket(port);
            while(true){
                //监听端口，是个阻塞的方法
                Socket socket = serverSocket.accept();
                //处理rpc请求，这里使用线程池来处理
                executor.submit(new HandleThread(map,socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        //从spring上下文中获取添加了RegisterService的注解的bean
        String[] beanNames = context.getBeanNamesForAnnotation(RegisterService.class);
        for(String beanName : beanNames){
           Object bean =  context.getBean(beanName);
            RegisterService annotation = bean.getClass().getAnnotation(RegisterService.class);
            Class interfaceClass = annotation.interfaceClass();
            //将接口的类名和对应的实例bean对应起来
            map.put(interfaceClass.getName(),bean);
        }
    }
}