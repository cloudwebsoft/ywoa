package com.cloudweb.oa.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.sys.DebugUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;

/**
 * 微信公众号
 */
@Controller
// @PropertySource("classpath:application.properties")
@RequestMapping("/public/wechat")
public class WeChatController {
    // 获取相关的参数,在application.properties文件中
    @Value("${wechat.appId}")
    private String appId;
    @Value("${wechat.appSecret}")
    private String appSecret;
    @Value("${wechat.url.accessToken}")
    private String accessTokenUrl;
    @Value("${wechat.url.apiTicket}")
    private String apiTicketUrl;
    
	@Autowired  
	private HttpServletRequest request;    
    
    //微信参数
    private String accessToken;
    private String jsApiTicket;
    //获取参数的时刻
    private Long getTiketTime = 0L;
    private Long getTokenTime = 0L;
    //参数的有效时间,单位是秒(s)
    private Long tokenExpireTime = 0L;
    private Long ticketExpireTime = 0L;

    //获取微信参数
    @RequestMapping("/wechatParam")
    @ResponseBody
    public Map<String, String> getWechatParam(String url){
        //当前时间
        long now = System.currentTimeMillis();
        String errMsg = "";

        //判断accessToken是否已经存在或者token是否过期
        if(StringUtils.isBlank(accessToken)||(now - getTokenTime > tokenExpireTime*1000)){
            JSONObject tokenInfo = getAccessToken();
            if (tokenInfo != null) {
                try {
                    if (tokenInfo.has("errcode")) {
                        errMsg = tokenInfo.getString("errmsg");
                        DebugUtil.log(getClass(), "getWechatParam", tokenInfo.toString());
                        Map<String, String> wechatParam = new HashMap<String, String>();
                        wechatParam.put("errMsg", errMsg);
                        return wechatParam;
                    } else {
                        accessToken = tokenInfo.getString("access_token");
                        tokenExpireTime = tokenInfo.getLong("expires_in");
                        //获取token的时间
                        getTokenTime = System.currentTimeMillis();
                    }
                } catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }

        //判断jsApiTicket是否已经存在或者是否过期
        if(StringUtils.isBlank(jsApiTicket)||(now - getTiketTime > ticketExpireTime*1000)){
            JSONObject ticketInfo = getJsApiTicket();
            if(ticketInfo!=null){
                try {
                    jsApiTicket = ticketInfo.getString("ticket");
                    ticketExpireTime = ticketInfo.getLong("expires_in");
                }catch (JSONException e){
                    LogUtil.getLog(getClass()).error(e);
                }

                getTiketTime = System.currentTimeMillis();
            }
        }

        //生成微信权限验证的参数
        Map<String, String> wechatParam = makeWXTicket(jsApiTicket,url);
        return wechatParam;
    }

    //获取accessToken
    private JSONObject getAccessToken() {
        //String accessTokenUrl = https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
        String requestUrl = accessTokenUrl.replace("APPID",appId).replace("APPSECRET",appSecret);
        JSONObject result = null;
        try {
        	result = new JSONObject(HttpUtil.doGet(requestUrl));
        }
        catch (Exception e) {
        	LogUtil.getLog(getClass()).error(e);
        }
        return result ;
    }

    //获取ticket
    private JSONObject getJsApiTicket(){
        //String apiTicketUrl = https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi
        String requestUrl = apiTicketUrl.replace("ACCESS_TOKEN", accessToken);
        JSONObject result = null;
        result = new JSONObject(HttpUtil.doGet(requestUrl));
        return result;
    }

    //生成微信权限验证的参数
    public Map<String, String> makeWXTicket(String jsApiTicket, String url) {
        Map<String, String> ret = new HashMap<String, String>();
        String nonceStr = createNonceStr();
        String timestamp = createTimestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsApiTicket +
                "&noncestr=" + nonceStr +
                "&timestamp=" + timestamp +
                "&url=" + url;
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(string1.getBytes(StandardCharsets.UTF_8));
			signature = byteToHex(crypt.digest());
		} catch (NoSuchAlgorithmException e) {
			LogUtil.getLog(getClass()).error(e);
		}

        ret.put("url", url);
        ret.put("jsapi_ticket", jsApiTicket);
        ret.put("nonceStr", nonceStr);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);
        ret.put("appid", appId);

        return ret;
    }
    
    //字节数组转换为十六进制字符串
    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
    
    //生成随机字符串
    private static String createNonceStr() {
        return UUID.randomUUID().toString();
    }
    //生成时间戳
    private static String createTimestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

	static class HttpUtil {
		// get请求
		public static com.alibaba.fastjson.JSONObject doGet(String requestUrl) {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			CloseableHttpResponse response = null;
			String responseContent = null;
			com.alibaba.fastjson.JSONObject result = null;
			try {
				// 创建Get请求，
				HttpGet httpGet = new HttpGet(requestUrl);
				// 执行Get请求，
				response = httpClient.execute(httpGet);
				// 得到响应体
				HttpEntity entity = response.getEntity();
				// 获取响应内容
				responseContent = EntityUtils.toString(entity, "UTF-8");
				// 转换为map
				result = JSON.parseObject(responseContent);
			} catch (IOException e) {
				LogUtil.getLog(WeChatController.class).error(e);
			}
			return result;
		}
	}
}
