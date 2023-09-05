package com.cloudwebsoft.framework.db;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>Title: 参考Spring的类名称及其含义，制作本类，用以实现无须关闭数据库连接的数据库操作</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: Cloud Web Soft</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class JdbcTemplate {
    int rowCount = 0; // 实际取得的记录行数
    int colCount = 0;
    int pageSize = 10;
    public int curPage = 1;
    public long total = 0; // 由sql语句得到的总记录条数

    // HashMap mapIndex; // 因为mapIndex为对象，所以不能作为参数传递，否则当重复调用JdbcTemplate时，返回ResultIterator时，就会传递mapIndex，在循环嵌套遍历表时，不同的表结构就会导致出现问题
    Connection connection = null;

    String connName;

    public JdbcTemplate() {
        this.connection = new Connection(Global.getDefaultDB());
        connName = connection.connName;
    }

    public JdbcTemplate(Connection conn) {
        this.connection = conn;
        connName = connection.connName;
    }

    public JdbcTemplate(DataSource ds) {
        this.connection = ds.getConnection();
        connName = connection.connName;
    }

    /**
     *
     * @param connName String 连接名称
     */
    public JdbcTemplate(String connName) {
        this.connName = connName;
        this.connection = new Connection(connName);
    }

    public JdbcTemplate(DataSource ds, int curPage, int pageSize) {
        this.connection = ds.getConnection();
        connName = connection.connName;
        this.curPage = curPage;
        this.pageSize = pageSize;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public long getTotal() {
        return total;
    }

    public int getColumnCount() {
        return colCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    /**
     * 执行查询，结果集置于result中
     * @param sql String
     * @return Vector
     */
    public ResultIterator executeQuery(String sql) throws SQLException {
        rowCount = 0;
        colCount = 0;
        ResultSet rs = null;
        Vector result = null;
        HashMap mapIndex = new HashMap();
        HashMap mapLabel = new HashMap();
        HashMap mapType = new HashMap();
        try {
            // 检查链接是否存在
            checkConnection();

            connection.prepareStatement(sql);
            rs = connection.executePreQuery();
            // rs = connection.executeQuery(sql); // 可能会导致速度慢，故用executePreQuery
            if (rs == null) {
                return new ResultIterator();
            } else {
                // 取得列名信息
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    // getColumnName返回的是sql语句中field的原始名字。getColumnLabel是field的SQL AS的值。
                    // mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), i);
                    mapLabel.put(rm.getColumnName(i).toUpperCase(), rm.getColumnLabel(i).toUpperCase());
                    mapType.put(rm.getColumnName(i).toUpperCase(), rm.getColumnType(i));
                }
                
                result = new Vector();
                ResultWrapper rsw = new ResultWrapper(rs);
                while (rsw.next()) {
                    Vector row = new Vector();
                    for (int i = 0; i < colCount; i++) {
                        row.addElement(rsw.getObject(i + 1));
                    }
                    result.addElement(row);
                    rowCount++;
                }
            }
        } finally {
            // @task:这儿在connection中又关闭了一次，两次关闭
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }

            if (autoClose && connection.getAutoCommit()) {
                connection.close();
                connection = null;
            }
        }

       ResultIterator ri = new ResultIterator(result, mapIndex);
       ri.setMapLabel(mapLabel);
       ri.setMapType(mapType);
       return ri;
    }

    /**
     * 执行查询，结果集置于result中
     * @param sql String
     * @return Vector
     */
    public ResultIterator executeQueryTFO(String sql) throws SQLException {
        rowCount = 0;
        colCount = 0;
        ResultSet rs = null;
        Vector result = null;
        HashMap mapIndex = new HashMap();
        HashMap mapLabel = new HashMap();
        HashMap mapType = new HashMap();
        try {
            // 检查链接是否存在
            checkConnection();

            rs = connection.executeQueryTFO(sql);
            if (rs == null) {
                return new ResultIterator();
            } else {
                // 取得列名信息
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    // getColumnName返回的是sql语句中field的原始名字。getColumnLabel是field的SQL AS的值。
                    // mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapLabel.put(rm.getColumnName(i).toUpperCase(), rm.getColumnLabel(i).toUpperCase());
                    
                    mapType.put(rm.getColumnName(i).toUpperCase(),  new Integer(rm.getColumnType(i)));                    
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
        } finally {
            // @task:这儿在connection中又关闭了一次，两次关闭
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }

            if (autoClose && connection.getAutoCommit()) {
                connection.close();
                connection = null;
            }
        }
        ResultIterator ri = new ResultIterator(result, mapIndex);
        ri.setMapLabel(mapLabel);
        ri.setMapType(mapType);
        return ri;
    }

    public void addBatch(String sql) throws SQLException {
    	checkConnection();
    	
        connection.addBatch(sql);
    }

    public int[] executeBatch() throws SQLException {
        int[] r = null;
        try {
            // 检查链接是否存在
            checkConnection();
            r = connection.executeBatch();
        }
        finally {
            if (autoClose && connection.getAutoCommit()) {
                connection.close();
                connection = null;
            }
        }
        return r;
    }

    /**
     * 填充PreparedStatement
     * @param ps PreparedStatement
     * @param objectParams Object[]
     * @throws SQLException
     */
    public static void fillPreparedStatement(PreparedStatement ps,
                                      Object[] objectParams) throws
            SQLException {
        if (objectParams == null) {
            return;
        }

        int len = objectParams.length;
        for (int i = 1; i <= len; i++) {
            Object obj = objectParams[i - 1];

            // ConnMonitor.onFillPreparedStatement(ps.getConnection(), i, obj);

            // logger.info("fillPreparedStatement: obj=" + obj);
            if (obj == null) {
                // mysql orcale下支持但SQLSERVER下不支持Types.NULL，需得变为Types.VARCHAR或其它才能通过，但是能不能彻底解决问题无相关资料
                // Date字段在三种数据库下都测试通过
                ps.setNull(i, Types.VARCHAR);
            }
            else if (obj instanceof String) {
                ps.setString(i, (String) obj);
            } else if (obj instanceof Integer) {
                ps.setInt(i, (Integer) obj);
            } else if (obj instanceof java.util.Date) {
                ps.setTimestamp(i, new Timestamp(((java.util.Date)obj).getTime()));
            } else if (obj instanceof Long) {
                ps.setLong(i, (Long) obj);
            } else if (obj instanceof Short) {
                ps.setShort(i, (Short) obj);
            } else if (obj instanceof Double) {
                ps.setDouble(i, (Double) obj);
            } else if (obj instanceof Float) {
                ps.setFloat(i, (Float) obj);
            } else if (obj instanceof Clob) {
                ps.setClob(i, (Clob) obj);
            } else if (obj instanceof Blob) {
                ps.setBlob(i, (Blob) obj);
            } else if (obj instanceof Boolean) {
                ps.setBoolean(i, (Boolean) obj);
            } else if (obj instanceof Byte) {
                ps.setByte(i, (Byte) obj);
            }
            else if (obj instanceof BigDecimal) {
                ps.setBigDecimal(i, (BigDecimal)obj);
            }
            else if (obj instanceof LocalDate) {
                Date d = DateUtil.asDate((LocalDate)obj);
                ps.setTimestamp(i, new Timestamp(d.getTime()));
            }
            else if (obj instanceof LocalDateTime) {
                Date d= DateUtil.asDate((LocalDateTime)obj);
                ps.setTimestamp(i, new Timestamp(d.getTime()));
            }
            else {
                throw new SQLException("fillPreparedStatement: Object " + obj + " type is not supported. It's sequence number is " + i + " in parameters");
            }
        }
    }

    /**
     * 分页操作，将ResultSet的信息保存在Vector中，以利用Iterator模式
     * @param sql String　sql查询语句
     * @param curPage int　当前页
     * @param pageSize int　页的记录条数
     * @return ResultIterator
     */
    public ResultIterator executeQuery(String sql, Object[] objectParams, int curPage, int pageSize) throws
            SQLException {
        this.curPage = curPage;
        this.pageSize = pageSize;

        rowCount = 0;
        colCount = 0;

        ResultSet rs = null;
        Vector result = null;
        PreparedStatement ps = null;
        HashMap mapIndex = new HashMap();
        HashMap mapLabel = new HashMap();
        HashMap mapType = new HashMap();
        try {
            // 检查链接是否存在
            checkConnection();

            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(sql);
            ps = connection.prepareStatement(countsql);
            fillPreparedStatement(ps, objectParams);

            rs = connection.executePreQuery();
            if (rs != null && rs.next()) {
                total = rs.getLong(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (ps!=null) {
                ps.close();
                ps = null;
            }

            // 防止受到攻击时，curPage被置为很大，或者很小
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages)
                curPage = totalpages;
            if (curPage <= 0)
                curPage = 1;

            ps = connection.prepareStatement(sql);

            if (total != 0)
                connection.setMaxRows(curPage * pageSize); //尽量减少内存的使用

            fillPreparedStatement(ps, objectParams);
            rs = connection.executePreQuery();
            if (rs == null) {
                return new ResultIterator();
            } else {
                // 取得列名信息
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    // getColumnName返回的是sql语句中field的原始名字。getColumnLabel是field的SQL AS的值。
                    // mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapLabel.put(rm.getColumnName(i).toUpperCase(), rm.getColumnLabel(i).toUpperCase());
                    mapType.put(rm.getColumnName(i).toUpperCase(),  new Integer(rm.getColumnType(i)));                    
                    
                }

                rs.setFetchSize(pageSize);

                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return new ResultIterator();
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
            if (ps!=null) {
                try {
                    ps.close();
                }
                catch (Exception e) {}
                ps = null;
            }
            if (autoClose && connection.getAutoCommit()) {
                connection.close();
                connection = null;
            }
        }
        ResultIterator ri = new ResultIterator(result, mapIndex, total);
        ri.setMapLabel(mapLabel);
        ri.setMapType(mapType);
        return ri;
    }

    /**
     * 取得数据库中的表名
     * @return Vector
     */
    public Vector getTableNames() {
        Vector v = new Vector();
        DatabaseMetaData dbmd = null;
        ResultSet rs = null;
        try {
            dbmd = connection.getCon().getMetaData();
            rs = dbmd.getTables(null, null, "%", new String[] {"TABLE"});
            //ResultSet rs=dbmd.getTables(null,null,"news",null);
            while (rs.next()) {
                v.addElement(rs.getString(3));
            }
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
        finally {
        	if (rs != null){
        		try {
					rs.close();
					rs = null;
				} catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
				}
        	}
            if (autoClose) {
                connection.close();
                connection = null;
            }
        }
        return v;
    }

    /**
     * 用于特殊的分页操作，当sql语句中有group by时，不能再用SQLFilter.getCountSql()方法来得到计算总数的SQL语句
     * @param sql String
     * @param objectParams Object[]
     * @param total long 总记录数
     * @param curPage int
     * @param pageSize int
     * @return ResultIterator
     * @throws SQLException
     */
    public ResultIterator executeQuery(String sql, Object[] objectParams, long total, int curPage, int pageSize) throws
            SQLException {
        this.curPage = curPage;
        this.pageSize = pageSize;

        rowCount = 0;
        colCount = 0;

        ResultSet rs = null;
        Vector result = null;
        PreparedStatement ps = null;
        HashMap mapIndex = new HashMap();
        HashMap mapLabel = new HashMap();
        HashMap mapType = new HashMap();
        try {
            // 检查链接是否存在
            checkConnection();

            // 防止受到攻击时，curPage被置为很大，或者很小
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages) {
                curPage = totalpages;
            }
            if (curPage <= 0) {
                curPage = 1;
            }

            ps = connection.prepareStatement(sql);

            if (total != 0) {
                connection.setMaxRows(curPage * pageSize); //尽量减少内存的使用
            }

            fillPreparedStatement(ps, objectParams);
            rs = connection.executePreQuery();
            if (rs == null) {
                return new ResultIterator();
            } else {
                // 取得列名信息
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    // getColumnName返回的是sql语句中field的原始名字。getColumnLabel是field的SQL AS的值。
                    // mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapLabel.put(rm.getColumnName(i).toUpperCase(), rm.getColumnLabel(i).toUpperCase());
                    mapType.put(rm.getColumnName(i).toUpperCase(),  new Integer(rm.getColumnType(i)));                    
                }

                rs.setFetchSize(pageSize);

                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                	ResultIterator ri = new ResultIterator();
                    ri.setMapType(mapType);
                    ri.setMapLabel(mapLabel);
                    
                    return ri;                	
                    // return new ResultIterator();
                }

                result = new Vector();

                ResultWrapper rsw = new ResultWrapper(rs);
                do {
                    Vector row = new Vector();
                    for (int i = 0; i < colCount; i++) {
                        row.addElement(rsw.getObject(i + 1));
                    }
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
            if (ps!=null) {
                try {
                    ps.close();
                }
                catch (Exception e) {}
                ps = null;
            }
            if (autoClose && connection.getAutoCommit()) {
                connection.close();
                connection = null;
            }
        }
        ResultIterator ri = new ResultIterator(result, mapIndex, total);
        ri.setMapLabel(mapLabel);
        ri.setMapType(mapType);
        return ri;
    }

    /**
     * 执行查询，取出全部信息置于ResultIterator中
     * @param sql String
     * @return Vector
     */
    public ResultIterator executeQuery(String sql, Object[] objectParams) throws
            SQLException {
        rowCount = 0;
        colCount = 0;

        ResultSet rs = null;
        Vector result = null;
        PreparedStatement ps = null;
        HashMap mapIndex = new HashMap();
        HashMap mapLabel = new HashMap();
        HashMap mapType = new HashMap();
        try {
            // 检查链接是否存在
            checkConnection();

            ps = connection.prepareStatement(sql);
            fillPreparedStatement(ps, objectParams);
            rs = connection.executePreQuery();
            if (rs == null) {
                return new ResultIterator();
            } else {
                // 取得列名信息
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    // getColumnName返回的是sql语句中field的原始名字。getColumnLabel是field的SQL AS的值。
                    // mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), i);
                    mapLabel.put(rm.getColumnName(i).toUpperCase(), rm.getColumnLabel(i).toUpperCase());
                    mapType.put(rm.getColumnName(i).toUpperCase(), rm.getColumnType(i));
                }

                result = new Vector();

                ResultWrapper rsw = new ResultWrapper(rs);
                while (rsw.next()) {
                    Vector row = new Vector();
                    for (int i = 0; i < colCount; i++) {
                        try {
                            row.addElement(rsw.getObject(i + 1));
                        }
                        catch (SQLException e) {
                            row.addElement(null);
                            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
                        }
                    }
                    result.addElement(row);
                    rowCount++;
                }
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps!=null) {
                ps.close();
            }
            if (autoClose && connection.getAutoCommit()) {
                connection.close();
                connection = null;
            }
        }
        ResultIterator ri = new ResultIterator(result, mapIndex);
        ri.setMapLabel(mapLabel);
        ri.setMapType(mapType);
        return ri;
    }

    /**
     * 执行查询，取出全部信息置于ResultIterator中
     * @param sql String
     * @return Vector
     */
    public ResultIterator executeQueryTFO(String sql, Object[] objectParams) throws
            SQLException {
        rowCount = 0;
        colCount = 0;

        ResultSet rs = null;
        Vector result = null;
        PreparedStatement ps = null;
        HashMap mapIndex = new HashMap();
        HashMap mapLabel = new HashMap();
        HashMap mapType = new HashMap();
        try {
            // 检查链接是否存在
            checkConnection();

            ps = connection.prepareStatementTFO(sql);
            fillPreparedStatement(ps, objectParams);
            rs = connection.executePreQuery();
            if (rs == null) {
                return new ResultIterator();
            } else {
                // 取得列名信息
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    // getColumnName返回的是sql语句中field的原始名字。getColumnLabel是field的SQL AS的值。
                    // mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapLabel.put(rm.getColumnName(i).toUpperCase(), rm.getColumnLabel(i).toUpperCase());
                    mapType.put(rm.getColumnName(i).toUpperCase(),  new Integer(rm.getColumnType(i)));                                        
                    
                }

                result = new Vector();

                ResultWrapper rsw = new ResultWrapper(rs);
                while (rsw.next()) {
                    Vector row = new Vector();
                    for (int i = 0; i < colCount; i++) {
                        try {
                            row.addElement(rsw.getObject(i + 1));
                        }
                        catch (SQLException e) {
                            row.addElement(null);
                            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
                        }
                    }
                    result.addElement(row);
                    rowCount++;
                }
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps!=null) {
                ps.close();
            }
            if (autoClose && connection.getAutoCommit()) {
                connection.close();
                connection = null;
            }
        }
        ResultIterator ri = new ResultIterator(result, mapIndex);
        ri.setMapLabel(mapLabel);
        ri.setMapType(mapType);
        return ri;
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
        HashMap mapIndex = new HashMap();
        HashMap mapLabel = new HashMap();
        HashMap mapType = new HashMap();
        try {
            // 检查链接是否存在
            checkConnection();
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(sql);
            // logger.debug(countsql);
            rs = connection.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getLong(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            // 防止受到攻击时，curPage被置为很大，或者很小
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages) {
                curPage = totalpages;
            }
            if (curPage <= 0) {
                curPage = 1;
            }

            if (total != 0) {
                connection.setMaxRows(curPage * pageSize); //尽量减少内存的使用
            }

            rs = connection.executeQuery(sql);
            // LogUtil.getLog(getClass()).info("executeQuery: rs=" + rs);
            if (rs == null) {
                return new ResultIterator();
            } else {
                // 取得列名信息
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                // LogUtil.getLog(getClass()).info("executeQuery: colCount=" + colCount);
                // LogUtil.getLog(getClass()).info("executeQuery: sql=" + sql);
                
                for (int i = 1; i <= colCount; i++) {
                    // getColumnName返回的是sql语句中field的原始名字。getColumnLabel是field的SQL AS的值。
                    // mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                    mapLabel.put(rm.getColumnName(i).toUpperCase(), rm.getColumnLabel(i).toUpperCase());
                    mapType.put(rm.getColumnName(i).toUpperCase(),  new Integer(rm.getColumnType(i)));   
                    // LogUtil.getLog(getClass()).info("executeQuery: rm.getColumnName(i).toUpperCase()=" + rm.getColumnName(i).toUpperCase() + " columnType=" + rm.getColumnType(i));
                    
                }

                rs.setFetchSize(pageSize);

                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (!rs.absolute(absoluteLocation)) {
                	ResultIterator ri = new ResultIterator();
                    ri.setMapType(mapType);
                    ri.setMapLabel(mapLabel);
                    
                    return ri;
                }

                result = new Vector();

                ResultWrapper rsw = new ResultWrapper(rs);
                do {
                    Vector row = new Vector();
                    for (int i = 1; i <= colCount; i++) {
                        row.addElement(rsw.getObject(i));
                    }
                    result.addElement(row);
                    rowCount++;
                } while (rsw.next());
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }
            if (autoClose && connection.getAutoCommit()) {
                connection.close();
                connection = null;
            }
        }
        ResultIterator ri = new ResultIterator(result, mapIndex, total);
        ri.setMapLabel(mapLabel);
        ri.setMapType(mapType);
        
        return ri;
    }

    /**
     * 执行更新
     * @param sql String
     * @return int
     * @throws SQLException
     */
    public int executeUpdate(String sql) throws SQLException {
        int r = 0;
        try {
            // 检查链接是否存在
            checkConnection();
            r = connection.executeUpdate(sql);
        } finally {
            if (autoClose && connection.getAutoCommit()) {
                connection.close();
                connection = null;
            }
        }
        return r;
    }

    public void addBatchByPreparedStatement(PreparedStatement ps, Object[] objectParams) throws SQLException {
        fillPreparedStatement(ps, objectParams);
        ps.addBatch();
    }

    public void executeBatchByPreparedStatement(PreparedStatement ps) throws SQLException {
        try {
            ps.executeBatch();
        } finally {
            if (autoClose && connection.getAutoCommit()) {
                connection.close();
                connection = null;
            }
        }
    }

    /**
     * 执行更新
     * @param sql String
     * @param objectParams Object[]
     * @return int
     * @throws SQLException
     */
    public int executeUpdate(String sql, Object[] objectParams) throws SQLException {
        int r = 0;
        PreparedStatement ps = null;
        try {
            // 检查链接是否存在
            checkConnection();
            ps = connection.prepareStatement(sql);
            fillPreparedStatement(ps, objectParams);
            r = connection.executePreUpdate();
        }
        finally {
        	/* 不能关闭，这样会导致excuteQuery(String sql, int curPage, int pageSize)调用出现连接已被关闭问题 
            if (ps!=null) {
                try {
                    ps.close();
                }
                catch (Exception e) {}
                ps = null;
            }
            */
            if (autoClose && connection.getAutoCommit()) {
                connection.close();
                connection = null;
            }
        }
        return r;
    }

    public void beginTrans() throws SQLException {
        connection.beginTrans();
    }

    public void commit() throws SQLException {
        // 如果config_cws.xml中强制不使用事务，则当executeUpdate等操作后，connection就会被close，并置为null
        if (connection!=null) {
            connection.commit();
        }
    }

    public void rollback() {
        connection.rollback();
    }

    /**
     * 当用于事务处理时，需要在finally块中关闭connection
     */
    public void close() {
        if (connection!=null) {
            connection.close();
        }
    }

    public boolean isClosed() {
        if (connection==null) {
            return true;
        }
        return connection.isClosed();
    }

    /**
     * 如果连接已关闭，则重新获取链接
     */
    public void checkConnection() throws SQLException {
        if (isClosed()) {
            connection = new Connection(connName);
        }
    }

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }

    private boolean autoClose = true;
}
