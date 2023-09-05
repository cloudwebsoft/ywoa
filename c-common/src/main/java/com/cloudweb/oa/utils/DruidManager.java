package com.cloudweb.oa.utils;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Connection;

@Component
public class DruidManager {
/*    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.initialSize}")
    private int initialSize;
    @Value("${spring.datasource.minIdle}")
    private int minIdle;
    @Value("${spring.datasource.maxActive}")
    private int maxActive;
    @Value("${spring.datasource.maxWait}")
    private int maxWait;
    @Value("${spring.datasource.timeBetweenEvictionRunsMillis}")
    private int timeBetweenEvictionRunsMillis;
    @Value("${spring.datasource.minEvictableIdleTimeMillis}")
    private int minEvictableIdleTimeMillis;
    @Value("${spring.datasource.validationQuery}")
    private String validationQuery;
    @Value("${spring.datasource.testWhileIdle}")
    private boolean testWhileIdle;
    @Value("${spring.datasource.testOnBorrow}")
    private boolean testOnBorrow;
    @Value("${spring.datasource.testOnReturn}")
    private boolean testOnReturn;
    @Value("${spring.datasource.poolPreparedStatements}")
    private boolean poolPreparedStatements;
    @Value("${spring.datasource.maxPoolPreparedStatementPerConnectionSize}")
    private int maxPoolPreparedStatementPerConnectionSize;
    @Value("${spring.datasource.filters}")
    private String filters;
    @Value("${spring.datasource.connectionProperties}")
    private String connectionProperties;
    @Value("${spring.datasource.useGlobalDataSourceStat}")
    private boolean useGlobalDataSourceStat;*/

    private static DruidManager druid;

    // @Autowired
    // @Qualifier("dataSource")
    private DataSource dataSource;

    //被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器调用一次，类似于Servlet的init()方法。被@PostConstruct修饰的方法会在构造函数之后，init()方法之前运行
    /*@PostConstruct //通过@PostConstruct实现初始化bean之前进行的操作，本处是为了支持非spring扫描的包中调用，当然也可以通过SpringHelper.getBean获取
    public void init() {
        druid = this;
        dataSource = SpringUtil.getBean(DataSource.class);
    }*/

    @Autowired
    public DruidManager(DataSource dataSource) {
        druid = this;
        druid.dataSource = dataSource;
    }

    public DruidManager() {

    }

    /*private void initPool() {
        dataSource = new DruidDataSource();

        Properties properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        dataSource.setDriverClassName(properties.getProperty("spring.datasource.driverClassName"));
        dataSource.setUrl(properties.getProperty("spring.datasource.url"));
        dataSource.setUsername(properties.getProperty("spring.datasource.username"));
        dataSource.setPassword(properties.getProperty("spring.datasource.password"));

        //configuration
        dataSource.setInitialSize(Integer.parseInt(properties.getProperty("spring.datasource.initialSize")));
        dataSource.setMinIdle(Integer.parseInt(properties.getProperty("spring.datasource.minIdle")));
        dataSource.setMaxActive(Integer.parseInt(properties.getProperty("spring.datasource.maxActive")));
        dataSource.setMaxWait(Integer.parseInt(properties.getProperty("spring.datasource.maxWait")));
        dataSource.setTimeBetweenEvictionRunsMillis(Integer.parseInt(properties.getProperty("spring.datasource.timeBetweenEvictionRunsMillis")));
        dataSource.setMinEvictableIdleTimeMillis(Integer.parseInt(properties.getProperty("spring.datasource.minEvictableIdleTimeMillis")));
        dataSource.setValidationQuery(properties.getProperty("spring.datasource.validationQuery"));

        dataSource.setTestWhileIdle(Boolean.valueOf(properties.getProperty("spring.datasource.testWhileIdle")));
        dataSource.setTestOnBorrow(Boolean.valueOf(properties.getProperty("spring.datasource.testOnBorrow")));
        dataSource.setTestOnReturn(Boolean.valueOf(properties.getProperty("spring.datasource.testOnReturn")));
        dataSource.setPoolPreparedStatements(Boolean.valueOf(properties.getProperty("spring.datasource.poolPreparedStatements")));

        dataSource.setMaxPoolPreparedStatementPerConnectionSize(Integer.parseInt(properties.getProperty("spring.datasource.maxPoolPreparedStatementPerConnectionSize")));
        dataSource.setUseGlobalDataSourceStat(Boolean.valueOf(properties.getProperty("spring.datasource.useGlobalDataSourceStat")));

        try {
            dataSource.setFilters(properties.getProperty("spring.datasource.filters"));
        } catch (SQLException e) {
            System.err.println("druid configuration initialization filter: " + e);
        }
        dataSource.setConnectionProperties(properties.getProperty("spring.datasource.connectionProperties"));

        // 防止当连接失败时不停地反复重连
        dataSource.setBreakAfterAcquireFailure(true);

        // dataSource.setPoolPreparedStatements(true);
    }*/

    /**
     * 创建单例模式
     *
     * @return JDBCDruid实例
     */
    public static synchronized DruidManager getInstance() {
        if (druid == null) {
            druid = new DruidManager();

            // druid.initPool();

            // 内置Tomcat启动时，SpringUtil.getBean有时applicationContext为null，是因为不同的类加载器？
            // druid.dataSource = SpringUtil.getBean(DataSource.class);
            druid.dataSource = SpringUtil.getDataSource();

            return druid;
        }
        else {
            return druid;
        }
    }

    public Connection getConnection() {
        // return DataSourceUtils.getConnection(SpringUtil.getBean(DataSource.class));
        return DataSourceUtils.getConnection(dataSource);
/*
        // 20200524 注释
        Connection connection = null;
        synchronized (dataSource) {
            connection = dataSource.getConnection();
        }
        return connection;*/
    }

    public DruidDataSource getDataSource() {
        // return (DruidDataSource) SpringUtil.getBean("dataSource");
        return (DruidDataSource)dataSource;
    }
}
