<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.json.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
%>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<%
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>报表列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../../inc/common.js"></script>
    <script src="../../js/jquery-1.9.1.min.js"></script>
    <script src="../../js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <script src="../../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../../js/jquery.form.js"></script>
</head>
<body>
<%
    long id = ParamUtil.getLong(request, "id", -1);
    if (id == -1) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_id")));
        return;
    }
    try {
        FormQueryReportRender fqrr = new FormQueryReportRender();
        out.print(fqrr.rend(request, id));
    } catch (ErrMsgException e) {
        out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
        e.printStackTrace();
    }
%>
</body>
</html>
