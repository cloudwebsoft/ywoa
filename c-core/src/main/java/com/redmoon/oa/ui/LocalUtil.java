package com.redmoon.oa.ui;

import cn.js.fan.util.ResBundle;
import cn.js.fan.web.Global;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sms.QxtMasMobileMsgUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

public class LocalUtil {
    public static final String OA_SESSION_LOCALE = "oa_locale";
    static final String resName = "res.common";
    
    public static String LoadString(HttpServletRequest request, String key) {
        return LoadString(request, resName, key);
    }
    public static String LoadString(HttpServletRequest request, String resource, String key) {
        String str = "";
        try {
            Locale locale = null;
            if (request==null) {
                locale = Global.locale;
            } else {
                locale = getLocale(request);
            }
            ResBundle rb = new ResBundle(resource, locale);
            str = rb.get(key);
        }
        catch (Exception e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(LocalUtil.class).error("LoadString: resource=" + resource + " key=" + key + " " + e.getMessage());
            com.cloudwebsoft.framework.util.LogUtil.getLog(LocalUtil.class).error(e);
        }
        return str;
    }
    

    public static Locale getLocale(HttpServletRequest request) {
        if (Global.localeSpecified) {
            return Global.getLocale();
        }
        if (request==null) {
             return Global.locale;
        }
        Locale locale = null;
       // HttpSession session = request.getSession(true);
       // Locale locale = (Locale) session.getAttribute(OA_SESSION_LOCALE);
       // if (locale != null) {
       //     return locale;
       // }
        // IE中为zh-cn，firefox中为zh-cn,zh;q=0.5 遨游好象不发送这个Accept-Language
        //String str = request.getHeader("Accept-Language"); // zh-cn

        // 不可用，因为在微信端没有经过spring security进行权限控制
        // String userName = SpringUtil.getUserName();

        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);

        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(userName);
        String str = userSetup.getLocal();
        
        if(str.equals("")){
        	str = "zh-CN";
        }
        
        String lang = null;
        String country = null;
        String[] ary = str.split("-");
        
        if (ary==null || ary.length!=2) {
        	return Global.getLocale();
        }
        
        lang = ary[0];
        country = ary[1];
        locale = new Locale(lang, country);
        return locale;
    }
}
