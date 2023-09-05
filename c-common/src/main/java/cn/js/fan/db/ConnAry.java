package cn.js.fan.db;

import com.cloudwebsoft.framework.util.LogUtil;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class ConnAry implements Serializable {
    Conn conn;
    String sql = null;
    int rowcount = 0;
    int colcount = 0;
// int limitcount = 0;
    Vector result = null;
    public String _WATCH = "";

    public ConnAry(String connname) {
        conn = new Conn(connname);
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void close() { // throws Throwable
        try {
            if (conn != null) {
                conn.close();
            }
            if (result != null) {
                result.removeAllElements();
            }
        } catch (java.lang.Throwable e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    public String get(int row, int col) {
        if (result == null || row >= result.size())return null;
        String r[] = (String[]) result.elementAt(row);
        if (col >= java.lang.reflect.Array.getLength(r))return null;
        return r[col];
    }

    public int getColumncount() {
        return colcount;
    }

    public Vector getResult() {
        return result;
    }

    public String[][] getResultAry() {
        if (rowcount == 0 || colcount == 0)
            return null;
        String[][] ary = new String[rowcount][colcount];
        for (int r = 0; r < rowcount; r++)
            for (int c = 0; c < colcount; c++) {
                ary[r][c] = get(r, c);
            }
        return ary;
    }

    public String[] getRow(int row) {
        if (result == null || row >= result.size())return null;
        return (String[]) result.elementAt(row);
        /*String ret[] = new String[colcount];
                 Vector r = (Vector)result.elementAt(row);
                 for (int i=0; i< colcount; i++)
                 ret[i] = (String)r.elementAt(i);
                 return ret;*/
    }

    public int getRowcount() {
        return rowcount;
    }

    public void handleException(Exception e) {
        _WATCH = e.getMessage();
        LogUtil.getLog(getClass()).error(e);
    }

    public void init() {
        rowcount = 0;
        colcount = 0;
        // limitcount = 0;
        result = null;
    }

    public void clear() throws Throwable {
        finalize();
    }

    public int query(String sql) {
        if (result != null) result.removeAllElements();
        rowcount = 0;
        colcount = 0;
        result = new Vector();
        int ret = 0;
        try {
            ResultSet rs = conn.executeQuery(sql);
            if (rs == null) {
                //ret = conn.getAffectedRows();
                ret = 0;
            } else {
                ResultSetMetaData rm = rs.getMetaData();
                colcount = rm.getColumnCount();
                while (rs.next()) {
                    String row[] = new String[colcount];
                    for (int i = 0; i < colcount; i++)
                        row[i] = rs.getString(i + 1);
                    result.addElement(row);
                    rowcount++;
                }
                rs.close(); // to release the resource.
                ret = result.size();
            }
        } catch (Exception e) {
            handleException(e);
            return -1;
        }
        return ret;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return conn.executeQuery(sql);
    }

    public int executeUpdate(String sql) throws SQLException {
        return conn.executeUpdate(sql);
    }

    public void beginTrans() throws SQLException {
        conn.beginTrans();
    }

    public void commit() throws SQLException {
        conn.commit();
    }

    public void rollback() {
        conn.rollback();
    }

    public boolean getAutoCommit() throws SQLException {
        return conn.getAutoCommit();
    }

    public int getTransactionIsolation() throws SQLException {
        return conn.getTransactionIsolation();
    }

}
