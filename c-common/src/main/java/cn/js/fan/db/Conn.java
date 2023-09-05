package cn.js.fan.db;

import java.sql.*;

import javax.naming.*;
import javax.sql.*;

import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.*;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.console.*;
import com.cloudweb.oa.utils.DruidManager;
import com.cloudwebsoft.framework.db.IConnection;
import cn.js.fan.util.RandomSecquenceCreator;
import com.cloudwebsoft.framework.util.LogUtil;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

public class Conn implements IConnection {
    private String id = "";

    // public: connection parameters
    boolean debug = true;
    java.sql.Connection con = null;

    Statement stmt = null;
    PreparedStatement pstmt = null;

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
    public Conn(String connname) {
        if (ConsoleConfig.isDebug()) {
            id = RandomSecquenceCreator.getId(10) + this.hashCode();
        }

        DBInfo dbi = Global.getDBInfo(connname);
        if (dbi == null) {
            LogUtil.getLog(getClass()).error("Conn:数据库连接池" + connname + "未找到");
            return;
        } else {
            isUsePool = dbi.isUsePool;
            DBDriver = dbi.DBDriver;
            ConnStr = dbi.ConnStr;
            PoolName = dbi.PoolName;
        }

        if (isUsePool) {
            initUsePool();
        } else {
            initNotUsePool();
        }

        /*
        try {
            // 判断是否支持事务
            DatabaseMetaData dm = null;
            dm = con.getMetaData();
            boolean supportTransaction = dm.supportsTransactions();
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("Conn:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        }
        */
        if (ConsoleConfig.isDebug()) {
            ConnMonitor.onGetConnection(this);
        }
    }

