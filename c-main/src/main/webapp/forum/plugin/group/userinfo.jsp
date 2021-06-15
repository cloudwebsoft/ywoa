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
String userName = ParamUtil.get(request, "username");
%>
<div><strong>创建的圈子</strong>：
        <%
	  GroupDb gd = new GroupDb();
	  String sql = gd.getTable().getSql("listmine");
	  Iterator ir = gd.list(sql, new Object[] {userName}).iterator();
	  while (ir.hasNext()) {
	  	gd = (GroupDb)ir.next();
		if (gd==null)
			continue;
	  %>
          <a href="forum/plugin/group/group.jsp?id=<%=gd.getLong("id")%>" target="_blank"><%=StrUtil.toHtml(gd.getString("name"))%></a>&nbsp;&nbsp;
          <%
	  }
	  %>
</div>
<div><strong>参与的圈子</strong>：
        <%
	  sql = gd.getTable().getSql("listattend");
	  ir = gd.list(sql, new Object[] {userName}).iterator();
	  while (ir.hasNext()) {
	  	gd = (GroupDb)ir.next();
		if (gd==null)
			continue;
	  %>
         <a href="forum/plugin/group/group.jsp?id=<%=gd.getLong("id")%>" target=_blank><%=gd.getString("name")%></a>&nbsp;&nbsp;
      <%
	  }
	  %>
</div>
