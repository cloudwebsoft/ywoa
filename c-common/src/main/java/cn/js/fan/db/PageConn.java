package cn.js.fan.db;

import cn.js.fan.util.file.FileUtil;
import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Vector;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

/*
 在旧版中 类中置属性Vector result;在executeQuery中将result作为返回值
 然后在finalize()中作如下操作
if (result != null) {
      result.removeAllElements();
      result = null;
}
 这就可能导致返回值result在被使用时，如果本类被构析，result中的信息就会丢失，变为size=0
 所以要注意，另外Vector还要注意线程同步的问题
 */

public class PageConn {
  int rowcount = 0; //实际取得的记录行数
  int colcount = 0;
  int pageSize = 10;
  public int curPage = 1;
  public long total = 0; //由sql语句得到的总记录条数

  HashMap mapIndex;
  String connname;

  public PageConn(String connname) {
    mapIndex = new HashMap();
    this.connname = connname;
  }

  public PageConn(String connname, int curPage, int pageSize) {
    mapIndex = new HashMap();
    this.curPage = curPage;
    this.pageSize = pageSize;
    this.connname = connname;
  }

  public long getTotal() {
    return total;
  }

  protected void finalize() throws Throwable {
    super.finalize();
  }

  public int getColumncount() {
    return colcount;
  }

  public int getRowcount() {
    return rowcount;
  }

  /**
   * 取出全部信息置于result中
   * @param sql String
   * @return Vector
   */
  public Vector executeQuery(String sql) {
    rowcount = 0;
    colcount = 0;

    ResultSet rs = null;
    Vector result = null;
    Conn conn = new Conn(connname);
    try {
      rs = conn.executeQuery(sql);
      if (rs == null) {
        return null;
      }
      else {
        //取得列名信息
        ResultSetMetaData rm = rs.getMetaData();
        colcount = rm.getColumnCount();
        for (int i = 1; i <= colcount; i++) {
          mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
        }

        result = new Vector();

        ResultWrapper rsw = new ResultWrapper(rs);
        while (rsw.next()) {
          Vector row = new Vector();
          for (int i = 0; i < colcount; i++)
            row.addElement(rsw.getObject(i + 1));
          result.addElement(row);
          rowcount++;
        }
      }
    }
    catch (Exception e) {
      LogUtil.getLog(getClass()).error(e);
      return null;
    }
    finally {
      if (rs != null) {
        try {
          rs.close();
        }
        catch (Exception e) {}
        rs = null;
      }
      if (conn != null) {
        conn.close();
        conn = null;
      }
    }
    return result;
  }

  /**
   * 分页操作，将ResultSet的信息保存在Vector中，以利用Iterator模式
   * @param sql String　sql查询语句
   * @param curPage int　当前页
   * @param pageSize int　页的记录条数
   * @return Vector　行信息存储于Vector中，所有行再存储于一个大的Vector中
   */
  public Vector executeQuery(String sql, int curPage, int pageSize) {
    this.curPage = curPage;
    this.pageSize = pageSize;

    rowcount = 0;
    colcount = 0;

    ResultSet rs = null;
    Vector result = null;
    Conn conn = new Conn(connname);
    try {
      //取得总记录条数
      String countsql = SQLFilter.getCountSql(sql);
      //logger.debug(countsql);
      rs = conn.executeQuery(countsql);
      if (rs != null && rs.next()) {
        total = rs.getLong(1);
      }
      if (rs != null) {
        rs.close();
        rs = null;
      }

      // 防止受到攻击时，curPage被置为很大，或者很小
      int totalpages = (int) Math.ceil((double) total / pageSize);
      if (curPage > totalpages)
          curPage = totalpages;
      if (curPage <= 0)
          curPage = 1;

      if (total != 0)
        conn.setMaxRows(curPage * pageSize); //尽量减少内存的使用

      rs = conn.executeQuery(sql);
      if (rs == null) {
        return null;
      }
      else {
        //取得列名信息
        ResultSetMetaData rm = rs.getMetaData();
        colcount = rm.getColumnCount();
        for (int i = 1; i <= colcount; i++) {
          mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
        }

        rs.setFetchSize(pageSize);

        int absoluteLocation = pageSize * (curPage - 1) + 1;
        if (rs.absolute(absoluteLocation) == false) {
          return null;
        }

        result = new Vector();

        ResultWrapper rsw = new ResultWrapper(rs);
        do {
          Vector row = new Vector();
          for (int i = 0; i < colcount; i++)
            row.addElement(rsw.getObject(i + 1));
          result.addElement(row);
          rowcount++;
        }
        while (rsw.next());
      }
    }
    catch (Exception e) {
      LogUtil.getLog(getClass()).error(e.getMessage());
      return null;
    }
    finally {
      if (rs != null) {
        try {
          rs.close();
        }
        catch (Exception e) {}
        rs = null;
      }
      if (conn != null) {
        conn.close();
        conn = null;
      }
    }
    return result;
  }

  public ResultIterator getAllResultIterator(String sql) {
    Vector r = executeQuery(sql);
    return new ResultIterator(r, mapIndex);
  }

  public ResultIterator getResultIterator(String sql) {
    Vector r = executeQuery(sql, curPage, pageSize);
    return new ResultIterator(r, mapIndex, total);
  }
}
