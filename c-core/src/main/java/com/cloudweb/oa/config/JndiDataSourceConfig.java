package com.cloudweb.oa.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;

// @MapperScan("com.sample.mybatis")
@ConditionalOnProperty(name = "report.type", matchIfMissing = false, havingValue = "runquan")
@Configuration
public class JndiDataSourceConfig {

    public final String MAPPER_LOCATIONS_PATH ="classpath:mybatis-mappers/*.xml";

    @Bean(destroyMethod="")
    public DataSource jndiDataSource() throws IllegalArgumentException, NamingException {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiName("java:comp/env/jdbc/oa");
        //bean.setResourceRef(true); // this was previously uncommented
        bean.setProxyInterface(DataSource.class);
        //bean.setLookupOnStartup(false); // this was previously uncommented
        bean.afterPropertiesSet();
        return (DataSource)bean.getObject();
    }

    /*@Bean
    public DataSourceTransactionManager transactionManager() throws NamingException {
        return new DataSourceTransactionManager(jndiDataSource());
    }

    // spring容器中只能有一个SqlSessionFactory，否则会与MybatisPlusHotConfig中冲突
    @Bean
    public SqlSessionFactory jndiSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        configureSqlSessionFactory(sessionFactory, jndiDataSource());
        return sessionFactory.getObject();
    }*/

    public void configureSqlSessionFactory(SqlSessionFactoryBean sessionFactoryBean, DataSource dataSource) throws IOException {
        sessionFactoryBean.setDataSource(dataSource);
        // PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
        // sessionFactoryBean.setMapperLocations(pathResolver.getResources(MAPPER_LOCATIONS_PATH));
    }
}

