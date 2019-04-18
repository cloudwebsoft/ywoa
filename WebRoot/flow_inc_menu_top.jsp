<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<%@ page import="cn.js.fan.util.*"%>
<%
String mynameTop = ParamUtil.get(request, "userName");
%>
<div class="tabs1Box">
<DIV id="tabs1">
  <ul>
	<li id="menu1"><a href="<%=request.getContextPath()%>/flow/flow_list.jsp?displayMode=<%=WorkflowMgr.DISPLAY_MODE_ATTEND%>&userName=<%=StrUtil.UrlEncode(mynameTop)%>"><span><lt:Label res="res.flow.Flow" key="iInFlow"/></span></a> </li>
	<li id="menu2"><a href="<%=request.getContextPath()%>/flow/flow_list.jsp?displayMode=<%=WorkflowMgr.DISPLAY_MODE_MINE%>&userName=<%=StrUtil.UrlEncode(mynameTop)%>"><span><lt:Label res="res.flow.Flow" key="initiatedFlow"/></span></a> </li>
	<li id="menu3"><a href="<%=request.getContextPath()%>/flow_list_done.jsp?userName=<%=StrUtil.UrlEncode(mynameTop)%>"><span><lt:Label res="res.flow.Flow" key="handleRecord"/></span></a></li>
	<li id="menu4"><a href="<%=request.getContextPath()%>/flow/flow_user_performance.jsp?userName=<%=StrUtil.UrlEncode(mynameTop)%>"><span><lt:Label res="res.flow.Flow" key="achievements"/></span></a></li>
  </ul>
</DIV>
</div>
