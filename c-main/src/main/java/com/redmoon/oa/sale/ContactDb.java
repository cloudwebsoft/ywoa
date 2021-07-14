package com.redmoon.oa.sale;

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
public class ContactDb extends ObjectDb {
    //contactType,contactDate,customerNameId,linkId,moneyCost,timeCost,contactContent,satisfy,feedback,memo
    private int id;
    private String contactType;
    private java.util.Date contactDate;
    private int customerNameId;
    private int linkId;
    private double moneyCost;
    private double timeCost;
    private String contactContent;
    private String satisfy;
    private String feedback;
    private String memo;

    public void initDB() {
        tableName = "contact";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new ContactCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE =
                "insert into " + tableName + " (contactType,contactDate,customerNameId,linkId,moneyCost,timeCost,contactContent,satisfy,feedback,memo) values (?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set  contactType=?,contactDate=?,customerNameId=?,linkId=?,moneyCost=?,timeCost=?,contactContent=?,satisfy=?,feedback=?,memo=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select contactType,contactDate,customerNameId,linkId,moneyCost,timeCost,contactContent,satisfy,feedback,memo from " +
                     tableName + " where id=?";
    }

    public ContactDb getContactDb(int id) {
        return (ContactDb) getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, contactType);
            if (contactDate != null)
              ps.setDate(2, new java.sql.Date(contactDate.getTime()));
            else
              ps.setDate(2, null);
            ps.setInt(3, customerNameId);
            ps.setInt(4, linkId);
            ps.setDouble(5, moneyCost);
            ps.setDouble(6, timeCost);
            ps.setString(7, contactContent);
            ps.setString(8, satisfy);
            ps.setString(9, feedback);
            ps.setString(10, memo);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                ContactCache rc = new ContactCache(this);
                rc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作失败！");
        } finally {
            if (conn != null) {
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
                ContactCache rc = new ContactCache(this);
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
    public ContactDb() {
      init();
  }
    public ContactDb(int id) {
        this.id = id;
        init();
        load();
    }
    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new ContactDb(pk.getIntValue());
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
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                contactType = rs.getString(1);
                try{
                 contactDate = rs.getDate(2);
                   } catch (Exception e) {
                   logger.error("load1:" + e.getMessage());
                }
                linkId = rs.getInt(3);
                customerNameId = rs.getInt(4);
                moneyCost = rs.getDouble(5);
                timeCost = rs.getDouble(6);
                contactContent = rs.getString(7);
                satisfy = rs.getString(8);
                feedback = rs.getString(9);
                memo = rs.getString(10);
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
            if (conn != null) {
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
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, contactType);
            if (contactDate != null)
              ps.setDate(2, new java.sql.Date(contactDate.getTime()));
            else
              ps.setDate(2, null);
            ps.setInt(2, customerNameId);
            ps.setInt(3, linkId);
            ps.setDouble(4, moneyCost);
            ps.setDouble(6, timeCost);
            ps.setString(7, contactContent);
            ps.setString(8, satisfy);
            ps.setString(9, feedback);
            ps.setString(10, memo);
            ps.setInt(11, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                ContactCache rc = new ContactCache(this);
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
    /**
        * @param typeId int
        * @return boolean
        */
       public boolean hasContactOfType(int typeId) {
           ResultSet rs = null;
           Conn conn = new Conn(connname);
           try {
               String sql = "select id from contact where typeId=?";
               PreparedStatement ps = conn.prepareStatement(sql);
               ps.setInt(1, typeId);
               rs = conn.executePreQuery();
               if (rs != null && rs.next()) {
                   return true;
               }
           } catch (SQLException e) {
               logger.error("hasContactOfType: " + e.getMessage());
           } finally {
               if (rs != null) {
                   try {
                       rs.close();
                   } catch (SQLException e) {}
                   rs = null;
               }
               if (conn != null) {
                   conn.close();
                   conn = null;
               }
           }
           return false;
       }

    public boolean isExist(String tableName) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            rs = conn.executeQuery(
                    "select id from contact where number='" + tableName +
                    "'");
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            logger.error("linkmanlist:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
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
                    result.addElement(getContactDb(rs.getInt(1)));
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
                    ContactDb ug = getContactDb(rs.getInt(1));
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
    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public void setContactDate(java.util.Date contactDate) {
        this.contactDate = contactDate;
    }

    public void setCustomerNameId(int customerNameId) {
        this.customerNameId = customerNameId;
    }

    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }

    public void setMoneyCost(double moneyCost) {
        this.moneyCost = moneyCost;
    }

    public void setTimeCost(double timeCost) {
        this.timeCost = timeCost;
    }

    public void setContactContent(String contactContent) {
        this.contactContent = contactContent;
    }

    public void setSatisfy(String satisfy) {
        this.satisfy = satisfy;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContactType() {
        return contactType;
    }

    public java.util.Date getContactDate() {
        return contactDate;
    }

    public int getCustomerNameId() {
        return customerNameId;
    }

    public int getLinkId() {
        return linkId;
    }

    public double getMoneyCost() {
        return moneyCost;
    }

    public double getTimeCost() {
        return timeCost;
    }

    public String getContactContent() {
        return contactContent;
    }

    public String getSatisfy() {
        return satisfy;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getMemo() {
        return memo;
    }

    public int getId() {
        return id;
    }
}
