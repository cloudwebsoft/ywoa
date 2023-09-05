package com.cloudwebsoft.framework.test;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import cn.js.fan.util.ParamUtil;
import java.util.Calendar;

public class HTMLGenerator extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=utf-8";

    // Initialize global variables
    public void init() throws ServletException {
    }

    // Process the HTTP Get request
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        response.setContentType(CONTENT_TYPE);
        service(request, response);
    }

    // Process the HTTP Post request
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        doGet(request, response);
    }

    public void destroy() {
    }

    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // http://localhost:8080/cwbbs/HTMLGenerator?fileName=doc_show&p0=id&v0=1
        String url = "";
        String name = "";

        ServletContext sc = getServletContext();

        String file_name = request.getParameter("fileName"); // 你要访问的jsp文件,如index.jsp
        // 则你访问这个servlet时加参数.如http://localhost/toHtml?file_name=index
        String p0 = ParamUtil.get(request, "p0"); // dir_code或者id
        String v0 = ParamUtil.get(request, "v0"); //
        String p1 = ParamUtil.get(request, "p1");
        String v1 = ParamUtil.get(request, "v1");

        Calendar cal = Calendar.getInstance();
        String year = "" + (cal.get(cal.YEAR));
        String month = "" + (cal.get(cal.MONTH) + 1);

        String filepath = "article/" + year + "/" + month;
        url = "/" + file_name + ".jsp?" + p0 + "=" + v0;
        if (!p1.equals(""))
            url = url + "&" + p1 + "=" + v1;

        // url = "/" + file_name + ".jsp"; // 这是要生成HTML的jsp文件,如//http://localhost/index.jsp的执行结果.
        String realPath = sc.getRealPath("/");
        name = v0 + ".htm"; // 这是生成的html文件名,如index.htm.
        String visualPath = filepath + "/" + name;
        String fullPath = realPath + visualPath; // 生成html的完整路径

        RequestDispatcher rd = sc.getRequestDispatcher(url);

        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        final ServletOutputStream stream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            public void write(byte[] data, int offset, int length) {
                os.write(data, offset, length);
            }

            public void write(int b) throws IOException {
                os.write(b);
            }
        };

        final PrintWriter pw = new PrintWriter(new OutputStreamWriter(os,
                "utf-8"));

        HttpServletResponse rep = new HttpServletResponseWrapper(response) {
            public ServletOutputStream getOutputStream() {
                return stream;
            }

            public PrintWriter getWriter() {
                return pw;
            }
        };

        rd.include(request, rep);

        pw.flush();

        FileOutputStream fos = new FileOutputStream(fullPath); // 把jsp输出的内容写到指定路径的htm文件中
        os.writeTo(fos);
        fos.close();

        response.sendRedirect(visualPath); // 书写完毕后转向htm页面
    }

}
