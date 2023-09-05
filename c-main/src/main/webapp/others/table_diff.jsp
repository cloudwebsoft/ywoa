<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.cloudwebsoft.framework.db.DataSource" %>
<%@ page import="org.apache.ibatis.annotations.Param" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%!
	/**
	 * 数据库比较工具，同步两个表的字段
	 * 以本应用表A为新版，表B为旧版，如用来比较日志表中的缺失字段
	 * 此文件暂无用
	 */

	// 增加列
	public void addColumn(HttpServletRequest request, String tableName, String columnName, String columnType, int columnSize, int nullable, String columnDef, String dbSource) throws SQLException {
		// ALTER TABLE `visual_module_setup` ADD COLUMN `module_code_log` VARCHAR(45) NOT NULL DEFAULT 'module_log' AFTER `is_edit_inplace`;
		String sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` ";
		if (columnType.equals("TINYINT UNSIGNED")) {
			sql += "TINYINT(" + columnSize + ") UNSIGNED";
		}
		else if (columnType.equals("INT UNSIGNED")) {
			sql += "INT(" + columnSize + ") UNSIGNED";
		}
		else if (columnType.equals("BIGINT UNSIGNED")) {
			sql += "BIGINT(" + columnSize + ") UNSIGNED";
		}
		else if (columnType.equals("DATE") || columnType.equals("DATETIME")) {
			sql += columnType;
		}
		else if (columnType.equals("DOUBLE")) {
			sql += columnType;
		}
		else {
			sql += columnType + "(" + columnSize + ")";
		}
		if (nullable==0) {
			sql += " NOT NULL";
		}
		if (columnDef!=null && !columnDef.equals("null") && !columnDef.equals("")) {
			if (columnType.equals("BIT")) {
				sql += " DEFAULT " + columnDef;
			}
			else {
				sql += " DEFAULT '" + columnDef + "'";
			}
		}
		LogUtil.getLog(getClass()).info(sql);
		JdbcTemplate jtOld = new JdbcTemplate(new DataSource(dbSource));
		jtOld.executeUpdate(sql);
	}

	// 创建表
	public void createTable(HttpServletRequest request, String tableName, String dbSource) throws ErrMsgException, SQLException {
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery("show create table " + tableName);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			String sql = rr.getString(2);
			try {
				JdbcTemplate jtOld = new JdbcTemplate(new DataSource(dbSource));
				jtOld.executeUpdate(sql);
			}
			catch (SQLException e) {
				LogUtil.getLog(getClass()).error(e);
			}
		}
	}

	// 更改列类型
	public void alterColumn(HttpServletRequest request, String tableName, String columnName, String columnType, int columnSize, int nullable, String columnDef, String dbSource) throws SQLException {
		String sql = "ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + columnName + "` ";
		if ("TINYINT UNSIGNED".equals(columnType)) {
			sql += "TINYINT(" + columnSize + ") UNSIGNED";
		}
		else if ("INT UNSIGNED".equals(columnType)) {
			sql += "INT(" + columnSize + ") UNSIGNED";
		}
		else if ("BIGINT UNSIGNED".equals(columnType)) {
			sql += "BIGINT(" + columnSize + ") UNSIGNED";
		}
		else if ("DATE".equals(columnType) || "DATETIME".equals(columnType)) {
			sql += columnType;
		}
		else if ("DOUBLE".equals(columnType)) {
			sql += columnType;
		}
		else {
			sql += columnType + "(" + columnSize + ")";
		}
		if (nullable==0) {
			sql += " NOT NULL";
		}
		if (columnDef!=null && !"null".equals(columnDef) && !"".equals(columnDef)) {
			if ("BIT".equals(columnType)) {
				sql += " DEFAULT " + columnDef;
			}
			else {
				sql += " DEFAULT '" + columnDef + "'";
			}
		}
		LogUtil.getLog(getClass()).info(sql);
		JdbcTemplate jtOld = new JdbcTemplate(new DataSource(dbSource));
		jtOld.executeUpdate(sql);
	}
%>
<!DOCTYPE HTML>
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
			font-size: 14px;
		}
		-->
	</style>
	<link href="index.css" rel="stylesheet" type="text/css">
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
</head>
<body>
<%
	String tableA = ParamUtil.get(request, "tableA");
	String tableB = ParamUtil.get(request, "tableB");
%>
<div style="text-align:center;margin:20px">
	<strong>请输入表名</strong>
	表A：<input id="tableA" name="tableA" value="<%=tableA%>"/>
	表B：<input id="tableB" name="tableB" value="<%=tableB%>"/>
	<input type="button" value="比较" onclick="window.location.href='table_diff.jsp?tableA=' + o('tableA').value + '&tableB=' + o('tableB').value;"/>
	&nbsp;&nbsp;&nbsp;&nbsp;
	<br/><br/>
