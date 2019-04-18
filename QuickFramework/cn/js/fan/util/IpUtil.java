package cn.js.fan.util;

import java.net.*;

/**
 * @Description: ip操作类
 * @author: 古月圣
 * @Date: 2015-8-5上午11:05:34
 */
public class IpUtil {
	private static final long a1 = getIpNum("10.0.0.0");
	private static final long a2 = getIpNum("10.255.255.255");
	private static final long b1 = getIpNum("172.16.0.0");
	private static final long b2 = getIpNum("172.31.255.255");
	private static final long c1 = getIpNum("192.168.0.0");
	private static final long c2 = getIpNum("192.168.255.255");
	private static final long d1 = getIpNum("10.44.0.0");
	private static final long d2 = getIpNum("10.69.0.255");

	/**
	 * @Description: 判断是否为内网IP
	 * @param ip
	 * @return
	 */
	public static boolean isInnerIP(String ip) {
		if (ip.toLowerCase().equals("localhost") || ip.equals("127.0.0.1")) {
			return true;
		}
		long n = getIpNum(ip);
		if (n == -1L) {
			return true;
		}
		return (n >= a1 && n <= a2) || (n >= b1 && n <= b2)
				|| (n >= c1 && n <= c2) || (n >= d1 && n <= d2);
	}

	public static boolean isDomain(String ip) {
		if (ip.toLowerCase().equals("localhost") || ip.equals("127.0.0.1")) {
			return false;
		}
		try {
			if (!ip.startsWith("http://")) {
				ip = "http://" + ip;
			}
			URL url = new URL(ip);
			String host = url.getHost();
			InetAddress address = null;
			address = InetAddress.getByName(host);
			if (!host.equalsIgnoreCase(address.getHostAddress())) {
				return true;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @Description: ip转long
	 * @param ipAddress
	 * @return
	 * @throws ErrMsgException
	 */
	private static long getIpNum(String ipAddress) {
		long a = 0;
		long b = 0;
		long c = 0;
		long d = 0;
		try {
			String[] ip = ipAddress.split("\\.");
			a = Integer.parseInt(ip[0]);
			b = Integer.parseInt(ip[1]);
			c = Integer.parseInt(ip[2]);
			d = Integer.parseInt(ip[3]);
		} catch (Exception e) {
			return -1L;
		}
		return a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
	}
}
