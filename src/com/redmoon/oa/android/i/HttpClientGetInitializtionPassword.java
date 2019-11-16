package com.redmoon.oa.android.i;

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
import org.json.JSONObject;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;


/**
 * @Description: ForgetPasswordAction接口向云端服务器请求初始密码
 * @author: lichao
 * @Date: 2015-7-15上午11:06:14
 */
public class HttpClientGetInitializtionPassword {
	public JSONObject getInitializtionPassword(String URL,JSONObject jGet) throws JSONException, IOException, Exception{
		   JSONObject jReturn = new JSONObject();
		   
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
				connection.setRequestProperty("Content-Type","utf-8");
				connection.connect();
				out = new DataOutputStream(connection.getOutputStream());
				
				out.write(jGet.toString().getBytes());
				out.flush();
				out.close();
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
				String lines;
				StringBuilder sb = new StringBuilder("");
				
				while ((lines = reader.readLine()) != null) {
				    lines = new String(lines.getBytes());
				    sb.append(lines);
				}
				
				try {
					jReturn = new JSONObject(sb.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				reader.close();
				// 断开连接
				connection.disconnect();
			} catch (MalformedURLException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				throw new MalformedURLException(e.toString());
			} catch (ProtocolException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				throw new ProtocolException(e.toString());
			} catch (UnsupportedEncodingException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				throw new UnsupportedEncodingException(e.toString());
			} catch (IOException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				throw new IOException(e.toString());
			}finally{
				if (out != null) {
					try {
						out.close();
						out = null;
					} catch (IOException e) {
						LogUtil.getLog(getClass()).error(StrUtil.trace(e));
						e.printStackTrace();
					}
				}
				
				if (reader != null) {
					try {
						reader.close();
						reader = null;
					} catch (IOException e) {
						LogUtil.getLog(getClass()).error(StrUtil.trace(e));
						e.printStackTrace();
					}
				}
				
				if (connection != null) {
					connection.disconnect();
					connection = null;
				}
			}
			
			return jReturn;
	   }
}
