package com.cloudwebsoft.framework.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
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
        } catch (UnknownHostException e) {
            LogUtil.getLog(IPUtil.class).error(e);
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
        // 通过了 HTTP 代理或者负载均衡服务器时才会添加该项。格式为X-Forwarded-For: client1, proxy1, proxy2，一般情况下，第一个ip为客户端真实ip，后面的为经过的代理服务器ip
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip)) {
            String[] ary = ip.split(",");
            ip = ary[0];
        } else {
            // nginx
            ip = request.getHeader("X-Real-IP");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                // apache
                ip = request.getHeader("Proxy-Client-IP");
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    // weblogic插件加上的头
                    ip = request.getHeader("WL-Proxy-Client-IP");
                    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                        ip = request.getRemoteAddr();
                    }
                }
            }
        }
        return ip;
    }

    /**
     * 取得IP地址所在的位置
     *
     * @param ip
     * @return
     */
    public static String getLocation(String ip) {
		/*
			JSONObject json = CustomerMgr.IpHttpClient("http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=js&ip=" + ip);

			if (json.getInt("ret") == 1) {
				if(json.getString("province").equals(json.getString("city"))){
					area = json.getString("province")+ "市";
				}else{
				   area = json.getString("province")+ "省" +json.getString("city");
				}
			}else{
				area = "无法查找省市";
			}
			*/

        // 2017-7-6 fgf 新浪自18-06-23已停用，换淘宝
        // {"code":0,"data":{"ip":"114.222.121.22","country":"中国","area":"","region":"江苏","city":"南京","county":"XX","isp":"电信","country_id":"CN","area_id":"","region_id":"320000","city_id":"320100","county_id":"xx","isp_id":"100017"}}
			/*String url = "http://ip.taobao.com/service/getIpInfo2.php?ip=" + ip;
			JSONObject json = CustomerMgr.IpHttpClient(url);
			if (json.has("data")) {
				JSONObject data = (JSONObject)json.get("data");
				area = data.getString("region") + "省" + data.getString("city") + "市";
			}
			else {
				area = "无法查找省市";
			}*/

        String area = "";
        // 百度定位
        String url = "http://api.map.baidu.com/location/ip?ak=czNBWnMXYBFd7u2vCcUbaOKlNRPOoOsD&ip=" + ip;
        JSONObject json = IpHttpClient(url);
        if (json.has("content")) {
            try {
                area = (String) ((JSONObject) json.get("content")).get("address");
            } catch (JSONException e) {
                LogUtil.getLog(IPUtil.class).error(e);
            }
        } else {
            area = "未找到省市";
        }
        return area;
    }

    public static JSONObject IpHttpClient(String url1) {
        JSONObject jobj = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(url1);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "text/html");
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.flush();
            out.close();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String lines;
            StringBuilder sb = new StringBuilder("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes());
                sb.append(lines);
            }
            try {
                String js = sb.toString().trim();
                int s1 = js.indexOf("{");
                int s2 = js.lastIndexOf('}');
                if (s1 != -1 && s2 != -1) {
                    js = js.substring(s1, s2 + 1);
                    jobj = new JSONObject(js);
                } else {
                    jobj = new JSONObject();
                    jobj.put("ret", 0);
                }
            } catch (JSONException e) {
                LogUtil.getLog(IPUtil.class).error(e);
            }
        } catch (IOException e) {
            LogUtil.getLog(IPUtil.class).error(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                LogUtil.getLog(IPUtil.class).error(e);
            }
            // 断开连接
            connection.disconnect();
        }

        return jobj;
    }

    /*
    public static void main(String[] args) throws Exception {
        long longip = ip2long("221.0.0.0");
    }
    */

}

