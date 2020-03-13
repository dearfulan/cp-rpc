package com.chenpp.proxy;

import com.chenpp.client.RpcClientHandler;
import com.chenpp.config.ZKConfig;
import com.chenpp.discovery.IServiceDiscovery;
import com.chenpp.request.RpcRequest;
import com.chenpp.util.RpcDecoder;
import com.chenpp.util.RpcEncoder;
import com.sun.corba.se.internal.CosNaming.BootstrapServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 2020/3/7
 * created by chenpp
 */


public class RpcInvocationHandler implements InvocationHandler {

    private String serviceName;

    private IServiceDiscovery serviceDiscovery;

    public RpcInvocationHandler(String serviceName, IServiceDiscovery serviceDiscovery) {
        this.serviceName = serviceName;
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 增强的InvocationHandler,接口调用方法的时候实际是调用socket进行传输
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //将远程调用需要的接口类、方法名、参数信息封装成RPCRequest
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setArgs(args);
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        return handleNetty(rpcRequest);
        //return handleSocket(rpcRequest);
    }


    private Object handleNetty(RpcRequest rpcRequest){
        //创建客户端线程池
        EventLoopGroup group = null;
        final RpcClientHandler handler = new RpcClientHandler();
        try{
            group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class);
            //添加客户端的处理器
            bootstrap.option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    /** 入参有5个，如下
                                     maxFrameLength：框架的最大长度。如果帧的长度大于此值，则将抛出TooLongFrameException。
                                     lengthFieldOffset：长度字段的偏移量：即对应的长度字段在整个消息数据中的位置
                                     lengthFieldLength：长度字段的长度：如：长度字段是int型表示，那么这个值就是4（long型就是8）
                                     lengthAdjustment：要添加到长度字段值的补偿值
                                     initialBytesToStrip：从解码帧中去除的第一个字节数
                                     */
                                    //自定义协议解码器
                                    .addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                    //自定义协议编码器
                                    .addLast("frameEncoder", new LengthFieldPrepender(4))
                                    //对象参数类型编码器
                                    .addLast("encoder", new ObjectEncoder())
                                    // 对象参数类型解码器
                                    .addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)))
                                    .addLast(handler);
                        }
                    });
            //通过service从zk获取服务端地址
            String address = serviceDiscovery.discover(serviceName);
            //绑定端口启动netty客户端
            String[] add = address.split(":");
            ChannelFuture future = bootstrap.connect(add[0], Integer.parseInt(add[1])).sync();
            //通过Netty发送  RPCRequest给服务端
            future.channel().writeAndFlush(rpcRequest).sync();
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
        //返回客户端获取的服务端输出
        return handler.getResponse();
    }

    private Object handleSocket(RpcRequest rpcRequest) throws IOException, ClassNotFoundException {
        String address = serviceDiscovery.discover(serviceName);
        //绑定端口启动netty客户端
        String[] add = address.split(":");
        //通过socket发送RPCRequest给服务端并获取结果返回
        Socket socket= new Socket(add[0],Integer.parseInt(add[1]));
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(rpcRequest);
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Object result = ois.readObject();
        return result;
    }
}
