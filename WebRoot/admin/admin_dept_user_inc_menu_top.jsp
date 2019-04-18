<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%
String deptCodeTop = ParamUtil.get(request, "deptCode");
KaoqinPrivilege kpvg = new KaoqinPrivilege();
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/admin/admin_dept_user.jsp?deptCode=<%=StrUtil.UrlEncode(deptCodeTop)%>"><span>部门用户</span></a></li>
    <li id="menu5"><a href="<%=request.getContextPath()%>/mywork/mywork_dept_day.jsp?deptCode=<%=StrUtil.UrlEncode(deptCodeTop)%>"><span>日报</span></a></li>
    <li id="menu6"><a href="<%=request.getContextPath()%>/mywork/mywork_dept_week.jsp?deptCode=<%=StrUtil.UrlEncode(deptCodeTop)%>"><span>周报</span></a></li>
    <li id="menu7"><a href="<%=request.getContextPath()%>/mywork/mywork_dept_month.jsp?deptCode=<%=StrUtil.UrlEncode(deptCodeTop)%>"><span>月报</span></a></li>
	<%if (com.redmoon.oa.kernel.License.getInstance().isEnterprise() || com.redmoon.oa.kernel.License.getInstance().isGroup() || com.redmoon.oa.kernel.License.getInstance().isPlatform()) {%>    
    <li id="menu4"><a href="<%=request.getContextPath()%>/workplan/workplan_burthen_dept.jsp?deptCode=<%=StrUtil.UrlEncode(deptCodeTop)%>"><span>工作计划负荷</span></a></li>
    <%}
	%>
    <li id="menu8"><a href="<%=request.getContextPath()%>/admin/flow_overdue_list.jsp?deptCode=<%=StrUtil.UrlEncode(deptCodeTop)%>"><span>超期流程</span></a></li>
  </ul>
</div>