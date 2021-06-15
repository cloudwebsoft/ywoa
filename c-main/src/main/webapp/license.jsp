<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*,
                 java.text.*,
                 cn.js.fan.util.*,
                 com.redmoon.oa.fileark.*,
                 cn.js.fan.cache.jcs.*,
                 cn.js.fan.web.*,
                 com.redmoon.oa.kernel.*,
                 com.redmoon.oa.pvg.*"
%>
<!DOCTYPE HTML>
<html>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="admin/default.css" rel="stylesheet" type="text/css">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="backup" scope="page" class="cn.js.fan.util.Backup"/>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
    <tbody>
    <tr>
        <td class="head">系统信息</td>
    </tr>
    </tbody>
</table>
<br>
<TABLE
        style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid"
        cellSpacing=0 cellPadding=3 width="95%" align=center>
    <!-- Table Head Start-->
    <TBODY>
    <TR>
        <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%"><font size="-1">许可证信息</font></TD>
    </TR>
    <TR class=row style="BACKGROUND-COLOR: #fafafa">
        <TD height="175" align="left" style="PADDING-LEFT: 10px"><p>授权单位：
          <%
          License license = License.getInstance();
          %>
          <%=license.getCompany()%> <br>
          用户数：<%=license.getUserCount()%> <br>
          类型：<%=license.getType()%> <br>
          到期时间：<%=DateUtil.format(license.getExpiresDate(), "yyyy-MM-dd")%> <br>
          域名：<%=license.getDomain()%><br/>
          流程设计器：<%=license.getFlowDesigner()%>
        </TD>
    </TR>
    <!-- Table Body End -->
    <!-- Table Foot -->
    <TR>
        <TD class=tfoot align=right>&nbsp;</TD>
    </TR>
    <!-- Table Foot -->
    </TBODY>
</TABLE>
</body>
</html>