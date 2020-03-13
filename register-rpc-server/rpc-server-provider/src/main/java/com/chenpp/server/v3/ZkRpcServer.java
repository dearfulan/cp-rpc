package com.chenpp.server.v3;

import com.chenpp.annotation.RpcService;
import com.chenpp.config.ZKConfig;
import com.chenpp.register.IServiceRegister;
import com.chenpp.server.v2.SpringHandleThread;
import com.chenpp.util.IpUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
 */
@Component
public class ZkRpcServer implements ApplicationContextAware, InitializingBean {


    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    private IServiceRegister registerCenter;

    //key 为对应的接口类名，valeu 为具体的实例
    private Map<String, Object> beanMappings = new HashMap<String, Object>();

    //当rpc server端初始化完成后,就会开启监听 这里也可以改成Socket调用
    public void afterPropertiesSet() throws Exception {
        nettyRpc();
       //socketRpc();
    }

    private void nettyRpc() throws InterruptedException {
        //定义主线程池
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //定义工作线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //类似于ServerSocket
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(workerGroup, bossGroup)
                .channel(NioServerSocketChannel.class)
                //定义工作线程的处理函数
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //添加编码/解码器 用于转化对应的传输数据  从字节流到目标对象称之为解码 反之则为编码
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //自定义协议解码器
                        /**
                         *  入参有5个，分别解释如下
                         *  maxFrameLength：框架的最大长度。如果帧的长度大于此值，则将抛出TooLongFrameException。
                         *  lengthFieldOffset：长度字段的偏移量：即对应的长度字段在整个消息数据中得位置
                         *  lengthFieldLength：长度字段的长度。如：长度字段是int型表示，那么这个值就是4（long型就是8）
                         *  lengthAdjustment：要添加到长度字段值的补偿值
                         *  initialBytesToStrip：从解码帧中去除的第一个字节数
                         */
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                //自定义协议编码器
                                .addLast(new LengthFieldPrepender(4))
                                //对象参数类型编码器
                                .addLast(new ObjectEncoder())
                                //对象参数类型解码器
                                .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)))
                                .addLast(new RpcServerHandler(beanMappings));
                    }
                })
                //boss线程池的最大线程数
                .option(ChannelOption.SO_BACKLOG, 128)
                //工作线程保持长连接
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        //绑定端口启动netty服务端
        ChannelFuture future = serverBootstrap.bind(ZKConfig.SERVER_PORT).sync();
        System.out.println("netty服务端启动,端口为:" + ZKConfig.SERVER_PORT + "....");
        future.channel().closeFuture().sync();
    }

    private void socketRpc(){
        ServerSocket serverSocket = null;
        try {
            //创建socket
            serverSocket = new ServerSocket(ZKConfig.SERVER_PORT);
            while(true){
                //监听端口，是个阻塞的方法
                Socket socket = serverSocket.accept();
                //处理rpc请求，这里使用线程池来处理
                executor.submit(new SpringHandleThread(beanMappings,socket));
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
        String[] beanNames = context.getBeanNamesForAnnotation(RpcService.class);
        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);
            RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
            Class interfaceClass = annotation.interfaceClass();
            String serviceName = annotation.serviceName();
            //将接口的类名和对应的实例bean的映射关系保存起来
            beanMappings.put(interfaceClass.getName(), bean);
            //注册实例到zk
            registerCenter.register(serviceName, IpUtils.getLocalHost(), ZKConfig.SERVER_PORT);
        }
    }
}