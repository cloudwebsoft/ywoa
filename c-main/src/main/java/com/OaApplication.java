package com;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.cloudweb.oa.listener.ProxoolListener;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JndiDataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.sql.DataSource;

// pagehelper-spring-boot-starter依赖，提供了自动配置分页插件的功能
@EnableJms
@SpringBootApplication(scanBasePackages = {"com.cloudweb.oa"}, exclude = {DruidDataSourceAutoConfigure.class, DataSourceAutoConfiguration.class, JndiDataSourceAutoConfiguration.class, PageHelperAutoConfiguration.class, MultipartAutoConfiguration.class, QuartzAutoConfiguration.class, FreeMarkerAutoConfiguration.class/*, RedisAutoConfiguration.class*/})
// @MapperScan(value = {"com.cloudweb.oa.dao", "com.cloudweb.oa.mapper"})
// @ComponentScan(basePackages={"com.cloudweb.oa"})
@ServletComponentScan
// @EnableAutoConfiguration(exclude = {PageHelperAutoConfiguration.class})
// @EnableAutoConfiguration(exclude={DruidDataSourceAutoConfigure.class, JndiDataSourceAutoConfiguration.class,})
@EnableSwagger2
@EnableTransactionManagement // 注解事务管理，等同于xml配置方式的 <tx:annotation-driven />
@EnableAsync
@ImportResource("classpath:com/cloudweb/config/applicationContext.xml")
public class OaApplication extends SpringBootServletInitializer {

    private static String[] args;
    private static ConfigurableApplicationContext context;

    /**
     * 事务管理器
     * @param dataSource 加上@Qualifier("dataSource")可用于多数据源的情况
     * @return
     */
    @Bean(name = "txManager")
    public PlatformTransactionManager txManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    public static void main(String[] args) {
        OaApplication.args = args;

        SpringApplication springApplication = new SpringApplication(OaApplication.class);
        // 在bean初始化前启动proxool监听器
        springApplication.addListeners(new ProxoolListener());

        OaApplication.context = springApplication.run(args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        // 内置tomcat启动时不会进入此方法

        // 也可以在属性文件中这样配置：logging.level.org.springframework.boot.context.web.ErrorPageFilter=off
        // set register error pagefilter false，以免报：......ErrorPageFilter | Cannot forward to error page for request [/us/leaveOffBatch.do] as the response has already been committed.
        setRegisterErrorPageFilter(false);

        // 在bean初始化前启动proxool监听器
        application.application().addListeners(new ProxoolListener());

        return application.sources(OaApplication.class);
    }

    /**
     * 显示声明CommonsMultipartResolver为mutipartResolver
     * @return
     */
/*    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver(){
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");
        resolver.setResolveLazily(true);//resolveLazily属性启用是为了推迟文件解析，以在在UploadAction中捕获文件大小异常
        resolver.setMaxInMemorySize(40960);
        resolver.setMaxUploadSize(1024*1024*1024);//上传文件大小 1024M 50*1024*1024
        return resolver;
    }*/

    /**
     * 只能用于内置tomcat的重启
     */
    public static void restart() {
        context.close();
        context = SpringApplication.run(OaApplication.class, args);
    }
}

