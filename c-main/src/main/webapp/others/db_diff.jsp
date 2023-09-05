<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.db.Paginator" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="cn.js.fan.security.*" %>
<%@ page import="com.redmoon.oa.util.*" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.cloudwebsoft.framework.db.DataSource" %>
<%@ page import="org.apache.ibatis.annotations.Param" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%!
    /**
     * 数据库比较工具，同步数据库中的表、字段
     * 以本应用为新版数据库，其它数据源为旧版数据库
     * 当点击“添加表单中的系统定义的列”时，需检查一下代码中针对version的判断条件
     */

    // 增加列
    public void addColumn(HttpServletRequest request, String tableName, String columnName, String columnType, int columnSize, int nullable, String columnDef, String dbSource) throws ErrMsgException, SQLException {
        // ALTER TABLE `visual_module_setup` ADD COLUMN `module_code_log` VARCHAR(45) NOT NULL DEFAULT 'module_log' AFTER `is_edit_inplace`;
        String sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` ";
        if ("TINYINT UNSIGNED".equals(columnType)) {
            sql += "TINYINT(" + columnSize + ") UNSIGNED";
        } else if ("INT UNSIGNED".equals(columnType)) {
            sql += "INT(" + columnSize + ") UNSIGNED";
        } else if ("BIGINT UNSIGNED".equals(columnType)) {
            sql += "BIGINT(" + columnSize + ") UNSIGNED";
        } else if ("DATE".equals(columnType) || "DATETIME".equals(columnType)) {
            sql += columnType;
        } else if ("DOUBLE".equals(columnType)) {
            sql += columnType;
        } else {
            sql += columnType + "(" + columnSize + ")";
        }
        if (nullable == 0) {
            sql += " NOT NULL";
        }
        if (columnDef != null && !"null".equals(columnDef) && !"".equals(columnDef)) {
            if ("BIT".equals(columnType)) {
                sql += " DEFAULT " + columnDef;
            } else {
                sql += " DEFAULT '" + columnDef + "'";
            }
        }
        // System.out.println(sql);
        LogUtil.getLog(getClass()).info(sql);
        try {
            JdbcTemplate jtOld = new JdbcTemplate(new DataSource(dbSource));
            jtOld.executeUpdate(sql);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    // 创建表
    public void createTable(HttpServletRequest request, String tableName, String dbSource) throws ErrMsgException, SQLException {
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = jt.executeQuery("show create table " + tableName);
        if (ri.hasNext()) {
            ResultRecord rr = (ResultRecord) ri.next();
            String sql = rr.getString(2);
            try {
                JdbcTemplate jtOld = new JdbcTemplate(new DataSource(dbSource));
                jtOld.executeUpdate(sql);
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
    }

    // 更改列类型
    public void alterColumn(HttpServletRequest request, String tableName, String columnName, String columnType, int columnSize, int nullable, String columnDef, String dbSource) throws ErrMsgException, SQLException {
        String sql = "ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + columnName + "` ";
        if (columnType.equals("TINYINT UNSIGNED")) {
            sql += "TINYINT(" + columnSize + ") UNSIGNED";
        } else if (columnType.equals("INT UNSIGNED")) {
            sql += "INT(" + columnSize + ") UNSIGNED";
        } else if (columnType.equals("BIGINT UNSIGNED")) {
            sql += "BIGINT(" + columnSize + ") UNSIGNED";
        } else if (columnType.equals("DATE") || columnType.equals("DATETIME")) {
            sql += columnType;
        } else if (columnType.equals("DOUBLE")) {
            sql += columnType;
        } else {
            sql += columnType + "(" + columnSize + ")";
        }
        if (nullable == 0) {
            sql += " NOT NULL";
        }
        if (columnDef != null && !columnDef.equals("null") && !columnDef.equals("")) {
            if (columnType.equals("BIT")) {
                sql += " DEFAULT " + columnDef;
            } else {
                sql += " DEFAULT '" + columnDef + "'";
            }
        }

        try {
            JdbcTemplate jtOld = new JdbcTemplate(new DataSource(dbSource));
            jtOld.executeUpdate(sql);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }
%>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title><%=Global.AppName%> - <%=Global.server%>
    </title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style type="text/css">
        body {
            margin-top: 0px;
            margin-bottom: 0px;
        }

        body, td, th {
            font-size: 14px;
        }
    </style>
    <link href="index.css" rel="stylesheet" type="text/css">
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
</head>
<body>
<div style="text-align:center;margin:20px">
    <strong>请选择对应其它系统的数据源</strong>
    <select id="dbSource" name="dbSource">
        <option value="">请选择</option>
        <%
            cn.js.fan.web.Config cfg = new cn.js.fan.web.Config();
            Iterator ir = cfg.getDBInfos().iterator();
            while (ir.hasNext()) {
                DBInfo di = (DBInfo) ir.next();
        %>
        <option value="<%=di.name%>" <%=di.isDefault ? "selected" : ""%>><%=di.name%>
        </option>
        <%
            }
        %>
    </select>
    &nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:;"
                               onclick="if ('oa'==$('#dbSource').val()) {alert('不能修改默认的数据源oa'); return;} openWin('../admin/db_conn_edit.jsp?dbSource=' + $('#dbSource').val(), 640, 480)">修改数据源</a>
    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="数据差异"
                                   onclick="window.location.href='db_diff.jsp?dbSource=' + o('dbSource').value;"/>
    &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="数据字典"
                                   onclick="window.location.href='db_dict.jsp?dbSource=' + o('dbSource').value;"/>
    <%--&nbsp;&nbsp;&nbsp;&nbsp;
    <a href="db_list_table.jsp">查看数据库中的表</a>--%>
    <br/><br/>
    （以本系统数据库为新版，与其它旧版系统比较，可在脚本设计器中定义数据源指向其它系统，数据源的定义保存于proxool.xml中）
</div>
<%
    String dbSource = ParamUtil.get(request, "dbSource");
%>
<script>
    $('#dbSource').val("<%=dbSource%>");
</script>
<%
    if ("".equals(dbSource)) {
        return;
    }

    String op = ParamUtil.get(request, "op");
    if (op.equals("createTable")) {
        String tableName = ParamUtil.get(request, "tableName");
        createTable(request, tableName, dbSource);
        out.print("表：" + tableName + " 创建成功！<br/>");
    } else if (op.equals("addColumn")) {
        // ALTER TABLE `visual_module_setup` ADD COLUMN `module_code_log` VARCHAR(45) NOT NULL DEFAULT 'module_log' AFTER `is_edit_inplace`;
        // "`" + ff.getName() + "` " + typeStr + " COMMENT " +
        // StrUtil.sqlstr(ff.getTitle());
        String tableName = ParamUtil.get(request, "tableName");
        String columnName = ParamUtil.get(request, "columnName");

        String columnType = ParamUtil.get(request, "columnType");
        int columnSize = ParamUtil.getInt(request, "columnSize");
        int nullable = ParamUtil.getInt(request, "nullable");
        String columnDef = ParamUtil.get(request, "columnDef");

        addColumn(request, tableName, columnName, columnType, columnSize, nullable, columnDef, dbSource);
        out.print("列：" + tableName + "." + columnName + " 添加成功！<br/>");
    } else if (op.equals("alterColumn")) {
        String tableName = ParamUtil.get(request, "tableName");
        String columnName = ParamUtil.get(request, "columnName");

        String columnType = ParamUtil.get(request, "columnType");
        int columnSize = ParamUtil.getInt(request, "columnSize");
        int nullable = ParamUtil.getInt(request, "nullable");
        String columnDef = ParamUtil.get(request, "columnDef");
        alterColumn(request, tableName, columnName, columnType, columnSize, nullable, columnDef, dbSource);
        out.print("列：" + tableName + "." + columnName + " 修改成功！<br/>");
    } else if (op.equals("alterFormTable")) {
        // 添加系统定义的列
        String database = "";
        String sqlDb = "select database()";
        JdbcTemplate jt = new JdbcTemplate(new DataSource(dbSource));
        ResultIterator riDb = jt.executeQuery(sqlDb);
        if (riDb.hasNext()) {
            ResultRecord rr = (ResultRecord) riDb.next();
            database = rr.getString(1);
        }

        // 旧系统版本
        double version = ParamUtil.getDouble(request, "version", 4);
        sqlDb = "SELECT * FROM oa_sys_ver";
        riDb = jt.executeQuery(sqlDb);
        if (riDb.hasNext()) {
            ResultRecord rr = (ResultRecord) riDb.next();
            String ver = rr.getString("version");
            version = StrUtil.toDouble(ver, 5);
        }

        String realPath = application.getRealPath("/");
        if (realPath.lastIndexOf("/") != realPath.length() - 1) {
            realPath += "/";
        }
        org.logicalcobwebs.proxool.ProxoolFacade.removeAllConnectionPools(5000); //
        org.logicalcobwebs.proxool.configuration.JAXPConfigurator.configure(realPath + "WEB-INF/proxool.xml", false);

        if (version < 3) {
            // 添加拉单及冲抵字段
            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                String tableName = rr.getString(1).toLowerCase();
                if (tableName.startsWith("ft_")) {
                    sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_flag` TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER `cws_status`, ADD COLUMN `cws_quote_id` INTEGER UNSIGNED AFTER `cws_flag`";
                    try {
                        jt.executeUpdate(sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_progress` INTEGER UNSIGNED NOT NULL DEFAULT 0 AFTER `cws_status`";
                    try {
                        jt.executeUpdate(sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (version < 4.0) {
            // 添加cws_parent_form抵字段
            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                String tableName = rr.getString(1).toLowerCase();
                if (tableName.startsWith("ft_")) {
                    // sql = "ALTER TABLE `" + tableName + "` DROP COLUMN `cws_parent_form`";
                    sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_parent_form` varchar(20) AFTER `cws_status`";
                    try {
                        jt.executeUpdate(sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (version < 5.0) {
            // 添加cws_create_date cws_modify_date cws_finish_date抵字段
            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                String tableName = rr.getString(1).toLowerCase();
                if (tableName.startsWith("ft_") && !tableName.endsWith("_log")) {
                    sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_create_date` datetime AFTER `cws_status`,  ADD COLUMN `cws_modify_date` datetime AFTER `cws_status`,  ADD COLUMN `cws_finish_date` datetime AFTER `cws_status`";
                    try {
                        jt.executeUpdate(sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (version < 6.0) {
            // 添加cws_quote_form
            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                String tableName = rr.getString(1).toLowerCase();
                if (tableName.startsWith("ft_") && !tableName.endsWith("_log")) {
                    sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_quote_form` varchar(20) AFTER `cws_status`";
                    try {
                        jt.executeUpdate(sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (version < 3.0) {
            // 因嵌套表格2控件的version改为了2，所以需处理一下
            String sql = "update form_field set description=defaultValue where description='' and macroType='nest_sheet'";
            try {
                jt.executeUpdate(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    TransmitData td = new TransmitData();
    String connName = dbSource; // 旧数据库
    String connName2 = Global.getDefaultDB(); // 本机为新数据库

    Conn conn = new Conn(connName);
    Connection con = conn.getCon();

    Conn conn2 = new Conn(connName2);
    Connection con2 = conn2.getCon();

    ResultSet rs_table_old = null;
    ResultSet rs_table_new = null;
    try {
        rs_table_old = td.getTableNames(con);
    } catch (Exception e) {
        e.printStackTrace();
        out.print("数据库连接错误！");
        return;
    }
    rs_table_new = td.getTableNames(con2);

    ResultSet rs_column_old = null;
    ResultSet rs_column_new = null;
    String table_old = "";
    String table_new = "";
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
    while (rs_table_old.next()) {
        table_dif = true;
        table_old = rs_table_old.getObject(3).toString();
        n++;
        if (true) {
            // continue;
        }
        while (rs_table_new.next()) {
            table_new = rs_table_new.getObject(3).toString();
            if (table_old.equals(table_new)) {
                Conn conn_old = new Conn(connName);
                Connection con_old = conn_old.getCon();
                DatabaseMetaData dmd_old = con_old.getMetaData();
                // 在比对时，发现有些删除的表还有残留在mysql的data目录下
                try {
                    // mysql-connector-java 6.0以下用这个方法
                    // rs_column_old = dmd_old.getColumns(null, null, table_old, null);
                    // 高版本用此方法
                    rs_column_old = dmd_old.getColumns(con_old.getCatalog(), con_old.getCatalog(), table_old, "%");
                } catch (SQLException ex) {
                    out.print("数据库中表" + table_old + "不存在，可能在数据库的目录：data/数据库名称 下仍存在有残余文件！");
                    return;
                }

                Conn conn_new = new Conn(connName2);
                Connection con_new = conn_new.getCon();
                DatabaseMetaData dmd_new = con_new.getMetaData();
                // mysql-connector-java 6.0以下用这个方法
                // rs_column_new = dmd_new.getColumns(null, null, rs_table_new.getObject(3).toString(), null);
                // 高版本用此方法
                rs_column_new = dmd_new.getColumns(con_new.getCatalog(), con_new.getCatalog(), rs_table_new.getObject(3).toString(), "%");

                // out.print("rs_table_new.getObject(3)=" + rs_table_new.getObject(3) + " rs_table_old.getObject(3)=" + rs_table_old.getObject(3) + "<BR>");

                // rs_column_old = td.getColumns(rs_table_old.getObject(3).toString(),"forum");
                // rs_column_new = td.getColumns(rs_table_new.getObject(3).toString(),"cwbbs");

                while (rs_column_old.next()) {
                    column_dif = true;
                    column_old = rs_column_old.getObject(4).toString();
                    while (rs_column_new.next()) {
                        column_new = rs_column_new.getObject(4).toString();
                        // out.print("column_old=" + column_old + " column_new=" + column_new + "<BR>");
                        if (column_old.equals(column_new)) {
                            // 比对长度
                            if (rs_column_new.getInt("COLUMN_SIZE") != rs_column_old.getInt("COLUMN_SIZE")) {
                                String columnTypeOld = rs_column_old.getString("TYPE_NAME");
                                String columnType = rs_column_new.getString("TYPE_NAME");
                                int columnSize = rs_column_new.getInt("COLUMN_SIZE");
                                int digits = rs_column_new.getInt("DECIMAL_DIGITS");
                                int nullable = rs_column_new.getInt("NULLABLE");
                                String columnDef = rs_column_new.getString("COLUMN_DEF");

                                boolean isFound = false;
                                if (op.equals("alterColumnBatch")) {
                                    String[] tableColumns = ParamUtil.getParameters(request, "tableColumn");
                                    if (tableColumns != null) {
                                        for (int k = 0; k < tableColumns.length; k++) {
                                            if ((table_new + "." + column_new).equals(tableColumns[k])) {
                                                alterColumn(request, table_new, column_new, columnType, columnSize, nullable, columnDef, dbSource);
                                                out.print("列：" + table_new + "." + column_new + " 修改成功！<br/>");
                                                isFound = true;
                                            }
                                        }
                                    }
                                }
                                if (!isFound) {
                                    change_column += "<input type='checkbox' name='tableColumn' value='" + table_old + "." + column_old + "'/>" + table_old + "." + column_old + "&nbsp;oldType=" + columnTypeOld + "&nbsp;newType=" + columnType + "&nbsp;size=" + rs_column_old.getObject("COLUMN_SIZE") + " new size=" + rs_column_new.getObject("COLUMN_SIZE") + "&nbsp;<a href='db_diff.jsp?op=alterColumn&dbSource=" + dbSource + "&tableName=" + table_new + "&columnName=" + column_new + "&columnSize=" + columnSize + "&nullable=" + nullable + "&columnDef=" + StrUtil.UrlEncode(columnDef) + "&columnType=" + columnType + "'>同步</a>";
                                    // change_column += "&nbsp;&nbsp;<a target='_blank' href='table_diff.jsp?tableA=" + table_new + "&tableB=" + table_new + "'>表格比较</a><BR/>";
                                    change_column += "<br/>";
                                }
                            }
                            column_dif = false;
                            break;
                        }
                    }
                    if (column_dif) {
                        del_column += table_old + "." + column_old + "<BR>";
                    }
                    rs_column_new.beforeFirst();
                }

                rs_column_old.beforeFirst();
                rs_column_new.beforeFirst();
                while (rs_column_new.next()) {
                    column_dif = true;
                    column_new = rs_column_new.getObject(4).toString();
                    while (rs_column_old.next()) {
                        column_old = rs_column_old.getObject(4).toString();
                        if (column_new.equals(column_old)) {
                            column_dif = false;
                            break;
                        }
                    }
                    if (column_dif) {
                        String columnType = rs_column_new.getString("TYPE_NAME");
                        int columnSize = rs_column_new.getInt("COLUMN_SIZE");
                        int digits = rs_column_new.getInt("DECIMAL_DIGITS");
                        int nullable = rs_column_new.getInt("NULLABLE");
                        String columnDef = rs_column_new.getString("COLUMN_DEF");

                        boolean isFound = false;
                        if (op.equals("addColumnBatch")) {
                            String[] tableColumns = ParamUtil.getParameters(request, "tableColumn");
                            if (tableColumns != null) {
                                for (int k = 0; k < tableColumns.length; k++) {
                                    if ((table_new + "." + column_new).equals(tableColumns[k])) {
                                        addColumn(request, table_new, column_new, columnType, columnSize, nullable, columnDef, dbSource);
                                        out.print("列：" + table_new + "." + column_new + " 添加成功！<br/>");
                                        isFound = true;
                                    }
                                }
                            }
                        }
                        if (!isFound) {
                            add_column += "<input type='checkbox' name='tableColumn' value='" + table_new + "." + column_new + "'/>" + table_new + "." + column_new + "&nbsp;&nbsp;类型：" + columnType + "&nbsp;默认值：" + columnDef + "&nbsp;允许空：" + nullable + "&nbsp;长度：" + columnSize + "&nbsp;<a href='db_diff.jsp?op=addColumn&dbSource=" + dbSource + "&tableName=" + table_new + "&columnName=" + column_new + "&columnSize=" + columnSize + "&nullable=" + nullable + "&columnDef=" + StrUtil.UrlEncode(columnDef) + "&columnType=" + columnType + "'>同步</a>";
                            // add_column += "&nbsp;&nbsp;<a target='_blank' href='table_diff.jsp?tableA=" + table_new + "&tableB=" + table_new + "'>表格比较</a><BR/>";
                            add_column += "<br/>";
                        }
                    }
                    rs_column_old.beforeFirst();
                }

                rs_column_old.close();
                rs_column_new.close();

                conn_old.close();
                conn_new.close();

                table_dif = false;
                rs_table_new.beforeFirst();
                break;
            }
        }
        if (table_dif) {
            del_table += table_old + "<BR>";
        }
        rs_table_new.beforeFirst();
    /*
	ResultSet rs_column = td.getColumns(rs_table_old.getObject(3).toString());
	while (rs_column.next()){
       out.print(rs_column.getObject(4));
	   out.print("<br>");
	}
	*/
    }

    rs_table_old.beforeFirst();
    rs_table_new.beforeFirst();

    while (rs_table_new.next()) {
        table_dif = true;
        table_new = rs_table_new.getObject(3).toString();
        while (rs_table_old.next()) {
            table_old = rs_table_old.getObject(3).toString();
            if (table_new.equals(table_old)) {
                table_dif = false;
                // rs_table_old.beforeFirst();
                break;
            }
        }
        if (table_dif) {
            boolean isFound = false;
            if (op.equals("createTableBatch")) {
                String[] tableNames = ParamUtil.getParameters(request, "tableName");
                if (tableNames != null) {
                    for (int k = 0; k < tableNames.length; k++) {
                        if ((table_new).equals(tableNames[k])) {
                            createTable(request, table_new, dbSource);
                            out.print("表：" + table_new + " 创建成功！<br/>");
                            isFound = true;
                        }
                    }
                }
            }
            if (!isFound) {
                add_table += "<input type='checkbox' name='tableName' value='" + table_new + "'/>" + table_new + "&nbsp;&nbsp;&nbsp;&nbsp;<a href='db_diff.jsp?op=createTable&dbSource=" + dbSource + "&tableName=" + table_new + "'>增加</a><br>";
            }
        }
        rs_table_old.beforeFirst();
    }

    conn.close();
    conn2.close();
%>
<div style="margin:20px">
    <form id="formCreateTable" method="post" action="db_diff.jsp?op=createTableBatch&dbSource=<%=dbSource%>">
        <%
            out.print("<strong>增加的表:</strong><BR>" + add_table);
            out.print("<br>");
        %>
        &nbsp;<input type="submit" value="批量增加表"/><br/><br/>
    </form>
    <%
        out.print("<strong>删除的表:</strong><BR>" + del_table);
        out.print("<br>");
    %>
    <form id="formAddCol" method="post" action="db_diff.jsp?op=addColumnBatch&dbSource=<%=dbSource%>">
        <%
            out.print("<strong>增加的列:</strong><BR>" + add_column);
            out.print("<br>");
        %>
        &nbsp;<input type="submit" value="批量增加列"/><br/><br/>
    </form>
    <%
        out.print("<strong>删除的列:</strong><BR>" + del_column);
        out.print("<br>");
    %>
    <form id="formAlterCol" method="post" action="db_diff.jsp?op=alterColumnBatch&dbSource=<%=dbSource%>">
        <%
            out.print("<strong>修改的列:</strong><BR>" + change_column);
            out.print("<br>");
        %>
        &nbsp;<input type="submit" value="批量修改列"/><br/><br/>
    </form>

    旧系统版本
    <select id="version" name="version">
        <option value="4">4</option>
        <option value="5">5</option>
    </select>
    &nbsp;<input type="button" value="增加表单中的系统定义的列"
                 onclick="window.location.href='db_diff.jsp?op=alterFormTable&dbSource=<%=dbSource%>'"/><br/>
</div>
</body>
</html>
