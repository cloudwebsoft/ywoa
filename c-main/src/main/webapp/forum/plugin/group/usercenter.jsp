<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String userName = privilege.getUser(request);
%>
<table width="100%" border="0" align="center" cellpadding="5" cellspacing="1">
  <tbody>
    <tr>
      <td height="23" align="left"><strong>创建的圈子</strong>：
        <%
	  long groupId = ParamUtil.getLong(request, "groupId", -1);
	  GroupDb gd = new GroupDb();
	  String sql = gd.getTable().getSql("listmine");
	  Iterator ir = gd.list(sql, new Object[] {privilege.getUser(request)}).iterator();
	  while (ir.hasNext()) {
	  	gd = (GroupDb)ir.next();
		if (gd==null)
			continue;
	  %>
          <a href="forum/plugin/group/group.jsp?id=<%=gd.getLong("id")%>" target="_blank"><%=gd.getString("name")%></a>&nbsp;&nbsp;
          <%
	  }
	  %>
      </td>
    </tr>
  </tbody>
</table>
<table width="100%" border="0" align="center" cellpadding="5" cellspacing="1">
  <tbody>
    <tr>
      <td height="23" align="left"><strong>参与的圈子</strong>：
        <%
	  sql = gd.getTable().getSql("listattend");
	  ir = gd.list(sql, new Object[] {privilege.getUser(request)}).iterator();
	  while (ir.hasNext()) {
	  	gd = (GroupDb)ir.next();
		if (gd==null)
			continue;
	  %>
         <a href="forum/plugin/group/group.jsp?id=<%=gd.getLong("id")%>" target=_blank><%=gd.getString("name")%></a>&nbsp;&nbsp;
      <%
	  }
	  %></td>
    </tr>
  </tbody>
</table>
