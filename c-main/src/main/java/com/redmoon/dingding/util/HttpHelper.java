package com.redmoon.dingding.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.dingding.domain.BaseDdObj;
import com.redmoon.dingding.enums.Enum;
import com.redmoon.dingding.service.auth.AuthService;
import com.redmoon.oa.sys.DebugUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;

import java.io.IOException;


/**
 * HTTP请求封装，建议直接使用sdk的API
 */
public class HttpHelper {
    public String url;

    private String postData;

    public HttpHelper(String url) {
        this.url = String.format("%saccess_token=%s", url, AuthService.getAccessToken());
    }

    public HttpHelper() {
    }

    public interface ICallBack<T extends BaseDdObj >{
        void doResult(T t);
        void doError(int errorCode,String msg);
    }

    private <T extends BaseDdObj> T onResult(T data) throws DdException {
        if (data.errcode != Enum.emErrorCode.emSuccess) {
            throw new DdException(data.errcode, data.errmsg, url, postData);
        }
        return data;
    }

    public <T extends BaseDdObj> T httpGet(Class<? extends BaseDdObj> classz) throws DdException {
        String resultStr = httpGet();
        if (resultStr != null && !resultStr.equals("")) {
            return (T) onResult(JSONObject.parseObject(resultStr, classz));
        } else {
            return null;
        }
    }

    /**
     * httpPost
     *
     * @param classz
     * @param data
     * @param <T>
     * @return
     * @throws DdException
     */
    public <T extends BaseDdObj> T httpPost(Class<? extends BaseDdObj> classz, Object data) throws DdException {
        DebugUtil.i(getClass(), "httpPost url=" + url + " data", data.toString());
        String resultStr = httpPost(data);
        DebugUtil.i(getClass(), "httpPost resultStr", resultStr);
        if (resultStr != null && !"".equals(resultStr)) {
            return (T) onResult(JSONObject.parseObject(resultStr, classz));
        } else {
            return null;
        }
    }

    public String httpGet() throws DdException {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().
                setSocketTimeout(2000).setConnectTimeout(2000).build();
        httpGet.setConfig(requestConfig);
        try {
            response = httpClient.execute(httpGet, new BasicHttpContext());
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new DdException(Enum.emErrorCode.emErrorUrlConnect, "请求异常");
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");
                return resultStr;

            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (response != null) try {
                response.close();
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        return null;
    }

    public String httpPost(Object data) throws DdException {
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().
                setSocketTimeout(2000).setConnectTimeout(2000).build();
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/json");

        try {
            postData = JSON.toJSONString(data);
            StringEntity requestEntity = new StringEntity(postData, "utf-8");
            httpPost.setEntity(requestEntity);
            response = httpClient.execute(httpPost, new BasicHttpContext());
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new DdException(Enum.emErrorCode.emErrorUrlConnect, "请求异常");
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");
                return resultStr;
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("url:" + url);
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }
        return null;
    }

    public <T extends  BaseDdObj> void httpPost(Object data,Class<? extends BaseDdObj> classz,ICallBack callBack) {
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().
                setSocketTimeout(2000).setConnectTimeout(2000).build();
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/json");
        try {
            postData = JSON.toJSONString(data);
            StringEntity requestEntity = new StringEntity(postData, "utf-8");
            httpPost.setEntity(requestEntity);
            response = httpClient.execute(httpPost, new BasicHttpContext());
            if (response.getStatusLine().getStatusCode() != 200) {
                callBack.doError(Enum.emErrorCode.emErrorUrlConnect, "请求异常");
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");
                callBack.doResult(JSONObject.parseObject(resultStr, classz));
            }
        } catch (IOException e) {
            callBack.doError(Enum.emErrorCode.emErrorUrlConnect, "请求异常");
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }
    }
}
