package com.cloudwebsoft.framework.servlet;

import com.cloudwebsoft.framework.util.LogUtil;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import cn.js.fan.util.StrUtil;

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
public class JspIncluder {
    public JspIncluder() {
    }

    public static void include(HttpServletRequest request, HttpServletResponse response, JspWriter out, String relativePath) {
        try {
            RequestDispatcher rd = request.getRequestDispatcher(
                    relativePath);
            // 追加在插入plugin的语句之后
            rd.include(request,
                       new ServletResponseWrapperInclude(response, out));
            // 插入在header.jsp后
            // rd.include(request, response);
        } catch (Exception e) {
            LogUtil.getLog(JspIncluder.class.getName()).error("include:" + StrUtil.trace(e));
        }
    }
}
