package com.chenpp.util;

import java.net.InetAddress;

/**
 * @Description
 * @Author: chenpp
 * @Date: 2020/3/10 19:55
 */
public class IpUtils {


    /**
     * 获取本机ip
     * */
    public static String getLocalHost(){
        try{
            return InetAddress.getLocalHost().getHostAddress();
        }catch (Exception e){
            System.out.println("获取本机ip失败");
            return "127.0.0.1";
        }
    }
}
