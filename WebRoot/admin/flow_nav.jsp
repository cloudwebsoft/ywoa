<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%
String typeCodeTop = ParamUtil.get(request, "typeCode");
Leaf toplf = new Leaf();
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/admin/flow_list.jsp?typeCode=<%=StrUtil.UrlEncode(typeCodeTop)%>"><span>流程列表</span></a></li>
    <li id="menu2"><a href="<%=request.getContextPath()%>/admin/flow_stat_month.jsp?typeCode=<%=StrUtil.UrlEncode(typeCodeTop)%>"><span>月统计</span></a></li>
    <li id="menu3"><a href="<%=request.getContextPath()%>/admin/flow_stat_year.jsp?typeCode=<%=StrUtil.UrlEncode(typeCodeTop)%>"><span>年统计</span></a></li>
    <%if (!typeCodeTop.equals("")) {
		toplf = toplf.getLeaf(typeCodeTop);
		if (toplf.getType()!=Leaf.TYPE_FREE) {	
	%>
		    <li id="menu4"><a href="<%=request.getContextPath()%>/flow/flow_analysis_year.jsp?typeCode=<%=StrUtil.UrlEncode(typeCodeTop)%>"><span>效率分析</span></a></li>
    <%	}
	}%>
  </ul>
</div>