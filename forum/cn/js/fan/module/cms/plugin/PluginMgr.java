package cn.js.fan.module.cms.plugin;

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
import cn.js.fan.module.cms.plugin.base.IPluginUnit;
import cn.js.fan.module.cms.ui.Skin;
import cn.js.fan.module.cms.Leaf;
import org.jdom.output.Format;
import java.net.URLDecoder;
import cn.js.fan.util.StrUtil;

public class PluginMgr {
    RMCache rmCache;
    final String group = "CMS_PLUGIN";
    final String ALLPLUGIN = "CMS_ALLPLUGIN";

    static Logger logger;
    public final String FILENAME = "cms_plugin.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public PluginMgr() {
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

    public PluginUnit getPluginUnit(String code) {
        PluginUnit pu = null;
        try {
            pu = (PluginUnit)rmCache.getFromGroup(code, group);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
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
                        String resource = child.getChildText("resource");
                        String adminEntrance = child.getChildText(
                                "adminEntrance");
                        String classUnit = child.getChildText("classUnit");
                        String type = child.getChildText("type");

                        Element e1 = child.getChild("page");
                        String addPage = StrUtil.getNullStr(e1.getChildText("add"));
                        String editPage = StrUtil.getNullStr(e1.getChildText("edit"));
                        String viewPage = StrUtil.getNullStr(e1.getChildText("view"));

                        Element skins = child.getChild("skins");
                        List skinlist = skins.getChildren();
                        Iterator skinir = skinlist.iterator();
                        Vector v = new Vector();
                        while (skinir.hasNext()) {
                            Element e = (Element) skinir.next();
                            Skin skin = new Skin();
                            skin.setCode(e.getAttributeValue("code"));
                            skin.setName(e.getChildText("name"));
                            skin.setAuthor(e.getChildText("author"));
                            skin.setPath(e.getChildText("path"));
                            v.addElement(skin);
                        }

                        pu = new PluginUnit(code);
                        pu.setResource(resource);
                        pu.setAdminEntrance(adminEntrance);
                        pu.setClassUnit(classUnit);
                        pu.setType(type);
                        pu.setSkins(v);
                        pu.setAddPage(addPage);
                        pu.setEditPage(editPage);
                        pu.setSkins(v);
                        pu.setViewPage(viewPage);
                        try {
                            rmCache.putInGroup(code, group,
                                               pu);
                        } catch (Exception e) {
                            logger.error("getPluginUnit:" + e.getMessage());
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

    public Vector getAllPlugin() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLPLUGIN, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (v==null) {
            v = new Vector();
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String code = child.getAttributeValue("code");
                    v.addElement(getPluginUnit(code));
                }
                try {
                    rmCache.putInGroup(ALLPLUGIN, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllPlugin:" + e.getMessage());
                }
            }
        }
        return v;
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
     * 取得对应于boardCode版面的所有PluginUnit，此处可以考虑加入缓存
     * @param boardCode String
     * @return Vector
     */
    public PluginUnit getPluginUnitOfDir(String dirCode) {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        if (lf.getPluginCode().equals(PluginUnit.DEFAULT))
            return null;
        else
            return getPluginUnit(lf.getPluginCode());
    }
}
