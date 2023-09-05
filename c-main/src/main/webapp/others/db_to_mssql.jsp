<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="cn.js.fan.security.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=Global.AppName%> - <%=Global.server%></title>
<style type="text/css">
<!--
body {
	margin-top: 0px;
	margin-bottom: 0px;
}
-->
</style>
<link href="css.css" rel="stylesheet" type="text/css">
<style type="text/css">
<!--
body,td,th {
	font-size: 12px;
}
-->
</style>
<link href="index.css" rel="stylesheet" type="text/css">
</head>

<body>
<%
TransmitData td = new TransmitData();
//td.getTableStruct("bc");
td.mysqlToMSSql(out, "zjrj");
/*
TransmitData td = new TransmitData();
ResultSet rs_table = td.getTableNames();
int i = 0;

while (rs_table.next()){  
	rs_table.getObject(3);

	ResultSet rs_column = td.getColumns(rs_table.getObject(3).toString());
	while (rs_column.next()){
        ResultSetMetaData rsmd = rs_column.getMetaData();
		out.print(i);
		out.print(rs_column.isLast());
	}  
	out.print("<br>");
}
*/
%>
</body>
</html>
