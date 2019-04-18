package com.redmoon.dingding.service.auth;


import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.oapi.lib.aes.DingTalkJsApiSingnature;
import com.redmoon.dingding.Config;
import com.redmoon.dingding.domain.AccessTokenDto;
import com.redmoon.dingding.domain.JsapiTicketDto;
import com.redmoon.dingding.util.HttpHelper;
import com.redmoon.dingding.util.DdException;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.system.OaSysVerMgr;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Date;

public class AuthService {
    //1小时50分钟
    public static final long cacheTime = 1000 * 60 * 55 * 2;

    /**
     * 获得accessToken
     * @return
     */
    public static String getAccessToken(){
        String _accessToken = "";
        try {
            long curTime = System.currentTimeMillis();
            OaSysVerMgr oaSysVerMgr = new OaSysVerMgr();
            oaSysVerMgr = oaSysVerMgr.getOaSysVer();
            String _oldAccToken = null;
            Date _oldAccTokenTime = null;
            if(oaSysVerMgr!=null){
                _oldAccToken = oaSysVerMgr.getDd_accesstoken();
                _oldAccTokenTime = oaSysVerMgr.getDd_accesstoken_time();
            }
            if (_oldAccToken == null ||_oldAccToken.equals("") || _oldAccTokenTime == null || curTime - _oldAccTokenTime.getTime() >= cacheTime){
                Config _config = Config.getInstance();
                String _corpId = _config.getCropId();
                String _corpSecret = _config.getCropSecret();
                HttpHelper _http = new HttpHelper();

                // 2018-12-17 钉钉改为通过appkey及appsecret获取access_token
                String appkey = _config.getProperty("appkey");
                String appsecret = _config.getProperty("appsecret");
                if (!"".equals(appkey)) {
                    DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
                    OapiGettokenRequest request = new OapiGettokenRequest();
                    request.setAppkey(appkey);
                    request.setAppsecret(appsecret);
                    request.setHttpMethod("GET");
                    OapiGettokenResponse response = client.execute(request);
                    if (response.getErrcode()==0) {
                        _accessToken = response.getAccessToken();
                    }
                    else {
                        DebugUtil.log(AuthService.class, "getAccessToken", response.getErrmsg());
                        return "";
                    }
                }
                else {
                    _http.url = String.format("https://oapi.dingtalk.com/gettoken?corpid=%s&corpsecret=%s", _corpId, _corpSecret);
                    AccessTokenDto _accessTokenDto = _http.httpGet(AccessTokenDto.class);
                    if(_accessTokenDto != null){
                        _accessToken = _accessTokenDto.access_token;
                    }
                }

                if(oaSysVerMgr == null){
                    oaSysVerMgr = new OaSysVerMgr();
                }
                oaSysVerMgr.updateDingDingAccToken(_accessToken,new Date());
            }else{
                _accessToken = _oldAccToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _accessToken;
    }

    /**
     * 获得jsApiTicket
     * 企业在使用JSAPI时，需要先获取jsapi_ticket生成签名数据，并将最终签名用的部分字段及签名结果返回到H5中，JS API底层将通过这些数据判断H5是否有权限使用JS API。
     * @return
     */
    public static String getJsapiTicket(){
        String _ticket = "";
        try {
            long curTime = System.currentTimeMillis();
            OaSysVerMgr oaSysVerMgr = new OaSysVerMgr();
            oaSysVerMgr = oaSysVerMgr.getOaSysVer();
            String _oldTicket = null;
            Date _oldTicketTime = null;
            if(oaSysVerMgr!=null){
                _oldTicket = oaSysVerMgr.getDd_jspapi_ticket();
                _oldTicketTime = oaSysVerMgr.getDd_jspapi_ticket_time();
            }
            if (_oldTicket == null ||_oldTicket.equals("") || _oldTicketTime == null || curTime - _oldTicketTime.getTime() >= cacheTime){
                HttpHelper _http = new HttpHelper("https://oapi.dingtalk.com/get_jsapi_ticket?type=jsapi&");
                JsapiTicketDto _jspApiTick = _http.httpGet(JsapiTicketDto.class);
                if(_jspApiTick!=null){
                    _ticket = _jspApiTick.ticket;
                }
                oaSysVerMgr.updateDingDingTicket(_ticket,new Date());
            }else{
                _ticket = _oldTicket;
            }

        } catch (DdException e) {
            e.printStackTrace();
        }
        return  _ticket;
    }

    /**
     * 签名
     * @param ticket
     * @param nonceStr
     * @param timeStamp
     * @param url
     * @return
     * @throws DdException
     */
    public static String sign(String ticket, String nonceStr, long timeStamp, String url) throws DdException {
        try {
            return DingTalkJsApiSingnature.getJsApiSingnature(url, nonceStr, timeStamp, ticket);
        } catch (Exception ex) {
            throw new DdException(0, ex.getMessage());
        }
    }

    /**
     * 计算当前请求的jsapi的签名数据<br/>
     * <p>
     * 如果签名数据是通过ajax异步请求的话，签名计算中的url必须是给用户展示页面的url
     *
     * @param request
     * @return
     */
    public static String getConfig(HttpServletRequest request) {
        String urlString = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        Config _config = Config.getInstance();
        String _corpId = _config.getCropId();
        String queryStringEncode = null;
        String url;
        if (queryString != null) {
            queryStringEncode = URLDecoder.decode(queryString);
            url = urlString + "?" + queryStringEncode;
        } else {
            url = urlString;
        }
        DebugUtil.log(AuthService.class, "getConfig", url);
        String nonceStr = "abcdefg";
        long timeStamp = System.currentTimeMillis() / 1000;
        String signedUrl = url;
        String ticket = null;
        String signature = null;
        String agentid = _config.getProperty("flowAgentId");
        try {
            AuthService.getAccessToken();
            ticket = AuthService.getJsapiTicket();
            signature = AuthService.sign(ticket, nonceStr, timeStamp, signedUrl);

        } catch (DdException e) {
        }
        String configValue = "{jsticket:'" + ticket + "',signature:'" + signature + "',nonceStr:'" + nonceStr + "',timeStamp:'"
                + timeStamp + "',corpId:'" + _corpId + "',agentid:'" + agentid + "'}";
        return configValue;
    }
}
