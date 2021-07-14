<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="javax.servlet.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String path = ParamUtil.get(request, "path");
    if (!(path.endsWith("png") || path.endsWith("jpg") || path.endsWith("gif") || path.endsWith("jpeg") || path.endsWith("bmp"))) {
        com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "任意文件读取 path=" + path);
        out.println(StrUtil.Alert_Redirect("Warn! u are a haker!", "index.jsp"));
        return;
    }
    if (path.startsWith(Global.getRootPath(request))) {
        path = path.substring(Global.getRootPath(request).length());
    } else if (path.startsWith("/")) {
        path = path.substring(1);
    }
    String file = "";
    try {
        file = Global.getRealPath() + path;
    } catch (Exception e) {
        e.printStackTrace();
    }

    int p = path.lastIndexOf(".");
    if (p != -1) {
        String ext = path.substring(p + 1);
        response.setContentType(MIMEMap.get(ext));
    }
    FileInputStream in = null;
    OutputStream o = null;
    try {
        if (!file.equals("")) {
            File tempFile = new File(file);
            if (!tempFile.exists()) {
                //path = path.replace(Global.AppName.toLowerCase() + "/","/");
                tempFile = new File(Global.getAppPath() + path);
            }
            o = response.getOutputStream();
            if (tempFile.exists()) {
                in = new FileInputStream(tempFile);
                int l = 0;
                byte[] buffer = new byte[4096];
                while ((l = in.read(buffer)) != -1) {
                    o.write(buffer, 0, l);
                }
                o.flush();
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (in != null) {
            in.close();
        }
        if (o != null) {
            o.close();
        }
    }
    out.clear();
    out = pageContext.pushBody();
%>