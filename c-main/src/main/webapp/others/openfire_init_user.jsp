<%@ page contentType="text/html; charset=utf-8" language="java" %>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.cloudwebsoft.framework.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.sso.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.util.file.*"%>
<html>
<head><title>初始化Openfire用户</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<%
    SyncUtil su = new SyncUtil();
	UserDb user = new UserDb();
	DeptUserDb dud = new DeptUserDb();
	Iterator ir = user.list().iterator();
	while (ir.hasNext()) {
		user = (UserDb)ir.next();
		Vector v = dud.getDeptsOfUser(user.getName());
		String deptCode = "";
		if (v.size()>0) {
			DeptDb dd = (DeptDb)v.elementAt(0);
			if (dd.isLoaded()) {
				deptCode = dd.getCode();
				su.userSync(user.getName(), deptCode, SyncUtil.CREATE, new Privilege().getUser(request));
			}
		}
		// System.out.println("init_user: " + user.getRealName());
	}
	out.print("操作成功！");
%>
</body>
</html> 
