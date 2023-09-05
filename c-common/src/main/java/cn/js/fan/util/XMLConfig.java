package cn.js.fan.util;

import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.utils.CommonConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;
import java.io.*;
import java.net.URL;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

import java.net.URLDecoder;

public class XMLConfig {
    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private String fileName;
    Document doc = null;
    Element root = null;

    String rootChild = "";
    String encoding = "gb2312";

    boolean isRealPath = false;

    boolean isJar = true;

    /**
     *
     * @param fileName
     * @param isRealPath 是否为真实路径，如为true，则在war包运行环境下，取classes目录 中的文件，如为false，则取数据库中的xml
     * @param encoding
     */
    public XMLConfig(String fileName, boolean isRealPath, String encoding) {
        this.encoding = encoding;
        this.isRealPath = isRealPath;
        this.fileName = fileName;

        if (isRealPath) {
            URL url = getClass().getResource("");
            String protocol = url.getProtocol();
            isJar = CommonConstUtil.RUN_MODE_JAR.equals(protocol);
            if (!isJar) {
                URL cfgURL = getClass().getResource("/" + fileName);
                String filePath = URLDecoder.decode(cfgURL.getFile());
                File file = new File(filePath);
                SAXBuilder sb = new SAXBuilder();
                try {
                    doc = sb.build(file);
                    root = doc.getRootElement();
                    properties = new XMLProperties(file, doc);
                } catch (JDOMException | IOException e) {
                    LogUtil.getLog(getClass()).error("XMLConfig:" + e.getMessage());
                }
            }
            else {
                this.fileName = fileName;
                InputStream inputStream = null;
                SAXBuilder sb = new SAXBuilder();
                try {
                    Resource resource = new ClassPathResource(fileName);
                    inputStream = resource.getInputStream();
                    doc = sb.build(inputStream);
                    root = doc.getRootElement();
                    properties = new XMLProperties(fileName, doc);
                } catch (JDOMException | IOException e) {
                    LogUtil.getLog(getClass()).error("XMLConfig:" + e.getMessage());
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
        else {
            int p = fileName.indexOf(".xml");
            if (p != -1) {
                this.fileName = fileName.substring(0, p);
            } else {
                this.fileName = fileName;
            }
            try {
                IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
                String xml = configUtil.getXml(fileName);

                SAXBuilder sb = new SAXBuilder();
                doc = sb.build(new InputSource(new StringReader(xml)));
                root = doc.getRootElement();
                properties = new XMLProperties(fileName, doc, true);
            } catch (JDOMException | IOException e) {
                LogUtil.getLog(getClass()).error("XMLConfig:" + e.getMessage());
            }
        }
    }

    public void setRootChild(String rootChild) {
        this.rootChild = rootChild;
    }

    public Element getRootElement() {
        return root;
    }

    public String get(String name) {
        return properties.getProperty(name);
    }

    public int getInt(String name) {
        String p = get(name);
        if (StrUtil.isNumeric(p)) {
            return Integer.parseInt(p);
        } else {
            return -65536;
        }
    }

    public void set(String name, String value) {
        properties.setProperty(name, value);
    }

    /**
     * 取出类似于config_forum.xml格式的配置文件中相应的描述
     * @param name String
     * @return String
     */
    public String getDescription(String name) {
        Element which = root.getChild(rootChild).getChild(name);
        if (which == null) {
            return null;
        }
        return which.getAttribute("desc").getValue();
    }

    /**
     * 用于设置类似于config_forum.xml格式的配置文件
     * @param name String
     * @param value String
     * @return boolean
     */
    public boolean put(String name, String value) {
        Element which = root.getChild(rootChild).getChild(name);
        if (which == null) {
            return false;
        }
        which.setText(value);
        writemodify();
        return true;
    }

    public void writemodify() {
        if (!isRealPath) {
            IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
            configUtil.putXml(fileName, doc);
        }
        else {
            if (!isJar) {
                URL cfgUrl = getClass().getResource("/" + fileName);
                String filePath = URLDecoder.decode(cfgUrl.getFile());

                String indent = "    ";
                Format format = Format.getPrettyFormat();
                format.setIndent(indent);
                format.setEncoding(encoding);
                XMLOutputter outp = new XMLOutputter(format);
                try {
                    FileOutputStream fout = new FileOutputStream(filePath);
                    outp.output(doc, fout);
                    fout.close();
                } catch (java.io.IOException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
            else {
                LogUtil.getLog(getClass()).error("jar中不能修改xml文件");
            }
        }
    }

    public Element getRoot() {
        return root;
    }
}
