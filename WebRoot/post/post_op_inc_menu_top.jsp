<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="com.redmoon.oa.post.*"%>
<%
int pid = cn.js.fan.util.ParamUtil.getInt(request, "post_id", 0);
String pname = "";
if (pid != 0) {
	PostDb post = new PostDb();
	post = post.getPostDb(pid);
	pname = post.getString("name");
}
%>
<div id="tabs1">
  <ul>
  	<li id="menu0"><a href="post_m.jsp"><span>岗位列表</span></a></li>
  
    <li id="menu1"><a href="post_op.jsp?op=edit&post_id=<%=pid%>"><span><%=pname%></span></a></li>
   
    <li id="menu2"><a href="post_user.jsp?post_id=<%=pid %>"><span>人员</span></a></li>
    <%
	if (com.redmoon.oa.kernel.License.getInstance().canUseSolution(com.redmoon.oa.kernel.License.SOLUTION_PERFORMANCE)) {   
    %>
    <li id="menu3"><a href="post_flow.jsp?post_id=<%=pid %>"><span>绩效考核流程</span></a></li>
    <%}%>
  </ul>
</div>

