package com.redmoon.forum.plugin.sweet;

import org.apache.log4j.Logger;
import cn.js.fan.web.SkinUtil;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ResBundle;
import com.redmoon.forum.plugin.PluginUnit;
import com.redmoon.forum.plugin.PluginMgr;
import cn.js.fan.base.ISkin;
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
public class SweetSkin implements ISkin {
    Logger logger = Logger.getLogger(SweetSkin.class.getName());
    public static String resource = null;
    public static final String code = "sweet";

    public SweetSkin() {
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
        else
            return rb.get(key);
    }

    public String LoadStr(HttpServletRequest request, String key) {
        return LoadString(request, key);
    }

}
