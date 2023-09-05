package com.cloudweb.oa.utils;

import cn.js.fan.util.Properties;
import cn.js.fan.util.PropertiesUtil;
import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.cache.SysConfigCache;
import com.cloudweb.oa.entity.SysConfig;
import com.cloudweb.oa.service.ISysConfigService;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;

@Component
public class ConfigUtil implements IConfigUtil {

    @Autowired
    ISysConfigService sysConfigService;

    @Autowired
    SysConfigCache sysConfigCache;

    /**
     * 获取XML
     *
     * @param name
     * @return
     */
    @Override
    public String getXml(String name) {
        // 可能会传错成文件名，所以需判断去掉.xml
        int p = name.indexOf(".xml");
        if (p != -1) {
            name = name.substring(0, p);
        }

        SysConfig sysConfig = sysConfigCache.getSysConfig(name);
        // SysConfig sysConfig = sysConfigService.getSysConfig(name);
        if (sysConfig == null) {
            throw new IllegalArgumentException("Config:" + name + " is not exist in table: sys_config");
        }
        return sysConfig.getXml();
    }

    @Override
    public Document getDocument(String fileName) {
        Document doc = null;
        SAXBuilder sb = new SAXBuilder();
        try {
            // 如果不是以jar方式运行，则取classes目录下面的proxool.xml
            if (!isRunJar()) {
                URL cfgUrl = getClass().getResource("/" + fileName);
                String cfgPath = URLDecoder.decode(cfgUrl.getFile());
                File file = new File(cfgPath);
                doc = sb.build(file);
            } else {
                // 如果是jar方式运行，则"当前目录/config"下如果存在，则取当前目录下面的文件
                String cfgPath = getJarPath() + File.separator + "config" + File.separator + fileName;
                File file = new File(cfgPath);
                if (file.exists()) {
                    doc = sb.build(file);
                } else {
                    // 判断当前目录下是否存在config_sys.xml
                    cfgPath = getJarPath() + File.separator + fileName;
                    file = new File(cfgPath);
                    if (file.exists()) {
                        doc = sb.build(file);
                    } else {
                        InputStream inputStream = null;
                        // 如果当前目录下面不存在，则取classes目录下面的config_sys.xml，此时因在jar包中不能被修改
                        Resource resource = new ClassPathResource(fileName);
                        try {
                            inputStream = resource.getInputStream();
                            doc = sb.build(inputStream);
                        } catch (IOException e) {
                            LogUtil.getLog(getClass()).error(e);
                        } finally {
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    LogUtil.getLog(getClass()).error(e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JDOMException | IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return doc;
    }

    /**
     * 根据优先级获取文件
     *
     * @param fileName
     * @return
     */
    @Override
    public InputStream getFile(String fileName) {
        try {
            // 如果不是以jar方式运行，则取classes目录下面的proxool.xml
            if (!isRunJar()) {
                URL cfgUrl = getClass().getResource("/" + fileName);
                String cfgPath = URLDecoder.decode(cfgUrl.getFile());
                File file = new File(cfgPath);
                if (file.exists()) {
                    return new FileInputStream(cfgPath);
                }
            } else {
                // 如果是jar方式运行，则"当前目录/config"下如果存在，则取当前目录下面的文件
                String cfgPath = getJarPath() + File.separator + "config" + File.separator + fileName;
                File file = new File(cfgPath);
                if (file.exists()) {
                    return new FileInputStream(file);
                } else {
                    // 判断当前目录下是否存在config_sys.xml
                    cfgPath = getJarPath() + File.separator + fileName;
                    file = new File(cfgPath);
                    if (file.exists()) {
                        return new FileInputStream(file);
                    } else {
                        // 如果当前目录下面不存在，则取classes目录下面的config_sys.xml，此时因在jar包中不能被修改
                        Resource resource = new ClassPathResource(fileName);
                        try {
                            return resource.getInputStream();
                        } catch (IOException e) {
                            LogUtil.getLog(getClass()).error(e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return null;
    }

    /**
     * 判断是否以jar方式运行
     * @return
     */
    @Override
    public boolean isRunJar() {
        URL url = ConfigUtil.class.getResource("");
        String protocol = url.getProtocol();
        return CommonConstUtil.RUN_MODE_JAR.equals(protocol);
    }

    public static String getJarPath() {
        // 需启动Spring初始化后才能获取
        /*ApplicationHome home = new ApplicationHome(ConfigUtil.class);
        //返回的是 d:\jxcg\c-main
        String path = home.getDir().getPath();
        int p = path.lastIndexOf(File.separator);
        if (p != -1) {
            path = path.substring(0, p);
        }
        return path;*/
        return System.getProperty("user.dir");
    }

    @Override
    public String getAppHome() {
        return getJarPath();
    }

    /**
     * 取得文件路径，用于：proxool.xml、license.dat、cache.ccf
     * @return
     */
    @Override
    public String getFilePath() {
        String cfgPath = "";
        // 如果不是以jar方式运行，则取classes目录下面的文件
        if (!isRunJar()) {
            URL cfgUrl = getClass().getResource("/");
            cfgPath = URLDecoder.decode(cfgUrl.getFile());
            if (cfgPath.endsWith("/")) {
                cfgPath = cfgPath.substring(0, cfgPath.length()-1);
            }
        } else {
            // 如果是jar方式运行，则"当前目录/config"下如果存在，则取当前目录下面的文件
            cfgPath = getJarPath() + File.separator + "config";
            File file = new File(cfgPath);
            if (!file.exists()) {
                // 判断当前目录下是否存在
                cfgPath = getJarPath();
            }
        }
        return cfgPath;
    }

    /**
     * 置application.properties中的参数
     * @param key
     * @param value
     */
    public void setApplicationProp(String key, String value) {
        String filePath = getFilePath();
        if ("".equals(filePath)) {
            LogUtil.getLog(getClass()).warn("Run by jar. application.properties is not found in config or user.dir.");
            return;
        }
        filePath += File.separator + "application.properties";
        PropertiesUtil propertiesUtil = new PropertiesUtil(filePath);
        propertiesUtil.setValue(key, value);
        try {
            propertiesUtil.saveFile(filePath);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    /**
     * 保存XML
     *
     * @param doc
     */
    @Override
    public void putXml(String name, Document doc) {
        // 可能会传错成文件名，所以需判断去掉.xml
        int p = name.indexOf(".xml");
        if (p != -1) {
            name = name.substring(0, p);
        }

        String indent = "    ";
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            outp.output(doc, os);
            String xml = os.toString("utf-8");

            SysConfig sysConfig = sysConfigService.getSysConfig(name);
            sysConfig.setXml(xml);
            sysConfigService.update(sysConfig);

            os.close();
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }
}
