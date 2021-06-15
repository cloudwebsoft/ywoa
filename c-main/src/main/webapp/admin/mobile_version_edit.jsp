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
<%@ page import="java.text.*" %>
<%@page import="com.redmoon.oa.pvg.Privilege" %>
<%@page import="com.redmoon.oa.android.SystemUpDb" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    int id = ParamUtil.getInt(request, "id");
    String CPages = ParamUtil.get(request, "CPages");
    String pageSize = ParamUtil.get(request, "pageSize");
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>版本信息修改</title>
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
<%
    SystemUpDb d = new SystemUpDb();
    d = d.getSystemUpDb(id);
    String client = d.getString("client");
%>
<form name="form1" action="mobile_version_do.jsp?op=edit" method="post">
	<table cellSpacing="0" cellPadding="0" width="100%">
		<tbody>
		<tr>
			<td class="tdStyle_1">手机版本信息</td>
		</tr>
		</tbody>
	</table>
    <input type="hidden" name="id" value="<%=id%>"/>
    <input type="hidden" name="CPages" value="<%=CPages%>"/>
    <input type="hidden" name="pageSize" value="<%=pageSize%>"/>
	<br/>
    <TABLE class="tabStyle_1" cellSpacing="0" cellPadding="0" width="98%">
        <TBODY>
        <TR>
            <TD class="tabStyle_1_title" colSpan="2">手机客户端版本</TD>
        </TR>
        <TR>
            <TD align="right" width="30%">版&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本：</TD>
            <TD align="left" width="70%"><INPUT title="版本" name="version_num" id="version_num" value="<%=d.getString("version_num")%>">&nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font></TD>
        </TR>
        <TR>
            <TD align="right">信&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;息：</TD>
            <TD align="left"><textarea name="version_name" cols="70" rows="3" id="version_name" title="名称"><%=d.getString("version_name")%></textarea>&nbsp;&nbsp;&nbsp;<font color="#FF0000">*</font></TD>
        </TR>
        <TR>
            <TD align="right">客&nbsp;&nbsp;&nbsp;&nbsp;户&nbsp;&nbsp;&nbsp;&nbsp;端：</TD>
            <TD align="left">
                <select id="client" name="client">
                    <option value="android" <% if (client.equals("android")) { %> selected="selected"<%} %>>Android</option>
                    <option value="ios" <% if (client.equals("ios")) { %> selected="selected"<%} %>>Ios</option>
                </select></TD>
        </TR>
    </TABLE>
    <table width="98%" align="center">
        <tr>
            <td width="100%" align="center">
                <input type="submit" value="确定" class="btn"/>
            </td>
        </tr>
    </table>&nbsp;<span style="margin:0 30px 0 0" class="tishi">&nbsp;</span>
</form>
</body>
</html>