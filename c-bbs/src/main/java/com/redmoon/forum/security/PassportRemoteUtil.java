package com.redmoon.forum.security;

import javax.servlet.http.*;

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
public class PassportRemoteUtil {
    public PassportRemoteUtil() {
        super();
    }

    /**
     * 远程注册
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param url String 论坛的URL，如：http://bbs.***.com
     * @param key String 加密密钥
     * @param forward String 集成登录后的跳转页面
     * @throws Exception
     */
    public void remoteRegist(HttpServletRequest request,
                             HttpServletResponse response, String url,
                             String key, String forward) throws Exception {
        String action = "regist"; // regist注册 login登录 logout退出登录
        String auth = ""; // 验证字符串
        String verify = ""; // 合法性检查字符串

        String uid = (String) request.getAttribute("uid"); // 用户名
        String nick = (String) request.getAttribute("nick"); // 呢称
        String pwd = (String) request.getAttribute("pwd"); // 用户原始密码
        String email = (String) request.getAttribute("email");
        String gender = (String) request.getAttribute("gender"); // 性别 M表示男 F表示女
        String birthday = (String) request.getAttribute("birthday"); // 出生日期 格式为yyyy-MM-dd
        String regip = (String) request.getAttribute("regip"); // 注册时的IP地址
        String regdate = (String) request.getAttribute("regdate"); // 注册日期 格式为yyyy-MM-dd HH:mm:ss
        String home = (String) request.getAttribute("home"); // 用户主页地址
        String qq = (String) request.getAttribute("qq"); // 用户QQ号
        String msn = (String) request.getAttribute("msn"); // 用户MSN号
        String fetion = (String) request.getAttribute("fetion"); // 用户飞信号
        String mobile = (String) request.getAttribute("mobile"); // 手机号
        String timezone = (String) request.getAttribute("timezone"); // 时区，默认为东八区GMT+08:00
        String realname = (String) request.getAttribute("realname"); // 用户真实姓名
        String time = "" + System.currentTimeMillis(); // 服务器的时间

        // 其中time、uid及nick为必填项
        // regdate不填，则以time为准

        auth = "?uid=" + uid + "|nick=" + nick + "|pwd=" + pwd + "|email=" +
               email + "|gender=" + gender
               + "|birthday=" + birthday + "|regip=" + regip + "|regdate=" +
               regdate + "|home=" + home
               + "|qq=" + qq + "|msn=" + msn + "|fetion=" + fetion + "|mobile=" +
               mobile
               + "|timezone=" + timezone + "|realname=" + realname + "|time=" +
               time;
        auth = cn.js.fan.security.ThreeDesUtil.encrypt2hex(key, auth);

        verify = cn.js.fan.security.SecurityUtil.MD5(action + auth + forward +
                key);
        String queryStr = "/passport.jsp?action=" + action + "&auth=" + auth +
                          "&forward=" + forward + "&verify=" + verify;

        url += queryStr;

        response.sendRedirect(url);
    }

    /**
     * 远程登录
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param url String 论坛的URL，如：http://bbs.***.com
     * @param key String 加密密钥
     * @param forward String 集成登录后的跳转页面
     * @throws Exception
     */
    public void remoteLogin(HttpServletRequest request,
                            HttpServletResponse response, String url,
                            String key, String forward) throws Exception {
        String action = "login"; // regist注册 login登录 logout退出登录
        String auth = ""; // 验证字符串
        String verify = ""; // 合法性检查字符串

        String uid = (String) request.getAttribute("uid"); // 用户名
        String nick = (String) request.getAttribute("nick"); // 呢称
        String pwd = (String) request.getAttribute("pwd"); // 用户原始密码
        String email = (String) request.getAttribute("email");
        String gender = (String) request.getAttribute("gender"); // 性别 M表示男 F表示女
        String birthday = (String) request.getAttribute("birthday"); // 出生日期 格式为yyyy-MM-dd
        String regip = (String) request.getAttribute("regip"); // 注册时的IP地址
        String regdate = (String) request.getAttribute("regdate"); // 注册日期 格式为yyyy-MM-dd HH:mm:ss
        String home = (String) request.getAttribute("home"); // 用户主页地址
        String qq = (String) request.getAttribute("qq"); // 用户QQ号
        String msn = (String) request.getAttribute("msn"); // 用户MSN号
        String fetion = (String) request.getAttribute("fetion"); // 用户飞信号
        String mobile = (String) request.getAttribute("mobile"); // 手机号
        String timezone = (String) request.getAttribute("timezone"); // 时区，默认为东八区GMT+08:00
        String realname = (String) request.getAttribute("realname"); // 用户真实姓名
        String time = "" + System.currentTimeMillis(); // 服务器的时间

        // 其中time、uid及nick为必填项
        // regdate不填，则以time为准

        auth = "?uid=" + uid + "|nick=" + nick + "|pwd=" + pwd + "|email=" +
               email + "|gender=" + gender
               + "|birthday=" + birthday + "|regip=" + regip + "|regdate=" +
               regdate + "|home=" + home
               + "|qq=" + qq + "|msn=" + msn + "|fetion=" + fetion + "|mobile=" +
               mobile
               + "|timezone=" + timezone + "|realname=" + realname + "|time=" +
               time;
        auth = cn.js.fan.security.ThreeDesUtil.encrypt2hex(key, auth);

        verify = cn.js.fan.security.SecurityUtil.MD5(action + auth + forward +
                key);
        String queryStr = "action=" + action + "&auth=&forward=" + forward +
                          "&verify=" + verify;

        url += queryStr;

        response.sendRedirect(url);
    }

