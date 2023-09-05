package cn.js.fan.util;

import com.cloudwebsoft.framework.util.LogUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

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
			LogUtil.getLog(IpUtil.class).error(e);
		} catch (UnknownHostException e) {
			LogUtil.getLog(IpUtil.class).error(e);
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

	/**
	 * 取得IP地址所在的位置
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
			// JSONObject jobj = (JSONObject) ((JSONObject) json.get("content")).get("address_detail");
			try {
				area = (String) ((JSONObject) json.get("content")).get("address");
			} catch (JSONException e) {
				LogUtil.getLog(IpUtil.class).error(e);
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
				}else{
					jobj = new JSONObject();
					jobj.put("ret", 0);
				}
			} catch (JSONException e) {
				LogUtil.getLog(IpUtil.class).error(e);
			}
		} catch (MalformedURLException e) {
			LogUtil.getLog(IpUtil.class).error(e);
		} catch (ProtocolException e) {
			LogUtil.getLog(IpUtil.class).error(e);
		} catch (UnsupportedEncodingException e) {
			LogUtil.getLog(IpUtil.class).error(e);
		} catch (IOException e) {
			LogUtil.getLog(IpUtil.class).error(e);
		}
		finally {
			try {
				reader.close();
			} catch (IOException e) {
				LogUtil.getLog(IpUtil.class).error(e);
			}
			// 断开连接
			connection.disconnect();
		}

		return jobj;
	}
}
