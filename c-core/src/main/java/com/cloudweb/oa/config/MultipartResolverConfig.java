package com.cloudweb.oa.config;

import cn.js.fan.web.Global;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MultipartResolverConfig {

    @Bean("multipartResolver")
    public MultipartResolver configMultipartResolver() {
        MyCommonsMultipartResolver commonsMultipartResolver = new MyCommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("UTF-8");
        commonsMultipartResolver.setMaxUploadSize(Global.getInstance().getMaxSize());//上传文件大小 1024M 1024*1024*1024
        commonsMultipartResolver.setMaxInMemorySize(4096);
        commonsMultipartResolver.setResolveLazily(true); // resolveLazily属性启用是为了推迟文件解析，以在在UploadAction中捕获文件大小异常

        // 置被排除的目录
        String[] excludeUrlArray = {"modular/create", "modular/update", "notice/create", "notice/save", "flow", "address", "module", "fileark", "android", "project", "form_m", "stamp", "workplan", "fwebedit", "visual", "message_oa", "setup", "ueditor", "wx/admin"};
        commonsMultipartResolver.setExcludeUrlArray(excludeUrlArray);

        // 置被排除的目录中的例外目录
        String[] exceptionUrlArray = {"android/i/uploadHeadImage", "android/i/modifyPersonInfor", "android/filecase/upload", "flow/setConds"};
        // "visual/savePageMobileList",
        commonsMultipartResolver.setExceptionArray(exceptionUrlArray);

        return commonsMultipartResolver;
    }

    @Bean
    @Order(0)
    public MultipartFilter multipartFilter() {
        MultipartFilter multipartFilter = new MultipartFilter();
        multipartFilter.setMultipartResolverBeanName("multipartResolver");
        return multipartFilter;
    }
}
