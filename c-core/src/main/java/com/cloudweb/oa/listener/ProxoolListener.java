package com.cloudweb.oa.listener;

import com.cloudweb.oa.utils.CommonConstUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.sys.DebugUtil;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
                // 因为listener优先级高，此时ConfigUtil中调用数据库取datasource时，通过SpringUtil获取，而此时该类尚未被初始化到
                /*ConfigUtil configUtil = new ConfigUtil();
                String xml = configUtil.getXml("proxool");
                org.logicalcobwebs.proxool.configuration.JAXPConfigurator.configure(new InputSource(new StringReader(xml)), false);*/

                // 判断是否在jar文件中运行
                URL url = getClass().getResource("");
                String protocol = url.getProtocol();
                // 如果不是以jar方式运行，则取classes目录下面的proxool.xml
                if (!CommonConstUtil.RUN_MODE_JAR.equals(protocol)) {
                    String path = getClass().getResource("/").getPath() + "proxool.xml";
                    JAXPConfigurator.configure(path, false);
                }
                else {
                    // 如果是jar方式运行，则"当前目录/config"下如果存在，则取当前目录下面的文件
                    String jarPath = System.getProperty("user.dir");
                    LogUtil.getLog(getClass()).info("onApplicationEvent jarPath: " + jarPath);

                    String curPath = jarPath + File.separator + "config" + File.separator + "proxool.xml";
                    File file = new File(curPath);
                    if (file.exists()) {
                        JAXPConfigurator.configure(curPath, false);
                    }
                    else {
                        // 判断当前目录下是否存在proxool.xml
                        curPath = jarPath + File.separator + "proxool.xml";
                        file = new File(curPath);
                        if (file.exists()) {
                            JAXPConfigurator.configure(curPath, false);
                        }
                        else {
                            // 如果当前目录下面不存在，则取classes目录下面的proxool.xml，此时因在jar包中不能被修改
                            Resource resource = new ClassPathResource("proxool.xml");
                            try {
                                JAXPConfigurator.configure(new InputSource(resource.getInputStream()), false);
                            } catch (IOException e) {
                                LogUtil.getLog(getClass()).error(e);
                            }
                        }
                    }
                }

                isProxoolConfigured = true;
            } catch (ProxoolException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
    }
}
