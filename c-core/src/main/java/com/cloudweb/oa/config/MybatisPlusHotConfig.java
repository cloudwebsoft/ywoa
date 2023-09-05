package com.cloudweb.oa.config;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisMapperRefresh;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.plugin.Interceptor;
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
import org.springframework.security.core.parameters.P;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Configuration
//@MapperScan("com.cloudweb.*.dao.*")
@PropertySource({"classpath:application.properties"})
@AutoConfigureAfter(SqlSessionFactory.class)
public class MybatisPlusHotConfig {
    // private static final String CONFIG_XML_PATH = "classpath:mybatis-configuration.xml";
    private static final String MAPPER_XML_PATH = "classpath*:com/cloudweb/oa/mapper/xml/*.xml";


//    @Autowired
//    private SqlSessionFactory sqlSessionFactory;

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

    public PageInterceptor pageInterceptor(){
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect","mysql");
        properties.setProperty("reasonable","true");
        properties.setProperty("autoRuntimeDialect","true");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }

    /**
     *
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        // sqlSessionFactory.setConfigLocation(pathMatchingResourcePatternResolver.getResource(CONFIG_XML_PATH));
        sqlSessionFactory.setMapperLocations(pathMatchingResourcePatternResolver.getResources(MAPPER_XML_PATH));
        sqlSessionFactory.setDataSource(dataSource);
        Interceptor[] arr = new Interceptor[1];
        arr[0] = pageInterceptor();
        sqlSessionFactory.setPlugins(arr);

        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        /*
        // 默认策略 参考: https://blog.csdn.net/w1014074794/article/details/125644080
        GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();
        dbConfig.setInsertStrategy(FieldStrategy.NOT_NULL);
        dbConfig.setUpdateStrategy(FieldStrategy.NOT_NULL);
        dbConfig.setSelectStrategy(FieldStrategy.NOT_EMPTY);
        */

        //自动填充配置
        //mybatis-plus全局配置设置元数据对象处理器为自己实现的那个
        globalConfig.setMetaObjectHandler(new EJMetaObjectHandler());
        //mybatisSqlSessionFactoryBean关联设置全局配置
        sqlSessionFactory.setGlobalConfig(globalConfig);
        return sqlSessionFactory.getObject();
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
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) SpringUtil.getBean("sqlSessionFactory");
        Resource[] resources = new Resource[0];
        try {
            resources = resourceResolver.getResources(mapperLocations);
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
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