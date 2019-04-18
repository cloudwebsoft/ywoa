package cn.js.fan.module.cms.ui;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.cache.jcs.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import org.apache.log4j.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

public class DesktopMgr {
    RMCache rmCache;
    final String group = "DESKTOP";
    final String ALLRENDER = "ALLDESKTOP";

    static Logger logger;
    public final String FILENAME = "config_desktop.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public DesktopMgr() {
        rmCache = RMCache.getInstance();
        logger = Logger.getLogger(this.getClass().getName());
        confURL = getClass().getClassLoader().getResource("/" + FILENAME);
    }

    public static void init() {
        if (!isInited) {
            xmlPath = confURL.getPath();
            xmlPath = URLDecoder.decode(xmlPath);

            SAXBuilder sb = new SAXBuilder();
            try {
                FileInputStream fin = new FileInputStream(xmlPath);
                doc = sb.build(fin);
                root = doc.getRootElement();
                fin.close();
                isInited = true;
            } catch (org.jdom.JDOMException e) {
                logger.error(e.getMessage());
            } catch (java.io.IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public Element getRootElement() {
        return root;
    }

    public void reload() {
        isInited = false;
        try  {
            rmCache.invalidateGroup(group);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public DesktopUnit getDesktopUnit(String code) {
        DesktopUnit pu = null;
        try {
            pu = (DesktopUnit)rmCache.getFromGroup(code, group);
        }
        catch (Exception e) {
            logger.error("getDesktopUnit:" + e.getMessage());
        }
        if (pu==null) {
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String ecode = child.getAttributeValue("code");
                    if (ecode.equals(code)) {
                        String name = child.getChildText("name");
                        String pageList = child.getChildText(
                                "pageList");
                        String pageShow = child.getChildText(
                                "pageShow");
                        String className = child.getChildText("className");
                        String type = child.getChildText("type");
                        pu = new DesktopUnit(code);
                        pu.setName(name);
                        pu.setPageList(pageList);
                        pu.setPageShow(pageShow);
                        pu.setClassName(className);
                        pu.setType(type);
                        try {
                            rmCache.putInGroup(code, group,
                                               pu);
                        } catch (Exception e) {
                            logger.error("getDesktopUnit:" + e.getMessage());
                        }
                        return pu;
                    }
                }
            }
        }
        else {
            pu.renew();
        }
        return pu;
    }

    public void writemodify() {
        String indent = "    ";
        boolean newLines = true;
        // XMLOutputter outp = new XMLOutputter(indent, newLines, "utf-8");
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(xmlPath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
    }


    /**
     * 生成单个页面
     * @param request HttpServletRequest
     * @param doc Document
     * @param pageNum int
     */
    public void createHtml(HttpServletRequest request, String systemCode
                           ) throws ErrMsgException {
        String filePath = Global.getRealPath() + "doc/template/index.htm";
        try {
            // LogUtil.getLog(getClass()).info("createHtml1:id=" + doc.getID() + " CPages=" + pageNum);
            TemplateLoader tl = new TemplateLoader(request, filePath);
            FileUtil fu = new FileUtil();
            File f = new File(Global.getRealPath() + "index.htm");
            fu.WriteFile(Global.getRealPath(),
                         tl.toString(), "UTF-8");
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("createHtml:" + e.getMessage());
        }
    }
}
