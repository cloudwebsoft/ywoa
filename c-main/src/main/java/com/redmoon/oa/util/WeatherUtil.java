package com.redmoon.oa.util;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import com.cloudwebsoft.framework.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

/**
 * 
 * 
 * @author Administrator
 * 
 */
public class WeatherUtil {
	
	public static String getJsonStringFromGZIP(InputStream is) {
		String jsonString = null;
		try {
			// InputStream is = response.getEntity().getContent();
			BufferedInputStream bis = new BufferedInputStream(is);
			bis.mark(2);
			// 取前两个字节
			byte[] header = new byte[2];
			int result = bis.read(header);
			// reset输入流到开始位置
			bis.reset();
			// 判断是否是GZIP格式
			int headerData = getShort(header);
			if (result != -1 && headerData == 0x1f8b) {
				is = new GZIPInputStream(bis);
			} else {
				is = bis;
			}
			InputStreamReader reader = new InputStreamReader(is, "utf-8");
			char[] data = new char[100];
			int readSize;
			StringBuffer sb = new StringBuffer();
			while ((readSize = reader.read(data)) > 0) {
				sb.append(data, 0, readSize);
			}
			jsonString = sb.toString();
			bis.close();
			reader.close();
		} catch (Exception e) {
			LogUtil.getLog(WeatherUtil.class).error(e);
		}
		return jsonString;
	}

	public static int getShort(byte[] data) {
		return (int) ((data[0] << 8) | data[1] & 0xFF);
	}
	
	public static void main(String[] args) {
		JSONObject json = get("靖江市");
		StringBuffer sb = new StringBuffer();
		if (json!=null) {
			try { 
				JSONObject data = json.getJSONObject("data");
				JSONArray ary = data.getJSONArray("forecast");
				for (int i=0; i<ary.length(); i++) {
					JSONObject jo = ary.getJSONObject(i);
					String date = jo.getString("date");
					String high = jo.getString("high");
					String low = jo.getString("low");
					String type = jo.getString("type");
					String fx = jo.getString("fengxiang");
					String fl = jo.getString("fengli");
					fl = fl.replace("<![CDATA[", "");
					fl = fl.replace("]]>", "");
					sb.append(date + "， " + high + " " + low + "，" + type + "，" + fx + " " + fl + "\n");	
				}
			}
			catch (JSONException e) {
				LogUtil.getLog(WeatherUtil.class).error(e);
			}
		}		
	}
	
	public static JSONObject get(String city) {
		URL url;
		try {
			url = new URL("http://wthrcdn.etouch.cn/weather_mini?city=" + StrUtil.UrlEncode(city));
			URLConnection connectionData = url.openConnection();
			connectionData.setConnectTimeout(1000);
			String str = getJsonStringFromGZIP(connectionData.getInputStream());
			return new JSONObject(str);
		} catch (MalformedURLException e) {
			LogUtil.getLog(WeatherUtil.class).error(e);
		} catch (IOException e) {
			LogUtil.getLog(WeatherUtil.class).error(e);
		} catch (JSONException e) {
			LogUtil.getLog(WeatherUtil.class).error(e);
		}
		return null;
	}
	
	/*
{
	"data": {
		"yesterday": {
			"date": "20日星期五",
			"high": "高温 29℃",
			"fx": "东南风",
			"low": "低温 18℃",
			"fl": "<![CDATA[3-4级]]>",
			"type": "多云"
		},
		"city": "镇江",
		"aqi": "78",
		"forecast": [{
			"date": "21日星期六",
			"high": "高温 26℃",
			"fengli": "<![CDATA[4-5级]]>",
			"low": "低温 19℃",
			"fengxiang": "东南风",
			"type": "雷阵雨"
		}, {
			"date": "22日星期天",
			"high": "高温 27℃",
			"fengli": "<![CDATA[4-5级]]>",
			"low": "低温 18℃",
			"fengxiang": "东南风",
			"type": "雷阵雨"
		}, {
			"date": "23日星期一",
			"high": "高温 18℃",
			"fengli": "<![CDATA[4-5级]]>",
			"low": "低温 13℃",
			"fengxiang": "北风",
			"type": "中雨"
		}, {
			"date": "24日星期二",
			"high": "高温 21℃",
			"fengli": "<![CDATA[3-4级]]>",
			"low": "低温 11℃",
			"fengxiang": "东北风",
			"type": "晴"
		}, {
			"date": "25日星期三",
			"high": "高温 21℃",
			"fengli": "<![CDATA[3-4级]]>",
			"low": "低温 13℃",
			"fengxiang": "东风",
			"type": "多云"
		}],
		"ganmao": "风较大，阴冷潮湿，较易发生感冒，体质较弱的朋友请注意适当防护。",
		"wendu": "24"
	},
	"status": 1000,
	"desc": "OK"
}


	 */

}