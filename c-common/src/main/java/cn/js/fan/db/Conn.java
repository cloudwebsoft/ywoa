package cn.js.fan.db;

import java.sql.*;

import javax.naming.*;
import javax.sql.*;

import cn.js.fan.web.*;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.console.*;
import com.cloudweb.oa.utils.DruidManager;
import org.apache.log4j.*;
import com.cloudwebsoft.framework.db.IConnection;
import cn.js.fan.util.RandomSecquenceCreator;
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

    Logger logger;
    boolean isUsePool = false;

    /**
     * @param connname String
     */
    // public: constructor to load driver and connect db
    public Conn(String connname) {
        if (ConsoleConfig.isDebug()) {
            id = RandomSecquenceCreator.getId(10) + this.hashCode();
        }

        logger = Logger.getLogger(Conn.class.getName());
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
            logger.error("Conn:" + e.getMessage());
            e.printStackTrace();
        }
        */

       ConnMonitor.onGetConnection(this);
    }

    public void initNotUsePool() {
      try {
          Class.forName(DBDriver);
      }
      // display corresponding error message when onload error occur
      catch (java.lang.ClassNotFoundException e) {
          logger.error("警告:Class not found exception occur. Message is:");
          logger.error(e.getMessage());
      }
      // establish connection to the database throught driver
      try {
          if (ConnStr.startsWith("proxool.")) {
              con = DriverManager.getConnection(ConnStr);
          }
          else {
              // Druid连接池获取连接
              con = DruidManager.getInstance().getConnection();
          }
      }
      // display sql error message
      catch (SQLException e) {
          logger.error("SQL Exception occur. Message is:");
          logger.error(e.getMessage());
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
        logger.error("Connection pool fail. Message is:");
        logger.error(e.getMessage());
      }
      catch (SQLException e) {
        logger.error("SQL Exception occur. Message is:");
        logger.error(e.getMessage());
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
            System.out.println("Query:" + sql + "---" + e.getMessage());
            throw e;
        }
        return rs;
    }

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
            System.out.println("Query:" + sql + "---" + e.getMessage());
            throw e;
        }
        return rs;
    }

    // perform a query without records returned
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

    // return the num of columns
    public int getColumns() {
        int columns = 0;
        try {
            this.resultsMeta = this.rs.getMetaData();
            columns = this.resultsMeta.getColumnCount();
        } catch (SQLException e) {}
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
            System.out.println("getRows error:" + e.getMessage());
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

        /*
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                // 在有些oracle8i上不是抛出SQLException
                // java.lang.NullPointerException  at oracle.jdbc.driver.ScrollableResultSet.close(ScrollableResultSet.java :148)
                System.out.println("Conn finalize1: " + e.getMessage());
            }
            rs = null;
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.out.println("Conn finalize2: " + e.getMessage());
            }
            stmt = null;
        }
        if (pstmt != null) {
            try {
                // prestmt.clearWarnings();
                // 如果原来已关闭，则proxool会报告WARN http-8080-Processor2 org.logicalcobwebs.proxool.zjzjxx - 000190 (01/01/00) - #1 registered a statement as closed which wasn't known to be open.
                *//*
                        public void registerClosedStatement(Statement statement) {
                 158         if (openStatements.contains(statement)) {
                 159             openStatements.remove(statement);
                 160         } else {
                 161             connectionPool.getLog().warn(connectionPool.displayStatistics() + " - #" + getId() + " registered a statement as closed which wasn't known to be open.");
                 162         }
                 163     }
                 *//*
                pstmt.close();
            } catch (SQLException e) {
                System.out.println("Conn finalize3: " + e.getMessage());
            }
            pstmt = null;
        }
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (SQLException e) {
                System.out.println("Conn finalize: " + e.getMessage());
            }
            con = null; // 防止因为多线程,导致二次关闭,使得其他线程出错
        }*/
    }

    public void beginTrans() throws SQLException {
        if (!Global.isTransactionSupported) {
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

    public void commit() throws SQLException {
        if (!Global.isTransactionSupported)
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

    public void rollback() {
        if (!Global.isTransactionSupported)
            return;
        try {
            con.rollback();
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (debug)
                System.out.print("Rollback Errors");
            //throw ex;
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
     * statement. The operation is automatically bypassed if cws knows that the
     * the JDBC driver or database doesn't support it.
     *
     * @param stmt the Statement to set the max number of rows for.
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
        }
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (pstmt!=null) {
            pstmt.close();
            pstmt = null;
        }
        pstmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                                       ResultSet.CONCUR_READ_ONLY);

        ConnMonitor.onPrepareStatement(this, sql);

        return pstmt;
    }

    public PreparedStatement prepareStatementTFO(String sql) throws SQLException {
        if (pstmt!=null) {
            pstmt.close();
            pstmt = null;
        }
        pstmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                                       ResultSet.CONCUR_READ_ONLY);

        ConnMonitor.onPrepareStatement(this, sql);

        return pstmt;
    }

    public ResultSet executePreQuery() throws SQLException {
        try {
            long beginTime = System.currentTimeMillis();
            rs = pstmt.executeQuery();
            ConnMonitor.onExecutePreQuery(this, System.currentTimeMillis() - beginTime);
        }
        catch (SQLException e) {
            System.out.print("Query:" + pstmt.toString() + "---" + e.getMessage());
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
            long beginTime = System.currentTimeMillis();
            rowcount = pstmt.executeUpdate();
            ConnMonitor.onExecutePreQuery(this, System.currentTimeMillis() - beginTime);
        } catch (SQLException e) {
            if (debug) {
                System.out.println("executePreUpdate:" + pstmt.toString() + "---"  + e.getMessage());
            }
            throw e;
        }
        return rowcount;
    }

}
