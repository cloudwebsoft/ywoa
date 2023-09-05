package cn.js.fan.db;

import java.util.Vector;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.sql.PreparedStatement;

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

/**
 *  本类的目的是替换PageConn
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class RMConn {
    //Vector result = null; //可能会导致线程不安全
    int rowCount = 0; //实际取得的记录行数
    int colCount = 0;
    int pageSize = 10;
    public int curPage = 1;
    public long total = 0; //由sql语句得到的总记录条数

    HashMap mapIndex;
    String connname;

    //用于预编译操作
    public Conn pconn = null;

    public RMConn(String connname) {
        mapIndex = new HashMap();
        this.connname = connname;
    }

    public RMConn(String connname, int curPage, int pageSize) {
        mapIndex = new HashMap();
        this.curPage = curPage;
        this.pageSize = pageSize;
        this.connname = connname;
    }
/*
    public void ClosePre() {
        if (prestmt!=null) {
            try { prestmt.close();} catch (Exception e) {} prestmt = null;
        }
        if (con!=null) {
            try { con.close(); } catch (Exception e) {} con = null;
        }
    }
*/
    public long getTotal() {
        return total;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public int getColumnCount() {
        return colCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    /**
     * 取出全部信息置于result中
     * @param sql String
     * @return Vector
     */
    public ResultIterator executeQuery(String sql) throws SQLException {
        rowCount = 0;
        colCount = 0;

        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = null;
        try {
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                //取得列名信息
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                }

                result = new Vector();

                ResultWrapper rsw = new ResultWrapper(rs);
                while (rsw.next()) {
                    Vector row = new Vector();
                    for (int i = 0; i < colCount; i++)
                        row.addElement(rsw.getObject(i + 1));
                    result.addElement(row);
                    rowCount++;
                }
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return new ResultIterator(result, mapIndex);
    }

    /**
     * 取出全部信息置于result中
     * @return Vector
     */
    public ResultIterator executePreQuery() throws SQLException {
        rowCount = 0;
        colCount = 0;

        ResultSet rs = null;
        Vector result = null;
        try {
            rs = pconn.executePreQuery();
            if (rs == null) {
                return null;
            } else {
                //取得列名信息
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                }

                result = new Vector();

                ResultWrapper rsw = new ResultWrapper(rs);
                while (rsw.next()) {
                    Vector row = new Vector();
                    for (int i = 0; i < colCount; i++)
                        row.addElement(rsw.getObject(i + 1));
                    result.addElement(row);
                    rowCount++;
                }
            }
        } catch (SQLException e) {
            throw e;
        } finally {
                if (pconn!=null) {
                    pconn.close();
                    pconn = null;
                }
        }
        return new ResultIterator(result, mapIndex);
    }

    /**
     * 分页操作，将ResultSet的信息保存在Vector中，以利用Iterator模式
     * @param sql String　sql查询语句
     * @param curPage int　当前页
     * @param pageSize int　页的记录条数
     * @return ResultIterator
     */
    public ResultIterator executeQuery(String sql, int curPage, int pageSize) throws
            SQLException {
        this.curPage = curPage;
        this.pageSize = pageSize;

        rowCount = 0;
        colCount = 0;

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
            } else {
                //取得列名信息
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
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
                    for (int i = 0; i < colCount; i++)
                        row.addElement(rsw.getObject(i + 1));
                    result.addElement(row);
                    rowCount++;
                } while (rsw.next());
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return new ResultIterator(result, mapIndex, total);
    }

    public int executeUpdate(String sql) throws SQLException {
        Conn conn = new Conn(connname);
        int r = 0;
        try {
            r = conn.executeUpdate(sql);
        } catch (SQLException e) {
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return r;
    }

    public int executePreUpdate() throws SQLException {
        int r = 0;
        try {
            r = pconn.executePreUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            if (pconn != null) {
                pconn.close();
                pconn = null;
            }
        }
        return r;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (pconn!=null) {
            pconn.close();
            pconn = null;
        }
        try {
            pconn = new Conn(connname);
            pconn.pstmt = pconn.prepareStatement(sql);
        } catch (SQLException e) {
            if (pconn!=null) {
                pconn.close();
                pconn = null;
            }
            throw e;
        }
        return pconn.pstmt;
    }
}
