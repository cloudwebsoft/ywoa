package com.cloudwebsoft.framework.util;

import java.net.*;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;

import org.apache.http.HttpStatus;

import java.io.InputStreamReader;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Config;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.codec.binary.Base64;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class NetUtil {
    public NetUtil() {
        super();
    }

    /**
     * get
     * @param request
     * @param charset
     * @param linkUrl
     * @return
     */
    public static String gather(HttpServletRequest request, String charset, String linkUrl) {
        StringBuffer str = new StringBuffer(100);
        HttpURLConnection huc = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(linkUrl);
            
    		Config config = new Config();

            String strIsProxy = config.getProperty("Application.isProxy");     
            boolean isProxy = strIsProxy!=null && strIsProxy.equals("true");
            
            if (!isProxy){ 
            	huc = (HttpURLConnection) url.openConnection();
            }
            else {
            	String ip = config.getProperty("Application.proxy.ip");
            	int port = StrUtil.toInt(config.getProperty("Application.proxy.port"), -1);
            	String name = config.getProperty("Application.proxy.name");
            	String pwd = config.getProperty("Application.proxy.pwd");
                InetSocketAddress addr = new InetSocketAddress(ip, port);  
                Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
                
            	huc = (HttpURLConnection) url.openConnection(proxy);
            	
            	if (name!=null && !"".equals(name)){ 
	                String headerkey = "Proxy-Authorization";  
	                Base64 base64 = new Base64();
	                String headerValue = "Basic " + base64.encodeToString((name + ":" + pwd).getBytes());
	                huc.setRequestProperty(headerkey, headerValue);   
            	}
            }
            
            huc.setConnectTimeout(10000);
            huc.setReadTimeout(10000);

            if (request!=null) {
	            String jsessionid = "";
	            Cookie c[] = request.getCookies();
	            if (c != null) {
	                for (int i = 0; i < c.length; i++) {
	                    if (c[i].getName().equalsIgnoreCase("jsessionid")) {
	                        jsessionid = c[i].getValue();
	                        huc.setRequestProperty("Cookie", "JSESSIONID="+jsessionid);
	                        break;
	                    }
	                }
	            }         
            }

            String type = huc.getContentType();
            if (type != null) {
                // charset = "utf-8";
                int index = type.indexOf("charset=");
                if (index != -1) {
                    index = index + 8;
                    charset = type.substring(index).trim();
                }
            }
            
            int status = huc.getResponseCode();
            InputStream in;
            if(status >= HttpStatus.SC_BAD_REQUEST) // 400
                in = huc.getErrorStream();
            else
                in = huc.getInputStream();               
            reader = new BufferedReader(
                    new InputStreamReader(in, charset));

            /*
                         String line = reader.readLine();
                         while (line != null) {
                str.append(line);
                line = reader.readLine();
                         }
             */

            char[] buffer = new char[10240];
            int count = 0;
            while ((count = reader.read(buffer)) > 0) {
                str.append(buffer, 0, count);
            }
        } catch (Exception e) {
            LogUtil.getLog("NetUtil").error("gather: " + linkUrl + " " + StrUtil.trace(e));
        } finally {
            try {
                reader.close();
            } catch (Exception e) {}
            huc.disconnect();
        }

        return str.toString();
    }
    
    /**
     * post
     * @param path
     * @param map
     * @return
     */
    public static String post(String path, Map<String, String[]> map){
        OutputStreamWriter out = null;
        BufferedReader in = null;        
        StringBuilder result = new StringBuilder(); 
        try {
            URL url = new URL(path);
            
    		Config config = new Config();

            String strIsProxy = config.getProperty("Application.isProxy");     
            boolean isProxy = strIsProxy!=null && strIsProxy.equals("true");
            
            HttpURLConnection conn = null; // (HttpURLConnection)url.openConnection();
            
            if (!isProxy){ 
            	conn = (HttpURLConnection) url.openConnection();
            }
            else {
            	String ip = config.getProperty("Application.proxy.ip");
            	int port = StrUtil.toInt(config.getProperty("Application.proxy.port"), -1);
            	String name = config.getProperty("Application.proxy.name");
            	String pwd = config.getProperty("Application.proxy.pwd");
                InetSocketAddress addr = new InetSocketAddress(ip, port);  
                Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
                
                conn = (HttpURLConnection) url.openConnection(proxy);
            	
            	if (name!=null && !"".equals(name)){ 
	                String headerkey = "Proxy-Authorization";  
	                Base64 base64 = new Base64();
	                String headerValue = "Basic " + base64.encodeToString((name + ":" + pwd).getBytes()); //�ʺ�������:������base64���ܷ�ʽ  
	                conn.setRequestProperty(headerkey, headerValue);   
            	}
            }            
            
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setUseCaches(false);
            
            conn.setRequestProperty("Accept-Charset", "UTF-8");     
            conn.setRequestProperty("contentType", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            if (map!=null) {
                for (String key : map.keySet()) {
                    String[] values = (String[]) map.get(key);
                    for (String val : values) {
                        out.write(key + "=" + StrUtil.UrlEncode(val) + "&");
                    }
                }
            }
            out.flush();  
            out.close();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            LogUtil.getLog(NetUtil.class).error(e);
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                LogUtil.getLog(NetUtil.class).error(ex);
            }
        }
        return result.toString();
    }    
	   
	 public static final long a1 = getIpNum("10.0.0.0");
	 public static final long a2 = getIpNum("10.255.255.255");
	 public static final long b1 = getIpNum("172.16.0.0");
	 public static final long b2 = getIpNum("172.31.255.255");
	 public static final long c1 = getIpNum("192.168.0.0");
	 public static final long c2 = getIpNum("192.168.255.255");
	 public static final long d1 = getIpNum("10.44.0.0");
	 public static final long d2 = getIpNum("10.69.0.255");
	 
	 /**
	  * isInnerIP
	  * @param ip
	  * @return
	  */
	 public static boolean isInnerIP(String ip){  
		 	boolean result = false;
		 	if (!ip.equalsIgnoreCase("localhost") && !ip.equalsIgnoreCase("127.0.0.1")){
		 		long n = getIpNum(ip);
		        result = (n >= a1 && n <= a2) || (n >= b1 && n <= b2) || (n >= c1 && n <= c2) || (n >= d1 && n <= d2);
		 	}else{
		 		result = true;
		 	}
	        
	        return result;
	 }  
	 
	 private static long getIpNum(String ipAddress) {  
		 long result = 0;
		 if (ipAddress.contains(".")){
		     String [] ip = ipAddress.split("\\.");  
		     if (ip.length == 4){
			     long a = Integer.parseInt(ip[0]);   
			     long b = Integer.parseInt(ip[1]);   
			     long c = Integer.parseInt(ip[2]);   
			     long d = Integer.parseInt(ip[3]);
			     result = a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;  
		     }
		 }
	     return  result;
	 }

    /**
     * 获取本地ip地址的方法(Linux环境和Java通用)
     *
     * @version:1.0
     * @description:
     * @author: lxz
     * @email:
     * @date:
     */
    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败" + e.toString());
        }
        return "";
    }
}

