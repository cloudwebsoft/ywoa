package com.redmoon.oa.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.kernel.License;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.web.Global;


/**
 * @Description: 
 * @author: 
 * @Date: 2015-8-28上午09:24:24
 */
public class MobileScanDownload {
	
	private final static String GET_IOSAPPID ="app_id/httpclient_app_id.jsp";
	public final static String IOS_NO_APPROVAL = "-1";
	
	/**
	 * 根据手机类型 获取Android Ios下载地址
	 * @Description: 
	 * @param isAndroid
	 * @return
	 */
	public static  String  downloadMobileClientByAngent(boolean isAndroid){
		String full_path =  Global.getFullRootPath()+ "/";
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		StringBuffer sb = new StringBuffer();
		if(isAndroid){
			sb.append(full_path).append(cfg.get("qrcode_andriod_download_path"));
			
		}else{
			String oa_version = cfg.get("version");
			String yimihome_url = cfg.get("yimihome_url")+"/"+GET_IOSAPPID;
			String iosAppId = getIosAppId(oa_version,yimihome_url);
			if(!iosAppId.equals("")){
				if(iosAppId.equals(IOS_NO_APPROVAL) ){
					sb.append(IOS_NO_APPROVAL);
				}else{
					sb.append(cfg.get("qrcode_ios_download_path"));
					sb.append(iosAppId);
				}
			}else{
				sb.append(IOS_NO_APPROVAL);
			}
		}	
		return sb.toString();
	}
	
	/**
	 * 
	 * @Description:根据版本号 请求网站的IosAppId
	 * @param oa_version
	 * @return
	 */
	private static String getIosAppId(String oa_version,String yimihome_url){
		String iosAppId = "-1";
		String enterpriseNum = License.getInstance().getEnterpriseNum(); 
		try {
			License license = License.getInstance();
			boolean isOem = license.isOem();
			List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
			NameValuePair param1 = new BasicNameValuePair("oa_version", oa_version);
			NameValuePair param2 = new BasicNameValuePair("enterpriseNum", enterpriseNum);
			NameValuePair param3 = new BasicNameValuePair("isOem", isOem?"1":"0");
			requestParams.add(param3);
			requestParams.add(param1);
			requestParams.add(param2);
			String result = MethodPost(yimihome_url,requestParams);
			if(!result.equals("")){
				  JSONObject jsonObj = new JSONObject(result);
				    int res = jsonObj.getInt("res");
				    if(res == 0){
				    	JSONObject resultObj = jsonObj.getJSONObject("result");
				    	int returnCode = resultObj.getInt("returnCode");
				    	if(returnCode == 0){
				    		iosAppId = resultObj.getString("iosAppid");
				    	}else{
				    		iosAppId = String.valueOf(returnCode);
				    	}
				    }
			}
		} catch (JSONException e) {
			LogUtil.getLog(MobileScanDownload.class).error(e.getMessage());
		} 
		return iosAppId;
	}
	
	/**
	 * HttpClient post请求
	 * @Description: 
	 * @param url
	 * @param nameValuePairs
	 * @return
	 */
	public static String MethodPost(String url, List<NameValuePair> nameValuePairs ){
		String result = "";
    	try {
    		HttpParams httpParams = new BasicHttpParams();
    		HttpConnectionParams.setConnectionTimeout(httpParams,5000); //设置连接超时为5秒
    	    org.apache.http.client.HttpClient httpCient = new DefaultHttpClient(httpParams);
    	    HttpPost httpPost = new HttpPost(url);
    	    if (nameValuePairs!=null && nameValuePairs.size()!=0) {
                //把键值对进行编码操作并放入HttpEntity对象中
    	    	httpPost.setEntity(new UrlEncodedFormEntity((List<NameValuePair>) nameValuePairs,HTTP.UTF_8));
    	    }
	    	HttpResponse httpResponse = httpCient.execute(httpPost); 
	    	int status = httpResponse.getStatusLine().getStatusCode();
	    	if (status == HttpStatus.SC_OK){
	    		HttpEntity httpEntity = httpResponse.getEntity();
		    	java.io.InputStream is = httpEntity.getContent(); 
		    	result = StreamToString(is);
	    	}
	    } catch (UnsupportedEncodingException e) {
	    	LogUtil.getLog(MobileScanDownload.class).error(e.getMessage());
	    } catch (ClientProtocolException e) {
	    	LogUtil.getLog(MobileScanDownload.class).error(e.getMessage());
		} catch (IOException e) {
			LogUtil.getLog(MobileScanDownload.class).error(e.getMessage());
		}
		return result;
		
	}
	
	
	/**
	 * 读流
	 * @Description: 
	 * @param resStream
	 * @return
	 */
	public static String StreamToString(InputStream resStream) {
		if (resStream != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					resStream));
			StringBuffer resBuffer = new StringBuffer();
			String resTemp = "";
			try {
				while ((resTemp = br.readLine()) != null) {
					resBuffer.append(resTemp);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(MobileScanDownload.class).error(e.getMessage());
			}finally{
				try {
					resStream.close();
					br.close();
				} catch (IOException e) {
					LogUtil.getLog(MobileScanDownload.class).error(e.getMessage());
				}
				
			}
			String response = resBuffer.toString();
			return response;
		}
		return null;
	}
	

}
