<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>通讯录-选择</title>
</head>
<%
    String type = ParamUtil.get(request, "type");
    String mode = ParamUtil.get(request, "mode");
    com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

    try {
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "type", type, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }

    try {
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mode", mode, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }

%>
<frameset rows="*" cols="180,*,220" framespacing="0" border="0">
    <frame src="address_left_sel.jsp?type=<%=type%>&mode=<%=mode%>" name="leftAddressFrame">
    <frame src="address_list_sel.jsp?type=<%=type%>&mode=<%=mode%>" name="mainAddressFrame">
    <frame src="address_sel.jsp" name="rightAddressFrame">
</frameset>
<noframes>
    <body>
    </body>
</noframes>
</html>
