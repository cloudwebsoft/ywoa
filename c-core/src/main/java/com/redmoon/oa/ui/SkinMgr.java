package com.redmoon.oa.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;
import org.jdom.Document;
import java.io.FileOutputStream;

import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;
import cn.js.fan.cache.jcs.RMCache;
import org.jdom.output.Format;

import com.redmoon.oa.person.UserSet;

import java.net.URLDecoder;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import javax.servlet.http.HttpServletRequest;
import javax.swing.*;

import cn.js.fan.web.Global;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class SkinMgr {
    static final String group = "OA_SKIN";
    static final String ALLSKIN = "ALL_OA_SKIN";

    static final String FILENAME = "oa_skin.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public static final String SKIN_CODE_LTE = "lte";

    public static final String DEFAULT_SKIN_CODE = SKIN_CODE_LTE;

    public SkinMgr() {
        confURL = getClass().getResource("/" + FILENAME);
    }

    public static void init() {
        if (!isInited) {
            xmlPath = confURL.getPath();
            xmlPath = URLDecoder.decode(xmlPath);

            InputStream inputStream = null;
            SAXBuilder sb = new SAXBuilder();
            try {
                Resource resource = new ClassPathResource("oa_skin.xml");
                inputStream = resource.getInputStream();
                doc = sb.build(inputStream);
                root = doc.getRootElement();

                /*FileInputStream fin = new FileInputStream(xmlPath);
                doc = sb.build(fin);
                root = doc.getRootElement();
                fin.close();*/

                isInited = true;
            } catch (JDOMException | IOException e) {
                LogUtil.getLog(SkinMgr.class).error(e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LogUtil.getLog(SkinMgr.class).error(e);
                    }
                }
            }
        }
    }

    public void setDefaultSkin(String skinCode) {
        init();
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(skinCode)) {
                    child.setAttribute("default", "true");
                } else {
                    child.setAttribute("default", "false");
                }
            }
        }
        writemodify();
        reload();
        UserSet.defaultSkin = getDefaultSkinCode();
    }

    public Element getRootElement() {
        return root;
    }

    public static void reload() {
        isInited = false;
        try  {
        	RMCache.getInstance().invalidateGroup(group);
        }
        catch (Exception e) {
            LogUtil.getLog(SkinMgr.class).error(e.getMessage());
        }
    }

    /**
     * 用于页面中获取skin
     * @param code String
     * @return Skin
     */
    public Skin getSkin(String code) {
        if (code==null || code.equals(""))
            return getSkinByCode(getDefaultSkinCode());

        Skin sk = getSkinByCode(code);
        if (sk==null)
            sk = getSkinByCode(getDefaultSkinCode());
        return sk;
    }

    /**
     * 取得皮肤的路径
     * @param request HttpServletRequest
     * @param isPrefixRootPath 是否加上rootPath前缀
     * @return String
     */
    public static String getSkinPath(HttpServletRequest request, boolean isPrefixRootPath) {
        String skincode = ParamUtil.get(request, "skincode");
        if (skincode == null || "".equals(skincode)){
        	skincode = UserSet.getSkin(request);
    		if (skincode==null || "".equals(skincode)){
    			skincode = UserSet.defaultSkin;
    		}
        }
        
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isSpecified = "2".equals(cfg.get("styleMode"));
		// 如果风格为轻简型，则只能使用lte皮肤
		if (isSpecified) {
			int styleSpecified = StrUtil.toInt(cfg.get("styleSpecified"), -1);
			if (styleSpecified == ConstUtil.UI_MODE_LTE) {
			    if (isPrefixRootPath) {
                    return Global.getRootPath(request) + "/skin/lte";
                }
			    else {
			        return "/skin/lte";
                }
			}
		}
		else {
            AuthUtil authUtil = SpringUtil.getBean(AuthUtil.class);
            if (authUtil.isUserLogin(request)) {
                IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
                Privilege pvg = new Privilege();
                UserSetup userSetup = userSetupService.getUserSetup(pvg.getUser(request));
				if (userSetup.getUiMode() == ConstUtil.UI_MODE_LTE) {
				    if (isPrefixRootPath) {
                        return Global.getRootPath(request) + "/skin/lte";
                    }
				    else {
                        return "/skin/lte";
                    }
				}
			}
		}
        
        SkinMgr skm = new SkinMgr();
        Skin skin = skm.getSkin(skincode);
        if (isPrefixRootPath) {
            return Global.getRootPath(request) + "/" + skin.getPath();
        }
        else {
            return "/" + skin.getPath();
        }
    }

    public static String getSkinPath(HttpServletRequest request) {
        return getSkinPath(request, true);
    }

    private Skin getSkinByCode(String code) {
        Skin skin = null;
        try {
            skin = (Skin)RMCache.getInstance().getFromGroup(code, group);
        }
        catch (Exception e) {
            LogUtil.getLog(SkinMgr.class).error("getSkin:" + e.getMessage());
        }
        if (skin==null) {
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String isDisplay = child.getAttributeValue("isDisplay");
/*                    if ("false".equals(isDisplay)) {
                    	continue;
                    }*/
                    String ecode = child.getAttributeValue("code");
                    if (ecode.equals(code)) {
                        String name = child.getChildText("name");
                        String author = child.getChildText("author");
                        String path = child.getChildText(
                                "path");
                        String tableBorderColor = child.getChildText("tableBorderColor");
                        boolean defaultSkin = child.getAttributeValue("default").equals("true");

                        int topHeight = StrUtil.toInt(child.getChildText("topHeight"));
                        int menuHeight = StrUtil.toInt(child.getChildText("menuHeight"));
                        int bottomHeight = StrUtil.toInt(child.getChildText("bottomHeight"));
                        int leftWidth = StrUtil.toInt(child.getChildText("leftWidth"));
                        String leftMenuTopBtn = child.getChildText("leftMenuTopBtn");

                        skin = new Skin();
                        skin.setCode(code);
                        skin.setName(name);
                        skin.setAuthor(author);
                        skin.setPath(path);
                        skin.setTableBorderClr(tableBorderColor);
                        skin.setDefaultSkin(defaultSkin);

                        skin.setTopHeight(topHeight);
                        skin.setMenuHeight(menuHeight);
                        skin.setBottomHeight(bottomHeight);
                        skin.setLeftWidth(leftWidth);
                        skin.setLeftMenuTopBtn(leftMenuTopBtn);
                        try {
                        	RMCache.getInstance().putInGroup(code, group, skin);
                        } catch (Exception e) {
                            LogUtil.getLog(SkinMgr.class).error("getSkin:" + e.getMessage());
                        }
                        break;
                    }
                }
            }
        }

        return skin;
    }

    public String getDefaultSkinCode() {
        for (Skin sk : getAllSkin()) {
            if (sk.isDefaultSkin()) {
                return sk.getCode();
            }
        }
        return DEFAULT_SKIN_CODE;
    }

    public Vector<Skin> getAllSkin() {
        Vector<Skin> v = null;
        try {
            v = (Vector<Skin>) RMCache.getInstance().getFromGroup(ALLSKIN, group);
        } catch (Exception e) {
            LogUtil.getLog(SkinMgr.class).error(e.getMessage());
        }
        if (v==null) {
            v = new Vector<>();
            init();
            List list = root.getChildren();
            if (list != null) {
                for (Object o : list) {
                    Element child = (Element) o;
                    String isDisplay = child.getAttributeValue("isDisplay");
                    if ("false".equals(isDisplay)) {
                        continue;
                    }
                    String code = child.getAttributeValue("code");
                    v.addElement(getSkinByCode(code));
                }
                try {
                	RMCache.getInstance().putInGroup(ALLSKIN, group, v);
                }
                catch (Exception e) {
                    LogUtil.getLog(SkinMgr.class).error("getAllSkin:" + e.getMessage());
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

}
