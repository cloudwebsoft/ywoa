package com.cloudweb.oa.config;

import java.io.IOException;
import java.util.Properties;

import com.cloudweb.oa.quartz.SpringJobFactory;
import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

/**
 * quartz配置
 */
@Configuration
public class SchedulerConfig {

    @Autowired
    private SpringJobFactory springJobFactory;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Bean(name="SchedulerFactory")
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setAutoStartup(true);
        factory.setStartupDelay(5);//延时5秒启动
        factory.setQuartzProperties(quartzProperties());
        factory.setJobFactory(springJobFactory);
        factory.setDataSource(dataSource);

        factory.setWaitForJobsToCompleteOnShutdown(true);//这样当spring关闭时，会等待所有已经启动的quartz job结束后spring才能完全shutdown。
        factory.setOverwriteExistingJobs(false);
        return factory;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    /*
     * quartz初始化监听器
     * 注释掉，否则会报错：org.quartz.impl.jdbcjobstore.JobStoreTX | Database connection shutdown unsuccessful
     * java.sql.SQLException: There is no DataSource named 'null'
	 * at org.quartz.utils.DBConnectionManager.shutdown(DBConnectionManager.java:135)
     */
    /*@Bean
    public QuartzInitializerListener executorListener() {
        return new QuartzInitializerListener();
    }*/

    /*
     * 通过SchedulerFactoryBean获取Scheduler的实例
     */
    @Bean(name="CwsScheduler")
    public Scheduler scheduler() throws IOException {
        return schedulerFactoryBean().getScheduler();
    }

}