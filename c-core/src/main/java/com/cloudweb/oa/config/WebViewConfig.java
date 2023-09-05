package com.cloudweb.oa.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.cloudweb.oa.utils.SysProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebMvc
@ComponentScan
public class WebViewConfig implements WebMvcConfigurer {

    @Autowired
    SysProperties sysProperties;

//    @Bean
//    public HttpMessageConverter<String> responseBodyConverter() {
//        return new StringHttpMessageConverter(Charset.forName("UTF-8"));
//    }

    /**
     * @Description: 注册jsp视图解析器
     */
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/pages/"); //配置放置jsp文件夹
        resolver.setSuffix(".jsp");
        // resolver.setViewNames("*");  // 使识别为jsp页面引擎
        resolver.setViewNames(new String[] { "/", "*" });
        resolver.setOrder(3);
        return resolver;
    }

    /**
     * @Description: 注册html视图解析器
     */
    @Bean
    public ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("utf-8");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    /**
     * @Description: 将自定义html视图解析器添加到模板引擎
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());

        // 使支持在jdk1.8下格式化 ${#temporals.format(user.entryDate,'yyyy-MM-dd')}
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

    /**
     * @Description: Thymeleaf视图解析器配置
     */
    @Bean
    public ThymeleafViewResolver viewResolverThymeLeaf() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();

        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setCharacterEncoding("utf-8");
        viewResolver.setViewNames(new String[]{"th/*"});
        viewResolver.setOrder(2);
        return viewResolver;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /**
     * -设置url后缀模式匹配规则
     * -该设置匹配所有的后缀，使用.do或.action都可以
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(true)	//设置是否是后缀模式匹配,即:/test.*
                .setUseTrailingSlashMatch(true);	//设置是否自动后缀路径模式匹配,即：/test/
    }

//    /**
//     * 该设置严格指定匹配后缀*.do
//     * @param dispatcherServlet
//     * @return
//     */
//    @Bean
//    public ServletRegistrationBean servletRegistrationBean(DispatcherServlet dispatcherServlet) {
//        ServletRegistrationBean bean = new ServletRegistrationBean(dispatcherServlet);
//        bean.addUrlMappings("*.do");
//        return bean;
//    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // registry.addViewController("/").setViewName("th/index"); // 重定向至templates/th/index.html
        // registry.addViewController("/").setViewName("index"); // Forwarding to [/WEB-INF/pages/index.jsp]
        // registry.addViewController("/").setViewName("/index"); // Forwarding to [/WEB-INF/pages//index.jsp]
        // registry.addViewController("/").setViewName("forward:index.jsp"); // 进入/index.jsp
        // registry.addViewController("/").setViewName("redirect:/index.jsp");

        // 内置Tomcat跳转到IndexController，但在外置Tomcat上如果有index.jsp，则还是跳转至index.jsp
        // registry.addRedirectViewController("/", "/index");

        // 外置Tomcat，删除index.jsp后指向了IndexController
        registry.addViewController("/").setViewName("forward:index");
        // registry.addViewController("/").setViewName("forward:/index");

        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    /**
     * @Description: 配置静态文件映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 不能去掉注释，否则会找不到静态资源文件
        // 第一个方法设置访问路径前缀，第二个方法设置资源路径
        // registry.addResourceHandler("/**").addResourceLocations("/WEB-INF/static/");
        // 将/static/**访问，映射至classpath:/static/
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");

		// 映射用户头像目录及上传文件目录 
        registry.addResourceHandler("/public/user/**").addResourceLocations("file:" + sysProperties.getUploadPath() + "public/user/");
        registry.addResourceHandler("/upfile/**").addResourceLocations("file:" + sysProperties.getUploadPath() + "upfile/");
        registry.addResourceHandler("/public/images/**").addResourceLocations("file:" + sysProperties.getUploadPath() + "public/images/");
    }

/*    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册登陆验证拦截器
        registry.addInterceptor(new IsLoginHandlerInterceptor()).addPathPatterns("/**")
                .excludePathPatterns("/","/user/login","/static/**");
    }*/

    @Bean
    public HttpMessageConverter<String> responseBodyStringConverter() {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        return converter;
    }

    /**
     * 修改StringHttpMessageConverter默认配置
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters){
        converters.add(responseBodyStringConverter());
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter fjc = new FastJsonHttpMessageConverter();
        FastJsonConfig fj = new FastJsonConfig();
        SerializerFeature[] serializerFeatures = new SerializerFeature[]{
                // 出key是包含双引号
                SerializerFeature.QuoteFieldNames,
                // 否输出为null的字段,若为null 则显示该字段
                SerializerFeature.WriteMapNullValue,
                // 值字段如果为null，则输出为0
                // SerializerFeature.WriteNullNumberAsZero,
                // List字段如果为null,输出为[],而非null
                SerializerFeature.WriteNullListAsEmpty,
                // 字符类型字段如果为null,输出为"",而非null
                // SerializerFeature.WriteNullStringAsEmpty,
                // Boolean字段如果为null,输出为false,而非null
                SerializerFeature.WriteNullBooleanAsFalse,
                // Date的日期转换器
                // SerializerFeature.WriteDateUseDateFormat,
                // 循环引用
                SerializerFeature.DisableCircularReferenceDetect,
        };

        fj.setSerializerFeatures(serializerFeatures);
        fj.setCharset(Charset.forName("UTF-8"));
        fjc.setFastJsonConfig(fj);
        converters.add(fjc);
    }
}