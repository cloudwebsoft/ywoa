<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.util.TransmitData" %>
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
Conn conn = null;
Conn conn1 = null;
Connection con = null;
ResultSet rs_column = null;
ResultSet rs_table = null;
String table = "";
String column = "";
String sql = "";
int column_type = -1;
TransmitData td = new TransmitData();
try{
	conn = new Conn("oa");
	con = conn.getCon();
	DatabaseMetaData dmd = con.getMetaData();
	rs_table = dmd.getTables(null, null, null, new String[] {"TABLE"});
	while (rs_table.next()){
		conn1 = new Conn("oa");
		if(!rs_table.getObject(3).toString().startsWith("BIN$")){
			rs_column = td.getColumns(rs_table.getObject(3).toString());
			while (rs_column.next()) {
				column_type = Integer.parseInt(rs_column.getObject(5).toString());
				if(column_type == java.sql.Types.LONGVARCHAR){
					sql = "ALTER TABLE " + rs_table.getObject(3).toString() + " DROP COLUMN " + rs_column.getObject(4).toString();
					conn1.executeUpdate(sql);
					sql = "ALTER TABLE " + rs_table.getObject(3).toString() + " ADD " + rs_column.getObject(4).toString() + " text NOT NULL DEFAULT ' '";
					conn1.executeUpdate(sql);
					out.print(rs_table.getObject(3).toString() + "=" + rs_column.getObject(4).toString() + "<br>");
				}				
			}
		}
		if (rs_column != null) {
			try {
				rs_column.close();
			} catch (Exception e) {}
			rs_column = null;
		}
		if (conn1 != null) {
			conn1.close();
			conn1 = null;
		}
	}
}catch(Exception e){
	out.print(StrUtil.trace(e));
}finally{
	if (rs_table != null) {
		try {
			rs_table.close();
		} catch (Exception e) {}
		rs_table = null;
	}
	if (conn != null) {
		conn.close();
		conn = null;
	}
	if (con != null) {
		con.close();
		con = null;
	}
}
%>
</body>
</html>
