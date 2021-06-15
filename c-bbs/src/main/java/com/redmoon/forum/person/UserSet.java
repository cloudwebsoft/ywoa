package com.redmoon.forum.person;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.CookieBean;
import javax.servlet.http.HttpServletResponse;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.ui.SkinMgr;
import com.redmoon.forum.Config;

/**
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
public class UserSet {
    public static String defaultSkin = SkinMgr.DEFAULT_SKIN_CODE;

    static {
        SkinMgr sm = new SkinMgr();
        defaultSkin = sm.getDefaultSkinCode();
    }

    public UserSet() {
    }

    public static String getSkin(HttpServletRequest request) {
        CookieBean cookiebean = new CookieBean();
        return StrUtil.getNullString(cookiebean.getCookieValue(request, "skin"));
    }

    public static void setSkin(HttpServletRequest request, HttpServletResponse response, String skinCode) {
        CookieBean cookiebean = new CookieBean();
        cookiebean.addCookie(response, "skin", skinCode, "/", 60*60*24*365); // 保存365天

        // 下面的方法不成功，因为setCookieMaxAge不能对本次新产生的cookie作设置
        // cookiebean.addCookie(response, "skin", skinCode, "/"); // 保存365天
        // cookiebean.setCookieMaxAge(request, response, "skin", 60*60*24*365); // 保存365天
    }
}
