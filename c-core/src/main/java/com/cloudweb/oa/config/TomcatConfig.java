package com.cloudweb.oa.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 经测试，该类仅管setDocumentRoot进行了配置，但对外部Tomcat并不起作用，所以不会冲突。经调试，外部Tomcat程序启动时也会进入embeddedServletContainerFactory()
 */
@Configuration
public class TomcatConfig {
/*    @Bean
    public EmbeddedServletContainerFactory embeddedServletContainerFactory() {
        ConfigurableEmbeddedServletContainer factory = new TomcatEmbeddedServletContainerFactory();
        factory.setDocumentRoot(new File("D:\\WorkSpace\\**\\src\\main\\webapp\\"));
        return (EmbeddedServletContainerFactory) factory;
    }*/

    @Value("${cloudweb.oa.root}")
    private String rootDoc;

    @Bean
    public AbstractServletWebServerFactory embeddedServletContainerFactory() {
        TomcatServletWebServerFactory tomcatServletWebServerFactory = new CustomTomcatServletWebServerFactory();
        tomcatServletWebServerFactory.setDocumentRoot(new File(rootDoc));
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
            collection.addMethod("DELETE");
            collection.addMethod("OPTIONS");
            collection.addMethod("TRACE");
            collection.addMethod("COPY");
            collection.addMethod("SEARCH");
            collection.addMethod("PROPFIND");
            securityConstraint.addCollection(collection);
            context.addConstraint(securityConstraint);
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