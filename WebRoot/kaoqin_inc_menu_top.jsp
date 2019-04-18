<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import = "com.redmoon.oa.pvg.Privilege"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%
String topUserName = ParamUtil.get(request, "userName");
KaoqinPrivilege kpvgTop = new KaoqinPrivilege();
%>
<div id="tabs1">
  <ul>
  	<%if (topUserName.equals("") || (!topUserName.equals("") && (topUserName.equals(new Privilege().getUser(request)) || kpvgTop.canAdminKaoqin(request)))) {%>
    <li id="menu1"><a class="black" href="<%=request.getContextPath()%>/kaoqin_sxb.jsp?userName=<%=StrUtil.UrlEncode(topUserName)%>"><span>上下班考勤</span></a></li>
	<%}%>
    <li id="menu2"><a class="black" href="<%=request.getContextPath()%>/kaoqin.jsp?userName=<%=StrUtil.UrlEncode(topUserName)%>"><span>考勤详情</span></a></li>
    <li id="menu3"><a class="black" href="<%=request.getContextPath()%>/attendance/leave_list_user.jsp?userName=<%=StrUtil.UrlEncode(topUserName)%>"><span>请假记录</span></a></li>
    <li id="menu4"><a class="black" href="<%=request.getContextPath()%>/attendance/jb_list_user.jsp?userName=<%=StrUtil.UrlEncode(topUserName)%>"><span>加班记录</span></a></li>
	<!--
    <li id="menu3"><a class="black" href="kaoqin_other.jsp?type=<%=StrUtil.UrlEncode("外出")%>"><span>外出</span></a></li>
    <li id="menu4"><a class="black" href="kaoqin_other.jsp?type=<%=StrUtil.UrlEncode("请假")%>"><span>请假</span></a></li>
	-->
  </ul>
</div>

