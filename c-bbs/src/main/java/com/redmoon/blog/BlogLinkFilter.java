package com.redmoon.blog;

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
public class BlogLinkFilter implements Filter {
    FilterConfig config;
    public static Config cfg = Config.getInstance();

    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws ServletException,
            IOException {
        if (req instanceof HttpServletRequest) {
            if (cfg.getBooleanProperty("isDomainMapToPath")) {
                HttpServletRequest request = (HttpServletRequest) req;
                // url=http://localhost:8080/cwbbs/log/asdfsadf.html requestURI=/cwbbs/log/asdfsadf.html
                // f-0-sqzw-25-30.html --> listtopic.jsp?boardcode=sqzw&CPages=25&threadType=30
                // f-1-sqzw-25.html --> listtopic_tree.jsp?boardcode=sqzw&CPages=25
                String uri = request.getRequestURI();
                // System.out.println(getClass() + " uri=" + uri);
                if (uri.indexOf(".")==-1) {
                    int p = uri.indexOf("blog");
                    String domainField = uri.substring(p + 5);
                    if (!domainField.equals("") && !domainField.startsWith("admin")) {
                        String url = "myblog.jsp?blogId=";

                        if (StrUtil.isNumeric(domainField)) {
                            url += domainField;
                        } else {
                            UserConfigDb ucd = new UserConfigDb();
                            long blogId = ucd.getBlogIdByDomain(domainField);
                            url += blogId;
                        }

                        // System.out.println(getClass() + " " + url + " domain=" + domainField);

                        RequestDispatcher rd = req.
                                               getRequestDispatcher(
                                url);
                        // ((HttpServletResponse)res).sendRedirect(url);
                        rd.forward(req, res);
                        return;
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