    public void initNotUsePool() {
        // 从JDBC 4.0开始，不需要加载驱动
        try {
            Class.forName(DBDriver);
        } catch (java.lang.ClassNotFoundException e) {
            LogUtil.getLog(getClass()).error("警告:Class not found exception occur. Message is:");
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        try {
            if (StrUtil.isEmpty(ConnStr)) {
                // Druid连接池获取连接
                con = DruidManager.getInstance().getConnection();
            } else {
                con = DriverManager.getConnection(ConnStr);
            }
        }
        // display sql error message
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("SQL Exception occur. Message is:");
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public void initUsePool() {
      try {
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup("java:comp/env");
        DataSource ds = (DataSource) envCtx.lookup(PoolName);
        con = ds.getConnection();
        // if (stmt==null)
        //    stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
        //                               ResultSet.CONCUR_READ_ONLY);
      }
      // display corresponding error message when onload error occur
      catch (NamingException e) {
        LogUtil.getLog(getClass()).error("Connection pool fail. Message is:");
        LogUtil.getLog(getClass()).error(e.getMessage());
      }
      catch (SQLException e) {
        LogUtil.getLog(getClass()).error("SQL Exception occur. Message is:");
        LogUtil.getLog(getClass()).error(e.getMessage());
      }
    }

    public java.sql.Connection getCon() {
        return this.con;
    }

    public Statement getStatement() {
        return this.stmt;
    }

    // perform a query with records returned
    public ResultSet executeQuery(String sql) throws SQLException {
        if (con == null) {
            return null;
        }
        if (stmt==null) {
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                       ResultSet.CONCUR_READ_ONLY);
        }
        try {
            long t = System.currentTimeMillis();
            rs = stmt.executeQuery(sql);
            if (Global.getInstance().isDebug()) {
                LogUtil.getLog(getClass()).info("sql: " + sql);
                LogUtil.getLog(getClass()).info("take " + (System.currentTimeMillis() - t) + " ms");
            }
            ConnMonitor.onExecuteQuery(this, sql, System.currentTimeMillis() - t);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("Query:" + sql);
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
        return rs;
    }

    public ResultSet executeQueryTFO(String sql) throws SQLException {
        if (con == null) {
            return null;
        }
        if (stmt==null) {
            stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                       ResultSet.CONCUR_READ_ONLY);
        }
        try {
            long t = System.currentTimeMillis();
            rs = stmt.executeQuery(sql);
            if (Global.getInstance().isDebug()) {
                LogUtil.getLog(getClass()).info("sql: " + sql);
                LogUtil.getLog(getClass()).info("take " + (System.currentTimeMillis() - t) + " ms");
            }
            ConnMonitor.onExecuteQuery(this, sql, System.currentTimeMillis() - t);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("sql:" + sql);
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
        return rs;
    }

    // perform a query without records returned
    public int executeUpdate(String sql) throws SQLException {
        if (con == null) {
            return 0;
        }

        if (stmt == null) {
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                       ResultSet.CONCUR_READ_ONLY);
        }
        int rowcount = 0;

        try {
            long t = System.currentTimeMillis();
            rowcount = stmt.executeUpdate(sql);
            if (Global.getInstance().isDebug()) {
                LogUtil.getLog(getClass()).info("sql: " + sql);
                LogUtil.getLog(getClass()).info("take " + (System.currentTimeMillis() - t) + " ms");
            }
            ConnMonitor.onExecuteQuery(this, sql, System.currentTimeMillis() - t);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("sql:" + sql);
            LogUtil.getLog(getClass()).error(e);
            throw e;
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
            LogUtil.getLog(getClass()).error(e);
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

    public void close() {
        ConnMonitor.onCloseConnection(this);

        // 20200524
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeStatement(pstmt);
        DataSourceUtils.releaseConnection(con, SpringUtil.getDataSource());
    }

    public void beginTrans() throws SQLException {
        if (!Global.isTransactionSupported) {
            return;
        }
        try {
            // boolean autoCommit=con.getAutoCommit();
            con.setAutoCommit(false);
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error("beginTrans error");
            LogUtil.getLog(getClass()).error(ex);
            throw ex;
        }
    }

    public void commit() throws SQLException {
        if (!Global.isTransactionSupported) {
            return;
        }
        try {
            con.commit();
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error("Commit error");
            LogUtil.getLog(getClass()).error(ex);
            throw ex;
        }
        finally {
            con.setAutoCommit(true);
        }
    }

    public void rollback() {
        if (!Global.isTransactionSupported) {
            return;
        }
        try {
            con.rollback();
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
    }

    public boolean getAutoCommit() throws SQLException {
        boolean result = false;
        try {
            result = con.getAutoCommit();
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
            throw ex;
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

    public void setFetchSize(int size) {
        try {
            // 如果使用了预编译
            if (pstmt!=null) {
                pstmt.setFetchSize(size);
                return;
            }
            // 如果没有使用预编译，为照顾到PageConn中的编写方式，此处要检测stmt是否为null
            if (stmt==null) {
                stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                           ResultSet.CONCUR_READ_ONLY);
            }
            stmt.setFetchSize(size);

        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    /**
     * Sets the max number of rows that should be returned from executing a
     * statement. The operation is automatically bypassed if cws knows that the
     * the JDBC driver or database doesn't support it.
     *
     * @param maxRows the max number of rows to return.
     */
    public void setMaxRows(int maxRows) throws
            SQLException {
        try {
            // 如果使用了预编译
            if (pstmt!=null) {
                pstmt.setMaxRows(maxRows);
                return;
            }
            // 如果没有使用预编译，为照顾到PageConn中的编写方式，此处要检测stmt是否为null
            if (stmt == null) {
                stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                           ResultSet.CONCUR_READ_ONLY);
            }
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
        if (pstmt!=null) {
            pstmt.close();
            pstmt = null;
        }
        if (Global.getInstance().isDebug()) {
            LogUtil.getLog(getClass()).info("sql: " + sql);
        }
        pstmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

        ConnMonitor.onPrepareStatement(this, sql);

        return pstmt;
    }

    public PreparedStatement prepareStatementTFO(String sql) throws SQLException {
        if (pstmt!=null) {
            pstmt.close();
            pstmt = null;
        }
        if (Global.getInstance().isDebug()) {
            LogUtil.getLog(getClass()).info("sql: " + sql);
        }
        pstmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ConnMonitor.onPrepareStatement(this, sql);

        return pstmt;
    }

    public ResultSet executePreQuery() throws SQLException {
        try {
            long t = System.currentTimeMillis();
            rs = pstmt.executeQuery();
            if (Global.getInstance().isDebug()) {
                LogUtil.getLog(getClass()).info("take " + (System.currentTimeMillis() - t) + " ms");
            }
            ConnMonitor.onExecutePreQuery(this, System.currentTimeMillis() - t);
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("Query:" + pstmt.toString());
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }

        return rs;
    }

    // perform a query without records returned
    public int executePreUpdate() throws SQLException {
        int rowcount = 0;
        if (con == null) {
            return 0;
        }
        try {
            long t = System.currentTimeMillis();
            rowcount = pstmt.executeUpdate();
            if (Global.getInstance().isDebug()) {
                LogUtil.getLog(getClass()).info("take " + (System.currentTimeMillis() - t) + " ms");
            }
            ConnMonitor.onExecutePreQuery(this, System.currentTimeMillis() - t);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("executePreUpdate:" + pstmt.toString());
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
        return rowcount;
    }

}
