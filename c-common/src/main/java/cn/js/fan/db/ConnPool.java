package cn.js.fan.db;

import java.sql.*;
import javax.naming.Context;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;

public class ConnPool {
  // public: connection parameters
  boolean debug = true;
  java.sql.Connection con = null;
  Statement stmt = null;
  ResultSet rs = null;
  ResultSetMetaData resultsMeta = null;
  int rows = 0;
  int columns = 0;
  String PoolName = null;
  boolean inited = false;

  // public: constructor to load driver and connect db
  public ConnPool() {

  }

  public void init() {
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      DataSource ds = (DataSource) envCtx.lookup("jdbc/"+PoolName);
      con = ds.getConnection();
      stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                 ResultSet.CONCUR_READ_ONLY);
    }
    // display corresponding error message when onload error occur
    catch (NamingException e) {
      LogUtil.getLog(getClass()).error(e);
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error(e);
    }
    inited = true;
  }

  public ConnPool(String pn) {
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      DataSource ds = (DataSource) envCtx.lookup("jdbc/"+pn);
      con = ds.getConnection();
      stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                 ResultSet.CONCUR_READ_ONLY);
    }
    // display corresponding error message when onload error occur
    catch (NamingException e) {
      LogUtil.getLog(getClass()).error(e);
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error(e);
    }
    PoolName = pn;
    inited = true;
  }

  public void setPOOLNAME(String pn) {
    if (!inited)
    {
      this.PoolName = pn;
      init();
    }
  }

  public String getPOOLNAME() {
    return this.PoolName;
  }

  // perform a query with records returned
  public ResultSet executeQuery(String sql) throws SQLException {
    if (con == null)
      return null;
    ResultSet rs = null;
    try {
      rs = stmt.executeQuery(sql);
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error("sql:" + sql);
      LogUtil.getLog(getClass()).error(e);
      throw e;
    }
    this.rs = rs;
    return rs;
  }

  // perform a query without records returned
  public int executeUpdate(String sql) throws SQLException {
    int rowcount = 0;
    if (con == null)
      return 0;
    try {
      rowcount = stmt.executeUpdate(sql);
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error("sql:" + sql);
      LogUtil.getLog(getClass()).error(e);
      throw e;
    }
    finally {
      return rowcount;
    }
  }

  // return the num of columns
  public int getColumns() {
    int columns = 0;
    try {
      this.resultsMeta = this.rs.getMetaData();
      columns = this.resultsMeta.getColumnCount();
    }
    catch (SQLException e) {}
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
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error(e);
    }
    return this.rows;
  }

  protected void finalize() throws Throwable {
    try {
      if (stmt != null) {
        stmt.close();
        stmt = null;
      }
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error(e);
    }
    try {
      if (con != null && !con.isClosed())
      {
        con.close();
        con = null;//防止因为多线程,导致二次关闭,使得其他线程出错
      }
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error(e);
    }
    super.finalize();
  }

  public void close() { // throws Throwable
    try {
      finalize();
    }
    catch (java.lang.Throwable e) {
      LogUtil.getLog(getClass()).error(e);
    }
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
      }
  }

  public boolean getAutoCommit() throws SQLException {
    boolean result = false;
    try {
      result = con.getAutoCommit();
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error(e);
      throw e;
    }
    return result;
  }

  public int getTransactionIsolation() throws SQLException {
    int re = 0;
    try {
      re = con.getTransactionIsolation();
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error(e);
    }
    return re;
  }

  public void setFetchSize(int s) {
    try {
      stmt.setFetchSize(s);
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error(e);
    }

  }

  /**
    * Sets the max number of rows that should be returned from executing a
    * statement. The operation is automatically bypassed if Jive knows that the
    * the JDBC driver or database doesn't support it.
    *
    * @param stmt the Statement to set the max number of rows for.
    * @param maxRows the max number of rows to return.
    */
   public void setMaxRows(int maxRows) throws
       SQLException {
     try {
       stmt.setMaxRows(maxRows);
     }
     catch (Throwable t) {
       // Ignore. Exception may happen if the driver doesn't support
       // this operation and we didn't set meta-data correctly.
       // However, it is a good idea to update the meta-data so that
       // we don't have to incur the cost of catching an exception
       // each time.
       LogUtil.getLog(getClass()).error(t);
     }
   }

}
