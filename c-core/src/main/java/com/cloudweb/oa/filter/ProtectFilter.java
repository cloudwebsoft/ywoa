package com.cloudweb.oa.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.js.fan.security.AntiXSS;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.security.ProtectSQLInjectException;
import com.cloudwebsoft.framework.security.ProtectXSSException;
import com.cloudwebsoft.framework.security.SecurityUtil;
import com.redmoon.oa.sys.DebugUtil;
import org.springframework.core.annotation.Order;

@WebFilter(urlPatterns = "/*", filterName = "protectFilter")
@Order(3)
public class ProtectFilter implements Filter {
    FilterConfig config;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws ServletException, IOException {
        // ServletContext context = config.getServletContext();
        if (req instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) req;

            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    // tomcat7 支持该属性，tomcat6不支持
                    cookie.setHttpOnly(true);
                }
            }

            String url = request.getRequestURL().toString();
            HttpServletResponse response = (HttpServletResponse) res;
            // 防止 AppScan 报：“Content-Security-Policy”头缺失或不安全
            // response.setHeader("Content-Security-Policy", "default-src 'self';font-src 'self' data:;connect-src 'self' https:;script-src 'self' 'unsafe-inline' 'unsafe-eval';frame-ancestors 'self';style-src 'self' 'unsafe-inline';media-src 'self';object-src 'self';img-src 'self'");
            // Header always set Content-Security-Policy "default-src 'self' http: https: 'unsafe-inline' 'unsafe-eval'"
            // response.setHeader("Content-Security-Policy", "default-src 'self' http: https: data: blob: 'unsafe-inline' 'unsafe-eval'; script-src 'self'; frame-ancestors 'self'");

            // SAMEORIGIN：页面只能加载入同源域名下的页面
            response.setHeader("X-Frame-Options", "SAMEORIGIN");
            // 1; mode=block 启用XSS保护，并在检查到XSS攻击时，停止渲染页面
            response.setHeader("X-XSS-Protection", "1; mode=block");

            // 判断如果是js文件
            String patternStr = "/.*?\\.js$"; // 360检测 num"><body ONLOAD=alert(42873)>
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    patternStr,
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher mat = pattern.matcher(url);
            boolean isJs = mat.find();

            if (url.contains(".dwr") || url.contains("/dwr")) {
                isJs = true;
                // 补上MIME类型，否则chrome会报：Refused to execute script from 'http://localhost:8093/tzcj/dwr/interface/MessageDb.js' because its MIME type ('') is not executable, and strict MIME type checking is enabled.
                response.setContentType("text/javascript;charset=utf-8");
            }
            if (!isJs) {
                if (url.contains("ajax_getpage.jsp")) {
                    isJs = true;
                }
                else if (url.contains("flow_js.jsp")) {
                    isJs = true;
                }
                else if (url.contains("flow_dispose_js.jsp")) {
                    isJs = true;
                }
            }

            boolean isController = false;
            if (url.contains(".do")) {
                isController = true;
            }
            if (url.contains("index")) {
                isController = false;
            }

            if (isJs) {
                response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'; style-src 'self'");
                response.setHeader("X-Content-Type-Options", "nosniff");
            }
            else if (isController) {
                response.setHeader("Content-Security-Policy", "default-src 'self' http: https: data: blob: 'unsafe-inline' 'unsafe-eval'; script-src 'self' 'unsafe-inline' 'unsafe-eval' blob:; style-src 'self' 'unsafe-inline'; frame-ancestors 'self'");
                response.setHeader("X-Content-Type-Options", "nosniff");
            }
            else {
                // http://g.alicdn.com/dingding/dingtalk-jsapi/2.3.0/dingtalk.open.js 钉钉，用于dd_login.jsp
                // hm.baidu.com为百度统计
                response.setHeader("Content-Security-Policy", "default-src 'self' http: https: data: blob: 'unsafe-inline' 'unsafe-eval'; img-src * 'self' data: blob:; script-src 'self' 'unsafe-inline' 'unsafe-eval' g.alicdn.com hm.baidu.com api.map.baidu.com dlswbr.baidu.com mapclick.map.baidu.com blob:; style-src 'self' 'unsafe-inline'; frame-ancestors 'self'");
            }

            if (!url.contains("error.jsp")) {
                boolean isValid = true;
                String kind = "";
                String param = "";
                String value = "";
                try {
                    // 过滤参数
                    SecurityUtil.filter(request, url);
                } catch (ProtectXSSException e) {
                    // e.printStackTrace();
                    isValid = false;
                    param = e.getParam();
                    value = e.getValue();
                    kind = "XSS";
                } catch (ProtectSQLInjectException e) {
                    // e.printStackTrace();
                    isValid = false;
                    param = e.getParam();
                    value = e.getValue();
                    kind = "SQLInject";
                }

                if (isValid) {
                    // 防链接注入
                    // Set path to '/admin/log_list.jsp;;";;alert(3995);;s="'
                    Pattern p = Pattern.compile("(.*?</script.*?>)", Pattern.CASE_INSENSITIVE);
                    String queryString = request.getQueryString();
                    if (queryString!=null) {
                        // URLDecoder: Illegal hex characters in escape (%) pattern - For input string，是由%引起的，%号的urlencode为%25
                        queryString = queryString.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                        queryString = queryString.replaceAll("\\+", "%2B");
                        queryString = URLDecoder.decode(queryString, "utf-8");
                    }
                    if (queryString!=null) {
                        Matcher m = p.matcher(queryString);
                        String queryString2 = m.replaceAll("");
                        if (!queryString.equals(queryString2)) {
                            isValid = false;
                            kind = "XSS";
                            param = "url";
                            value = request.getRequestURL() + "?" + queryString;
                        }

                        // Set parameter 'other:module_id:xmxxgl_qx:id:sbsjToDate's value to '2019-01-01"onclick=alert(3248)//' (Variant ID: 14935)
                        // Set parameter 'srcType's value to 'file"onload	=alert(20684);' (Variant ID: 21420)
                        try {
                            queryString2 = AntiXSS.stripEvent(queryString);
                            if (!queryString.equals(queryString2)) {
                                isValid = false;
                                kind = "XSS";
                                param = "url";
                                value = request.getRequestURL() + "?" + queryString;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (isValid) {
                        //  Set path to '/admin/log_list.jsp?;";;alert(3995);;s="'
                        p = Pattern.compile("(;\".*?;.*?\")", Pattern.CASE_INSENSITIVE);
                        if (queryString != null) {
                            Matcher m = p.matcher(queryString);
                            String queryString2 = m.replaceAll("");
                            if (!queryString.equals(queryString2)) {
                                isValid = false;
                                kind = "XSS";
                                param = "url";
                                value = request.getRequestURL() + "?" + queryString;
                            }
                        }
                    }

                    if (isValid) {
                        // http://localhost:8093/tzcj/admin/log_list.jsp;;%22;;alert(3995);;s=
                        // http://localhost:8093/tzcj/admin/log_list.jsp;;";;alert(3995);;s=
                        String urlDecoded = URLDecoder.decode(url, "utf-8");
                        p = Pattern.compile("(;+\";+.*?;+.*?\"?)", Pattern.CASE_INSENSITIVE);
                        Matcher m = p.matcher(urlDecoded);
                        String url2 = m.replaceAll("");
                        if (!url2.equals(urlDecoded)) {
                            isValid = false;
                            kind = "XSS";
                            param = "url";
                            value = url;
                        }
                    }

                    // DebugUtil.i(getClass(), "url=", url);
                    if (isValid) {
                        if (url.contains("reportServlet")) {
                            if (queryString != null) {
                                // DebugUtil.i(getClass(), url, queryString);
                                // reportServlet year1=2020"A==alert(1671)==1
                                // reportServlet Set parameter 'reportName's value to 'report1 alert(8185) ' (Variant ID: 20980)
                                p = Pattern.compile("=*\\s*?\\S+\\(.*?\\)", Pattern.CASE_INSENSITIVE);
                                Matcher m = p.matcher(queryString);
                                String queryString2 = m.replaceAll("");
                                // DebugUtil.i(getClass(), url, queryString2);
                                if (!queryString.equals(queryString2)) {
                                    isValid = false;
                                    kind = "XSS";
                                    param = "url";
                                    value = request.getRequestURL() + "?" + queryString;
                                }
                            }

                            p = Pattern.compile("=*\\s*?\\S+\\(.*?\\)", Pattern.CASE_INSENSITIVE);
                            Enumeration paramNames = request.getParameterNames();
                            while (paramNames.hasMoreElements()) {
                                String paramName = (String) paramNames.nextElement();
                                String[] paramValues = request.getParameterValues(paramName);
                                for (int i = 0; i < paramValues.length; i++) {
                                    String paramValue;
                                    if (paramValues.length==1) {
                                        paramValue = ParamUtil.get(request, paramName);
                                    }
                                    else {
                                        paramValue = paramValues[i];
                                    }
                                    Matcher m = p.matcher(paramValue);
                                    String paramValue2 = m.replaceAll("");
                                    // DebugUtil.i(getClass(), paramName, paramValue);
                                    if (!paramValue.equals(paramValue2)) {
                                        isValid = false;
                                        kind = "XSS";
                                        param = paramName;
                                        value = paramValue;
                                    }
                                }
                            }
                        }
                    }
                }

                if (!isValid) {
                    com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("Filter error: url=" + url + " type=protect&kind=" + kind + "&param=" + param + "&value=" + StrUtil.UrlEncode(value));
                    String toUrl = Global.getFullRootPath(request) + "/error.jsp?type=protect&kind=" + kind + "&param=" + param + "&value=" + StrUtil.UrlEncode(value) + "&sourceUrl=" + StrUtil.UrlEncode(url);
                    response.sendRedirect(toUrl);
                    return;
                }

                // 防止整数溢出错误
                if (url.indexOf("reportServlet")!=-1) {
                    String strAction = ParamUtil.get(request, "action");
                    try {
                        Integer.parseInt(strAction);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        String toUrl = Global.getFullRootPath(request) + "/error.jsp?type=protect&info=" + StrUtil.UrlEncode("reportServlet action=" + strAction + "  为空或整数溢出");
                        response.sendRedirect(toUrl);
                        return;
                    }
                }
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.config = config;
    }

    @Override
    public void destroy() {
        this.config = null;
    }
}
