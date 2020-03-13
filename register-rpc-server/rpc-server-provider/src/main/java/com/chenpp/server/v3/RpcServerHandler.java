package com.chenpp.server.v3;

import com.chenpp.config.ZKConfig;
import com.chenpp.request.RpcRequest;
import io.netty.channel.*;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Description
 * @Author: chenpp
 * @Date: 2020/3/10 20:21
 */
public class RpcServerHandler extends ChannelInboundHandlerAdapter {

    private Map<String,Object> serviceMap;

    public RpcServerHandler(Map<String,Object> serviceMap){
        this.serviceMap = serviceMap;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest rpcRequest = (RpcRequest)msg;
        String className = rpcRequest.getClassName();
        Object result = null;
        ChannelFuture future = null;
        try {
            Class<?> clazz = Class.forName(className);
            //这里无法实例化，因为传入的是接口类型，接口无法实例化,所以需要从注册的serviceMap获取到
            Object[] parameters = rpcRequest.getArgs();
            Object serviceInstance = serviceMap.get(clazz.getName());
            if (parameters == null) {
                Method method = clazz.getMethod(rpcRequest.getMethodName());
                result = method.invoke(serviceInstance);
            } else {
                Class[] types = new Class[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    types[i] = parameters[i].getClass();
                }
                Method method = clazz.getMethod(rpcRequest.getMethodName(), types);
                result = method.invoke(serviceInstance, parameters);
            }
            if (result == null) {
                // 如果方法结果为空，将一个默认的OK结果给客户端
                future = ctx.writeAndFlush(ZKConfig.DEFAULT_MSG);
            } else {
                // 将返回值写给客户端写给客户端结果
                future = ctx.writeAndFlush(result);
            }
            // 释放通道，不是关闭连接
            future.addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if( future != null) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
