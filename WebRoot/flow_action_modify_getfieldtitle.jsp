<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%
String dept = ParamUtil.get(request, "dept");
String deptName = "";
String userNames = ParamUtil.get(request, "userNames");
if (!dept.equals("")) {
	String[] deptAry = dept.split(",");
	int len = deptAry.length;
	com.redmoon.oa.dept.DeptMgr dm = new com.redmoon.oa.dept.DeptMgr();
	for (int i=0; i<len; i++) {
		if (deptName.equals(""))
			deptName = dm.getDeptDb(deptAry[i]).getName();
		else
			deptName += "," + dm.getDeptDb(deptAry[i]).getName();
	}
	%>
	<script>
    window.parent.setDeptName("<%=deptName%>");
    </script>
	<%
}

if (!userNames.equals("")) {
	String userRealNames = "";
	String[] ary = StrUtil.split(userNames, ",");
	if (ary!=null) {
		int len = ary.length;
		UserDb user = new UserDb();
		for (int i=0; i<len; i++) {
			user = user.getUserDb(ary[i]);
			if (userRealNames.equals("")) {
				userRealNames = user.getRealName();
			}
			else {
				userRealNames += "," + user.getRealName();
			}
		}
		%>
		<script>
		window.parent.setUsers("<%=userNames%>", "<%=userRealNames%>");
		</script>
        <%
	}
}
%>
