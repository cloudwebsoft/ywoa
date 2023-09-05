<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%!
	String connname = Global.getDefaultDB();

    public ResultSet getTableNames() throws SQLException {
        Conn conn = new Conn(connname);
        Connection con = conn.getCon();
        DatabaseMetaData dmd = con.getMetaData();
        ResultSet rs = dmd.getTables(null, null, null, new String[] {"TABLE"});
        if (rs != null) {
            return rs;
        } else {
            return null;
        }
    }

    public ResultSet getColumns(String tableName) throws SQLException {
        Conn conn = new Conn(connname);
        Connection con = conn.getCon();
        DatabaseMetaData dmd = con.getMetaData();
        ResultSet rs = dmd.getColumns(null, null, tableName.toUpperCase(), null);
        con.close();
        con = null;
        if (rs != null) {
            return rs;
        } else {
            return null;
        }
    }
	
    public void mysqlToOra(JspWriter out, String conn_name) throws
            ErrMsgException, SQLException, java.io.IOException {
        Conn conn_mysql = new Conn(connname);
        PreparedStatement pstmt_mysql = null;
        Conn conn_ora = new Conn(conn_name);
        PreparedStatement pstmt_ora = null;
        ResultSet rs_table = null;
        ResultSet rs_column = null;
        ResultSet rs = null;
        String sql_select = "";
        String sql_insert = "";
        String tableName = "";
        String columns = "";
        String columnValues = "";
        int[] columnType = null;
        try {
            conn_ora.beginTrans();
            rs_table = getTableNames();
            while (rs_table.next()) {
                columns = "";
                columnValues = "";

                tableName = rs_table.getObject(3).toString(); //获得表名
                if (tableName.toLowerCase().equals("yearcount") ||
                    tableName.toLowerCase().equals("monthcount") ||
                    tableName.toLowerCase().equals("lastly") ||
                    tableName.toLowerCase().equals("daycount")) // 表的列名为数字，oracle中非法
                    continue;
                //sql_insert:insert into lastly (BC,OS,IP,Date) values (?,?,?,?)

                out.println("tableName=" + tableName + "<BR>");

                sql_insert = "insert into " + tableName;
                rs_column = getColumns(rs_table.getObject(3).toString());
                int rowCount = 0;
                Vector v = new Vector();
                while (rs_column.next()) {
                    String colName = rs_column.getObject(4).toString();
                    v.addElement(colName);
                    columns += colName; //获得字段名
                    columnValues += "?";
                    // isLast会导致：对只转发结果集的无效操作: isLast
                    // if (!rs_column.isLast()) {
                        columns += ",";
                        columnValues += ",";
                    // }
                    rowCount++;
                }
                if (columns.lastIndexOf(",")==columns.length()-1) {
                    columns = columns.substring(0, columns.length() - 1);
                    columnValues = columnValues.substring(0, columnValues.length() - 1);
                }
                columnType = new int[rowCount];
                int k = 0;

                // rs_column.beforeFirst();
                rs_column = getColumns(rs_table.getObject(3).toString());

                while (rs_column.next()) {
                    columnType[k] = Integer.parseInt(
                            rs_column.getObject(5).toString());
                    k++;
                }

                // 获得数据
                sql_select = "select " + columns + " from " + tableName;
                // System.out.println(getClass() + " " + sql_select);
                pstmt_mysql = conn_mysql.prepareStatement(sql_select);
                rs = conn_mysql.executePreQuery();

                sql_insert = "insert into " + tableName + " (" + columns +
                             ") values (" + columnValues + ")";
                LogUtil.getLog(getClass()).info("sql_insert:" + sql_insert);
                while (rs.next()) {
                    pstmt_ora = conn_ora.prepareStatement(sql_insert);
                    int i = 0;
                    while (i < rowCount) {
                        // tinyint(1)在getString()后得到的为布尔值true或false
                        if (columnType[i] == java.sql.Types.VARCHAR) {
                            pstmt_ora.setString(i + 1, rs.getString(i + 1));
                        } else if (columnType[i] == java.sql.Types.BOOLEAN) {
                            pstmt_ora.setInt(i + 1, rs.getInt(i + 1));
                        } else if (columnType[i] == java.sql.Types.TIMESTAMP) {
                            pstmt_ora.setTimestamp(i + 1, rs.getTimestamp(i + 1));
                        } else if (columnType[i] == java.sql.Types.DATE) {
                            pstmt_ora.setDate(i + 1, rs.getDate(i + 1));
                        } else if (columnType[i] == java.sql.Types.LONGVARCHAR) { // text类型
                            pstmt_ora.setString(i + 1, rs.getString(i + 1));
                        } else if (columnType[i] == java.sql.Types.TINYINT ||
                                   columnType[i] == java.sql.Types.INTEGER ||
                                   columnType[i] == java.sql.Types.BIT) {
                            pstmt_ora.setInt(i + 1, rs.getInt(i + 1));
                        } else if (columnType[i] == java.sql.Types.BIGINT) {
                            pstmt_ora.setLong(i + 1, rs.getLong(i + 1));
                        } else if (columnType[i] == java.sql.Types.DECIMAL) {
                            pstmt_ora.setFloat(i + 1, rs.getFloat(i + 1));
                        } else if (columnType[i] == java.sql.Types.CHAR) {
                            pstmt_ora.setString(i + 1, rs.getString(i + 1));
                        }
                        else if (columnType[i] == java.sql.Types.REAL) {
                            pstmt_ora.setFloat(i + 1, rs.getFloat(i + 1));
                        }
                        else if (columnType[i] == java.sql.Types.DOUBLE) {
                            pstmt_ora.setFloat(i + 1, rs.getFloat(i + 1));
                        }
                        else
                            throw new ErrMsgException(v.elementAt(i) + " 类型 " +
                                    columnType[i] + " 不支持！ ");

                        i++;
                    }
                    conn_ora.executePreUpdate();

                }

            }
            conn_ora.commit();
        } catch (SQLException e) {
            conn_ora.rollback();
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn_mysql != null) {
                conn_mysql.close();
                conn_mysql = null;
            }
            if (conn_ora != null) {
                conn_ora.close();
                conn_ora = null;
            }
        }
    }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>将MYSQL数据导入至ORACLE数据库（先要在ORACLE中通过INTELLGIENT CONVERTER导入表结构）</title>
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
	try{
		// td.mysqlToOra(out,"oa");
		mysqlToOra(out,"zjrj");
	}catch(Exception e){
		e.printStackTrace();
		out.print(e.getMessage());
	}
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
