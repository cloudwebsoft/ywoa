<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%
com.redmoon.oa.pvg.Privilege privilegeInc = new com.redmoon.oa.pvg.Privilege();
String mynameTop = ParamUtil.get(request, "userName");
if(mynameTop.equals("")){
	mynameTop = privilegeInc.getUser(request);
}

ModulePrivDb mpdTop = new ModulePrivDb("project");
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/project/project_list.jsp?userName=<%=StrUtil.UrlEncode(mynameTop)%>"><span>我参与的项目</span></a></li>
    <li id="menu2"><a href="<%=request.getContextPath()%>/project/project_list.jsp?showType=mine&userName=<%=StrUtil.UrlEncode(mynameTop)%>"><span>我创建的项目</span></a></li>
    <li id="menu4"><a href="<%=request.getContextPath()%>/project/project_list.jsp?showType=favorite&userName=<%=StrUtil.UrlEncode(mynameTop)%>"><span>关注的项目</span></a></li>
	<%if (mpdTop.canUserAppend(privilegeInc.getUser(request))) {%>
    <li id="menu3"><a href="<%=request.getContextPath()%>/visual/project_add.jsp?formCode=project"><span>创建项目</span></a>
	<%}%>
	</li>
  </ul>
</div>
