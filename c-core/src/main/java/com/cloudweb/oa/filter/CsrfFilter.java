package com.cloudweb.oa.filter;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.redmoon.oa.sys.DebugUtil;
import org.apache.log4j.Logger;
import org.springframework.core.annotation.Order;

import javax.servlet.FilterConfig;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 跨站请求伪造过滤器
 */
@WebFilter(urlPatterns = "/*", filterName = "csrfFilter")
@Order(4)
public class CsrfFilter implements Filter {
    private static final Logger logger = Logger.getLogger(CsrfFilter.class);

    // 白名单
    private List<String> whiteUrls;

    private int size = 0;

    @Override
    public void init(FilterConfig filterConfig) {
        // 读取文件
        String path = CsrfFilter.class.getResource("/").getFile();
        whiteUrls = readFile(path + "csrf_white.txt");
        size = null == whiteUrls ? 0 : whiteUrls.size();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            String referer = req.getHeader("referer");
            String serverName = request.getServerName();

            String url = req.getRequestURL().toString();
            // 忽略首页
            if (url.contains("index") || url.endsWith("/")) {
                chain.doFilter(request, response);
                return;
            }

            if (!validate(referer, serverName)) {
                // 获取请求url地址
                // logger.info("referurl----->" + referurl);
                // DebugUtil.e(getClass(), "url1", url + " referer=" + referer + " serverName=" + serverName);

                if (!isWhiteReq(referer)) {
                    String kind = "CSRF";
                    String param = "referer";
                    String value = referer;

                    DebugUtil.e(getClass(), "url", url + " referer=" + referer + " serverName=" + serverName);

                    com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("Filter error: url=" + url + " type=protect&kind=" + kind + "&param=" + param + "&value=" + StrUtil.UrlEncode(value));
                    String toUrl = Global.getFullRootPath(req) + "/error.jsp?type=protect&kind=" + kind + "&param=" + param + "&value=" + StrUtil.UrlEncode(value) + "&sourceUrl=" + StrUtil.UrlEncode(url);
                    res.sendRedirect(toUrl);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("doFilter", e);
        }
        chain.doFilter(request, response);
    }

    // 判断是否同个域名
    public static boolean validate(String referer, String serverName) {
        // 链接来源地址
        // DebugUtil.i("CsrfFilter", "validate", "refer is "+referer + " serverName is " + request.getServerName());
        // System.out.println("serverName is"+request.getServerName());
        if (referer == null || !referer.contains(serverName)) {
            return false;
        }
        else {
            return true;
        }
    }

    /*
     * 判断是否是白名单
     */
    private boolean isWhiteReq(String referUrl) {
        if (referUrl == null || "".equals(referUrl)) {
            return true;
        } else {
            if (size == 0) {
                return false;
            }
            String refHost = "";
            referUrl = referUrl.toLowerCase();
            if (referUrl.startsWith("http://")) {
                refHost = referUrl.substring(7);
            } else if (referUrl.startsWith("https://")) {
                refHost = referUrl.substring(8);
            }

            for (String urlTemp : whiteUrls) {
                if (refHost.indexOf(urlTemp.toLowerCase()) > -1) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void destroy() {
    }

    private List<String> readFile(String fileName) {
        List<String> list = new ArrayList<String>();
        BufferedReader reader = null;
        FileInputStream fis = null;
        try {
            File f = new File(fileName);
            if (f.isFile() && f.exists()) {
                fis = new FileInputStream(f);
                reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!"".equals(line)) {
                        list.add(line);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("readFile", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.error("InputStream关闭异常", e);
            }
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                logger.error("FileInputStream关闭异常", e);
            }
        }
        return list;
    }
}