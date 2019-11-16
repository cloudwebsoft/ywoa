<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="com.redmoon.oa.android.system.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<html>
<head><title>选择图标</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link href="../<%=SkinMgr.getSkinPath(request)%>/css.css" rel="stylesheet" type="text/css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
</head>
<body>
<%
    String op = ParamUtil.get(request, "op");
    if (op.equals("add")) {
        try {
            MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
            if (!mr.isExist(request)) {
                out.print(StrUtil.jAlert_Back("已经存在,不能重复添加！", "提示"));
                return;
            }

            boolean re = mr.create(request);
            if (re) {
                out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "mobile_applist_config_list.jsp"));
            } else {
                out.print(StrUtil.jAlert_Back("操作失败！", "提示"));
            }
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        }
    } else if (op.equals("edit")) {
        try {
            String id = ParamUtil.get(request, "id");
            MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
            boolean re = mr.save(request);
            if (re) {
                out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "mobile_applist_config_edit.jsp?id=" + id));
            } else {
                out.print(StrUtil.jAlert_Back("操作失败！", "提示"));
            }
        } catch (ErrMsgException e) {
            out.print(StrUtil.Alert_Back(e.getMessage()));
        }
    }
%>
</body>
</html>