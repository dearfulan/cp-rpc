package com.chenpp.proxy;

import com.chenpp.request.RpcRequest;

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

    private String host;
    private int port;

    public RpcInvocationHandler(String host,int port){
        this.host = host;
        this.port = port;
    }

    /**
     * 增强的InvocationHandler,接口调用方法的时候实际是调用socket进行传输
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setArgs(args);
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        //通过socket发送RPCRequest给服务端并获取结果返回
        Socket socket= new Socket(host,port);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(rpcRequest);
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Object result = ois.readObject();
        return result;
    }
}
