package com.redmoon.weixin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.WordUtil;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Config;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.util.MobileScanDownload;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-7-21下午01:53:32
 */
public class HttpUtil {
	public static String MethodGet(String url){
		String result = "";
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 5000); //设置连接超时为5秒
	    org.apache.http.client.HttpClient httpClient = new DefaultHttpClient(httpParams);
	    
		Config cfg = new Config();
        String strIsProxy = cfg.getProperty("Application.isProxy");     
        boolean isProxy = strIsProxy!=null && strIsProxy.equals("true");
        
        if (isProxy) {
	    	String ip = cfg.getProperty("Application.proxy.ip");
	    	int port = StrUtil.toInt(cfg.getProperty("Application.proxy.port"), -1);
	/*    	String name = cfg.getProperty("Application.proxy.name");
	    	String pwd = cfg.getProperty("Application.proxy.pwd");*/
	    	// LogUtil.getLog(HttpUtil.class).info("MethodGet:" + ip + " port:" + port);
	    	HttpHost proxy = new HttpHost(ip, port);
	    	httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
	    
        /* 需要验证 
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("chenlb", "123456"); 
        httpClient.getState().setProxyCredentials(AuthScope.ANY, creds); 
        */  	    
	    
	    HttpGet httpGet = new HttpGet(url);
	    try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int status = httpResponse.getStatusLine().getStatusCode();
	    	if (status == HttpStatus.SC_OK){
	    		HttpEntity httpEntity = httpResponse.getEntity();
		    	java.io.InputStream is = httpEntity.getContent();
		    	result = StreamToString(is);
				DebugUtil.i(HttpUtil.class, "MethodGet", "url=" + url + " result=" + result);
			}
		} catch (IOException e) {
			LogUtil.getLog(HttpUtil.class).error(e);
		}
		return result;


	}
	public static String MethodPost(String url, String body){
		String result = "";
    	try {
    		HttpParams httpParams = new BasicHttpParams();
    		HttpConnectionParams.setConnectionTimeout(httpParams,5000); //设置连接超时为5秒
    	    org.apache.http.client.HttpClient httpClient = new DefaultHttpClient(httpParams);
    	    
    		Config cfg = new Config();
            String strIsProxy = cfg.getProperty("Application.isProxy");     
            boolean isProxy = strIsProxy!=null && strIsProxy.equals("true");
            
            if (isProxy) {
	        	String ip = cfg.getProperty("Application.proxy.ip");
	        	int port = StrUtil.toInt(cfg.getProperty("Application.proxy.port"), -1);
	        	// String name = cfg.getProperty("Application.proxy.name");
	        	// String pwd = cfg.getProperty("Application.proxy.pwd");
	        	// LogUtil.getLog(HttpUtil.class).info("MethodGet:" + ip + " port:" + port);
	
	        	HttpHost proxy = new HttpHost(ip, port);
	        	httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }
            
    	    HttpPost httpPost = new HttpPost(url);
    		httpPost.setEntity(new StringEntity(body, HTTP.UTF_8));
	    	HttpResponse httpResponse = httpClient.execute(httpPost); 
	    	int status = httpResponse.getStatusLine().getStatusCode();
	    	if (status == HttpStatus.SC_OK){
	    		HttpEntity httpEntity = httpResponse.getEntity();
		    	java.io.InputStream is = httpEntity.getContent(); 
		    	result = StreamToString(is);
				DebugUtil.i(HttpUtil.class, "MethodPost", "url=" + url + " result=" + result);
	    	}
	    } catch (IOException e) {
	    	LogUtil.getLog(HttpUtil.class).error(e);
	    }
		return result;
		
	}
	
	
	/**
	 * 读流
	 * @Description: 
	 * @param resStream
	 * @return
	 */
	public static String StreamToString(InputStream resStream)  {
		if (resStream != null) {
			StringBuffer resBuffer = new StringBuffer();
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(
						resStream,"UTF-8"));
				String resTemp = "";
				while ((resTemp = br.readLine()) != null) {
					resBuffer.append(resTemp);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(HttpUtil.class).error(e.getMessage());
			}
			finally{
				try {
					resStream.close();
					if (br!=null) {
						br.close();
					}
				} catch (IOException e) {
					LogUtil.getLog(HttpUtil.class).error(e.getMessage());
				}
				
			}
			String response = resBuffer.toString();
			return response;
		}
		return null;
	}
	

}
