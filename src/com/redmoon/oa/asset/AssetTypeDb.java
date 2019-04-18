package com.redmoon.oa.asset;
import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

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
public class AssetTypeDb extends ObjectDb {
    private int id;

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;

    public AssetTypeDb() {
        init();
    }

    public AssetTypeDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "asset_type_info";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new AssetTypeCache(this);
        isInitFromConfigDB = false;
        //name depreciationRate  depreciationYears  abstracts
        QUERY_CREATE =
                "insert into " + tableName + " (name,depreciationRate,depreciationYears,abstracts) values (?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set name=? ,depreciationRate=? , depreciationYears=? , abstracts=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select name, depreciationRate, depreciationYears, abstracts from " + tableName + " where id=?";
    }

    public AssetTypeDb getAssetTypeDb(int id) {
        return (AssetTypeDb)getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            //name depreciationRate  depreciationYears  abstracts
            ps.setString(1, name);
            ps.setString(2, depreciationRate);
            ps.setInt(3, depreciationYears);
            ps.setString(4, abstracts);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                AssetTypeCache rc = new AssetTypeCache(this);
                rc.refreshCreate();
            }
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作失败！");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                AssetTypeCache rc = new AssetTypeCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("del: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new AssetTypeDb(pk.getIntValue());
    }

    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            //name depreciationRate  depreciationYears  abstracts
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                name = rs.getString(1);
                depreciationRate = rs.getString(2);
                depreciationYears = rs.getInt(3);
                abstracts = rs.getString(4);
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {}
                rs = null;
            }
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
    }

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
         boolean re = false;
         try {
        //name depreciationRate  depreciationYears  abstracts
             PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
             ps.setString(1, name);
             ps.setString(2, depreciationRate);
             ps.setInt(3, depreciationYears);
             ps.setString(4, abstracts);
             ps.setInt(5, id);
             re = conn.executePreUpdate()==1?true:false;
             if (re) {
                 AssetTypeCache rc = new AssetTypeCache(this);
                 primaryKey.setValue(new Integer(id));
                 rc.refreshSave(primaryKey);
             }
         } catch (SQLException e) {
             logger.error("save: " + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
        return re;
    }
    public boolean isExist(String tableName){
         ResultSet rs = null;
         Conn conn = new Conn(connname);
         try {
             rs = conn.executeQuery("select id from asset_type_info where name='"+tableName+"'" );
             if(rs.next()) return true;
         } catch (SQLException e) {
             logger.error("list:" + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
         return false ;
     }



    /**
     * 取出全部信息置于result中
     */
    public Vector list(String sql) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        try {
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    result.addElement(getAssetTypeDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            logger.error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }
    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();

        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    AssetTypeDb ug = getAssetTypeDb(rs.getInt(1));
                    result.addElement(ug);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {e.printStackTrace();}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    public String getName() {
        return name;
    }

    public String getDepreciationRate() {
        return depreciationRate;
    }

    public int getDepreciationYears() {
        return depreciationYears;
    }

    public String getAbstracts() {
        return abstracts;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepreciationRate(String depreciationRate) {
        this.depreciationRate = depreciationRate;
    }

    public void setDepreciationYears(int depreciationYears) {
        this.depreciationYears = depreciationYears;
    }

    public void setAbstracts(String abstracts) {
        this.abstracts = abstracts;
    }

//name depreciationRate  depreciationYears  abstracts
    private String name;
    private String depreciationRate;
    private int depreciationYears;
    private String abstracts;
}
