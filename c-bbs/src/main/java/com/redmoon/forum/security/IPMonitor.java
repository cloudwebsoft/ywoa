package com.redmoon.forum.security;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.*;
import cn.js.fan.util.*;

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
public class IPMonitor {
    private String message;

    public IPMonitor() {
    }

    /**
     * 判断IP是否合法
     * @param req
     * @param ip
     * @return
     */
    public boolean isValid(HttpServletRequest req, String ip) {
        if (ip == null || ip.trim().equals(""))
            return true;
        ForbidIPDb fid = new ForbidIPDb();
        fid = fid.getForbidIPDb(ip);
        // System.out.println("userservice.java ip=" + ip);
        if (fid.isLoaded()) {
            String str = SkinUtil.LoadString(req,
                                             "res.forum.security.IPMonitor",
                                             "err_forbidden");
            message = str + fid.getReason();
            return false;
        }

        ForbidIPRangeDb fir = new ForbidIPRangeDb();
        if (!fir.isValid(ip)) {
            String str = SkinUtil.LoadString(req,
                                             "res.forum.security.IPMonitor",
                                             "err_forbidden");
            message = str + fir.getReason();
            return false;
        }
        return true;
    }

    /**
     * IP格式是否合法，IP中可以含有通配符，必须为四段由.分隔的地址，IPV6地址不支持
     * @param ip String
     * @return boolean
     */
    public static boolean isIPFormatValid(String ip) {
        String[] ips = ip.split("\n");
        for (int i = 0; i < ips.length; i++) {
            if (!ips[i].trim().matches("^([01]?[0-9][0-9]|[01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9][0-9]|[01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9][0-9]|[01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9][0-9]|[01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$") &&
                !ips[i].trim().matches("^([01]?[0-9][0-9]|[01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9][0-9]|[01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9][0-9]|[01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.\\*$") &&
                !ips[i].trim().matches("^([01]?[0-9][0-9]|[01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9][0-9]|[01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.\\*\\.\\*$") &&
                !ips[i].trim().matches(
                        "^([01]?[0-9][0-9]|[01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.\\*\\.\\*\\.\\*$")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * @param ip
     * @return
     */
    public boolean isIPOfRegistSpecialScope(String ip) {
        RegConfig rcfg = new RegConfig();
        String[] ips = rcfg.getStringArrProperty("specialIPRegCtrl");
        if (ips == null)
            return false;
        if (ip == null || ip.trim().equals(""))
            return false;
        return isIPOfScope(ip, ips);
    }

    /**
     * 判断IP是否能访问
     * @param ip
     * @return
     */
    public boolean isIPCanVisit(String ip) {
        if (ip == null || ip.trim().equals(""))
            return true;

        RegConfig rcfg = new RegConfig();
        String sList = rcfg.getProperty("IPVisitList").trim();
        String forbidList = rcfg.getProperty("IPForbidList").trim();

        if (sList.equals("")) {
            if (forbidList.equals(""))
                return true;
            else {
                // 如果被允许的不为空，则忽略被禁止的IP
                String[] ips = null;
                ips = StrUtil.split(forbidList, "\n");
                // System.out.println(getClass() + " ips=" + ips + " ip=" + ip);
                return !isIPOfScope(ip, ips);
            }
        }
        else {
            // 如果被允许的不为空，则忽略被禁止的IP
            String[] ips = null;
            ips = StrUtil.split(sList, "\n");
            if (ips == null)
                return true;
            return isIPOfScope(ip, ips);
        }
    }

    /**
     * 判断IP是否在指定范围内
     * @param ip
     * @param ips IP地址范围数组
     * @return
     */
    public boolean isIPOfScope(String ip, String[] ips) {
        String[] ary = StrUtil.split(ip, "\\.");
        String u = "";
        int len = ary.length;
        for (int j = 0; j < len; j++) {
            if (ary[j].length() < 3)
                ary[j] = StrUtil.PadString(ary[j], '0', 3, true);
            u += ary[j];
        }

        // 不足12位的，则补齐
        if (u.length()<12)
            u = StrUtil.PadString(u, '0', 12, false);
        // System.out.println(getClass() + " len=" + len + " user ip=" + u);

        long ul = 0;
        try {
            ul = Long.parseLong(u);
        } catch (Exception e) {
            return false;
        }

        String e = "", b = "";

        len = ips.length;
        for (int i = 0; i < len; i++) {
            e = "";
            b = "";
            // System.out.println(getClass() + " len=" + len + " ips[i]=" + ips[i]);
            String[] ary1 = StrUtil.split(ips[i], "\\.");
            int alen = ary1.length;

            for (int k = 0; k < alen; k++) {
                if (!ary1[k].equals("*")) {
                    if (ary1[k].length() < 3)
                        ary1[k] = StrUtil.PadString(ary1[k], '0', 3, true);
                    b += ary1[k];
                    e += ary1[k];
                } else {
                    b += "000";
                    e += "255";
                }
            }

            if (b.length()<12)
                b = StrUtil.PadString(b, '0', 12, false);
            if (e.length()<12)
                e = StrUtil.PadString(e, '0', 12, false);
            long bl = Long.parseLong(b);
            long el = Long.parseLong(e);

            if (ul >= bl && ul <= el) {
                return true;
            }
        }
        return false;
    }

    /**
     * IP是否在管理员后台IP访问列表范围内
     */
    public boolean isIPCanAdmin(String ip) {
        RegConfig rcfg = new RegConfig();
        String[] ips = rcfg.getStringArrProperty("adminIPVisitList");
        if (ips == null)
            return true;
        if (ip == null || ip.trim().equals(""))
            return true;

        return isIPOfScope(ip, ips);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
