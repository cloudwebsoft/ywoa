<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "rtx.*"%>
<html>
<head>
<title>init dept</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="common.css" type="text/css">
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%
RTXUtil.initDepts("root");
/*
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery("select * from department order by layer");
RTXSvrApi RtxsvrapiObj = new RTXSvrApi();
DeptDb dd2 = new DeptDb();
if (RtxsvrapiObj.Init()) {
	DeptDb dd = new DeptDb();
	while (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		if (rr.getString("code").equals("root"))
			continue;
		out.println(rr.getString("code") + "--" + rr.getString("name") + "<BR>");
		dd = dd2.getDeptDb(rr.getString("code"));
		RTXUtil.initDept(dd, RtxsvrapiObj);
	}
}
RtxsvrapiObj.UnInit();
*/
%>
</body>
</html>