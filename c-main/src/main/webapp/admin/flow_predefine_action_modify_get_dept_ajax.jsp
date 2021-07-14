<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%
String dept = ParamUtil.get(request, "dept");
String deptName = "";
if (!dept.equals("")) {
	String[] deptAry = StrUtil.split(dept, ",");
	int len = 0;
	if (deptAry!=null) {
		len = deptAry.length;
	}
	
	com.redmoon.oa.dept.DeptMgr dm = new com.redmoon.oa.dept.DeptMgr();
	for (int i=0; i<len; i++) {
		DeptDb dd = dm.getDeptDb(deptAry[i]);
		String dName = "";
		if (dd.isLoaded()) {
			dName = dd.getName();
		}
		else {
			dName = "不存在(" + deptAry[i] + ")";
		}
		if (deptName.equals("")) {
			deptName = dName;
		} else {
			deptName += "," + dName;
		}
	}
	out.print(deptName);
}
%>