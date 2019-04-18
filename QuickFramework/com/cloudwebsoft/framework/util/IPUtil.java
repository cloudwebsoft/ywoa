package com.cloudwebsoft.framework.util;

import java.net.UnknownHostException;
import java.net.InetAddress;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: IP地址处理工具</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class IPUtil {

    public static int str2Ip(String ip) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ip); // 在给定主机名的情况下确定主机的 IP 址。
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
            return 0;
        }
        byte[] bytes = address.getAddress(); // 返回此 InetAddress 对象的原始 IP 地址
        int a, b, c, d;
        a = byte2int(bytes[0]);
        b = byte2int(bytes[1]);
        c = byte2int(bytes[2]);
        d = byte2int(bytes[3]);
        int result = (a << 24) | (b << 16) | (c << 8) | d;
        return result;
    }

    public static int byte2int(byte b) {
        int l = b & 0x07f;
        if (b < 0) {
            l |= 0x80;
        }
        return l;
    }

    public static long ip2long(String ip) {
        int ipNum = str2Ip(ip);
        return int2long(ipNum);
    }

    public static long int2long(int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }

    public static String long2ip(long ip) {
        int[] b = new int[4];
        b[0] = (int) ((ip >> 24) & 0xff);
        b[1] = (int) ((ip >> 16) & 0xff);
        b[2] = (int) ((ip >> 8) & 0xff);
        b[3] = (int) (ip & 0xff);
        String x;
        Integer p;
        p = new Integer(0);
        x = p.toString(b[0]) + "." + p.toString(b[1]) + "." + p.toString(b[2]) +
            "." + p.toString(b[3]);

        return x;
    }

    public static String getRemoteAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip!=null && ip.length()>0 && !ip.equalsIgnoreCase("unknown")) {
            String[] ary = ip.split(",");
            return ary[0];
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /*
    public static void main(String[] args) throws Exception {
        long longip = ip2long("221.0.0.0");
        System.out.println(longip);
        System.out.println(long2ip(longip));
    }
    */

}

