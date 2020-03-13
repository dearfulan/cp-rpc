package com.chenpp.request;

import java.io.Serializable;
import java.net.ServerSocket;

/**
 * 2020/3/7
 * created by chenpp
 * 请求rpc时需要的参数
 */
public class RpcRequest implements Serializable {

    private String className;
    private String methodName;
    private Object[] args;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
