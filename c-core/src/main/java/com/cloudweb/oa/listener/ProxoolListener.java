package com.cloudweb.oa.listener;

import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * 经测试，该类在内、外置Tomcat中运行时，都会被调用
 */
public class ProxoolListener implements ApplicationListener<ApplicationPreparedEvent> {
    private static boolean isProxoolConfigured = false;

    /**
     * 如果是implements ServletContextListener，则因加载顺序在Spring初始化Bean后，会报错
     * 而ApplicationListener是在bean初始化前启动监听器
     */

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent applicationPreparedEvent) {
        // 会被调用多次，可能是因为引入了spring cloud使可刷新配置的原因
        if (!isProxoolConfigured) {
            try {
                //单元测试未启动web service,所以要自已加载proxool configurator
                String path = getClass().getResource("/").getPath();
                path = path + "../proxool.xml";
                JAXPConfigurator.configure(path, false);

                isProxoolConfigured = true;
            } catch (ProxoolException e) {
                e.printStackTrace();
            }
        }
    }
}
