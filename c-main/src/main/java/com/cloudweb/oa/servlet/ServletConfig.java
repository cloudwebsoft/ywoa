package com.cloudweb.oa.servlet;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletConfig {

    @Bean
    public ServletRegistrationBean<cn.js.fan.util.Log4jInit> log4jServlet() {
        ServletRegistrationBean<cn.js.fan.util.Log4jInit> servletRegistrationBean = new ServletRegistrationBean<>(
                new cn.js.fan.util.Log4jInit(), "/log4j");
        servletRegistrationBean.addInitParameter("log4j", "WEB-INF/log4j.properties");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean<org.jfree.chart.servlet.DisplayChart> jfreeChartServlet() {
        ServletRegistrationBean<org.jfree.chart.servlet.DisplayChart> servletRegistrationBean = new ServletRegistrationBean<>(
                new org.jfree.chart.servlet.DisplayChart(), "/servlet/DisplayChart");
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean<uk.ltd.getahead.dwr.DWRServlet> dwrServlet() {
        ServletRegistrationBean<uk.ltd.getahead.dwr.DWRServlet> servletRegistrationBean = new ServletRegistrationBean<>(
                new uk.ltd.getahead.dwr.DWRServlet(), "/dwr/*");
        servletRegistrationBean.addInitParameter("debug", "false");
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean<com.runqian.report4.view.ReportServlet> reportServlet() {
        ServletRegistrationBean<com.runqian.report4.view.ReportServlet> servletRegistrationBean = new ServletRegistrationBean<>(
                new com.runqian.report4.view.ReportServlet(), "/reportServlet");
        servletRegistrationBean.addInitParameter("configFile", "/WEB-INF/reportConfig.xml");
        servletRegistrationBean.setLoadOnStartup(2);
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean<com.runqian.util.webutil.SetContextServlet> setContextServlet() {
        ServletRegistrationBean<com.runqian.util.webutil.SetContextServlet> servletRegistrationBean = new ServletRegistrationBean<>(
                new com.runqian.util.webutil.SetContextServlet(), "/setContextServlet");
        servletRegistrationBean.setLoadOnStartup(3);
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean<com.raq.web.view.DMServlet> dmServlet() {
        ServletRegistrationBean<com.raq.web.view.DMServlet> servletRegistrationBean = new ServletRegistrationBean<>(
                new com.raq.web.view.DMServlet(), "/DMServlet");
        servletRegistrationBean.addInitParameter("configFile", "/WEB-INF/dmConfig.xml");
        servletRegistrationBean.addUrlMappings("/DMServletAjax");
        servletRegistrationBean.setLoadOnStartup(4);
        return servletRegistrationBean;
    }

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
    public ServletRegistrationBean<com.redmoon.oa.exam.UploadFile> examUploadFileServlet() {
        ServletRegistrationBean<com.redmoon.oa.exam.UploadFile> servletRegistrationBean = new ServletRegistrationBean<>(
                new com.redmoon.oa.exam.UploadFile(), "/ueditor/ExamUploadFile");
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean<com.alibaba.druid.support.http.StatViewServlet> druidStatViewServlet() {
        ServletRegistrationBean<com.alibaba.druid.support.http.StatViewServlet> servletRegistrationBean = new ServletRegistrationBean<>(
                new com.alibaba.druid.support.http.StatViewServlet(), "/druid/*");
        return servletRegistrationBean;
    }
}
