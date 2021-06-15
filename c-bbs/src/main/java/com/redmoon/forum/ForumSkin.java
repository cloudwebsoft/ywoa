package com.redmoon.forum;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import java.util.TimeZone;
import java.util.Date;
import cn.js.fan.util.DateUtil;
import com.redmoon.forum.person.UserMgr;
import com.redmoon.forum.person.UserDb;

/**
 * <p>Title: 论坛前台相关处理</p>
 *
 * <p>Description: 目前本类主要是用于时间的国际化</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ForumSkin {
    public ForumSkin() {
    }

    public static String formatDate(HttpServletRequest request, Date d) {
        if (d==null)
            return "";
        
        TimeZone tz = Global.timeZone;
        if (Privilege.isUserLogin(request)) {
            String userName = Privilege.getUser(request);
            UserMgr um = new UserMgr();
            UserDb user = um.getUser(userName);
            if (user.isLoaded())
                tz = user.getTimeZone();
        }
        return DateUtil.formatDate(d, DateFormat.MEDIUM,
                                       SkinUtil.getLocale(request), tz);
    }

    public static String formatDateTime(HttpServletRequest request, Date d) {
        if (d==null)
            return "";
        
        TimeZone tz = Global.timeZone;
        if (Privilege.isUserLogin(request)) {
            String userName = Privilege.getUser(request);
            UserMgr um = new UserMgr();
            UserDb user = um.getUser(userName);
            if (user.isLoaded())
                tz = user.getTimeZone();
        }
        // logger.info("formatDateTime:" + tz);
        return DateUtil.formatDateTime(d, DateFormat.MEDIUM, DateFormat.MEDIUM, SkinUtil.getLocale(request), tz);
    }

    public static String formatDateTimeShort(HttpServletRequest request, Date d) {
        if (d==null)
            return "";

        if (SkinUtil.getLocale(request).getLanguage().equalsIgnoreCase("zh")) {
        	return DateUtil.format(d, "yyyy-MM-dd");
        }
        
        TimeZone tz = Global.timeZone;
        if (Privilege.isUserLogin(request)) {
            String userName = Privilege.getUser(request);
            UserMgr um = new UserMgr();
            UserDb user = um.getUser(userName);
            if (user.isLoaded())
                tz = user.getTimeZone();
        }
        // logger.info("formatDateTime:" + tz);
        return DateUtil.formatDateTime(d, DateFormat.SHORT, DateFormat.MEDIUM, SkinUtil.getLocale(request), tz);
    }
}
