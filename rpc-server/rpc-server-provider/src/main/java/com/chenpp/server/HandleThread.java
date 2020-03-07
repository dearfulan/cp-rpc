package com.chenpp.server;

import com.chenpp.request.RpcRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * 2020/3/7
 * created by chenpp
 * 处理RPC请求的线程
 */
public class HandleThread implements Runnable {

    private Socket socket;

    private Object serviceInstance;

    public HandleThread(Object serviceInstance, Socket socket) {
        this.socket  = socket;
        this.serviceInstance = serviceInstance;
    }

    public void run() {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            //从socket中获取RPC请求
            ois = new ObjectInputStream(socket.getInputStream());
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            Object result = invoke(rpcRequest);
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(result);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    ois.close();
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Object invoke(RpcRequest rpcRequest) {
        String className = rpcRequest.getClassName();
        Object result = null;
        try {
            Class<?> clazz = Class.forName(className);
            //这里无法实例化，因为传入的是接口类型，接口无法实力哈
            Object[] parameters = rpcRequest.getArgs();
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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
