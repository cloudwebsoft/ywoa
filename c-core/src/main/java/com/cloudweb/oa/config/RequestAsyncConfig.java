package com.cloudweb.oa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class RequestAsyncConfig implements WebMvcConfigurer {

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private TimeoutCallableProcessingInterceptor timeoutCallableProcessingInterceptor;

    @Override
    public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
        //超时时间3600秒
        configurer.setDefaultTimeout(3600000);
        configurer.setTaskExecutor(threadPoolTaskExecutor);
        // configurer.registerCallableInterceptors(timeoutCallableProcessingInterceptor());
        configurer.registerCallableInterceptors(timeoutCallableProcessingInterceptor);
    }

    @Bean
    public TimeoutCallableProcessingInterceptor timeoutCallableProcessingInterceptor() {
        return new TimeoutCallableProcessingInterceptor();
    }
}