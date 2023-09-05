package com.cloudweb.oa.config;

import com.cloudweb.oa.utils.CommonConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;

/**
 * 经测试，该类仅管setDocumentRoot进行了配置，但对外部Tomcat并不起作用，所以不会冲突。经调试，外部Tomcat程序启动时也会进入embeddedServletContainerFactory()
 */
@DependsOn({"SpringUtil"}) // SpringUtil已设为-100000，优先级最高
@Configuration
public class TomcatConfig {

    @Value("${sys.web.docPath}")
    private String rootDoc;

    /**
     * 使用内置tomcat调试时，需启用此方法
     * @return
     */
    @Bean
    public AbstractServletWebServerFactory embeddedServletContainerFactory() {
        TomcatServletWebServerFactory tomcatServletWebServerFactory = new CustomTomcatServletWebServerFactory();
        // 判断是否在jar文件中运行
        URL url = getClass().getResource("");
        String protocol = url.getProtocol();
        // 如果不是以jar方式运行，则置ROOT位置
        if (!CommonConstUtil.RUN_MODE_JAR.equals(protocol)) {
            tomcatServletWebServerFactory.setDocumentRoot(new File(rootDoc));
        }
        return tomcatServletWebServerFactory;
    }

    /**
     * Actuator指定端口，通过内置tomcat启动时，TomcatWebServerFactory子类是匿名内部类。它必须是静态的内部类或顶级类，以便可以实例化，否则会报如下错误
     * org.springframework.beans.FatalBeanException: ServletWebServerFactory implementation com.cloudweb.oa.config.TomcatConfig$1 cannot be instantiated.
     * To allow a separate management port to be used, a top-level class or static inner class should be used instead
     */
    static final class CustomTomcatServletWebServerFactory
            extends TomcatServletWebServerFactory {
        /*
        @Override
        protected void customizeConnector(Connector connector) {
            int maxSize = 50000000;
            super.customizeConnector(connector);
            connector.setMaxPostSize(maxSize);
            connector.setMaxSavePostSize(maxSize);
            if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
                ((AbstractHttp11Protocol<?>) connector.getProtocolHandler())
                        .setMaxSwallowSize(maxSize);
            }
        }
        */

        @Override
        protected TomcatWebServer getTomcatWebServer(org.apache.catalina.startup.Tomcat tomcat) {
            tomcat.enableNaming();
            return super.getTomcatWebServer(tomcat);
        }

        /**
         * tomcat在8.5.2 中 修改了加载jar的方式，8.5.2 版本会解析jar中MANIFEST.MF文件，
         * 当该文件包含class-path属性时，会把该属性对象值，解析成需要加载的jar给加载进来
         * 让tomcat不去扫描那些jar即可
         */
        @Override
        protected void postProcessContext(Context context) {
            // pom.xml中如果把tomcat降至8.0.23，setScanManifest报错
            ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);

            // 禁止内置Tomcat不安全的http方法
            SecurityConstraint securityConstraint = new SecurityConstraint();
            securityConstraint.setUserConstraint("CONFIDENTIAL");
            SecurityCollection collection = new SecurityCollection();
            collection.addPattern("/*");
            collection.addMethod("HEAD");
            collection.addMethod("PUT");
//            collection.addMethod("DELETE");
            // 如果打开则会导致预检不能通过，报：has been blocked by CORS policy: Response to preflight request doesn't pass access control check
            // collection.addMethod("OPTIONS");
            collection.addMethod("TRACE");
            collection.addMethod("COPY");
            collection.addMethod("SEARCH");
            collection.addMethod("PROPFIND");
            securityConstraint.addCollection(collection);
            context.addConstraint(securityConstraint);

            SysProperties sysProperties = SpringUtil.getBean(SysProperties.class);
            // 构建内置tomcat运行时报表需用到的数据源context
            if ("runquan".equals(sysProperties.getReportType())) {
                ContextResource resource = new ContextResource();
                resource.setName("jdbc/oa");
                // Tomcat 8关于javax.sql.DataSource的指定方式为org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory
                // resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
                resource.setType(DataSource.class.getName());
                resource.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");

                resource.setProperty("url", sysProperties.getDatasourceUrl());
                resource.setProperty("username", sysProperties.getDatasourceUsername());
                resource.setProperty("password", sysProperties.getDatasourcePassword());
                context.getNamingResources().addResource(resource);
            }
        }
    }

    /*
    @Bean
    public AbstractServletWebServerFactory embeddedServletContainerFactory() {

        TomcatServletWebServerFactory tomcatServletWebServerFactory = new TomcatServletWebServerFactory()  {
            *//**
             * tomcat在8.5.2 中 修改了加载jar的方式，8.5.2 版本会解析jar中MANIFEST.MF文件，
             * 当该文件包含class-path属性时，会把该属性对象值，解析成需要加载的jar给加载进来
             * 让tomcat不去扫描那些jar即可
             *//*
            @Override
            protected void postProcessContext(Context context) {
                // pom.xml中如果把tomcat降至8.0.23，setScanManifest报错
                ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);

                // 禁止内置Tomcat不安全的http方法
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                collection.addMethod("HEAD");
                collection.addMethod("PUT");
                collection.addMethod("DELETE");
                collection.addMethod("OPTIONS");
                collection.addMethod("TRACE");
                collection.addMethod("COPY");
                collection.addMethod("SEARCH");
                collection.addMethod("PROPFIND");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcatServletWebServerFactory.setDocumentRoot(new File(rootDoc));
        return  tomcatServletWebServerFactory;
    }*/
}