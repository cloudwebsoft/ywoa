package com.cloudweb.oa.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisMapperRefresh;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Properties;

@Configuration
//@MapperScan("com.cloudweb.*.dao.*")
@PropertySource({"classpath:application.properties"})
@AutoConfigureAfter(SqlSessionFactory.class)
public class MybatisPlusHotConfig {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Value("${mybatis-plus.mapper-locations}")
    private String mapperLocations;

    @Value("${mybatis-plus.refresh-mapper}")
    private Boolean refreshMapper;

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    /*
     * 分页插件，自动识别数据库类型
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * 打印 sql
     */
    @Bean
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        // 格式化sql语句
        Properties properties = new Properties();
        properties.setProperty("format", "true");
        performanceInterceptor.setProperties(properties);
        return performanceInterceptor;
    }

    /* * oracle数据库配置JdbcTypeForNull
     * 参考：https://gitee.com/baomidou/mybatisplus-boot-starter/issues/IHS8X
     不需要这样配置了，参考 yml:
     mybatis-plus:
       confuguration
         dbc-type-for-null: 'null' */
    @Bean
    public ConfigurationCustomizer configurationCustomizer(){
        return new MybatisPlusCustomizers();
    }

    class MybatisPlusCustomizers implements ConfigurationCustomizer {

        @Override
        public void customize(org.apache.ibatis.session.Configuration configuration) {
            configuration.setJdbcTypeForNull(JdbcType.NULL);
        }
    }

    /**
     * mapper.xml 热加载
     * @return
     */
    @Bean
    public com.cloudweb.oa.runnable.MybatisMapperRefresh mybatisMapperRefresh(){
        // SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) SpringHelper.getBean("sqlSessionFactory");
        Resource[] resources = new Resource[0];
        try {
            resources = resourceResolver.getResources(mapperLocations);
        } catch (IOException e) {
            e.printStackTrace();
        }
        com.cloudweb.oa.runnable.MybatisMapperRefresh mybatisMapperRefresh = new com.cloudweb.oa.runnable.MybatisMapperRefresh(resources,sqlSessionFactory,10,5,refreshMapper);
        return mybatisMapperRefresh;

    }

    /**
     * 逻辑删除
     * @return
     */
    @Bean
    public ISqlInjector sqlInjector() {
        return new LogicSqlInjector();
    }




//	    @Bean
//	    @ConfigurationProperties("spring.datasource.druid" )
//	    public DataSource dataSource() {
//	        return DruidDataSourceBuilder
//	                .create()
//	                .build();
//	    }



}