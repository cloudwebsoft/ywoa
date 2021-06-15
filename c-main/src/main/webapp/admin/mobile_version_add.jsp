<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.account.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.BasicDataMgr" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@page import="cn.js.fan.db.Paginator" %>
<%@page import="cn.js.fan.db.ListResult" %>
<%@page import="com.redmoon.oa.basic.SelectMgr" %>
<%@page import="com.redmoon.oa.basic.SelectDb" %>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@page import="com.redmoon.oa.basic.SelectOptionDb" %>
<%@ page import="com.redmoon.oa.android.SystemUpMgr" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>版本信息添加</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
    <script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
    <script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script>
        var curObjId;

        function selectNode(code, name) {
            $(curObjId).value = code;
            $(curObjId + "Desc").value = name;
        }
    </script>
</head>
<body>
<form name="form1" action="mobile_version_do.jsp?op=add" method="post">
    <table cellSpacing="0" cellPadding="0" width="100%">
        <tbody>
        <tr>
            <td class="tdStyle_1">手机版本信息</td>
        </tr>
        </tbody>
    </table>
    <br/>
    <table class=tabStyle_1 cellSpacing=0 cellPadding=0 width="98%">
        <tbody>
        <TR>
            <TD class=tabStyle_1_title colSpan=4>手机客户端版本</TD>
        </TR>
        </tbody>
        <TR>
            <TD width="22%" align=right>版&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本：</TD>
            <TD align=left width="21%"><INPUT title=版本 name=version_num id="version_num">&nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font></TD>
        </TR>
        <TR>
            <TD width="17%" align=right>信&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;息：</TD>
            <TD width="40%" align=left>
                <textarea name="version_name" cols="50" rows="3" id="version_name" title="名称"></textarea>&nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font>
            </TD>
        </TR>
        <TR>
            <TD align="right">客&nbsp;&nbsp;&nbsp;&nbsp;户&nbsp;&nbsp;&nbsp;&nbsp;端：</TD>
            <TD align="left">
                <select id="client" name="client">
                    <option value="android" selected="selected">Android</option>
                    <option value="ios">Ios</option>
                </select></TD>
        </TR>
    </table>
    <table width="98%" align="center">
        <tr>
            <td width="100%" align="center">
				<input type="submit" value="确定" class="btn"/>
				&nbsp;&nbsp;&nbsp;&nbsp;
				<input type="button" value="返回" class="btn" onclick="window.history.back()"/>
			</td>
        </tr>
    </table>
</form>
</body>
</html>
