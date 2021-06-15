package com.redmoon.forum.plugin;

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
import com.redmoon.forum.plugin.base.IPluginUnit;
import com.redmoon.forum.ui.Skin;
import org.jdom.output.Format;
import java.net.URLDecoder;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ErrMsgException;

/**
 *
 * <p>Title: 插件管理</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PluginMgr {
    RMCache rmCache;
    static final String group = "PLUGIN";
    final String ALLPLUGIN = "ALLPLUGIN";

    static Logger logger;
    public final String FILENAME = "plugin.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public PluginMgr() {
        rmCache = RMCache.getInstance();

        logger = Logger.getLogger(this.getClass().getName());
        confURL = getClass().getResource("/" + FILENAME);
    }

    public static void init() {
        if (!isInited) {
            xmlPath = confURL.getPath();
            xmlPath = URLDecoder.decode(xmlPath);

            // System.out.println(PluginMgr.class.getName() + " xmlPath2=" + xmlPath);

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

    public static void reload() {
        isInited = false;
        try {
            RMCache.getInstance().invalidateGroup(group);
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
                        String button = child.getChildText("button");
                        String renderCode = child.getChildText("renderCode");
                        boolean isShowName = child.getChildText("isShowName").equals("true");

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
                        Element e1 = child.getChild("page");
                        String addTopicPage = StrUtil.getNullStr(e1.getChildText("addtopic"));
                        String editTopicPage = StrUtil.getNullStr(e1.getChildText("edittopic"));
                        String replyTopicPage = StrUtil.getNullStr(e1.getChildText("addreply"));
                        String showTopicPage = StrUtil.getNullStr(e1.getChildText("showtopic"));
                        String userCenterPage = StrUtil.getNullStr(e1.getChildText("usercenter"));
                        String userInfoPage = StrUtil.getNullStr(e1.
                                getChildText("userinfo"));

                        pu = new PluginUnit(code);
                        pu.setResource(resource);
                        pu.setAdminEntrance(adminEntrance);
                        pu.setClassUnit(classUnit);
                        pu.setType(type);
                        pu.setSkins(v);
                        pu.setAddTopicPage(addTopicPage);
                        pu.setEditTopicPage(editTopicPage);
                        pu.setAddReplyPage(replyTopicPage);
                        pu.setButton(button);
                        pu.setRenderCode(renderCode);
                        pu.setShowTopicPage(showTopicPage);
                        pu.setShowName(isShowName);
                        pu.setUserCenterPage(userCenterPage);
                        pu.setUserInfoPage(userInfoPage);
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
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        // XMLOutputter outp = new XMLOutputter(indent, newLines, "utf-8");
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
    public Vector getAllPluginUnitOfBoard(String boardCode) {
        Vector v = null;
        try {
            v = (Vector)rmCache.getFromGroup("all_plugin_" + boardCode, group);
        }
        catch (Exception e) {
            logger.error("getAllPluginUnitOfBoard1:" + e.getMessage());
        }
        if (v==null) {
            v = new Vector();
            Vector vplugin = getAllPlugin();
            if (vplugin.size() > 0) {
                Iterator irplugin = vplugin.iterator();
                while (irplugin.hasNext()) {
                    PluginUnit pu = (PluginUnit) irplugin.next();

                    IPluginUnit ipu = pu.getUnit();
                    if (ipu == null)
                        continue;
                    // 如果该插件为通用型
                    if (pu.getType().equals(pu.TYPE_FORUM)) {
                        v.addElement(pu);
                        continue;
                    }
                    if (ipu.isPluginBoard(boardCode)) {
                        v.addElement(pu);
                    }
                }
            }
            try {
                rmCache.putInGroup("all_plugin_" + boardCode, group, v);
            }
            catch (Exception e) {
                logger.error("getAllPluginUnitOfBoard2:" + e.getMessage());
            }
        }
        return v;
    }

    public void set(String code, String property, String textValue) {
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.listIterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(code)) {
                    List list1 = child.getChildren();
                    if (list1 != null) {
                        Iterator ir1 = list1.listIterator();
                        while (ir1.hasNext()) {
                            Element childContent = (Element) ir1.next();
                            System.out.println(getClass() + " name=" + childContent.getName() + " " + property);
                            if (childContent.getName().equals(property)) {
                                childContent.setText(textValue);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    public void delPluginUnit(String code) {
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.listIterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(code)) {
                    root.removeContent(child);
                    writemodify();
                    reload();
                    break;
                }
            }
        }
    }

}
