package com.cloudwebsoft.framework.util;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import static java.lang.System.out;


public class HttpHelper {

    // get 请求
    public static String httpGet(String url, Header[] headers) throws Exception {
        HttpUriRequest uriRequest = new HttpGet(url);
        if (null != headers) {
            uriRequest.setHeaders(headers);
        }
        CloseableHttpClient httpClient = null;
        try {
            httpClient = declareHttpClientSSL(url);
            CloseableHttpResponse httpresponse = httpClient.execute(uriRequest);
            HttpEntity httpEntity = httpresponse.getEntity();
            String result = EntityUtils.toString(httpEntity, REQ_ENCODEING_UTF8);
            return result;
        } catch (ClientProtocolException e) {
            out.println(String.format("http请求失败，uri{%s},exception{%s}", new Object[] { url, e }));
        } catch (IOException e) {
            out.println(String.format("IO Exception，uri{%s},exception{%s}", new Object[] { url, e }));
        } finally {
            if (null != httpClient) {
                httpClient.close();
            }
        }
        return null;
    }

    // post 请求
    public static String httpPost(String url, String params) throws Exception {
        HttpPost post = new HttpPost(url);
        post.addHeader("Content-Type", "application/json;charset=" + REQ_ENCODEING_UTF8);
        // 设置传输编码格式
        StringEntity stringEntity = new StringEntity(params, REQ_ENCODEING_UTF8);
        stringEntity.setContentEncoding(REQ_ENCODEING_UTF8);
        post.setEntity(stringEntity);
        HttpResponse httpresponse = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = declareHttpClientSSL(url);
            httpresponse = httpClient.execute(post);
            HttpEntity httpEntity = httpresponse.getEntity();
            String result = EntityUtils.toString(httpEntity, REQ_ENCODEING_UTF8);
            return result;
        } catch (ClientProtocolException e) {
            out.println(String.format("http请求失败，uri{%s},exception{%s}", new Object[] { url, e }));
        } catch (IOException e) {
            out.println(String.format("IO Exception，uri{%s},exception{%s}", new Object[] { url, e }));
        } finally {
            if (null != httpClient)
                httpClient.close();
        }
        return null;
    }

    private static CloseableHttpClient declareHttpClientSSL(String url) {
        if (url.startsWith("https://")) {
            return sslClient();
        } else {
            return HttpClientBuilder.create().setConnectionManager(httpClientConnectionManager).build();
        }
    }

    /**
     * 设置SSL请求处理
     *
     * @param httpClient
     */
    private static CloseableHttpClient sslClient() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] xcs, String str) {
                }

                public void checkServerTrusted(X509Certificate[] xcs, String str) {
                }
            };
            ctx.init(null, new TrustManager[] { tm }, null);
            SSLConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
            return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    // this is config
    private static final String REQ_ENCODEING_UTF8 = "utf-8";
    private static PoolingHttpClientConnectionManager httpClientConnectionManager;

    public HttpHelper() {
        httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        httpClientConnectionManager.setMaxTotal(100);
        httpClientConnectionManager.setDefaultMaxPerRoute(20);
    }

    // get 请求
    public static String httpGet(String url) throws Exception {
        return httpGet(url, null);
    }
}
