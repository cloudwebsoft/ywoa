package com.redmoon.oa.util;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import java.sql.*;
import cn.js.fan.web.Global;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class TransmitData extends ObjectDb {
    public int id;

    public TransmitData() {
        connname = Global.defaultDB;
    }

    public TransmitData(int id) {
        this.id = id;
        init();
        load();
    }

    public TransmitData getTransmitData(int id) {
        return (TransmitData) getObjectDb(new Integer(id));
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new TransmitData(pk.getIntValue());
    }

    @Override
    public void initDB() {
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);

        isInitFromConfigDB = false;
    }

    @Override
    public boolean save() throws ErrMsgException {
        return true;
    }

    @Override
    public void load() {
    }

    @Override
    public boolean del() throws ErrMsgException {
        return true;
    }

    @Override
    public boolean create() throws ErrMsgException {
        return true;
    }

    /**
     * 数据表的结构
     * */
    public Vector getTableStruct(String table) {
        Conn conn = new Conn(connname);
        Connection con = conn.getCon();

        Vector vect = new Vector();
        try {
            /*
             * rs = QuerySQL("select * from "+table);
             * ResultSetMetaData rmd =rs.getMetaData();
             * int cols = rmd.getColumnCount();
             * for(int i=1;i <=cols;i++)
             * {
             *     Hashtable hash = new Hashtable();
             *     //hash.put("目录名",rmd.getCatalogName(i));
             *     //hash.put("列返回值类型名",rmd.getColumnClassName(i));
             *     hash.put("列定义大小",rmd.getColumnDisplaySize(i)+"");
             *     //hash.put("列标签",rmd.getColumnLabel(i));
             *     hash.put("字段名",rmd.getColumnName(i));
             *     hash.put("列类型编号",rmd.getColumnType(i)+"");
             *     hash.put("列标准类型名",rmd.getColumnTypeName(i));
             *     hash.put("列精确度",rmd.getPrecision(i)+"");
             *     //hash.put("10",rmd.getScale(i)+"");
             *     //hash.put("11",rmd.getSchemaName(i));
             *     //hash.put("表名",rmd.getTableName(i));
             *     //hash.put("13",rmd.isAutoIncrement(i)+"");
             *     //hash.put("大小写敏感",rmd.isCaseSensitive(i)+"");
             *     //hash.put("是否为金额",rmd.isCurrency(i)+"");
             *     //hash.put("是否可写",rmd.isDefinitelyWritable(i)+"");
             *     hash.put("是否可为空",rmd.isNullable(i)+"");
             *     //hash.put("是否只读",rmd.isReadOnly(i)+"");
             *     //hash.put("是否可查询",rmd.isSearchable(i)+"");
             *     hash.put("是否数字",rmd.isSigned(i)+"");
             *     //hash.put("是否可写",rmd.isWritable(i)+""); vect.add(hash); }
             */
            DatabaseMetaData dmd = con.getMetaData();
            ResultSet rs = dmd.getColumns(null, "XNC", table.toUpperCase(), null);
            ResultSetMetaData rmd = rs.getMetaData();
            int cols = rmd.getColumnCount();
            while (rs.next()) {
                Hashtable hash = new Hashtable();
                hash.put("列定义大小", rs.getString("CHAR_OCTET_LENGTH") + "");
                String f = rs.getString("COLUMN_NAME");
                ResultSet r = conn.executeQuery("select " + f + " from " +
                                                table);
                ResultSetMetaData rm = r.getMetaData();
                hash.put("字段名", f + "");
                hash.put("列类型编号", rm.getColumnType(1) + "");
                hash.put("列标准类型名", rm.getColumnTypeName(1) + "");
                hash.put("是否可为空", rm.isNullable(1) + "");
                hash.put("是否数字", rm.isSigned(1) + "");
                hash.put("列定义大小", rm.getColumnDisplaySize(1) + "");
                hash.put("列精确度", rs.getString("NUM_PREC_RADIX") + "");
                r.close();
                Statement stst = r.getStatement();
                if (stst != null)
                    stst.close();
                vect.add(hash);
            }
            Statement stmt = rs.getStatement();
            rs.close();
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            LogUtil.getLog(CSSUtil.class).error(e);
        } catch (AbstractMethodError e) {
            LogUtil.getLog(CSSUtil.class).error(e);
        }
        return vect;
    }

    public ResultSet getTableNames(Connection con) throws SQLException {

            DatabaseMetaData dmd = con.getMetaData();
            // mysql-connector-java 6.0以下用这个方法，高版本用此方法会取出所有的库中的所有的表
            // ResultSet rs = dmd.getTables(null, null, null, new String[]{"TABLE"});
            // mysql-connector-java 6.0以上用这个方法
            ResultSet rs = dmd.getTables(con.getCatalog(), con.getCatalog(), "%", new String[]{"TABLE"});
            if (rs != null) {
                return rs;
            } else {
                return null;
            }
    }

	/*
    public ResultSet getTableNames(String conName) throws SQLException {
        Conn conn = null;
        try {
            conn = new Conn(conName);
            Connection con = conn.getCon();
            DatabaseMetaData dmd = con.getMetaData();
            // mysql-connector-java 6.0以下用这个方法，高版本用此方法会取出所有的库中的所有的表
            // ResultSet rs = dmd.getTables(null, null, null, new String[]{"TABLE"});
            // mysql-connector-java 6.0以上用这个方法
            ResultSet rs = dmd.getTables(con.getCatalog(), con.getCatalog(), "%", new String[]{"TABLE"});
            if (rs != null) {
                return rs;
            } else {
                return null;
            }
        }
        finally {
            conn.close();
        }
    }
	*/

    public ResultSet getColumns(String tableName, String conName) throws
            SQLException {
        Conn conn = null;
        try {
            conn = new Conn(conName);
            Connection con = conn.getCon();
            DatabaseMetaData dmd = con.getMetaData();
            ResultSet rs = dmd.getColumns(null, null, tableName, null);
            if (rs != null) {
                return rs;
            } else {
                return null;
            }
        }
        finally {
            conn.close();
        }
    }

    public ResultSet getColumns(String tableName) throws SQLException {
        Conn conn = null;
        try {
            conn = new Conn(connname);
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
        finally {
            conn.close();
        }
    }

    public void mysqlToOra(JspWriter out, String conn_name) throws
            ErrMsgException, SQLException, IOException {
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
            rs_table = getTableNames(conn_mysql.getCon());
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

                LogUtil.getLog(getClass()).info("tableName=" + tableName);

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
                pstmt_mysql = conn_mysql.prepareStatement(sql_select);
                rs = conn_mysql.executePreQuery();

                sql_insert = "insert into " + tableName + " (" + columns +
                             ") values (" + columnValues + ")";
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
            throw new ErrMsgException(StrUtil.trace(e));
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

    public void mysqlToMSSql(JspWriter out, String conn_name) throws
            ErrMsgException, SQLException, IOException {
        Conn conn_mysql = new Conn(connname);
        PreparedStatement pstmt_mysql = null;
        Conn conn_mssql = new Conn(conn_name);
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
            conn_mssql.beginTrans();
            rs_table = getTableNames(conn_mysql.getCon());
            while (rs_table.next()) {
                columns = "";
                columnValues = "";

                tableName = rs_table.getObject(3).toString(); //获得表名
                if (tableName.toLowerCase().equals("yearcount") ||
                    tableName.toLowerCase().equals("monthcount") ||
                    tableName.toLowerCase().equals("lastly") ||
                    tableName.toLowerCase().equals("daycount") ||
                    tableName.toLowerCase().equals("bc")) // 表的列名为数字，oracle中非法
                    continue;
                //sql_insert:insert into lastly (BC,OS,IP,Date) values (?,?,?,?)

                LogUtil.getLog(getClass()).info("tableName=" + tableName);

                sql_insert = "insert into " + tableName;
                rs_column = getColumns(rs_table.getObject(3).toString());
                int rowCount = 0;
                Vector v = new Vector();
                while (rs_column.next()) {
                    String colName = rs_column.getObject(4).toString();
                    v.addElement(colName);
                    columns += colName; //获得字段名
                    columnValues += "?";
                    if (!rs_column.isLast()) {
                        columns += ",";
                        columnValues += ",";
                    }
                    rowCount++;
                }
                columnType = new int[rowCount];
                int k = 0;
                rs_column.beforeFirst();
                while (rs_column.next()) {
                    columnType[k] = Integer.parseInt(
                            rs_column.getObject(5).toString());
                    k++;
                }

                // 获得数据
                sql_select = "select " + columns + " from " + tableName;
                pstmt_mysql = conn_mysql.prepareStatement(sql_select);
                rs = conn_mysql.executePreQuery();

              // conn_mssql.executeUpdate("SET IDENTITY_INSERT " + tableName + " ON");

                sql_insert = "insert into " + tableName + " (" + columns +
                             ") values (" + columnValues + ")";
                while (rs.next()) {
                    pstmt_ora = conn_mssql.prepareStatement(sql_insert);
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
                    conn_mssql.executePreUpdate();

                }

            }
            conn_mssql.commit();
        } catch (SQLException e) {
            conn_mssql.rollback();
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
            if (conn_mssql != null) {
                conn_mssql.close();
                conn_mssql = null;
            }

        }
    }

    /**
     * 从oracle库导为mysql
     * @param out JspWriter
     * @param conn_name String
     * @throws ErrMsgException
     * @throws SQLException
     * @throws IOException
     */
    public void oraToMysql(JspWriter out, String conn_name) throws ErrMsgException, SQLException, IOException {
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

        boolean isBegin = false;
        try {
            conn_ora.beginTrans();
            rs_table = getTableNames(conn_mysql.getCon());
            Vector tableV = new Vector();
            while (rs_table.next()) {
                tableName = rs_table.getObject(3).toString(); //获得表名
                tableV.addElement(tableName);
            }

            Iterator tableIr = tableV.iterator();
            while (tableIr.hasNext()) {
                tableName = (String)tableIr.next(); //获得表名

                columns = "";
                columnValues = "";

                if (tableName.toLowerCase().equals("yearcount") || tableName.toLowerCase().equals("monthcount") || tableName.toLowerCase().equals("lastly") || tableName.toLowerCase().equals("daycount")) // 表的列名为数字，oracle中非法
                    continue;

                // BIN$zZkhwwD1R2+zRgBqPnjWWg==$0表之前都是ORACLE的系统表，注意这个表的表名不一定是确定的
                // 要根据实际的情况来看是什么表名，通过LogUtil.getLog(getClass()).info(tableName)来观察表名
                // 经测试每次观察的表名都是系统表在前面出现
                if (tableName.equalsIgnoreCase("BIN$zZkhwwD1R2+zRgBqPnjWWg==$0")) {
                    isBegin = true;
                    continue;
                }

                if (!isBegin) {
                    continue;
                }
                // oracle中的系统表则跳过
                // if (tableName.indexOf("$")!=-1 || tableName.startsWith("SYS_")|| tableName.startsWith("MGMT_") || tableName.equalsIgnoreCase("COUNTRIES") || tableName.equals("DEPARTMENTS"))
                //    continue;

                //sql_insert:insert into lastly (BC,OS,IP,Date) values (?,?,?,?)

                LogUtil.getLog(getClass()).info("tableName=" + tableName);

                sql_insert = "insert into " + tableName;
                rs_column = getColumns(tableName);
                int rowCount = 0;
                Vector v = new Vector();
                Vector vType = new Vector();
                while (rs_column.next()) {
                    String colName = rs_column.getObject(4).toString();
                    v.addElement(colName);
                    // columns += colName; //获得字段名
                    // columnValues += "?";
                    // if (!rs_column.isLast()) {
                    //    columns += ",";
                    //    columnValues += ",";
                    // }
                    if (columns.equals(""))
                        columns = colName;
                    else
                        columns += "," + colName;

                    if (columnValues.equals(""))
                        columnValues = "?";
                    else
                        columnValues += ",?";

                    vType.addElement(rs_column.getObject(5).toString());
                    rowCount ++;
                }

                columnType = new int[rowCount];
                int k = 0;
                Iterator ir = vType.iterator();
                while (ir.hasNext()) {
                    columnType[k] = Integer.parseInt(
                            (String) ir.next());
                    k++;
                }

                // 获得数据
                sql_select = "select " + columns + " from " + tableName;
                pstmt_mysql = conn_mysql.prepareStatement(sql_select);
                rs = conn_mysql.executePreQuery();

                sql_insert = "insert into " + tableName + " (" + columns +
                             ") values (" + columnValues + ")";
                while (rs.next()) {
                    pstmt_ora = conn_ora.prepareStatement(sql_insert);
                    int i = 0;
                    while (i < rowCount) {
                        // tinyint(1)在getString()后得到的为布尔值true或false
                        if (columnType[i] == java.sql.Types.VARCHAR) {
                            pstmt_ora.setString(i+1, rs.getString(i + 1));
                        }
                        else if (columnType[i] == java.sql.Types.BOOLEAN) {
                            pstmt_ora.setInt(i+1, rs.getInt(i + 1));
                        }
                        else if (columnType[i] == java.sql.Types.TIMESTAMP) {
                            pstmt_ora.setTimestamp(i + 1, rs.getTimestamp(i + 1));
                        } else if (columnType[i] == java.sql.Types.DATE) {
                            pstmt_ora.setDate(i + 1, rs.getDate(i + 1));
                        } else if (columnType[i] == java.sql.Types.LONGVARCHAR) { // text类型
                            pstmt_ora.setString(i+1, rs.getString(i + 1));
                        } else if (columnType[i] == java.sql.Types.TINYINT || columnType[i] == java.sql.Types.INTEGER || columnType[i] == java.sql.Types.BIT) {
                            pstmt_ora.setInt(i+1, rs.getInt(i + 1));
                        } else if (columnType[i] == java.sql.Types.BIGINT) {
                            pstmt_ora.setLong(i+1, rs.getLong(i + 1));
                        } else if (columnType[i] == java.sql.Types.DECIMAL) {
                            pstmt_ora.setFloat(i+1, rs.getFloat(i + 1));
                        } else if (columnType[i] == java.sql.Types.CHAR) {
                            pstmt_ora.setString(i+1, rs.getString(i + 1));
                        }
                        else if (columnType[i]==java.sql.Types.OTHER) {
                            pstmt_ora.setString(i+1, rs.getString(i + 1));
                        }
                        else {
                            throw new ErrMsgException(v.elementAt(i) + " 类型 " + columnType[i] + " 不支持！ ");
                        }

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

    /**
     * 将oracle表中的内容导致mysql
     * @param out JspWriter
     * @param conn_ora_name String
     * @param tableV Vector 表名的Vector
     * @throws ErrMsgException
     * @throws SQLException
     * @throws IOException
     */
    public void oraToMysqlTable(JspWriter out, String conn_ora_name, Vector tableV) throws ErrMsgException, SQLException, IOException {
        Conn conn_mysql = new Conn(connname);
        PreparedStatement pstmt_mysql = null;
        Conn conn_ora = new Conn(conn_ora_name);
        PreparedStatement pstmt_ora = null;
        ResultSet rs_column = null;
        ResultSet rs = null;
        String sql_select = "";
        String sql_insert = "";
        String tableName = "";
        String columns = "";
        String columnValues = "";
        int[] columnType = null;

        boolean isBegin = false;
        try {
            conn_mysql.beginTrans();

            Iterator tableIr = tableV.iterator();
            while (tableIr.hasNext()) {
                tableName = (String)tableIr.next(); //获得表名

                out.print("tableName=" + tableName + "<BR>");
                columns = "";
                columnValues = "";

                // BIN$zZkhwwD1R2+zRgBqPnjWWg==$0表之前都是ORACLE的系统表，注意这个表的表名不一定是确定的
                // 要根据实际的情况来看是什么表名，通过LogUtil.getLog(getClass()).info(tableName)来观察表名
                // 经测试每次观察的表名都是系统表在前面出现
                if (tableName.equalsIgnoreCase("BIN$zZkhwwD1R2+zRgBqPnjWWg==$0")) {
                    isBegin = true;
                    continue;
                }

                // oracle中的系统表则跳过
                // if (tableName.indexOf("$")!=-1 || tableName.startsWith("SYS_")|| tableName.startsWith("MGMT_") || tableName.equalsIgnoreCase("COUNTRIES") || tableName.equals("DEPARTMENTS"))
                //    continue;

                //sql_insert:insert into lastly (BC,OS,IP,Date) values (?,?,?,?)

                out.println("tableName2=" + tableName + "<BR>");

                sql_insert = "insert into " + tableName;
                rs_column = getColumns(conn_ora_name, tableName);
                int rowCount = 0;
                Vector v = new Vector();
                Vector vType = new Vector();
                while (rs_column.next()) {
                    String colName = rs_column.getObject(4).toString();
                    v.addElement(colName);
                    // columns += colName; //获得字段名
                    // columnValues += "?";
                    // if (!rs_column.isLast()) {
                    //    columns += ",";
                    //    columnValues += ",";
                    // }
                    if (tableName.equalsIgnoreCase("users") && colName.equalsIgnoreCase("nick")) {
                        continue;
                    } else if (tableName.equalsIgnoreCase("users") && colName.equalsIgnoreCase("FORUMNAME")) {
                        continue;
                    }
                    if (columns.equals(""))
                        columns = colName;
                    else
                        columns += "," + colName;

                    if (columnValues.equals(""))
                        columnValues = "?";
                    else
                        columnValues += ",?";

                    vType.addElement(rs_column.getObject(5).toString());
                    rowCount ++;
                }

                columnType = new int[rowCount];
                int k = 0;
                Iterator ir = vType.iterator();
                while (ir.hasNext()) {
                    columnType[k] = Integer.parseInt(
                            (String) ir.next());
                    k++;
                }

                // 获得数据
                sql_select = "select " + columns + " from " + tableName;
                out.println(sql_select);
                pstmt_ora = conn_ora.prepareStatement(sql_select);
                rs = conn_ora.executePreQuery();

                sql_insert = "insert into " + tableName + " (" + columns +
                             ") values (" + columnValues + ")";
                out.println("sql_insert:" + sql_insert);
                while (rs.next()) {
                    pstmt_mysql = conn_mysql.prepareStatement(sql_insert);
                    int i = 0;
                    while (i < rowCount) {
                        // out.println(v.elementAt(i) + " " + columnType[i] + " = " + rs.getString(i + 1) + "<BR>");

                        // tinyint(1)在getString()后得到的为布尔值true或false
                        if (columnType[i] == java.sql.Types.VARCHAR) {
                            pstmt_mysql.setString(i+1, rs.getString(i + 1));
                        }
                        else if (columnType[i] == java.sql.Types.BOOLEAN) {
                            pstmt_mysql.setInt(i+1, rs.getInt(i + 1));
                        }
                        else if (columnType[i] == java.sql.Types.TIMESTAMP) {
                            pstmt_mysql.setTimestamp(i + 1, rs.getTimestamp(i + 1));
                        } else if (columnType[i] == java.sql.Types.DATE) {
                            pstmt_mysql.setDate(i + 1, rs.getDate(i + 1));
                        } else if (columnType[i] == java.sql.Types.LONGVARCHAR) { // text类型
                            pstmt_mysql.setString(i+1, rs.getString(i + 1));
                        } else if (columnType[i] == java.sql.Types.TINYINT || columnType[i] == java.sql.Types.INTEGER || columnType[i] == java.sql.Types.BIT) {
                            pstmt_mysql.setInt(i+1, rs.getInt(i + 1));
                        } else if (columnType[i] == java.sql.Types.BIGINT) {
                            pstmt_mysql.setLong(i+1, rs.getLong(i + 1));
                        } else if (columnType[i] == java.sql.Types.DECIMAL) {
                            pstmt_mysql.setFloat(i+1, rs.getFloat(i + 1));
                        } else if (columnType[i] == java.sql.Types.CHAR) {
                            pstmt_mysql.setString(i+1, rs.getString(i + 1));
                        }
                        else if (columnType[i] == java.sql.Types.REAL) {
                            pstmt_mysql.setDouble(i+1, rs.getDouble(i + 1));
                        }
                        else if (columnType[i]==java.sql.Types.OTHER) {
                            pstmt_mysql.setString(i+1, rs.getString(i + 1));
                        }
                        else
                            throw new ErrMsgException(v.elementAt(i) + " 类型 " + columnType[i] + " 不支持！ ");

                        i++;
                    }
                    conn_mysql.executePreUpdate();

                }

            }
            conn_mysql.commit();
        } catch (SQLException e) {
            conn_mysql.rollback();
            out.println(e.getMessage() + "<BR>");
            throw new ErrMsgException(StrUtil.trace(e));
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
}
