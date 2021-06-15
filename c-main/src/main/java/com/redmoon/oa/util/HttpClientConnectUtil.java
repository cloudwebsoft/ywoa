package com.redmoon.oa.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import org.json.JSONException;
import com.cloudwebsoft.framework.util.LogUtil;


/**
 * @Description: 获取HttpURLConnection连接
 * @author: lichao
 * @Date: 2015-8-28上午11:43:52
 */
public class HttpClientConnectUtil {

	// 获取HttpURLConnection连接
	public static StringBuilder getHttpConnect(String URL, String str) throws JSONException, IOException {
		HttpURLConnection connection = null;
		DataOutputStream out = null;
		BufferedReader reader = null;

		try {
			URL url = new URL(URL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type", "utf-8");
			
			// 20170416 fgf 加入超时设置，否则如果yimihome.com崩溃，可能会导致程序卡住无法启动
			connection.setConnectTimeout(8000); // 8秒
			connection.setReadTimeout(8000);
			
			connection.connect();
			out = new DataOutputStream(connection.getOutputStream());

			out.write(str.getBytes());
			out.flush();
			out.close();

			reader = new BufferedReader(new InputStreamReader(connection
					.getInputStream(), "utf-8"));
			String lines;
			StringBuilder sb = new StringBuilder("");

			while ((lines = reader.readLine()) != null) {
				lines = new String(lines.getBytes());
				sb.append(lines);
			}

			return sb;
		} catch (MalformedURLException e) {
			LogUtil.getLog("getHttpConnect").error(e);
			throw new MalformedURLException(e.toString());
		} catch (ProtocolException e) {
			LogUtil.getLog("getHttpConnect").error(e);
			throw new ProtocolException(e.toString());
		} catch (UnsupportedEncodingException e) {
			LogUtil.getLog("getHttpConnect").error(e);
			throw new UnsupportedEncodingException(e.toString());
		} catch (IOException e) {
			LogUtil.getLog("getHttpConnect").error(e);
			throw new IOException(e.toString());
		} finally {
			if (out != null) {
				out.close();
			}

			if (reader != null) {
				reader.close();
			}

			connection.disconnect();
		}
	}

}
