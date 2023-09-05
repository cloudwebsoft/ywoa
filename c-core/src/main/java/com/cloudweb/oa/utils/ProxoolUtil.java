package com.cloudweb.oa.utils;

import cn.js.fan.util.XMLProperties;
import com.cloudwebsoft.framework.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;

@Slf4j
@Component
public class ProxoolUtil {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "proxool.xml";

    private String cfgPath;

    Document doc = null;
    Element root = null;

    public ProxoolUtil() {
        try {
            SAXBuilder sb = new SAXBuilder();
            // 置config_sys.xml文件所在目录的优先级
            URL url = getClass().getResource("");
            String protocol = url.getProtocol();
            // 如果不是以jar方式运行，则取classes目录下面的proxool.xml
            if (!CommonConstUtil.RUN_MODE_JAR.equals(protocol)) {
                URL cfgUrl = getClass().getResource("/" + CONFIG_FILENAME);
                cfgPath = URLDecoder.decode(cfgUrl.getFile());
                File file = new File(cfgPath);
                doc = sb.build(file);
                root = doc.getRootElement();
                properties = new XMLProperties(file, doc);
            }
            else {
                // 如果是jar方式运行，则"当前目录/config"下如果存在，则取当前目录下面的文件
                cfgPath = ConfigUtil.getJarPath() + File.separator + "config" + File.separator + CONFIG_FILENAME;
                File file = new File(cfgPath);
                if (file.exists()) {
                    doc = sb.build(file);
                    root = doc.getRootElement();
                    properties = new XMLProperties(file, doc);
                }
                else {
                    // 判断当前目录下是否存在config_sys.xml
                    cfgPath = ConfigUtil.getJarPath() + File.separator + CONFIG_FILENAME;
                    file = new File(cfgPath);
                    if (file.exists()) {
                        doc = sb.build(file);
                        root = doc.getRootElement();
                        properties = new XMLProperties(file, doc);
                    }
                    else {
                        InputStream inputStream = null;
                        // 如果当前目录下面不存在，则取classes目录下面的config_sys.xml，此时因在jar包中不能被修改
                        Resource resource = new ClassPathResource(CONFIG_FILENAME);
                        try {
                            inputStream = resource.getInputStream();
                            doc = sb.build(inputStream);
                            root = doc.getRootElement();
                            properties = new XMLProperties(CONFIG_FILENAME, doc);
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
            log.error("Config:" + e.getMessage());
        }
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public Document getDoc() {
        return doc;
    }

    public Element getRoot() {
        return root;
    }

    public String getCfgPath() {
        return cfgPath;
    }

    public void write() {
        String indent = "    ";
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(cfgPath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }
}