    /**
     * 远程退出
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param url String 论坛的URL，如：http://bbs.***.com
     * @param key String 加密密钥
     * @param forward String 集成登录后的跳转页面
     * @throws Exception
     */
    public void remoteLogout(HttpServletRequest request,
                             HttpServletResponse response, String url,
                             String key, String forward) throws Exception {
        String action = "logout"; // regist注册 login登录 logout退出登录
        String auth = ""; // 验证字符串
        String verify = ""; // 合法性检查字符串

        verify = cn.js.fan.security.SecurityUtil.MD5(action + forward + key);
        String queryStr = "action=" + action + "&auth=" + auth + "&forward=" +
                          forward + "&verify=" + verify;

        url += queryStr;

        response.sendRedirect(url);
    }

    /**
     * 远程注册后台用户
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param url String
     * @param key String
     * @param forward String
     * @throws Exception
     */
    public void remoteSuperRegist(HttpServletRequest request,
                                  HttpServletResponse response, String url,
                                  String key, String forward) throws Exception {
        String action = "regist"; // regist注册 login登录 logout退出登录
        String auth = ""; // 验证字符串
        String verify = ""; // 合法性检查字符串

        String uid = (String) request.getAttribute("uid"); // 用户名
        String desc = (String) request.getAttribute("desc"); // 描述
        String pwd = (String) request.getAttribute("pwd"); // 用户原始密码
        String realname = (String) request.getAttribute("realname"); // 用户真实姓名
        String time = "" + System.currentTimeMillis(); // 服务器的时间

        auth = "uid=" + uid + "|pwd=" + pwd + "|desc=" + desc + "|realname=" +
               realname + "|time=" + time;
        auth = cn.js.fan.security.ThreeDesUtil.encrypt2hex(key, auth);

        verify = cn.js.fan.security.SecurityUtil.MD5(action + auth + forward +
                key);
        String queryStr = "/passport_super.jsp?action=" + action + "&auth=" + auth +
                          "&forward=" + forward + "&verify=" + verify;

        url += queryStr;
        
        // @task:这里应改为URLConnection方式调用

        response.sendRedirect(url);
    }

    public void remoteSuperLogin(HttpServletRequest request,
                                 HttpServletResponse response, String url,
                                 String key, String forward) throws Exception {
        String action = "login"; // regist注册 login登录 logout退出登录
        String auth = ""; // 验证字符串
        String verify = ""; // 合法性检查字符串

        String uid = (String) request.getAttribute("uid"); // 用户名
        String desc = (String) request.getAttribute("desc"); // 描述
        String pwd = (String) request.getAttribute("pwd"); // MD5加密过的用户密码
        String realname = (String) request.getAttribute("realname"); // 用户真实姓名
        String time = "" + System.currentTimeMillis(); // 服务器的时间

        auth = "uid=" + uid + "|pwd=" + pwd + "|desc=" + desc + "|realname=" +
               realname + "|time=" + time;
        auth = cn.js.fan.security.ThreeDesUtil.encrypt2hex(key, auth);

        verify = cn.js.fan.security.SecurityUtil.MD5(action + auth + forward +
                key);
        String queryStr = "/passport_super.jsp?action=" + action + "&auth=" + auth +
                          "&forward=" + forward + "&verify=" + verify;

        url += queryStr;

        response.sendRedirect(url);
    }

    /**
     * 远程退出后台管理
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param url String 论坛的URL，如：http://bbs.***.com
     * @param key String 加密密钥
     * @param forward String 集成登录后的跳转页面
     * @throws Exception
     */
    public void remoteSuperLogout(HttpServletRequest request,
                                  HttpServletResponse response, String url,
                                  String key, String forward) throws Exception {
        String action = "logout"; // regist注册 login登录 logout退出登录
        String auth = ""; // 验证字符串
        String verify = ""; // 合法性检查字符串

        verify = cn.js.fan.security.SecurityUtil.MD5(action + forward + key);

        String queryStr = "/passport_super.jsp?action=" + action + "&auth=" + auth +
                          "&forward=" + forward + "&verify=" + verify;

        url += queryStr;

        response.sendRedirect(url);
    }
}
