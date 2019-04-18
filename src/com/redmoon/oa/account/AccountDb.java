package com.redmoon.oa.account;

import java.sql.*;
import java.util.Vector;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

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
public class AccountDb extends ObjectDb {
    public AccountDb() {
        init();
    }

    public AccountDb(String name) {
        this.name = name;
        init();
        load();
    }

    // name,userName,pwd
    public void initDB() {
        tableName = "account";
        primaryKey = new PrimaryKey("name", PrimaryKey.TYPE_STRING);
        objectCache = new AccountCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (name, userName, unit_code) values (?,?,?)";
        QUERY_SAVE = "update " + tableName + " set userName=? where name=?";
        QUERY_LIST =
                "select name from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where name=?";
        QUERY_LOAD = "select userName,unit_code from " + tableName + " where name=?";
    }

    public AccountDb getAccountDb(String name) {
        return (AccountDb)getObjectDb(name);
    }

    public AccountDb getUserAccount(String userName) {
        ResultSet rs = null;
        PreparedStatement ps = null;
        String sql = "select name from " + tableName + " where userName=?";
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                String name = rs.getString(1);
                return getAccountDb(name);
            }
        } catch (SQLException e) {
            logger.error("getUserAccount: " + e.getMessage());
        } finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return null;
    }

    public boolean create() throws ErrMsgException {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, name);
            ps.setString(2, userName);
            ps.setString(3, unitCode);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                AccountCache rc = new AccountCache(this);
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
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setString(1, name);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                AccountCache rc = new AccountCache(this);
                primaryKey.setValue(name);
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
        return new AccountDb(pk.getStrValue());
    }

    /**
     * 清除帐号中的userName，用于create和save方法，以免用户名重复
     * @param userName String
     * @throws ErrMsgException
     */
    public void clearAccountUserName(String accountUserName) throws ErrMsgException {
        String sql = "select name from " + tableName + " where userName=?";
        ResultSet rs = null;
        PreparedStatement ps = null;
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, accountUserName);
            rs = conn.executePreQuery();
            if (rs != null) {
               while (rs.next()) {
                   AccountDb ad = getAccountDb(rs.getString(1));
                   // System.out.println("AccountDb clearAccountUserName ad=" + ad.getName());
                   ad.setUserName("");
                   ad.save();
               }
            }
        } catch (SQLException e) {
            logger.error("clearAccountUserName: " + e.getMessage());
        } finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
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
        PreparedStatement ps = null;
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(QUERY_LOAD);
            ps.setString(1, name);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                userName = StrUtil.getNullStr(rs.getString(1));
                unitCode = rs.getString(2);
                loaded = true;
                primaryKey.setValue(name);
            }
        } catch (SQLException e) {
            logger.error("load: " + e.getMessage());
        } finally {
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
        boolean re = false;
        Conn conn = new Conn(connname);
         try {
             PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
             ps.setString(1, userName);
             ps.setString(2, name);
             re = conn.executePreUpdate()==1?true:false;
             if (re) {
                 AccountCache rc = new AccountCache(this);
                 primaryKey.setValue(name);
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

    public boolean isExist(String acc){
        if (acc==null || acc.equals(""))
            return false;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            rs = conn.executeQuery("select name from account where name=" + StrUtil.sqlstr(acc));
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

                if (total != 0) {
                    conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
                }

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
                        AccountDb ug = getAccountDb(rs.getString(1));
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
                    } catch (Exception e) {}
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
                    result.addElement(getAccountDb(rs.getString(1)));
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


    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    /**
     * 工号
     */
    private String name;
    /**
     * 用户名
     */
    private String userName;
    private String unitCode;
}
