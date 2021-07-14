package com.cloudweb.oa.config;

import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.listener.SystemListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

/**
 * 系统配置类
 */
@Configuration
public class SystemConfig {

    @Bean
    public ServletListenerRegistrationBean myListener() {
        ServletListenerRegistrationBean registrationBean = new ServletListenerRegistrationBean(new SystemListener());
        return registrationBean;
    }

    /**
     * SprinUtil.getRequest在非spring管理的场景下是取不到值的，如public/robot/robot_reply.jsp，需通过RequestContextListener才能取到
     * @return
     */
    @Bean
    public RequestContextListener requestContextListener(){
        return new RequestContextListener();
    }
}