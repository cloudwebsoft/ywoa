<%@ page contentType="text/html;charset=utf-8" %>
<%@page import="cn.js.fan.util.MIMEMap" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="cn.js.fan.util.StrUtil" %>
<%@page import="com.redmoon.oa.message.Attachment" %>
<%@page import="com.redmoon.oa.message.MessageDb" %>
<%@page import="com.redmoon.oa.message.MessageMgr" %>
<%@page import="java.io.BufferedInputStream" %>
<%@ page import="java.io.BufferedOutputStream" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.io.IOException" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = request.getParameter("priv");
    if (priv == null) {
        priv = "read";
    }
    if (!privilege.isUserPrivValid(request, priv)) {
        //response.setContentType("text/html;charset=gb2312");
        out.print("<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
        out.println(StrUtil.makeErrMsg("权限非法"));
        return;
    }

    int msgId = ParamUtil.getInt(request, "msgId");
    int attId = ParamUtil.getInt(request, "attachId");

    MessageMgr mm = new MessageMgr();
    MessageDb md = mm.getMessageDb(msgId);
    Attachment att = md.getAttachment(attId);

    response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
    response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(att.getName()));

    // 使客户端直接下载，上句会使IE在本窗口中打开文件，下句也一样
    // response.setContentType("application/octet-stream");
    // response.setHeader("Content-disposition","attachment; filename="+att.getName());

    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;

    try {
        bis = new BufferedInputStream(new FileInputStream(att.getFullPath()));
        bos = new BufferedOutputStream(response.getOutputStream());

        byte[] buff = new byte[2048];
        int bytesRead;

        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
        }
    } catch (final IOException e) {
        System.out.println("IOException." + e);
    } finally {
        if (bis != null) {
            bis.close();
        }
        if (bos != null) {
            bos.close();
        }
    }

    out.clear();
    out = pageContext.pushBody();

%>