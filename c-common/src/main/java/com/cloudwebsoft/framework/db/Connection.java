package com.cloudwebsoft.framework.db;

import java.sql.*;
import javax.naming.Context;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.utils.DruidManager;
import com.cloudweb.oa.utils.SpringUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.console.ConnMonitor;
import cn.js.fan.util.RandomSecquenceCreator;
import com.cloudwebsoft.framework.console.ConsoleConfig;
import com.cloudwebsoft.framework.util.LogUtil;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * <p>Title: 对于java.sql.Connection的封装</p>
 *
 * <p>Description: 用以取代cn.js.fan.db.Conn，增加了isSupportTransaction()判别是否支持事务</p>
 *
 * <p>Copyright: Copyright (c) 2006.6.6</p>
 *
 * <p>Company: </p>
 *
 * @author Blue Wind
 * @version 1.0
 */
public class Connection implements IConnection {
    private String id = "";

    // public: connection parameters
    boolean debug = false;
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

    boolean supportTransaction = false;

    public String connName;

    /**
     * @param connname String
     */
    // public: constructor to load driver and connect db
    public Connection(String connname) {
        if (ConsoleConfig.isDebug()) {
            id = RandomSecquenceCreator.getId(10) + this.hashCode();
        }

        this.connName = connname;
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

        // 如果系统设定不支持事务处理
        /*if (!Global.isTransactionSupported) {
            supportTransaction = false;
        } else {
            try {
                // 判断是否支持事务
                DatabaseMetaData dm = null;
                dm = con.getMetaData();
                supportTransaction = dm.supportsTransactions();

                System.out.println(getClass() + " supportTransaction " + (double)(System.currentTimeMillis() - t) /1000);
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }*/

        if (ConsoleConfig.isDebug()) {
            ConnMonitor.onGetConnection(this);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * 是否支持事务 2006.6.6
     *
     * @return boolean
     */
    public boolean isSupportTransaction() {
        return supportTransaction;
    }

    public void initNotUsePool() {
        // 从JDBC 4.0开始，不需要加载驱动
        /*try {
          Class.forName(DBDriver);
        }
        catch (java.lang.ClassNotFoundException e) {
          LogUtil.getLog(getClass()).error("警告:Class not found exception occur. Message is:");
          LogUtil.getLog(getClass()).error(e.getMessage());
        }*/
        try {
            if (StrUtil.isEmpty(ConnStr)) {
                // Druid连接池获取连接
                con = DruidManager.getInstance().getConnection();
            } else {
                con = DriverManager.getConnection(ConnStr);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    public void initUsePool() {
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup(PoolName);
            con = ds.getConnection();
        } catch (NamingException e) {
            LogUtil.getLog(getClass()).error("Connection pool fail. Message is:");
            LogUtil.getLog(getClass()).error(e.getMessage());
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("SQL Exception occur. Message is:");
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    /**
     * 取得原始的连接
     *
     * @return Connection
     */
    public java.sql.Connection getCon() {
        return this.con;
    }

    public Statement getStatement() {
        return this.stmt;
    }

    /**
     * 使用stmt查询
     *
     * @param sql String
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        if (con == null) {
            return null;
        }
        long t = System.currentTimeMillis();
        if (stmt == null) {
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        }

        try {
            long beginTime = System.currentTimeMillis();
            rs = stmt.executeQuery(sql);
            if (Global.getInstance().isDebug()) {
                LogUtil.getLog(getClass()).info("sql: " + sql);
                LogUtil.getLog(getClass()).info("take " + (System.currentTimeMillis() - t) + " ms");
            }
            ConnMonitor.onExecuteQuery(this, sql, System.currentTimeMillis() - beginTime);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("sql:" + sql);
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
        return rs;
    }

    /**
     * 用于DB2
     *
     * @param sql String
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet executeQueryTFO(String sql) throws SQLException {
        if (con == null) {
            return null;
        }
        if (stmt == null) {
            stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
        }
        try {
            long beginTime = System.currentTimeMillis();
            rs = stmt.executeQuery(sql);
            if (Global.getInstance().isDebug()) {
                LogUtil.getLog(getClass()).info("sql: " + sql);
                LogUtil.getLog(getClass()).info("take " + (System.currentTimeMillis() - beginTime) + " ms");
            }
            ConnMonitor.onExecuteQuery(this, sql, System.currentTimeMillis() - beginTime);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("sql:" + sql);
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
        return rs;
    }

    public void addBatch(String sql) throws SQLException {
        if (stmt == null) {
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        }
        stmt.addBatch(sql);
    }

    public int[] executeBatch() throws SQLException {
        return stmt.executeBatch();
    }

    /**
     * 使用stmt更新
     *
     * @param sql String
     * @return int
     * @throws SQLException
     */
    public int executeUpdate(String sql) throws SQLException {
        if (con == null) {
            return 0;
        }

        if (stmt == null) {
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        }
        int rowcount = 0;
        if (Global.getInstance().isDebug()) {
            LogUtil.getLog(getClass()).info("executeUpdate: " + sql);
        }
        try {
            long beginTime = System.currentTimeMillis();
            rowcount = stmt.executeUpdate(sql);
            if (Global.getInstance().isDebug()) {
                LogUtil.getLog(getClass()).info("sql: " + sql);
                LogUtil.getLog(getClass()).info("take " + (System.currentTimeMillis() - beginTime) + " ms");
            }
            ConnMonitor.onExecuteQuery(this, sql, System.currentTimeMillis() - beginTime);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("sql:" + sql);
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
        return rowcount;
    }

    /**
     * 取得结果集的列数
     *
     * @return int
     */
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

    /**
     * 取得结果集的行数
     *
     * @return int
     */
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

    /**
     * 关闭连接
     */
    public void close() { // throws Throwable
        ConnMonitor.onCloseConnection(this);

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeStatement(pstmt);
        DataSourceUtils.releaseConnection(con, SpringUtil.getDataSource());

        /*
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                // 在IAS904版本上，rs.close会出现问题，所以不应该catch SQLException
            }
            rs = null;
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
            }
            stmt = null;
        }
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (Exception e) {
            }
            con = null; // 防止因为多线程,导致二次关闭,使得其他线程出错
        }
        */
    }

    /**
     * 开始事务
     *
     * @throws SQLException
     */
    public void beginTrans() throws SQLException {
        if (!isSupportTransaction()) {
            LogUtil.getLog(getClass()).warn("Transaction is not supported");
            return;
        }
        try {
            // boolean autoCommit=con.getAutoCommit();
            con.setAutoCommit(false);
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
            throw ex;
        }
    }

    /**
     * 提交事务
     *
     * @throws SQLException
     */
    public void commit() throws SQLException {
        if (!isSupportTransaction()) {
            return;
        }
        try {
            con.commit();
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
            throw ex;
        } finally {
            con.setAutoCommit(true);
        }
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        if (!isSupportTransaction()) {
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
            if (pstmt != null) {
                pstmt.setFetchSize(size);
                return;
            }
            // 如果没有使用预编译，为照顾到PageConn中的编写方式，此处要检测stmt是否为null
            if (stmt == null) {
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
     * statement. The operation is automatically bypassed if CWBBS knows that the
     * the JDBC driver or database doesn't support it.
     *
     * @param maxRows the max number of rows to return.
     */
    public void setMaxRows(int maxRows) throws
            SQLException {
        try {
            // 如果使用了预编译
            if (pstmt != null) {
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

    /**
     * 创建pstmt
     *
     * @param sql String
     * @return PreparedStatement
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (pstmt != null) {
            pstmt.close();
            pstmt = null;
        }
        if (Global.getInstance().isDebug()) {
            LogUtil.getLog(getClass()).info("sql: " + sql);
        }
        pstmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        return pstmt;
    }

    /**
     * 创建pstmt，因为DB2中的LOB字段在读取时，光标类型只能是TYPE_FORWARD_ONLY
     *
     * @param sql String
     * @return PreparedStatement
     * @throws SQLException
     */
    public PreparedStatement prepareStatementTFO(String sql) throws SQLException {
        if (pstmt != null) {
            pstmt.close();
            pstmt = null;
        }
        pstmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        return pstmt;
    }

    /**
     * 使用pstmt查询
     *
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet executePreQuery() throws SQLException {
        try {
            long beginTime = System.currentTimeMillis();
            rs = pstmt.executeQuery();
            if (Global.getInstance().isDebug()) {
                LogUtil.getLog(getClass()).info("executePreQuery take " + (System.currentTimeMillis() - beginTime) + " ms");
            }
            ConnMonitor.onExecutePreQuery(this, System.currentTimeMillis() - beginTime);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }

        return rs;
    }

    /**
     * 使用pstmt更新
     *
     * @return int
     * @throws SQLException
     */
    public int executePreUpdate() throws SQLException {
        int rowcount = 0;
        if (con == null) {
            return 0;
        }
        try {
            long beginTime = System.currentTimeMillis();
            rowcount = pstmt.executeUpdate();
            ConnMonitor.onExecutePreQuery(this, System.currentTimeMillis() - beginTime);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
        return rowcount;
    }

    public boolean isClosed() {
        if (con == null) {
            return true;
        }
        boolean re = false;
        try {
            re = con.isClosed();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return re;
    }

}
