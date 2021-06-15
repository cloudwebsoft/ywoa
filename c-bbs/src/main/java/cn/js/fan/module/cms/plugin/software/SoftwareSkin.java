package cn.js.fan.module.cms.plugin.software;

import org.apache.log4j.Logger;
import cn.js.fan.web.SkinUtil;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ResBundle;
import cn.js.fan.base.ISkin;
import cn.js.fan.module.cms.plugin.PluginMgr;
import cn.js.fan.module.cms.plugin.PluginUnit;

/**
 *
 * <p>Title: </p>
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
public class SoftwareSkin implements ISkin {
    static Logger logger = Logger.getLogger(SoftwareSkin.class.getName());
    public static String resource = null;
    public static String code = SoftwareUnit.code;

    public SoftwareSkin() {
    }

/*
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
*/
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
