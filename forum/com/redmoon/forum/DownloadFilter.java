package com.redmoon.forum;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import cn.js.fan.web.SkinUtil;

/**
 *
 * <p>Title: 下载过滤器</p>
 *
 * <p>Description: 对重要目录进行过滤，如：log，过滤非法盗链</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DownloadFilter implements Filter {
    FilterConfig config;

    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws ServletException,
            IOException {
        ServletContext context = config.getServletContext();

        if (req instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) req;
            Privilege privilege = new Privilege();
            // 如果是处理在downloads目录下的文件
            String url = request.getRequestURL().toString();

            // Logger.getLogger(this.getClass().getName()).info("doFilter" +
            //                          " url=" + url + " server=" + request.getServerName() + " pathInfo=" + request.getPathInfo() + " requestURI=" + request.getRequestURI());

            if (url.indexOf("forum/upfile") != -1) {
                Config cfg = Config.getInstance();
                if (cfg.getBooleanProperty("forum.checkReferer")) {
                    // 防盗链
                    String callingPage = request.getHeader("Referer");
                    // Logger.getLogger(this.getClass().getName()).info("doFilter referer=" +
                    //                  callingPage + " url=" + url + " server=" + request.getServerName());
                    if (callingPage == null ||
                        callingPage.indexOf(request.getServerName()) != -1) {
                    } else {
                        // 非法
                        url = "http://" + request.getServerName() + ":" +
                              request.getServerPort() + request.getContextPath() +
                              "/images/err_pvg.gif"; // onerror.htm";//  + request.getRequestURI();
                        ((HttpServletResponse) res).sendRedirect(url);
                        return;
                    }
                }
            }
            else if (url.indexOf("upfile/blog/music")!=-1) {
            	if (!com.redmoon.forum.Privilege.isUserLogin(request)) {
                    ((HttpServletResponse)res).sendError(404);
            		/*
            		res.setContentType("text/html;charset=utf-8");
                    PrintWriter out = res.getWriter();
                    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_not_login")));
                    */
            		return;
            	}
            }
            else {
                // 如果不是管理员
                if (!privilege.isMasterLogin(request)) {
                    RequestDispatcher rd = null;
                    // url = "http://" + request.getServerName() + ":" +
                    //      request.getServerPort() + request.getContextPath() +
                    //      "/onerror.htm"; //  + request.getRequestURI();
                    // url = "../onerror.htm"; // 只能用相对路径
                    res.setContentType("text/html;charset=utf-8");
                    PrintWriter out = res.getWriter();
                    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
                    // 也可以重定向
                    // Logger.getLogger(this.getClass().getName()).info("doFilter" +
                    //                  " url=" + url + " server=" + request.getServerName() + " pathInfo=" + request.getPathInfo());
                    // rd = req.getRequestDispatcher(url);
                    // rd.forward(req, res); // 有时这行会不行，所以用下行
                    // ((HttpServletResponse) res).sendRedirect(url);
                    return;
                }
            }
            chain.doFilter(req, res);
        }
    }

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
    }

    public void destroy() {
        this.config = null;
    }
}
