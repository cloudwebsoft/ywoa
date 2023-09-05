package com.cloudweb.oa.listener;

import cn.js.fan.util.PropertiesUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.utils.CommonConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.fusesource.hawtbuf.UTF8Buffer;
import org.springframework.beans.propertyeditors.URIEditor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 标准的 Servlet 监听器
 */
@Slf4j
public class SystemListener implements ServletContextListener {

    BrokerService brokerService = new BrokerService();

    /**
     * 应用启动时自动执行
     * 当Servlet 容器启动Web 应用时调用该方法。在调用完该方法之后，容器再对Filter 初始化，
     * 并且对那些在Web 应用启动时就需要被初始化的Servlet 进行初始化
     *
     * @param servletContextEvent
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            log.info("SystemListener contextInitialized...");

            boolean isServerOpen = false;
            IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
            String cfgPath = configUtil.getFilePath();
            // 先找外部配置文件，找不到，则用jar包内的
            PropertiesUtil propertiesUtil = new PropertiesUtil(cfgPath + "/application.properties");
            // 如果没有外部配置文件，则在jar包中找
            if (propertiesUtil.getSafeProperties() == null) {
                Resource resource = new ClassPathResource("application.properties");
                InputStream is = resource.getInputStream();
                log.info("SystemListener: 载入jar包中的application.properties");
                propertiesUtil = new PropertiesUtil(is);
                String val = propertiesUtil.getValue("activemq.isServerOpen");
                isServerOpen = Boolean.parseBoolean(val);
            }
            else {
                // 如果有外部配置文件
                String val = propertiesUtil.getValue("activemq.isServerOpen");
                // 如果从外部配置文件中取到
                if (!StrUtil.isEmpty(val)) {
                    isServerOpen = Boolean.parseBoolean(val);
                }
                else {
                    // 如果从外部配置文件中未取到，则从class path中取
                    Resource resource = new ClassPathResource("application.properties");
                    InputStream is = resource.getInputStream();
                    log.info("SystemListener: 载入class path中的application.properties");
                    propertiesUtil = new PropertiesUtil(is);
                    val = propertiesUtil.getValue("activemq.isServerOpen");
                    isServerOpen = Boolean.parseBoolean(val);
                }
            }
            log.info("activemq.isServerOpen: " + isServerOpen);
            if (!isServerOpen) {
                return;
            }

            /*Config config = Config.getInstance();
            boolean mqIsServerOpen = config.getBooleanProperty("mqIsServerOpen");
            if (!mqIsServerOpen) {
                return;
            }*/

            log.info("启动 ActiveMQ");
            // 关闭jmx
            System.setProperty("org.apache.activemq.broker.jmx.createConnector", "false");

            /*String mpServer = config.get("mqServer");
            int mqPort = config.getInt("mqPort");
            String mpUser = config.get("mqUser");
            String mpPwd = config.get("mqPwd");*/

            String server = propertiesUtil.getValue("activemq.server");
            String port = propertiesUtil.getValue("activemq.port");
            String user = propertiesUtil.getValue("activemq.user");
            String pwd = propertiesUtil.getValue("activemq.pwd");

            // 设置 ActiveMQ 消息服务器用于被客户端连接的 url 地址,实际开发中，地址应该在配置文件中可配置，不要写死
            String serviceURL = "tcp://" + server + ":" + port;
            // BrokerService 表示 ActiveMQ 服务，每一个 BrokerService 表示一个消息服务器实例
            // 如果想启动多个，只需要 start 多个不同端口的 BrokerService 即可

            // 如果启用jmx，则一台服务器上不能安装多个activemq，因为jmx的默认端口为1019，不能被多个mq占用
            // 会报：failed to start JMX connector Cannot bind to URL [rmi://localhost:1099/jmxrmi]: javax.naming.NameAlreadyBoundException: jmxrmi
            // 注释掉后仍会占用1099，需置org.apache.activemq.broker.jmx.createConnector为false，
            // 或者启动时调jvm参数 -Dorg.apache.activemq.broker.jmx.createConnector = false
            // brokerService.setUseJmx(true);//设置是否应将代理的服务公开到jmx中。默认是 true

            brokerService.addConnector(serviceURL); // 为指定地址添加新的传输连接器
            // 启动 ActiveMQ 服务，此时客户端便可以使用提供的地址进行连接，然后发送消息过来，或者从这里消费消息。
            // 注意：这里内嵌启动后，默认是没有提供 8161 端口的 web 管理界面的，照样能做消息中间件使用

            SimpleAuthenticationPlugin sap = new SimpleAuthenticationPlugin();
            AuthenticationUser au = new AuthenticationUser(user, pwd, "users");
            ArrayList list = new ArrayList();
            list.add(au);
            sap.setUsers(list); // 用户验证
            brokerService.setPlugins(new BrokerPlugin[] { sap });
            brokerService.setPersistent(false); // 持久化

/*
            // 内嵌式采用持久化时，JmsConfig中配置的 listener 收不到消息，独立部署的ActiveMQ则能收到
            brokerService.setPersistent(true); // 持久化
            // brokerService.setBrokerName("CwsBroker");
            // String mqDataPath = "/activemq"; // 存储位置
            // brokerService.getPersistenceAdapter().setDirectory(new File(mqDataPath));
            // 生成于D:\tomcat8.5.49\bin目录下
            File dataKahadbFile = new File("kahadb");
            KahaDBStore kahaDBStore = new KahaDBStore();
            kahaDBStore.setDirectory(dataKahadbFile);
            // kahaDBStore.setJournalDiskSyncInterval(1024 * 1000); // NoSuchMethodError: org.apache.activemq.store.kahadb.KahaDBStore.setJournalDiskSyncInterval(J)V
            // 当Metadata Cache中更新的索引到达了1000时，才同步到磁盘上的Metadata Store中
            kahaDBStore.setIndexWriteBatchSize(100);
            kahaDBStore.setEnableIndexWriteAsync(true);
            brokerService.setPersistenceAdapter(kahaDBStore);*/

            brokerService.start();

            // https://www.jianshu.com/p/074bb3e8fe02
/*            BrokerFactoryBean broker = new BrokerFactoryBean();
            Resource resource = new ClassPathResource("activemq.xml");
            broker.setConfig(resource);
            broker.setStart(true);*/

            // org.apache.xbean.spring.context.impl.URIEditor e;

            log.info("启动内嵌 ActiveMQ 服务器完成......");
        } catch (Exception e) {
            log.error("启动内嵌 ActiveMQ 服务器失败...");
            LogUtil.getLog(getClass()).error(e);
        }
    }

    /**
     * 应用销毁时自动执行
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            brokerService.stop();
        } catch (Exception e) {
            log.error("停止内嵌 ActiveMQ 服务器失败...");
            LogUtil.getLog(getClass()).error(e);
        }
        log.info("关闭内嵌 ActiveMQ 服务器成功......");
    }
}