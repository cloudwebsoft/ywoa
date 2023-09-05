package com.cloudweb.oa.config;

import com.cloudwebsoft.framework.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;

/**
 * 用于内置Tomcat运行时，启动后自动打开IE
 */
@Component
public class MyCommandRunner implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(MyCommandRunner.class);

    @Value("${spring.web.loginUrl}")
    private String loginUrl;

    @Value("${spring.web.browser}")
    private String browser;

    @Value("${spring.web.chromePath}")
    private String chromePath;

    @Value("${spring.web.autoOpen}")
    private boolean isOpen;

    @Override
    public void run(String... args) throws Exception {
        if (isOpen) {
            if ("chrome".equalsIgnoreCase(browser)) {
                String cmd = chromePath + " " + loginUrl;
                Runtime run = Runtime.getRuntime();
                try {
                    run.exec(cmd);
                    logger.debug("启动浏览器打开项目成功");
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
            else {
                try {
                    // 打开IE
                    Runtime.getRuntime().exec("C:\\Program Files\\Internet Explorer\\iexplore.exe " + loginUrl);
                    // 打开默认浏览器
                    // Runtime.getRuntime().exec("cmd /c start " + loginUrl);
                } catch (Exception ex) {
                    LogUtil.getLog(getClass()).error(ex);
                }
            }
        }
    }
}