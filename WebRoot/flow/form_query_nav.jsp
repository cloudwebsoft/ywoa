<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%
boolean isSystemTop = ParamUtil.get(request, "isSystem").equals("true");
%>
<div id="tabs1">
  <ul>
 	<li id="menu0"><a href="form_query_list.jsp?isSystem=<%=isSystemTop%>"><span>流程查询</span></a></li>
    <%if (isSystemTop) {%>
 	<!--<li id="menu1"><a href="form_query_warrant_list.jsp?isSystem=<%=isSystemTop%>"><span>授权查询</span></a></li>-->
    <%}%>
 	<li id="menu2"><a href="javascript:;" onclick="addTab('查询设计器', '<%=request.getContextPath()%>/flow/designer/designer.jsp?isSystem=<%=isSystemTop%>')"><span>查询设计器</span></a></li>
	<%if (com.redmoon.oa.kernel.License.getInstance().isPlatform()) {%>
 	<li id="menu3"><a href="javascript:;" onclick="addTab('自由查询', '<%=request.getContextPath()%>/flow/form_query_script.jsp?isSystem=<%=isSystemTop%>')"><span>自由查询</span></a></li>
	<%}%>    
  </ul>
</div>
