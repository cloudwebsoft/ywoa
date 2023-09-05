package com.cloudweb.oa.servlet;

import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class ServletConfig {

    /*@Bean
    public ServletRegistrationBean<cn.js.fan.util.Log4jInit> log4jServlet() {
        ServletRegistrationBean<cn.js.fan.util.Log4jInit> servletRegistrationBean = new ServletRegistrationBean<>(new cn.js.fan.util.Log4jInit(), "/log4j");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }*/

    @Bean
    public ServletRegistrationBean<com.cloudweb.oa.servlet.AppInit> appInitServlet() {
        ServletRegistrationBean<com.cloudweb.oa.servlet.AppInit> servletRegistrationBean = new ServletRegistrationBean<>(
                new com.cloudweb.oa.servlet.AppInit(), "/appInit");
        servletRegistrationBean.setLoadOnStartup(5);
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean<com.redmoon.weixin.servlet.WXCallBackServlet> wxCallBackServlet() {
        ServletRegistrationBean<com.redmoon.weixin.servlet.WXCallBackServlet> servletRegistrationBean = new ServletRegistrationBean<>(
                new com.redmoon.weixin.servlet.WXCallBackServlet(), "/servlet/WXCallBack");
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean<com.redmoon.weixin.servlet.WXAddressCallBackServlet> wxAddressCallBackServlet() {
        ServletRegistrationBean<com.redmoon.weixin.servlet.WXAddressCallBackServlet> servletRegistrationBean = new ServletRegistrationBean<>(
                new com.redmoon.weixin.servlet.WXAddressCallBackServlet(), "/servlet/WXAddressCallBackServlet");
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean<com.redmoon.dingding.servlet.EventChangeReceiveServlet> ddEventChangeReceiveServlet() {
        ServletRegistrationBean<com.redmoon.dingding.servlet.EventChangeReceiveServlet> servletRegistrationBean = new ServletRegistrationBean<>(
                new com.redmoon.dingding.servlet.EventChangeReceiveServlet(), "/servlet/DdEventChangeReceiveServlet");
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean<com.redmoon.oa.ue.UploadFile> ueUploadFileServlet() {
        ServletRegistrationBean<com.redmoon.oa.ue.UploadFile> servletRegistrationBean = new ServletRegistrationBean<>(
                new com.redmoon.oa.ue.UploadFile(), "/ueditor/UploadFile");
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean<com.alibaba.druid.support.http.StatViewServlet> druidStatViewServlet() {
        ServletRegistrationBean<com.alibaba.druid.support.http.StatViewServlet> servletRegistrationBean = new ServletRegistrationBean<>(
                new com.alibaba.druid.support.http.StatViewServlet(), "/druid/*");
        return servletRegistrationBean;
    }
}
