package cn.js.fan.db;

import java.sql.*;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.web.Global;
import cn.js.fan.util.NumberUtil;
import java.math.BigDecimal;

public class ResultWrapper {
    ResultSet rs = null;
    ResultSetMetaData rm = null;

    public ResultWrapper(ResultSet rs) throws SQLException {
        this.rs = rs;
        rm = rs.getMetaData();
    }

/*
    oracle的相关资料
     public class Oracle9Dialect extends Dialect {

         public Oracle9Dialect() {
             super();
             registerColumnType( Types.BIT, "NUMBER(1,0)" );
             registerColumnType( Types.BIGINT, "NUMBER(19,0)" );
             registerColumnType( Types.SMALLINT, "NUMBER(5,0)" );
             registerColumnType( Types.TINYINT, "NUMBER(3,0)" );
             registerColumnType( Types.INTEGER, "NUMBER(10,0)" );
             registerColumnType( Types.CHAR, "CHAR(1)" );
             registerColumnType( Types.VARCHAR, "VARCHAR2($l)" );
             registerColumnType( Types.FLOAT, "FLOAT" );
             registerColumnType( Types.DOUBLE, "DOUBLE PRECISION" );
             registerColumnType( Types.DATE, "DATE" );
             registerColumnType( Types.TIME, "DATE" );
             registerColumnType( Types.TIMESTAMP, "DATE" );
             //registerColumnType( Types.VARBINARY, "RAW" );
             registerColumnType( Types.VARBINARY, "LONG RAW" );
             registerColumnType( Types.VARBINARY, 255, "RAW($l)" );
             registerColumnType( Types.NUMERIC, "NUMBER(19, $l)" );
             registerColumnType( Types.BLOB, "BLOB" );
             registerColumnType( Types.CLOB, "CLOB" );
*/

    /**
     * 根据类型，自动获取相应类型的值
     * @param col int
     * @return Object
     * @throws SQLException
     */
    public Object getObject(int col) throws SQLException {
        int t = rm.getColumnType(col);

        // LogUtil.getLog(getClass()).info("col=" + col + " getColumnType=" + t);

        Object obj = null;
        switch (t) {
          case Types.ARRAY: obj = rs.getArray(col); break;
          case Types.BIGINT: obj = new Long(rs.getLong(col)); break;
          case Types.BINARY: obj = rs.getBinaryStream(col); break;
          case Types.BIT: obj = new Integer(rs.getInt(col)); break;
          case Types.BLOB: obj = rs.getBlob(col); break;
          case Types.BOOLEAN: obj = new Boolean(rs.getBoolean(col)); break;
          case Types.CHAR: obj = rs.getString(col); break;
          case Types.CLOB: obj = rs.getClob(col); break;
          case Types.DATALINK: obj = rs.getString(col); break;
          case Types.DATE: Timestamp ts = rs.getTimestamp(col); if (ts!=null) obj = new Date(ts.getTime()); else obj=null; break;
          case Types.DECIMAL: obj = rs.getBigDecimal(col); break;
          case Types.DISTINCT: obj = rs.getString(col); break;
          case Types.DOUBLE: obj = new Double(rs.getDouble(col)); break;
          case Types.FLOAT: obj = new Float(rs.getFloat(col)); break;
          case Types.INTEGER: obj = new Integer(rs.getInt(col)); break;
          case Types.JAVA_OBJECT: obj = rs.getString(col); break;
          case Types.LONGVARBINARY: obj = rs.getBigDecimal(col); break;
          case Types.LONGVARCHAR: obj = rs.getString(col); break;
          case Types.NULL: obj = null; break;
          case Types.NUMERIC:
              // int  getPrecision (int column) 指定列类型的精确度(类型的长度): 。
              // int  getScale (int column) 获取指定列的小数点右边的位数。
              // oracle.jdbc.OracleResultSetMetaData orm = (oracle.jdbc.OracleResultSetMetaData )rm;
              // 经测试，无论是使用oralce的OracleResultSetMetaData还是JDBC的ResultSetMetaData，两者的值都为0
              // This is a bug report on the Oracle thin jdbc driver 2001-4-30    http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4452330
              if (Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {
                  /*
                  2010.10.23
                  float和double的精度是由尾数的位数来决定的。浮点数在内存中是按科学计数法来存储的，其整数部分始终是一个隐含着的“1”，由于它是不变的，故不能对精度造成影响。
                  float：2^23 = 8388608，一共七位，这意味着最多能有7位有效数字，但绝对能保证的为6位，也即float的精度为6~7位有效数字；
                  double：2^52 = 4503599627370496，一共16位，同理，double的精度为15~16位
                  */
                  // double表现不了double d = 1000000000000000010D 这样的数字，输出会变为1000000000000000000
                  // 因此改用getBigDecimal
                  obj = rs.getBigDecimal(col);
              } else {
                  if (rm.getScale(col) > 0) {
                      obj = new Double(rs.getDouble(col));
                  } else
                      obj = new Long(rs.getLong(col));
              }
              break;
          case Types.OTHER: obj = rs.getString(col); break;
          case Types.REAL:
              // LogUtil.getLog(getClass()).info("Types.REAL col=" + col + " Global.dbVersion=" + Global.dbVersion + " " + Global.DB_MYSQL);
              if (Global.db.equals(Global.DB_MYSQL)) {
                  obj = new Float(rs.getFloat(col));
                  // LogUtil.getLog(getClass()).info("Types.REAL2 col=" + col);
              }
              else
                  obj = rs.getString(col);
              break; // MySQL的float对应于REAL，而SQLServer的REAL对应于REAL
          case Types.REF: obj = rs.getString(col); break;
          case Types.SMALLINT: obj = new Integer(rs.getInt(col)); break;
          case Types.STRUCT: obj = rs.getString(col); break;
          case Types.TIME: obj = rs.getTime(col); break;
          case Types.TIMESTAMP: obj = rs.getTimestamp(col); break;
          case Types.TINYINT: obj = new Integer(rs.getInt(col)); break;
          case Types.VARBINARY: obj = rs.getBinaryStream(rs.getInt(col)); break;
          case Types.VARCHAR: obj = rs.getString(col); break;
          default: obj = rs.getString(col);
        }
        return obj;
    }

    public boolean next() throws SQLException {
        return rs.next();
    }
}
