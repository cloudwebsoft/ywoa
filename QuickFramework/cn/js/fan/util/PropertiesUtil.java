package cn.js.fan.util;

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;
import javax.servlet.jsp.*;
import java.util.Set;

public class PropertiesUtil {
    private String fileName;
    private Properties p;
    private FileInputStream in;
    private FileOutputStream out;
    String charset = "gb2312";

    /**
     * 根据传进的文件名载入文件
     * @param fileName String
     */
    public PropertiesUtil(String fileName, String charset) {
        this.fileName = fileName;
        this.charset = charset;
        File file = new File(fileName);
        try {
            in = new FileInputStream(file);
            p = new Properties(charset);
            // 载入文件
            p.load(in, charset);
            in.close();
        }
        catch (FileNotFoundException e) {
            System.err.println("配置文件config.properties找不到！");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.err.println("读取配置文件config.properties错误！");
            e.printStackTrace();
        }
    }

    public PropertiesUtil(String fileName) {
        this.fileName = fileName;
        File file = new File(fileName);
        try {
            in = new FileInputStream(file);
            p = new Properties(charset);
            // 载入文件
            p.load(in);
            in.close();
        }
        catch (FileNotFoundException e) {
            System.err.println("配置文件config.properties找不到！");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.err.println("读取配置文件config.properties错误！");
            e.printStackTrace();
        }
    }

    /**
     * 配置文件一律为config.propertities，并且统一放在web应用的根目录下。
     * @return String
     */
    public static String getConfigFile(HttpServlet hs) {
        return getConfigFile(hs, "config.properties");
    }

    /**
     * 在servlet中使用,直接用this作为参数,HttpServlet类型
     * 根据配置文件名从当前web应用的根目录下找出配置文件
     * @param hs HttpServlet
     * @param configFileName String配置文件名字
     * @return String
     */
    public static String getConfigFile(HttpServlet hs, String configFileName) {
        String configFile = "";
        ServletContext sc = hs.getServletContext();
        configFile = sc.getRealPath("/" + configFileName);
        if (configFile == null || configFile.equals("")) {
            configFile = "/" + configFileName;
        }
        return configFile;
    }

    /**
     * jsp中用pageContext作参数
     * @param hs PageContext
     * @param configFileName String 配置文件名字
     * @return String
     */
    public static String getConfigFile(PageContext hs, String configFileName) {
        String configFile = "";
        ServletContext sc = hs.getServletContext();
        configFile = sc.getRealPath("/" + configFileName);
        if (configFile == null || configFile.equals("")) {
            configFile = "/" + configFileName;
        }
        return configFile;
    }

    public Set getKeys() {
        return p.keySet();
    }

    /**
     * 指定配置项名称，返回配置值
     * @param itemName String
     * @return String
     */
    public String getValue(String itemName) {
        String str = "";
        try {
            str = new String(p.getProperty(itemName).getBytes("ISO8859_1"), charset);
        }
        catch (Exception e) {
            System.out.println("PropertiesUtil: getValue " + e.getMessage());
        }
        return str;
    }

    /**
     * 指定配置项名称和默认值，返回配置值
     * @param itemName String
     * @param defaultValue String
     * @return String
     */
    public String getValue(String itemName,
                           String defaultValue) {
        return p.getProperty(itemName, defaultValue);
    }

    /**
     * 设置配置项名称及其值
     * @param itemName String
     * @param value String
     */
    public void setValue(String itemName, String value) {
        try {
            value = new String(value.getBytes(charset), "ISO8859_1");
        }
        catch (Exception e) {
            System.out.println("PropertiesUtil: setValue " + e.getMessage());
        }
        p.setProperty(itemName, value);
        return;
    }

    /**
     * 保存配置文件，指定文件名和抬头描述
     * @param fileName String
     * @param description String
     * @throws Exception
     */
    public void saveFile(String fileName, String description) throws Exception {
        try {
            File f = new File(fileName);
            out = new FileOutputStream(f);
            p.store(out, description); // 保存文件
            out.close();
        }
        catch (IOException ex) {
            throw new Exception
                    ("无法保存指定的配置文件:" + fileName);
        }
    }

    /**
     * 保存配置文件，指定文件名
     * @param fileName String
     * @throws Exception
     */
    public void saveFile(String fileName)
            throws Exception {
        saveFile(fileName, "");
    }
}
