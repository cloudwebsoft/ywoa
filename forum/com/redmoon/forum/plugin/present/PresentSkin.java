package com.redmoon.forum.plugin.present;

import org.apache.log4j.Logger;
import cn.js.fan.web.SkinUtil;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ResBundle;
import com.redmoon.forum.plugin.PluginUnit;
import com.redmoon.forum.plugin.PluginMgr;
import cn.js.fan.base.ISkin;
import com.redmoon.forum.ui.*;
import java.util.Iterator;

/**
 *
 * <p>Title: 杂项的显示</p>
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
public class PresentSkin implements ISkin {
    static Logger logger = Logger.getLogger(PresentSkin.class.getName());
    public static String resource = null;
    public static String code = PresentUnit.code;

    public PresentSkin() {
    }

    public static Skin getSkin(String skinCode) {
        PluginMgr pm = new PluginMgr();
        PluginUnit pu = pm.getPluginUnit(code);
        Iterator ir = pu.getSkins().iterator();
        while (ir.hasNext()) {
            Skin skin = (Skin)ir.next();
            if (skin.getCode().equals(skinCode))
                return skin;
        }
        return null;
    }

    public static String getSkinPath(String skinCode) {
        Skin skin = getSkin(skinCode);
        if (skin==null)
            return "default";
        else
            return skin.getPath();
    }

     public static String getResource() {
        if (resource==null) {
            PluginMgr pm = new PluginMgr();
            PluginUnit pu = pm.getPluginUnit(code);
            return pu.getResource();
        }
        return resource;
    }

    public static String LoadString(HttpServletRequest request, String key) {
        Locale locale = SkinUtil.getLocale(request);
        ResBundle rb = new ResBundle(getResource(), locale);
        if (rb == null)
            return "";
        else {
            String str = "";
            try {
                str = rb.get(key);
            }
            catch (Exception e) {
                logger.error("LoadString:" + key + " " + e.getMessage());
            }
            return str;
        }
    }

    public String LoadStr(HttpServletRequest request, String key) {
        return LoadString(request, key);
    }
}
