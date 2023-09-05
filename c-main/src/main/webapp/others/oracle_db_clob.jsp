<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
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
		Vector v = new Vector(); 
		String tableName = rs_table.getObject(3).toString();
		// if (tableName.equalsIgnoreCase("sq_message")) {
		if(tableName.indexOf("$")==-1 && tableName.indexOf("SDO_")==-1 && tableName.indexOf("METASTYLESHEET")==-1 && !rs_table.getObject(3).toString().startsWith("ARGUMENT$") &&!rs_table.getObject(3).toString().startsWith("BIN$") && !rs_table.getObject(3).toString().startsWith("OL$") && !rs_table.getObject(3).toString().startsWith("EXF$")&& !rs_table.getObject(3).toString().startsWith("DR$")){
			rs_column = td.getColumns(rs_table.getObject(3).toString());
			while (rs_column.next()) {
				LogUtil.getLog(getClass()).info(rs_column.getObject(4).toString());
				String colName = rs_column.getObject(4).toString();
				column_type = Integer.parseInt(rs_column.getObject(5).toString());
				if(column_type == java.sql.Types.CLOB){
                    v.addElement(rs_column.getObject(4).toString());
					out.println("tableName=" + tableName + " colName=" + colName + " column_type=" + column_type + "<BR>");
				}				
			}
			// v.removeAllElements();
				
			if(v.size() > 0){
				Iterator ir = v.iterator();
				conn1 = new Conn("oa");
				if(v.size() == 1){						
					column = ir.next().toString();
					sql = "ALTER TABLE " + rs_table.getObject(3).toString() + " DROP (" + column + ")";
					out.print(sql+"<BR>");
					conn1.executeUpdate(sql);
					sql = "ALTER TABLE " + rs_table.getObject(3).toString() + " ADD " + column + " LONG";
					out.print(sql+"<BR>");
					
					conn1.executeUpdate(sql);
			
				}else{
					//plugin_debate有4个clob字段
					while(ir.hasNext()){
						column = ir.next().toString();
						sql = "ALTER TABLE " + rs_table.getObject(3).toString() + " DROP (" + column + ")";
					out.print(sql+"<BR>");
						conn1.executeUpdate(sql);
						sql = "ALTER TABLE " + rs_table.getObject(3).toString() + " ADD " + column + " VARCHAR2(4000)";
					out.print(sql+"<BR>");
						conn1.executeUpdate(sql);
					}
				}
			}
			if (tableName.equalsIgnoreCase("work_plan_user"))
				break;
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
	out.print(e.getMessage());
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
成功！
</body>
</html>
