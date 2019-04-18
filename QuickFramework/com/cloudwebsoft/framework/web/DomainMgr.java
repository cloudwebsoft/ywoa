package com.cloudwebsoft.framework.web;

import java.io.FileInputStream;
import java.net.URL;
import org.jdom.Document;
import java.io.FileOutputStream;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import org.apache.log4j.Logger;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;
import cn.js.fan.cache.jcs.RMCache;
import org.jdom.output.Format;
import java.net.URLDecoder;
import cn.js.fan.util.StrUtil;

/**
 *
 * <p>Title: 管理域名，详见domain.xml</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DomainMgr {
    RMCache rmCache;
    final String group = "Domain";
    final String ALLREDIRECT = "ALLDomain";

    static Logger logger;
    public final String FILENAME = "domain.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public DomainMgr() {
        rmCache = RMCache.getInstance();

        logger = Logger.getLogger(this.getClass().getName());
        confURL = getClass().getClassLoader().getResource(FILENAME);
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

    public DomainUnit getDomainUnit(String subDomain) {
        DomainUnit pu = null;
        try {
            pu = (DomainUnit)rmCache.getFromGroup(subDomain, group);
        }
        catch (Exception e) {
            logger.error("getDomainUnit:" + e.getMessage());
        }
        if (pu==null) {
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String sd = child.getAttributeValue("subDomain");
                    if (sd.equals(subDomain)) {
                        String className = StrUtil.getNullStr(child.getChildText("className"));
                        boolean used = child.getChildText("isUsed").equals("true");
                        String url = StrUtil.getNullStr(child.getChildText("url"));
                        String exclude = StrUtil.getNullStr(child.getChildText("exclude"));
                        boolean regMatch = child.getChildText("isRegexMatch").equals("true");
                        boolean redirect = child.getChildText("isRedirect").equals("true");
                        pu = new DomainUnit(subDomain);
                        pu.setClassName(className);
                        pu.setUsed(used);
                        pu.setUrl(url);
                        pu.setExclude(exclude);
                        pu.setExcludeSubDomains(StrUtil.split(exclude, ","));
                        pu.setRegexMatch(regMatch);
                        pu.setRedirect(redirect);
                        try {
                            rmCache.putInGroup(subDomain, group,
                                               pu);
                        } catch (Exception e) {
                            logger.error("getDomainUnit:" + e.getMessage());
                        }
                        return pu;
                    }
                }
            }
        }
        return pu;
    }

    public Vector getAllDomainUnit() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLREDIRECT, group);
        } catch (Exception e) {
            logger.error("getAllDomainUnit:" + e.getMessage());
        }
        if (v==null) {
            v = new Vector();
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String subDomain = child.getAttributeValue("subDomain");
                    v.addElement(getDomainUnit(subDomain));
                }
                try {
                    rmCache.putInGroup(ALLREDIRECT, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllDomainUnit:" + e.getMessage());
                }
            }
        }
        return v;
    }

    public void writemodify() {
        String indent = "    ";
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
}
