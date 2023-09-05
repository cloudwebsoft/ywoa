package com.cloudweb.oa.filter;

/*import org.apache.struts2.dispatcher.ActionContextCleanUp;
import org.apache.struts2.dispatcher.FilterDispatcher;
import org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter;*/
import com.cloudweb.oa.utils.ConfigUtil;
import com.cloudweb.oa.utils.FileUtil;
import com.cloudweb.oa.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@DependsOn("SpringUtil")
@Configuration
public class FilterConfig {

/*    @Bean
    public FilterRegistrationBean<ActionContextCleanUp> RegistStrutsCleanup() {
        // 通过FilterRegistrationBean实例设置优先级可以生效
        FilterRegistrationBean<ActionContextCleanUp> bean = new FilterRegistrationBean<ActionContextCleanUp>();
        bean.setFilter(new ActionContextCleanUp());
        bean.setName("struts-cleanup");
        bean.addUrlPatterns("/*");
        bean.setOrder(4);
        return bean;
    }*/

/*
    @Bean
    public FilterRegistrationBean RegistStruts2() {
        FilterRegistrationBean frgb = new FilterRegistrationBean();
        frgb.setFilter(new StrutsPrepareAndExecuteFilter());
        List list = new ArrayList();
        // list.add("/public/android/*");
        list.add("/public/android/notice/*");
        list.add("/public/android/messages/*");
        list.add("/public/android/users/*");
        list.add("/public/android/module/*");
        list.add("/public/android/visual/*");
        list.add("/public/android/address/*");
        list.add("/public/android/sms/*");
        list.add("/public/android/mywork/*");
        list.add("/public/android/myinfo/*");
        list.add("/public/android/location/*");
        list.add("/public/android/skins/*");
        list.add("/public/android/general/*");
        // list.add("/report/*");
        // list.add("/update/*");
        list.add("/ymoa/*");
        frgb.setUrlPatterns(list);
        frgb.setOrder(5);
        return frgb;
    }*/

    /**
     * 配置允许跨域，此filter需启用，因为CorsFilter中指定了需JWT需暴露的header
     * @return
     */
    @Bean
    public FilterRegistrationBean registerFilter() {
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>();

        List<String> list = new ArrayList<>();
        /*list.add("/document/*"); // xiaocaicloud.com
        list.add("/public/app/*"); // 手机端
        list.add("/mobile/*"); // 小程序端登录
        list.add("/modular/*"); // 小程序端智能模块
        list.add("/mini/*"); // 小程序端接口
        list.add("/i/*"); // 小程序端接口，我*/

        // 读取白名单文件
        /*List<String> whiteUrls;
        ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
        whiteUrls = FileUtil.read(configUtil.getFile("cors_white.txt"));
        list.addAll(whiteUrls);
        bean.setUrlPatterns(list);*/

        bean.addUrlPatterns("/*");

        bean.setFilter(new CorsFilter());
        // 过滤顺序，从小到大依次过滤
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        // bean.addInitParameter("exclusions","/some,/hello");
        return bean;
    }
}
