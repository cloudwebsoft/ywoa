package com.redmoon.forum.plugin.dig;

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
import cn.js.fan.cache.jcs.RMCache;

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
public class DigSkin implements ISkin {
    static Logger logger = Logger.getLogger(DigSkin.class.getName());
    public static String resource = null;
    public static String code = DigUnit.code;

    static final String dig = "plugin.dig";

    public static final String DEFAULT_SKIN_CODE = "default";


    public DigSkin() {
    }

    public static Skin getSkin(String skinCode) {
        if (code==null || code.equals(""))
            return getSkinByCode(DEFAULT_SKIN_CODE);

        Skin sk = getSkinByCode(skinCode);
        if (sk==null)
            sk = getSkinByCode(DEFAULT_SKIN_CODE);
        return sk;
    }

    private static Skin getSkinByCode(String skinCode) {
        Skin sk = null;
        try {
            sk = (Skin) RMCache.getInstance().getFromGroup(skinCode, dig);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (sk == null) {
            PluginMgr pm = new PluginMgr();
            PluginUnit pu = pm.getPluginUnit(code);
            Iterator ir = pu.getSkins().iterator();
            while (ir.hasNext()) {
                Skin skin = (Skin) ir.next();
                if (skin.getCode().equals(skinCode)) {
                    sk = skin;
                    try {
                        RMCache.getInstance().putInGroup(skinCode, dig,
                                sk);
                    } catch (Exception e) {
                        logger.error("getSkinByCode:" + e.getMessage());
                    }
                    break;
                }
            }
        }
        return sk;
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
