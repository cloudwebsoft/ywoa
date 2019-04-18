<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.sys.SysUtil" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>系统初始化</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script src="<%=request.getContextPath()%>/js/jquery-1.9.1.min.js"></script>
    <script src="<%=request.getContextPath()%>/js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="<%=request.getContextPath()%>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
    <TBODY>
    <TR>
        <TD class="tdStyle_1">清空系统</TD>
    </TR>
    </TBODY>
</TABLE>
<br>
<%
    if (!privilege.isUserPrivValid(request, "admin")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");
    if ("init".equals(op)) {
        try {
            SysUtil.initSystem();
        }
        catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
            return;
        }
        out.print(StrUtil.jAlert_Back("操作成功", "提示"));
        return;
    }
%>
<div style="text-align: center; margin-top: 30px">
    <input type="button" onclick="initSys()" class="btn" value="系统清空"/>
</div>
</body>
<script>
    function initSys() {
        jConfirm('您确定要清空系统么？清空前请做好数据及文件的备份！', '提示', function(r) {
            if (!r) {
                return;
            }
            jConfirm('请再次确认要清空系统么？清空前如果未做好备份，可能会丢失数据！', '提示', function(r) {
                if (!r) {
                    return;
                }
                window.location.href="sys_init.jsp?op=init";
            });
        });
    }
</script>
</html>
