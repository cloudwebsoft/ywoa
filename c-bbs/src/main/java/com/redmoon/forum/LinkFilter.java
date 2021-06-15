package com.redmoon.forum;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import cn.js.fan.util.StrUtil;

/**
 *
 * <p>Title: 伪静态过滤器</p>
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
public class LinkFilter implements Filter {
    FilterConfig config;

    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws ServletException,
            IOException {
        if (req instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) req;
            // url=http://localhost:8080/cwbbs/log/asdfsadf.html requestURI=/cwbbs/log/asdfsadf.html
            // f-0-sqzw-25-30.html --> listtopic.jsp?boardcode=sqzw&CPages=25&threadType=30
            // f-1-sqzw-25.html --> listtopic_tree.jsp?boardcode=sqzw&CPages=25
            String uri = request.getRequestURI();
            // System.out.println(getClass() + " uri=" + uri);
            if (uri.endsWith(".html")) {
                // 贴子列表页
                int p = uri.indexOf("f-");
                if (p > 0) {
                    char mode = uri.charAt(p + 2);
                    if (mode=='0' || mode=='1') {
                        int p2 = p + 4;
                        int q = uri.indexOf("-", p2);
                        if (q > 0) {
                            String boardcode = uri.substring(p2, q);
                            String pageNum = "";
                            String threadType = "" + ThreadTypeDb.THREAD_TYPE_NONE;
                            int r = uri.indexOf("-", q + 1);
                            if (r==-1) {
                                pageNum = uri.substring(q + 1,
                                        uri.length() - 5);
                            }
                            else {
                                pageNum = uri.substring(q + 1, r);

                                int pp = r + 1;
                                int qq = uri.length() - 5;
                                if (pp < qq) {
                                    threadType = uri.substring(pp, qq);
                                }
                            }
                            if (StrUtil.isNumeric(pageNum)) {
                                String page;
                                if (mode == '0')
                                    page = "listtopic.jsp";
                                else
                                    page = "listtopic_tree.jsp";
                                String url = page + "?boardcode=" +
                                             boardcode +
                                             "&CPages=" + pageNum + "&threadType=" + threadType;
                                RequestDispatcher rd = req.
                                        getRequestDispatcher(
                                        url);
                                // ((HttpServletResponse)res).sendRedirect(url);
                                rd.forward(req, res);
                                    return;
                            }
                        }
                    }
                }
                else {
                    // t-0-200-1-201.html --> showtopic.jsp?rootid=200&CPages=1#201
                    // t-1-200-200.html   --> showtopic_tree.jsp?rootid=200&showid=200
                    p = uri.indexOf("t-");
                    if (p>0) {
                        char mode = uri.charAt(p + 2);
                        if (mode == '0') {
                            p = p + 4;
                            int q = uri.indexOf("-", p + 1);
                            if (q > 0) {
                                String rootid = uri.substring(p, q);
                                String pageNum = uri.substring(q + 1,
                                        uri.length() - 5);
                                if (StrUtil.isNumeric(pageNum)) {
                                    String url = "showtopic.jsp?rootid=" +
                                                 rootid +
                                                 "&CPages=" + pageNum;
                                    RequestDispatcher rd = req.
                                            getRequestDispatcher(url);
                                    rd.forward(req, res);
                                    return;
                                }
                            }
                        }
                        else if (mode=='1') { // 树形
                            p = p + 4;
                            int q = uri.indexOf("-", p + 1);
                            if (q > 0) {
                                String rootid = uri.substring(p, q);
                                String showid = uri.substring(q + 1, uri.length() - 5);
                                if (StrUtil.isNumeric(showid)) {
                                    String url = "showtopic_tree.jsp?rootid=" + rootid +
                                                 "&showid=" + showid;
                                    RequestDispatcher rd = req.getRequestDispatcher(url);
                                    rd.forward(req, res);
                                    return;
                                }
                            }
                        }
                    }
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
