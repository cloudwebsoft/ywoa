package com.redmoon.oa.ui;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.Privilege;

import cn.js.fan.util.ResBundle;
import cn.js.fan.web.Global;

public class LocalUtil {
	static Logger logger = Logger.getLogger(LocalUtil.class.getName());
    public static final String OA_SESSION_LOCALE = "oa_locale";
    static final String resName = "res.common";
    
    public static String LoadString(HttpServletRequest request, String key) {
        return LoadString(request, resName, key);
    }
    public static String LoadString(HttpServletRequest request, String resource, String key) {
        String str = "";
        try {
            Locale locale = null;
            if (request==null)
                locale = Global.locale;
            else
                locale = getLocale(request);
            ResBundle rb = new ResBundle(resource, locale);
            str = rb.get(key);
        }
        catch (Exception e) {
            logger.error("LoadString: resource=" + resource + " key=" + key + " " + e.getMessage());
            e.printStackTrace();
        }
        return str;
    }
    

    public static Locale getLocale(HttpServletRequest request) {
        if (Global.localeSpecified)
            return Global.getLocale();
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
        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);
        UserSetupDb userSetupDb = new UserSetupDb();
        userSetupDb = userSetupDb.getUserSetupDb(userName);
        String str = userSetupDb.getLocal(); // zh-CN 
        
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
        //session.setAttribute(OA_SESSION_LOCALE, locale);
        return locale;
    }
    
    
  
}
