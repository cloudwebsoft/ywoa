package com.cloudwebsoft.framework.db;

import java.sql.*;
import javax.naming.Context;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import cn.js.fan.web.Global;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.console.ConnMonitor;
import cn.js.fan.util.RandomSecquenceCreator;
import com.cloudwebsoft.framework.console.ConsoleConfig;
import java.util.Vector;

/**
 *
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

    Logger logger;
    boolean isUsePool = false;

    boolean supportTransaction = false;

    public String connName;

    /**
     * @param connname String
     */
    // public: constructor to load driver and connect db
    public Connection(String connname) {
        if (ConsoleConfig.isDebug())
            id = RandomSecquenceCreator.getId(10) + this.hashCode();

        this.connName = connname;
        logger = Logger.getLogger(Connection.class.getName());
        DBInfo dbi = Global.getDBInfo(connname);
        if (dbi == null) {
            logger.error("Conn:数据库连接池" + connname + "未找到");
            return;
        } else {
            isUsePool = dbi.isUsePool;
            DBDriver = dbi.DBDriver;
            ConnStr = dbi.ConnStr;
            PoolName = dbi.PoolName;
        }

        if (isUsePool)
            initUsePool();
        else
            initNotUsePool();

        // 系统设定不支持事务处理
        if (!Global.isTransactionSupported)
            supportTransaction = false;
        else {
            try {
                // 判断是否支持事务
                DatabaseMetaData dm = null;
                dm = con.getMetaData();
                supportTransaction = dm.supportsTransactions();
            } catch (SQLException e) {
                logger.error("Conn:" + e.getMessage());
                e.printStackTrace();
            }
        }

        ConnMonitor.onGetConnection(this);

    }

    public String getId() {
        return id;
    }

    /**
     * 是否支持事务 2006.6.6
     * @return boolean
     */
    public boolean isSupportTransaction() {
        return supportTransaction;
    }

    public void initNotUsePool() {
      try {
          Class.forName(DBDriver);
      }
      // display corresponding error message when onload error occur
      catch (java.lang.ClassNotFoundException e) {
          logger.error(
                  "警告:Class not found exception occur. Message is:");
          logger.error(e.getMessage());
      }
      // establish connection to the database throught driver
      try {
          con = DriverManager.getConnection(ConnStr);
          // if (stmt==null)
          //    stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
          //                             ResultSet.CONCUR_READ_ONLY);
      }
      // display sql error message
      catch (SQLException e) {
          logger.error("SQL Exception occur. Message is:");
          logger.error(e.getMessage());
      }
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
        logger.error("Connection pool fail. Message is:");
        logger.error(e.getMessage());
      }
      catch (SQLException e) {
        logger.error("SQL Exception occur. Message is:");
        logger.error(e.getMessage());
      }
    }

    /**
     * 取得原始的连接
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
     * @param sql String
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        if (con == null)
            return null;
        if (stmt==null)
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                       ResultSet.CONCUR_READ_ONLY);
        try {
            long beginTime = System.currentTimeMillis();
            rs = stmt.executeQuery(sql);
            ConnMonitor.onExecuteQuery(this, sql, System.currentTimeMillis() - beginTime);
        } catch (SQLException e) {
            // if (debug)
                System.out.println("executeQuery exception: sql=" + sql + "\r\n" + e.getMessage());
            throw e;
        }
        return rs;
    }

    /**
     * 用于DB2
     * @param sql String
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet executeQueryTFO(String sql) throws SQLException {
        if (con == null)
            return null;
        if (stmt==null)
            stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                       ResultSet.CONCUR_READ_ONLY);
        try {
            long beginTime = System.currentTimeMillis();
            rs = stmt.executeQuery(sql);
            ConnMonitor.onExecuteQuery(this, sql, System.currentTimeMillis() - beginTime);
        } catch (SQLException e) {
            if (debug)
                System.out.println("Query:" + sql + "---" + e.getMessage());
            throw e;
        }
        return rs;
    }

    public void addBatch(String sql) throws SQLException {
        if (stmt==null)
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                       ResultSet.CONCUR_READ_ONLY);
        stmt.addBatch(sql);
    }

    public int[] executeBatch()throws SQLException {
        return stmt.executeBatch();
    }

    /**
     * 使用stmt更新
     * @param sql String
     * @return int
     * @throws SQLException
     */
    public int executeUpdate(String sql) throws SQLException {
        if (con == null)
            return 0;

        if (stmt == null)
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                       ResultSet.CONCUR_READ_ONLY);
        int rowcount = 0;

        try {
            long beginTime = System.currentTimeMillis();
            rowcount = stmt.executeUpdate(sql);
            ConnMonitor.onExecuteQuery(this, sql, System.currentTimeMillis() - beginTime);
        } catch (SQLException e) {
            if (debug) {
                System.out.println("Update:" + sql + "--" + e.getMessage());
            }
            throw e;
        }
        return rowcount;
    }

    /**
     * 取得结果集的列数
     * @return int
     */
    public int getColumns() {
        int columns = 0;
        try {
            this.resultsMeta = this.rs.getMetaData();
            columns = this.resultsMeta.getColumnCount();
        } catch (SQLException e) {}
        return columns;
    }

    /**
     * 取得结果集的行数
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
            System.out.println("getRows error:" + e.getMessage());
        }
        return this.rows;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * 关闭连接
     */
    public void close() { // throws Throwable
        ConnMonitor.onCloseConnection(this);

        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                // 在IAS904版本上，rs.close会出现问题，所以不应该catch SQLException
                System.out.println("Conn finalize1: " + e.getMessage());
            }
            rs = null;
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                System.out.println("Conn finalize2: " + e.getMessage());
            }
            stmt = null;
        }
        // 这里并没有关闭pstmt，不会引起proxool报二次关闭的问题，但是如果调用Connection的时候，要注意手工关闭PreparedStatement
        /*
                     if (pstmt != null) {
            try {
                // prestmt.clearWarnings();
                // 如果原来已关闭，则proxool会报告WARN http-8080-Processor2 org.logicalcobwebs.proxool.zjzjxx - 000190 (01/01/00) - #1 registered a statement as closed which wasn't known to be open.
                pstmt.close();
            } catch (SQLException e) {
                System.out.println("Conn finalize3: " + e.getMessage());
            }
            pstmt = null;
                     }
         */
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (Exception e) {
                System.out.println("Conn finalize: " + e.getMessage());
            }
            con = null; // 防止因为多线程,导致二次关闭,使得其他线程出错
        }
    }
    /**
     * 开始事务
     * @throws SQLException
     */
    public void beginTrans() throws SQLException {
        if (!isSupportTransaction()) {
            System.out.println("Transaction is not supported");
            return;
        }
        try {
            // boolean autoCommit=con.getAutoCommit();
            con.setAutoCommit(false);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.print("beginTrans Errors");
            throw ex;
        }
    }

    /**
     * 提交事务
     * @throws SQLException
     */
    public void commit() throws SQLException {
        if (!isSupportTransaction())
            return;
        try {
            con.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (debug)
                System.out.print("Commit Errors");
            throw ex;
        }
        finally {
            con.setAutoCommit(true);
        }
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        if (!isSupportTransaction())
            return;
        try {
            con.rollback();
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (debug)
                System.out.print("Rollback Errors");
            // throw ex;
        }
    }

    public boolean getAutoCommit() throws SQLException {
        boolean result = false;
        try {
            result = con.getAutoCommit();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("getAutoCommit fail" + ex.getMessage());
            throw ex;
        }
        return result;
    }

    public int getTransactionIsolation() throws SQLException {
        int re = 0;
        try {
            re = con.getTransactionIsolation();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("getTransactionIsolation fail" + e.getMessage());
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
            System.out.println("setFetchSize fail:" + e.getMessage());
        }

    }

    /**
     * Sets the max number of rows that should be returned from executing a
     * statement. The operation is automatically bypassed if CWBBS knows that the
     * the JDBC driver or database doesn't support it.
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
            if (stmt == null)
                stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                           ResultSet.CONCUR_READ_ONLY);
            stmt.setMaxRows(maxRows);
        } catch (Throwable t) {
            // Ignore. Exception may happen if the driver doesn't support
            // this operation and we didn't set meta-data correctly.
            // However, it is a good idea to update the meta-data so that
            // we don't have to incur the cost of catching an exception
            // each time.
            System.out.println("conn.setMaxRows:" + t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * 创建pstmt
     * @param sql String
     * @return PreparedStatement
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (pstmt!=null) {
            pstmt.close();
            pstmt = null;
        }
        pstmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                                       ResultSet.CONCUR_READ_ONLY);
        return pstmt;
    }

    /**
     * 创建pstmt，因为DB2中的LOB字段在读取时，光标类型只能是TYPE_FORWARD_ONLY
     * @param sql String
     * @return PreparedStatement
     * @throws SQLException
     */
    public PreparedStatement prepareStatementTFO(String sql) throws SQLException {
        if (pstmt!=null) {
            pstmt.close();
            pstmt = null;
        }
        pstmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                                       ResultSet.CONCUR_READ_ONLY);
        return pstmt;
    }

    /**
     * 使用pstmt查询
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet executePreQuery() throws SQLException {
        try {
            long beginTime = System.currentTimeMillis();
            rs = pstmt.executeQuery();
            ConnMonitor.onExecutePreQuery(this, System.currentTimeMillis() - beginTime);
        }
        catch (SQLException e) {
            System.out.print("executePreQuery:" + pstmt.toString() + "---" + e.getMessage());
            throw e;
        }

        return rs;
    }

    /**
     * 使用pstmt更新
     * @return int
     * @throws SQLException
     */
    public int executePreUpdate() throws SQLException {
        int rowcount = 0;
        if (con == null)
            return 0;
        try {
            long beginTime = System.currentTimeMillis();
            rowcount = pstmt.executeUpdate();
            ConnMonitor.onExecutePreQuery(this, System.currentTimeMillis() - beginTime);
        } catch (SQLException e) {
            if (debug) {
                System.out.println(getClass() + "executePreUpdate:" + pstmt.toString() + "---"  + e.getMessage());
            }
            throw e;
        }
        return rowcount;
    }

    public boolean isClosed() {
        if (con==null)
            return true;
        boolean re = false;
        try {
            re = con.isClosed();
        }
        catch (SQLException e) {
            System.out.println(getClass() + " isClosed:" + e.getMessage());
        }
        return re;
    }

}
