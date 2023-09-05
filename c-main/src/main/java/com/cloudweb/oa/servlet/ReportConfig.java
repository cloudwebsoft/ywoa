package com.cloudweb.oa.servlet;

import com.runqian.report4.view.ReportServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 润乾报表初始化
 */
@ConditionalOnProperty(name = "report.type", matchIfMissing = false, havingValue = "runquan")
@Configuration
public class ReportConfig {

    @Bean
    public ServletRegistrationBean<ReportServlet> reportServlet() {
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
}
