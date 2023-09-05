package com.cloudweb.oa.security;

import com.cloudweb.oa.filter.JwtFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@DependsOn("SpringUtil")
@Component
public class BeanConfig {

    /**
     * 重复执行的filter实现一次执行过程
     * 不能放在FilterConfig中，会报 Failed to introspect Class [com.cloudweb.oa.utils.JwtUtil] from ClassLoader [ParallelWebappClassLoader
     * @param filter
     * @return {@link FilterRegistrationBean}
     * @author
     * @date 2020/11/26 17:31
     */
    @Bean
    public FilterRegistrationBean registration(JwtFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * 使用BCrypt进行加密
     *
     * @return {@link PasswordEncoder}
     * @author LCheng
     * @date 2020/11/26 17:57
     */
    /*@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }*/
}
