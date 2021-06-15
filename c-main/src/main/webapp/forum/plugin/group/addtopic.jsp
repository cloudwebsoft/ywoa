<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%><script src="<%=request.getContextPath()%>/inc/common.js"></script>
<TABLE width="100%">
  <TBODY>
	<TR>
	  <TD height="23" align="left" bgcolor="#F9FAF3">圈子：
	    <%
	  long groupId = ParamUtil.getLong(request, "groupId", -1);
	  GroupDb gd = new GroupDb();
	  Privilege pvg = new Privilege();
	  String sql = gd.getTable().getSql("listattend");
	  Iterator ir = gd.list(sql, new Object[] {pvg.getUser(request)}).iterator();
	  while (ir.hasNext()) {
	  	gd = (GroupDb)ir.next();
		if (gd==null) {
			continue;
		}
	  %>
	  	<input name="group_id" value="<%=gd.getLong("id")%>" type="checkbox" <%=groupId==gd.getLong("id")?"checked":""%> />
	  	<%=gd.getString("name")%>
	    <%
	  }
	  %></TD>
    </TR>
  </TBODY>
</TABLE>
