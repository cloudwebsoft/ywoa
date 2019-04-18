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
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery("select * from department order by layer");
int k = 0;
String sql = "update department set id=? where code=?";
while (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	if (rr.getString("code").equals("root")) {
		jt.executeUpdate(sql, new Object[]{new Integer(0), rr.getString("code")});
		continue;
	}
	k++;
	jt.executeUpdate(sql, new Object[]{new Integer(k), rr.getString("code")});
	out.println(rr.getString("code") + "--" + rr.getString("name") + "--id=" + k + "<BR>");
}

%>
</body>
</html>