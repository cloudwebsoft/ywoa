<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%
com.redmoon.oa.pvg.Privilege pvgTop = new com.redmoon.oa.pvg.Privilege();
String deptCodeTop = ParamUtil.get(request, "deptCode");
DeptDb ddTop = new DeptDb();
ddTop = ddTop.getDeptDb(deptCodeTop);
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/crm/crm_dept_user.jsp?deptCode=<%=StrUtil.UrlEncode(deptCodeTop)%>"><span><%=ddTop.getName()%></span></a></li>
<!--
    <li id="menu2"><a href="<%=request.getContextPath()%>/crm/workplan_jd_qy.jsp?deptCode=<%=StrUtil.UrlEncode(deptCodeTop)%>"><span>进度</span></a></li>
    <li id="menu3"><a href="<%=request.getContextPath()%>/crm/linkman_contact_not_qy.jsp?deptCode=<%=StrUtil.UrlEncode(deptCodeTop)%>"><span>未完成</span></a></li>
-->
  </ul>
</div>