</div>
<%
	String dbSource = ParamUtil.get(request, "dbSource");
	dbSource = Global.getDefaultDB();
	if ("".equals(tableA) || "".equals(tableB)) {
		return;
	}

	String op = ParamUtil.get(request, "op");
	if ("addColumn".equals(op)) {
		// ALTER TABLE `visual_module_setup` ADD COLUMN `module_code_log` VARCHAR(45) NOT NULL DEFAULT 'module_log' AFTER `is_edit_inplace`;
		// "`" + ff.getName() + "` " + typeStr + " COMMENT " +
		// StrUtil.sqlstr(ff.getTitle());
		String columnName = ParamUtil.get(request, "columnName");

		String columnType = ParamUtil.get(request, "columnType");
		int columnSize = ParamUtil.getInt(request, "columnSize");
		int nullable = ParamUtil.getInt(request, "nullable");
		String columnDef = ParamUtil.get(request, "columnDef");

		try {
			addColumn(request, tableB, columnName, columnType, columnSize, nullable, columnDef, dbSource);
			out.print("列：" + tableB + "." + columnName + " 添加成功！<br/>");
		} catch (SQLException e) {
			e.printStackTrace();
			out.print(e.getMessage());
		}
	}
	else if ("alterColumn".equals(op)) {
		String columnName = ParamUtil.get(request, "columnName");
		String columnType = ParamUtil.get(request, "columnType");
		int columnSize = ParamUtil.getInt(request, "columnSize");
		int nullable = ParamUtil.getInt(request, "nullable");
		String columnDef = ParamUtil.get(request, "columnDef");
		try {
			alterColumn(request, tableB, columnName, columnType, columnSize, nullable, columnDef, dbSource);
		} catch (SQLException e) {
			e.printStackTrace();
			out.print(e.getMessage());
		}
		out.print("列：" + tableB + "." + columnName + " 修改成功！<br/>");
	}

	TransmitData td = new TransmitData();
	String connName = dbSource; // 旧数据库
	String connName2 = Global.getDefaultDB(); // 本机为新数据库

	Conn conn = new Conn(connName);
	Connection con = conn.getCon();

	Conn conn2 = new Conn(connName2);
	Connection con2 = conn2.getCon();

	ResultSet rs_column_old = null;
	ResultSet rs_column_new = null;
	String column_old = "";
	String column_new = "";
	String add_table = "";
	String del_table = "";
	String add_column = "";
	String change_column = "";
	String del_column = "";
	boolean table_dif = true;
	boolean column_dif = true;

	int n = 0;

	Conn conn_old = new Conn(connName);
	Connection con_old = conn_old.getCon();
	DatabaseMetaData dmd_old = con_old.getMetaData();
	// 在比对时，发现有些删除的表还有残留在mysql的data目录下
	try {
		// mysql-connector-java 6.0以下用这个方法
		rs_column_old = dmd_old.getColumns(null, null, tableB, null);
		// 高版本用此方法
		// rs_column_old = dmd_old.getColumns(con_old.getCatalog(), con_old.getCatalog(), tableB, "%");
	}
	catch (SQLException ex) {
		out.print("数据库中表" + tableB + "不存在，可能在数据库的目录：data/数据库名称 下仍存在有残余文件！");
		return;
	}

	Conn conn_new = new Conn(connName2);
	Connection con_new = conn_new.getCon();
	DatabaseMetaData dmd_new = con_new.getMetaData();
	// mysql-connector-java 6.0以下用这个方法
	rs_column_new = dmd_new.getColumns(null, null, tableA.toString(), null);
	// 高版本用此方法
	// rs_column_new = dmd_new.getColumns(con_new.getCatalog(), con_new.getCatalog(), rs_table_new.getObject(3).toString(), "%");

	// out.print("rs_table_new.getObject(3)=" + rs_table_new.getObject(3) + " rs_table_old.getObject(3)=" + rs_table_old.getObject(3) + "<BR>");

	// rs_column_old = td.getColumns(rs_table_old.getObject(3).toString(),"forum");
	// rs_column_new = td.getColumns(rs_table_new.getObject(3).toString(),"cwbbs");

	while (rs_column_old.next()) {
		column_dif = true;
		column_old = rs_column_old.getObject(4).toString();
		while (rs_column_new.next()){
			column_new = rs_column_new.getObject(4).toString();
			// out.print("column_old=" + column_old + " column_new=" + column_new + "<BR>");
			if(column_old.equals(column_new)){
				// 比对长度
				if (rs_column_new.getInt("COLUMN_SIZE")!=rs_column_old.getInt("COLUMN_SIZE")) {
					String columnTypeOld = rs_column_old.getString("TYPE_NAME");
					String columnType = rs_column_new.getString("TYPE_NAME");
					int columnSize = rs_column_new.getInt("COLUMN_SIZE");
					int digits = rs_column_new.getInt("DECIMAL_DIGITS");
					int nullable = rs_column_new.getInt("NULLABLE");
					String columnDef = rs_column_new.getString("COLUMN_DEF");

					boolean isFound = false;
					if (op.equals("alterColumnBatch")) {
						String[] tableColumns = ParamUtil.getParameters(request, "tableColumn");
						if (tableColumns!=null) {
							for (int k=0; k<tableColumns.length; k++) {
								if ((tableB + "." + column_new).equals(tableColumns[k])) {
									try {
										alterColumn(request, tableB, column_new, columnType, columnSize, nullable, columnDef, dbSource);
									} catch (SQLException e) {
										e.printStackTrace();
									}
									out.print("列：" + tableB + "." + column_new + " 修改成功！<br/>");
									isFound = true;
								}
							}
						}
					}
					if (!isFound) {
						change_column += "<input type='checkbox' name='tableColumn' value='" + tableB + "." + column_old + "'/>" + tableB + "." + column_old + "&nbsp;oldType=" + columnTypeOld + "&nbsp;newType=" + columnType + "&nbsp;size=" + rs_column_old.getObject("COLUMN_SIZE") + " new size=" + rs_column_new.getObject("COLUMN_SIZE") + "&nbsp;<a href='table_diff.jsp?op=alterColumn&dbSource=" + dbSource + "&tableA=" + tableA + "&tableB=" + tableB + "&columnName=" + column_new + "&columnSize=" + columnSize + "&nullable=" + nullable + "&columnDef=" + StrUtil.UrlEncode(columnDef) + "&columnType=" + columnType + "'>同步</a><BR>";
					}
				}
				column_dif = false;
				break;
			}
		}
		if(column_dif){
			del_column += tableB + "." + column_old + "<BR>";
		}
		rs_column_new.beforeFirst();
	}

	rs_column_old.beforeFirst();
	rs_column_new.beforeFirst();
	while (rs_column_new.next()) {
		column_dif = true;
		column_new = rs_column_new.getObject(4).toString();
		while (rs_column_old.next()){
			column_old = rs_column_old.getObject(4).toString();
			if(column_new.equals(column_old)) {
				column_dif = false;
				break;
			}
		}
		if(column_dif){
			String columnType = rs_column_new.getString("TYPE_NAME");
			int columnSize = rs_column_new.getInt("COLUMN_SIZE");
			int digits = rs_column_new.getInt("DECIMAL_DIGITS");
			int nullable = rs_column_new.getInt("NULLABLE");
			String columnDef = rs_column_new.getString("COLUMN_DEF");

			boolean isFound = false;
			if ("addColumnBatch".equals(op)) {
				String[] tableColumns = ParamUtil.getParameters(request, "tableColumn");
				if (tableColumns!=null) {
					for (int k=0; k<tableColumns.length; k++) {
						if ((tableA + "." + column_new).equals(tableColumns[k])) {
							try {
								addColumn(request, tableB, column_new, columnType, columnSize, nullable, columnDef, dbSource);
							} catch (SQLException throwables) {
								throwables.printStackTrace();
							}
							out.print("列：" + tableB + "." + column_new + " 添加成功！<br/>");
							isFound = true;
						}
					}
				}
			}
			if (!isFound) {
				add_column += "<input type='checkbox' name='tableColumn' value='" + tableA + "." + column_new + "'/>" + tableA + "." + column_new + "&nbsp;&nbsp;类型：" + columnType + "&nbsp;默认值：" + columnDef + "&nbsp;允许空：" + nullable + "&nbsp;长度：" + columnSize + "&nbsp;<a href='table_diff.jsp?op=addColumn&dbSource=" + dbSource + "&tableA=" + tableA + "&tableB=" + tableB + "&columnName=" + column_new + "&columnSize=" + columnSize + "&nullable=" + nullable + "&columnDef=" + StrUtil.UrlEncode(columnDef) + "&columnType=" + columnType + "'>同步</a><BR>";
			}
		}
		rs_column_old.beforeFirst();
	}

	rs_column_old.close();
	rs_column_new.close();

	conn_old.close();
	conn_new.close();

	table_dif = false;

	conn.close();
	conn2.close();
%>
<form id="formAddCol" method="post" action="table_diff.jsp?op=addColumnBatch&dbSource=<%=dbSource%>&tableA=<%=tableA%>&tableB=<%=tableB%>">
	<%
		out.print("<strong>A增加的列:</strong><BR>" + add_column);
		out.print("<br>");
	%>
	&nbsp;<input type="submit" value="批量增加列"/><br/><br/>
</form>
<%
	out.print("<strong>A删除的列:</strong><BR>" + del_column);
	out.print("<br>");
%>
<form id="formAlterCol" method="post" action="table_diff.jsp?op=alterColumnBatch&dbSource=<%=dbSource%>&tableA=<%=tableA%>&tableB=<%=tableB%>">
	<%
		out.print("<strong>A修改的列:</strong><BR>" + change_column);
		out.print("<br>");
	%>
	&nbsp;<input type="submit" value="批量修改列"/><br/><br/>
</form>
</body>
</html>
