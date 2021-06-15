package com.cloudwebsoft.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspWriter;

/**
 * 参考自tomcat，使include可以追加到页面中，而不是在页面的底端，直接使用RequestDispatcher.include会把include的内容置于页面的顶端
 * 详见com.redmoon.forum.plugin.debate.DebateViewShowMsg.render(...)
 * ServletResponseWrapper used by the JSP 'include' action.
 *
 * This wrapper response object is passed to RequestDispatcher.include(), so
 * that the output of the included resource is appended to that of the
 * including page.
 */

public class ServletResponseWrapperInclude extends HttpServletResponseWrapper {

    /**
     * PrintWriter which appends to the JspWriter of the including page.
     */
    private PrintWriter printWriter;

    private JspWriter jspWriter;

    public ServletResponseWrapperInclude(ServletResponse response,
                                       JspWriter jspWriter) {
      super((HttpServletResponse)response);
      this.printWriter = new PrintWriter(jspWriter);
      this.jspWriter = jspWriter;
    }

    /**
     * Returns a wrapper around the JspWriter of the including page.
     */
    public PrintWriter getWriter() throws IOException {
      return printWriter;
    }

    public ServletOutputStream getOutputStream() throws IOException {
      throw new IllegalStateException();
    }

    /**
     * Clears the output buffer of the JspWriter associated with the including
     * page.
     */
    public void resetBuffer() {

    }
}
