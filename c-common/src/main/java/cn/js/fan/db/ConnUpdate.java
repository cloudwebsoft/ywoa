package cn.js.fan.db;

import java.sql.*;
import javax.naming.Context;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.DruidManager;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

public class ConnUpdate {
    // public: connection parameters
    boolean debug = true;
    java.sql.Connection con = null;

    Statement stmt = null;
    PreparedStatement prestmt = null;

    ResultSet rs = null;
    ResultSetMetaData resultsMeta = null;
    int rows = 0;
    int columns = 0;
    String PoolName = null;
    String DBDriver = "";  // "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    String ConnStr = "";   // "jdbc:microsoft:sqlserver://127.0.0.1:1433;DatabaseName=zjnldw;User=sa;Password=111111";

    boolean isUsePool = false;

    /**
     * @param connname String
     */
    // public: constructor to load driver and connect db
    public ConnUpdate(String connname) {
        isUsePool = Global.getDBInfo(connname).isUsePool;
        DBDriver = Global.getDBInfo(connname).DBDriver;
        ConnStr = Global.getDBInfo(connname).ConnStr;
        PoolName = Global.getDBInfo(connname).PoolName;

        if (isUsePool)
            initUsePool();
        else
            initNotUsePool();
    }

    public void initNotUsePool() {
        try {
            Class.forName(DBDriver);
        }
        // display corresponding error message when onload error occur
        catch (java.lang.ClassNotFoundException e) {
            LogUtil.getLog(getClass()).error(
                    "警告:Class not found exception occur. Message is:");
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        // establish connection to the database throught driver
        try {
            if (ConnStr.startsWith("proxool.")) {
                con = DriverManager.getConnection(ConnStr);
            } else {
                // Druid连接池获取连接
                con = DruidManager.getInstance().getConnection();
            }
        }
        // display sql error message
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("SQL Exception occur. Message is:");
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public void initUsePool() {
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup(PoolName);
            con = ds.getConnection();
        }
        // display corresponding error message when onload error occur
        catch (NamingException e) {
            LogUtil.getLog(getClass()).error("Connection pool fail. Message is:");
            LogUtil.getLog(getClass()).error(e.getMessage());
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("SQL Exception occur. Message is:");
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public java.sql.Connection getCon() {
        return this.con;
    }

    // perform a query with records returned
    public ResultSet executeQuery(String sql) throws SQLException {
        if (con == null)
            return null;
        if (stmt == null)
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("sql:" + sql);
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
        this.rs = rs;
        return rs;
    }

    // perform a query without records returned
    public int executeUpdate(String sql) throws SQLException {
        if (stmt == null)
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        int rowcount = 0;
        if (con == null)
            return 0;
        try {
            rowcount = stmt.executeUpdate(sql);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("sql:" + sql);
            LogUtil.getLog(getClass()).error(e);
            throw e;
        } finally {

        }
        return rowcount;
    }

    // return the num of columns
    public int getColumns() {
        int columns = 0;
        try {
            this.resultsMeta = this.rs.getMetaData();
            columns = this.resultsMeta.getColumnCount();
        } catch (SQLException e) {
        }
        return columns;
    }

    // return the num of rows
    public int getRows() {
        rows = 0;
        try {
            //获取记录总数
            rs.last();
            rows = rs.getRow();
            rs.beforeFirst();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return this.rows;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void close() { // throws Throwable
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeStatement(prestmt);
        DataSourceUtils.releaseConnection(con, SpringUtil.getDataSource());
        /*
        try {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        } catch (SQLException e) {
        }
        try {
            if (prestmt != null) {
                prestmt.close();
                prestmt = null;
            }
        } catch (SQLException e) {
        }

        try {
            if (con != null && !con.isClosed()) {
                con.close();
                con = null; //防止因为多线程,导致二次关闭,使得其他线程出错
            }
        } catch (SQLException e) {
        }
        */
    }

    public void beginTrans() throws SQLException {
        if (!Global.isTransactionSupported)
            return;
        try {
            //boolean autoCommit=con.getAutoCommit();
            con.setAutoCommit(false);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
    }

    public void commit() throws SQLException {
        if (!Global.isTransactionSupported)
            return;
        try {
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
    }

    public void rollback() {
        if (!Global.isTransactionSupported)
            return;
        try {
            con.rollback();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            //throw ex;
        }
    }

    public boolean getAutoCommit() throws SQLException {
        boolean result = false;
        try {
            result = con.getAutoCommit();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
        return result;
    }

    public int getTransactionIsolation() throws SQLException {
        int re = 0;
        try {
            re = con.getTransactionIsolation();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return re;
    }

    public void setFetchSize(int s) {
        try {
            stmt.setFetchSize(s);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    /**
     * Sets the max number of rows that should be returned from executing a
     * statement. The operation is automatically bypassed if Jive knows that the
     * the JDBC driver or database doesn't support it.
     *
     * @param maxRows the max number of rows to return.
     */
    public void setMaxRows(int maxRows) throws
            SQLException {
        try {
            stmt.setMaxRows(maxRows);
        } catch (Throwable t) {
            // Ignore. Exception may happen if the driver doesn't support
            // this operation and we didn't set meta-data correctly.
            // However, it is a good idea to update the meta-data so that
            // we don't have to incur the cost of catching an exception
            // each time.
            LogUtil.getLog(getClass()).error(t);
        }
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        prestmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        return prestmt;
    }

    public ResultSet executePreQuery() throws SQLException {
        try {
            rs = prestmt.executeQuery();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("Query:" + prestmt.toString());
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }

        return rs;
    }

    // perform a query without records returned
    public int executePreUpdate() throws SQLException {
        int rowcount = 0;
        if (con == null)
            return 0;
        try {
            rowcount = prestmt.executeUpdate();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("Query:" + prestmt.toString());
            LogUtil.getLog(getClass()).error(e);
            throw e;
        } finally {
            return rowcount;
        }
    }

}
