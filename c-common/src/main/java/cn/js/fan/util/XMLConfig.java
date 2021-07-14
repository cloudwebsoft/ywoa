package cn.js.fan.util;

import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;
import java.io.*;
import java.net.URL;
import cn.js.fan.util.XMLProperties;
import org.apache.log4j.Logger;
import cn.js.fan.util.StrUtil;
import java.net.URLDecoder;

public class XMLConfig {
    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private String filePath;
    Logger logger;
    Document doc = null;
    Element root = null;

    String rootChild = "";
    String encoding = "gb2312";

    public XMLConfig(String filePath, boolean isRealPath, String encoding) {
        this.encoding = encoding;
        this.filePath = filePath;
        if (!isRealPath) {
            URL cfgURL = getClass().getResource("/" + filePath);
            this.filePath = cfgURL.getFile();
            this.filePath = URLDecoder.decode(this.filePath);
        }

        // System.out.println(getClass().getName() + " " + this.filePath);

        File file = new File(this.filePath);

        logger = Logger.getLogger(XMLConfig.class.getName());

        SAXBuilder sb = new SAXBuilder();
        try {
            doc = sb.build(file);
            root = doc.getRootElement();
            properties = new XMLProperties(file, doc);
        } catch (org.jdom.JDOMException e) {
            logger.error("XMLConfig:" + e.getMessage());
        } catch (java.io.IOException e) {
            logger.error("XMLConfig:" + e.getMessage());
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
        } else
            return -65536;
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
        if (which == null)
            return null;
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
        if (which == null)
            return false;
        which.setText(value);
        writemodify();
        return true;
    }

    public void writemodify() {
        String indent = "    ";
        boolean newLines = true;
        // XMLOutputter outp = new XMLOutputter(indent, newLines, "gb2312");
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding(encoding);
        System.out.println("XMLConfig.java writemodify:encoding=" + encoding + " filePath=" + filePath);

        XMLOutputter outputter = new XMLOutputter(format);

        try {
            FileOutputStream fout = new FileOutputStream(filePath);
            outputter.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {
            System.out.println("XMLConfig.java writemodify:" + e.getMessage());
        }
    }

    public Element getRoot() {
        return root;
    }
}
