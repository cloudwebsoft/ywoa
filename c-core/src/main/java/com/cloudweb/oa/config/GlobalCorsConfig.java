package com.cloudweb.oa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

// 暂不启用，因为已经用了CorsFilter
// @Configuration
public class GlobalCorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // 添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        // 放行哪些原始域
        config.addAllowedOrigin("*");
        //是否发送Cookie信息
        config.setAllowCredentials(true);
        // 放行哪些原始域(请求方式)
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");     //get
        config.addAllowedMethod("PUT");     //put
        config.addAllowedMethod("POST");    //post
        config.addAllowedMethod("DELETE");  //delete
        config.addAllowedMethod("PATCH");
        config.addAllowedHeader("*");

        // 添加映射路径
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/document/*", config);
        configSource.registerCorsConfiguration("/public/app/*", config);
        configSource.registerCorsConfiguration("/mobile/*", config);

        // 返回新的CorsFilter.
        return new CorsFilter(configSource);
    }
}