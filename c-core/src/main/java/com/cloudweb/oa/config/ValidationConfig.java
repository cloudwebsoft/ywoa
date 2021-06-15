package com.cloudweb.oa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;


@Configuration
public class ValidationConfig {

    @Bean(name = "validMessageSource")
    public MessageSource validMessageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        // 解决文乱码的问题，在application.yml中设置spring.messages.encoding=utf-8无效
        source.setDefaultEncoding("utf-8");//读取配置文件的编码格式
        source.setCacheMillis(-1);//缓存时间，-1表示不过期
        source.setBasename("i18n/ValidationMessages");
        return source;
    }

    @Bean
    public Validator validator() {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        MessageInterpolatorFactory interpolatorFactory = new MessageInterpolatorFactory();
        factoryBean.setMessageInterpolator(interpolatorFactory.getObject());
        factoryBean.setValidationMessageSource(validMessageSource());
        return factoryBean;
    }
}