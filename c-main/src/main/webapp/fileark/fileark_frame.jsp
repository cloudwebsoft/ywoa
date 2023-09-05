<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>文件柜</title>
    <script>
        function setCols(cols) {
            frm.cols = cols;
        }

        function getCols() {
            return frm.cols;
        }
    </script>
</head>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    String dirCode = ParamUtil.get(request, "dir_code");
%>
<frameset id="frm" cols="200,*" framespacing="3" frameborder="1">
    <noframes>
        <body></body>
    </noframes>
    <frame src="fileark_left.jsp?dir_code=<%=StrUtil.UrlEncode(dirCode)%>" id="leftFileFrame" name="leftFileFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1"/>
    <frame src="fileark_main.jsp?dir_code=<%=StrUtil.UrlEncode(dirCode)%>" id="mainFileFrame" name="mainFileFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1"/>
</frameset>
</html>
