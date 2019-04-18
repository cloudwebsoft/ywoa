<%@ page contentType="text/html;charset=utf-8" %>
<%@page import="cn.js.fan.util.*" %>
<%@page import="cn.js.fan.web.Global" %>
<%@page import="com.redmoon.oa.*" %>
<%@page import="java.io.*" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String user = ParamUtil.get(request, "user");
    if ("".equals(user)) {
        if (!privilege.isUserPrivValid(request, "read")) {
            System.out.println("警告非法用户，你无访问此页的权限！");
            return;
        }
    }

    String filename = ParamUtil.get(request, "filename");
    String extname = request.getParameter("extname");
    String prop = request.getParameter("prop");
    if (filename == null) {
        System.out.println("缺少文件名！");
        return;
    }

    filename = filename + "." + extname;

    Config cfg = new Config();
    String noticefilepath;
    if ("activex".equals(prop)) {
        noticefilepath = prop;
    } else {
        noticefilepath = cfg.get(prop);
    }

    String filePath = Global.getRealPath() + "/" + noticefilepath + "/" + filename;
    if ("li".equals(prop)) {
        filePath = Global.getRealPath() + "WEB-INF/" + prop + filename;
    }

    response.setContentType("application/" + extname);
    response.setHeader("Content-disposition", "attachment; filename=" + filename);

    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;

    try {
        bis = new BufferedInputStream(new FileInputStream(filePath));
        bos = new BufferedOutputStream(response.getOutputStream());

        byte[] buff = new byte[2048];
        int bytesRead;

        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
        }

    } catch (final IOException e) {
        System.out.println("出现IOException." + e);
    } finally {
        if (bis != null)
            bis.close();
        if (bos != null)
            bos.close();
    }

    out.clear();
    out = pageContext.pushBody();
%>



