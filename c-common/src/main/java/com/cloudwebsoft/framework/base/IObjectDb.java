package com.cloudwebsoft.framework.base;

/**
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

// Imports
import java.io.Serializable;
import java.util.Vector;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;

public interface IObjectDb extends Serializable {

  // Methods
  public void init();
  public void renew();
  public void initDB();
  public Object[] getObjectBlock(String query, String groupKey, int startIndex);
  public IObjectDb getObjectDb(Object primaryKeyValue);
  public ObjectBlockIterator getObjects(String query, int startIndex, int endIndex);
  public ObjectBlockIterator getObjects(String query, String groupKey, int startIndex, int endIndex);
  public boolean isLoaded();
  public void setLoaded(boolean loaded);
  public int getBlockSize();
  public PrimaryKey getPrimaryKey();
  public boolean create(JdbcTemplate jt) throws ErrMsgException, ResKeyException;
  public void load(JdbcTemplate jt) throws ErrMsgException, ResKeyException;
  public boolean save(JdbcTemplate jt) throws ErrMsgException, ResKeyException;
  public boolean del(JdbcTemplate jt) throws ErrMsgException, ResKeyException;
  public IObjectDb getObjectRaw(PrimaryKey primaryKey);
  public int getObjectCount(String sql);
  public Vector list();
  public Vector list(String QUERY_LIST);
  public Vector list(int start, int end);
  public Vector list(String sql, int start, int end);
  public void setBlockSize(int blockSize);
  public String getTableName();
}
