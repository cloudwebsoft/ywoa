package com.redmoon.forum.security;

import javax.servlet.*;
import javax.servlet.http.*;
import cn.js.fan.util.*;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.Config;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class LoginMonitor {
    static long delaytime = 60000; // 延时时间为60秒
    static int maxfailcount = 3;
    static long failtimespan = 30000; // 在半分钟内的最大出错次数为3

    static final String prefix = "cwbbs";

    static {
        initParam();
    }

    public LoginMonitor() {
    }

    public static void initParam() {
        Config cfg = Config.getInstance();
        failtimespan = cfg.getIntProperty("forum.loginFailInterval") * 1000;
        maxfailcount = cfg.getIntProperty("forum.loginFailCount");
        delaytime = cfg.getIntProperty("forum.loginFailDelay") * 1000;
    }

    /**
     * 初始化登录失败次数为0，在登录界面中使用,用于防暴力破解
     * @param request
     * @param response
     */
    public static void initLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String c = (String) session.getAttribute(prefix + "_loginfail_count");
        if (c == null)
            session.setAttribute(prefix + "_loginfail_count", "0");
    }

    public static boolean canLogin(HttpServletRequest request) throws
            ErrMsgException {
        HttpSession session = request.getSession(true);
        String strcount = (String) session.getAttribute(prefix +
                "_loginfail_count");
        int count = 0;
        if (strcount == null) {
            session.setAttribute(prefix + "_loginfail_count", "0"); // 考虑到入口有多个，所以此处不抛出异常，因此initLogin在填写登录信息页面中不必使用
            // throw new ErrMsgException(SkinUtil.LoadString(request, "err_login_invalid")); // 非法登录，因为在登录界面时未写入session zjpages_loginfailcount的值
        } else {
            try {
                count = Integer.parseInt(strcount);
            } catch (Exception e) {
                throw new ErrMsgException("Login count error！");
            }
        }
        if (count <= maxfailcount)
            return true;
        long first = 0;
        long last = 0;
        try {
            first = Long.parseLong((String) session.getAttribute(
                    prefix + "_loginfail_first"));
            last = Long.parseLong((String) session.getAttribute(
                    prefix + "_loginfail_last"));
        } catch (NumberFormatException e) {
            throw new ErrMsgException("Login fail time error！");
        }
        long timespan = last - first;
        if (timespan <= failtimespan) { // 出错大于maxfailcount时，如果时间间隔小于预定值，则怀疑被攻击，采取措施
            long tspan = System.currentTimeMillis() - last;
            tspan = (delaytime - tspan) / 1000;
            if (tspan > 0) {
                // throw new ErrMsgException("对不起，您已在" + failtimespan / 1000 +
                //                          "秒内登录出错超过" + maxfailcount + "次，您被延时" +
                //                          delaytime / 1000 + "秒登录，您还需" + tspan +
                //                          "秒才可以登录！");
                String str = SkinUtil.LoadString(request, "err_login_can_not");
                str = str.replaceFirst("\\$s", "" + failtimespan/1000);
                str = str.replaceFirst("\\$c", "" + maxfailcount);
                str = str.replaceFirst("\\$d", "" + delaytime/1000);
                str = str.replaceFirst("\\$t", "" + tspan);
                throw new ErrMsgException(str);
            }
        } else
            session.setAttribute(prefix + "_loginfail_count", "0");
        return true;
    }

    /**
     * 根据登录是否成功修改session中的相应的变量
     * @param request
     * @param response
     * @param count
     */
    public static void afterLogin(HttpServletRequest request,
                                  boolean isloginsuccess,
                                  boolean keepsession) throws ErrMsgException {
        HttpSession session = request.getSession(true);
        if (isloginsuccess) {
            if (!keepsession)
                session.invalidate(); //如果不需保留，则销除session
            return;
        }
        String strcount = (String) session.getAttribute(prefix +
                "_loginfail_count");
        int count = 0;
        if (strcount == null) {
            throw new ErrMsgException("After:" + SkinUtil.LoadString(request, "err_login_invalid")); //非法登录，因为在登录界面时未写入session zjpages_loginfailcount的值
        } else {
            try {
                count = Integer.parseInt(strcount);
            } catch (Exception e) {
                throw new ErrMsgException("After login coutn error！");
            }
        }
        count++;
        session.setAttribute(prefix + "_loginfail_count", "" + count);
        long t = System.currentTimeMillis();
        // 置登录失败第一次的时间和最后次的时间
        if (count == 1) {
            session.setAttribute(prefix + "_loginfail_first", "" + t);
            session.setAttribute(prefix + "_loginfail_last", "" + t);
        } else
            session.setAttribute(prefix + "_loginfail_last",
                                 "" + System.currentTimeMillis());
        long timespan = 0;
        long first = 0, last = 0;
        if (count == 1) {
            // throw new ErrMsgException("您已失败了" + count + "次！请注意：如果" +
            //                          failtimespan / 1000 + "秒内大于" +
            //                          maxfailcount + "次您将被延时" +
            //                          delaytime / 1000 + "秒登录！");
            String str = SkinUtil.LoadString(request, "err_login_fail_one");
            str = str.replaceFirst("\\$c", "" + count);
            str = str.replaceFirst("\\$s", "" + failtimespan/1000);
            str = str.replaceFirst("\\$m", "" + maxfailcount);
            str = str.replaceFirst("\\$d", "" + delaytime/1000);
            throw new ErrMsgException(str);
        }
        else
            last = t;
        try {
            first = Long.parseLong((String) session.getAttribute(
                    prefix + "_loginfail_first"));
        } catch (NumberFormatException e) {
            throw new ErrMsgException("Error when parseLong loginfail_first");
        }
        timespan = (last - first) / 1000;
        // throw new ErrMsgException("您已在" + timespan + "秒中失败了" + count +
        //                          "次！请注意：如果" + failtimespan / 1000 + "秒内大于" +
        //                          maxfailcount + "次您将被延时" + delaytime / 1000 +
        //                          "秒登录！");
        String str = SkinUtil.LoadString(request, "err_login_fail");
        str = str.replaceFirst("\\$t", "" + timespan);
        str = str.replaceFirst("\\$c", "" + count);
        str = str.replaceFirst("\\$f", "" + failtimespan/1000);
        str = str.replaceFirst("\\$m", "" + maxfailcount);
        str = str.replaceFirst("\\$s", "" + delaytime/1000);
        throw new ErrMsgException(str);
    }
}
