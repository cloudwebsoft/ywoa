<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%
	String deptCode = ParamUtil.get(request, "deptCode");

	if (deptCode.equals("")) {
		out.print(SkinUtil.makeInfo(request, "请选择某个部门！"));
		return;
	}
	Privilege pvgTop = new Privilege();
	if (!pvgTop.canUserAdminDept(request, deptCode)) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	com.redmoon.oa.dept.DeptDb dd = new com.redmoon.oa.dept.DeptDb();
	dd = dd.getDeptDb(deptCode);
	if (dd==null || !dd.isLoaded()) {
		out.print(StrUtil.Alert("部门" + deptCode + "不存在！"));
		return;
	}

%>
<div class="tabs1Box">
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="flow_performance_user_list.jsp?deptCode=<%=StrUtil.UrlEncode(deptCode)%>"><span>月绩效统计</span></a></li>
    <li id="menu2"><a href="flow_performance_user_list_sum.jsp?deptCode=<%=StrUtil.UrlEncode(deptCode)%>"><span>月流程处理次数</span></a></li>
    <li id="menu3"><a href="flow_performance_user_list_average.jsp?deptCode=<%=StrUtil.UrlEncode(deptCode)%>"><span>月平均绩效</span></a></li>
  </ul>
</div>
</div>