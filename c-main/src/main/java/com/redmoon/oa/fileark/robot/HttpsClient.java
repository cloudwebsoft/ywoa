package com.redmoon.oa.fileark.robot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Leehom on 2017/11/27.
 */
public class HttpsClient {

    private static final int CONNECT_TIME_OUT = 15000;
    private static final int READ_TIME_OUT = 15000;
    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }};
    private static final HostnameVerifier NOT_VERYFY = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    };

    /**
     * 发起http请求并获取结果
     *
     * @param link   请求地址
     * @param method 请求方式（GET、POST）
     * @param data   提交的数据
     * @return JSONObject(通过JSONObject.get ( key)的方式获取json对象的属性值)
     */
    public static String httpRequest(String link, String method, String data) {
        String ret = "";
        try {
            URL url = new URL(link);
            //如果地址是https开头,这里返回的HttpURLConnection实例其实是HttpURLConnection
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(CONNECT_TIME_OUT);
            http.setReadTimeout(READ_TIME_OUT);
            http.setDoInput(true);
            if (data != null && data.length() > 0) {
                http.setDoOutput(true);
                http.setRequestMethod(method);
                OutputStream out = http.getOutputStream();
                out.write(data.toString().getBytes());
                out.flush();
            }
            http.connect();
            int code = http.getResponseCode();
            if (code == 200) {
                InputStream in = http.getInputStream();
                byte[] buf = new byte[512];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int actual = in.read(buf);
                while (actual != -1) {
                    bos.write(buf, 0, actual);
                    actual = in.read(buf);
                }
                in.close();
                in = null;
                bos.flush();
                bos.close();
                bos = null;

                ret = bos.toString("utf-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 发起https请求并获取结果
     *
     * @param link   请求地址
     * @param method 请求方式（GET、POST）
     * @param data   提交的数据
     * @return JSONObject(通过JSONObject.get ( key)的方式获取json对象的属性值)
     */
    public static String httpsRequest(String link, String method, String data) {
        // Create a trust manager that does not validate certificate chains
        // Install the all-trusting trust manager
        try {// 注意这部分一定要
            HttpsURLConnection.setDefaultHostnameVerifier(NOT_VERYFY);
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            return httpRequest(link, method, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}