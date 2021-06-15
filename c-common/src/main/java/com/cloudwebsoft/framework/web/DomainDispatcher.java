package com.cloudwebsoft.framework.web;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: 当系统支持泛域名时，用以重定向处理</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DomainDispatcher {
    public static boolean isSubDomainSupported = false;
    public static String baseDomain = null;

    public DomainDispatcher() {
    }

    /**
     * 匹配子域名
     * @param serverName String
     * @param subDomain String
     * @param exclude String
     * @return String
     */
    public static String matchSubDomain(String serverName, DomainUnit ru) {
        String subDomain = ru.getSubDomain() + "." + baseDomain;
        // LogUtil.getLog(DomainDispatcher.class).info("subDomain=" + subDomain);
        // LogUtil.getLog(DomainDispatcher.class).info("serverName=" + serverName);

        if (!ru.isRegexMatch()) {
            // 非正则匹配
            if (subDomain.equals(serverName))
                return ru.getSubDomain();
            else
                return "";
        }
        else {
            // 正则匹配
            String sub = "";
            // 替换需转义的字符
            subDomain = subDomain.replaceAll("\\.", "\\\\.");
            subDomain = subDomain.replaceAll("\\?", "\\\\?");
            // subDomain = subDomain.replaceAll("\\+", "\\\\+");
            // 替换规则中的*字符
            String patString = subDomain.replaceAll("\\*", "(.*?)");
            // LogUtil.getLog(DomainDispatcher.class).info("patString=" + patString);

            Pattern pat = Pattern.compile(
                    patString,
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher m = pat.matcher(serverName);
            if (m.find()) {
                sub = m.group(1);
                // LogUtil.getLog(DomainDispatcher.class).info("sub=" + sub);

                String[] excludeSubDomains = ru.getExcludeSubDomains();
                // 检查是否被排除
                if (excludeSubDomains != null) {
                    int len = excludeSubDomains.length;
                    for (int i = 0; i < len; i++) {
                        if (sub.equals(excludeSubDomains[i]))
                            return "";
                    }
                }
            }
            return sub;
        }
    }

    /**
     * 取得所访问网站的一级域名
     * @param request HttpServletRequest
     * @return String
     */
    public static String getBaseDomain(HttpServletRequest request) {
        String serverName = request.getServerName();
        String baseDom = "";
        // 取得本站主机名，即一级域名
        String[] domainParts = StrUtil.split(serverName, "\\.");
        int len = domainParts.length;
        if (len == 1 || StrUtil.isNumeric(domainParts[len - 1])) {
            // 如果是IP地址或localhost
            baseDom = serverName;
        } else {
            // 取得一级域名，如 zjrj.cn
            if (domainParts[len - 2].equalsIgnoreCase("gov") &&
                domainParts[len - 1].equalsIgnoreCase("cn")) {
                baseDom = domainParts[len - 3] + "." +
                             domainParts[len - 2] + "." +
                             domainParts[len - 1];
            } else if (domainParts[len - 2].equalsIgnoreCase("com") &&
                       domainParts[len - 1].equalsIgnoreCase("cn")) {
                baseDom = domainParts[len - 3] + "." +
                             domainParts[len - 2] + "." +
                             domainParts[len - 1];
            } else
                baseDom = domainParts[len - 2] + "." +
                             domainParts[len -
                             1];
        }
        return baseDom;
    }

    /**
     * 二级域名重定向
     * @param request HttpServletRequest
     * @param res HttpServletResponse
     * @return int 0 没有匹配的二级域名 1有匹配的二级域名
     * @throws ServletException
     * @throws IOException
     */
    public static int dispatch(HttpServletRequest request, HttpServletResponse res) throws
            ServletException, IOException {
        if (request.getRequestURI().equals("/index.jsp")) {
            String serverName = request.getServerName();
            if (baseDomain == null) {
                // 取得本站主机名，即一级域名
                baseDomain = getBaseDomain(request);
            }

            DomainMgr rm = new DomainMgr();
            Vector v = rm.getAllDomainUnit();
            Iterator ir = v.iterator();
            // 按照domain.xml中的顺序依次匹配，因此对于在XML配置中的排列顺序是敏感的
            while (ir.hasNext()) {
                DomainUnit ru = (DomainUnit) ir.next();

                LogUtil.getLog(DomainDispatcher.class).info("ru.isUsed=" + ru.isUsed());

                if (ru.isUsed()) {
                    // 比对域名中是否含有subDomain
                    String domainField = matchSubDomain(serverName, ru);
                    LogUtil.getLog(DomainDispatcher.class).info("domainField=" + domainField);
                    if (!domainField.equals("")) {
                        String url = ru.getUrl();
                        if (!url.equals("")) {
                            if (ru.isRegexMatch()) {
                                url = StrUtil.format(url, new String[] {domainField});
                            }
                        } else if (!ru.getClassName().equals("")) {
                            url = ru.getIDomainDispatcher().getUrl(request,
                                    domainField, ru);
                        }
                        LogUtil.getLog(DomainDispatcher.class).info("url=" + url);

                        if (!url.equals("")) {
                            RequestDispatcher rd = request.
                                    getRequestDispatcher(url);
                            // @task:注意静态页面好象只能redirect
                            if (ru.isRedirect())
                                res.sendRedirect(url);
                            else
                                rd.forward(request, res);
                            return 1;
                        }
                    }
                }
            }
        }
        return 0;
    }
}
