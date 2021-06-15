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
public class LinkmanDb extends ObjectDb {
//customerNameId,postPriv,linkmanName,linkmanSex,birthday,hobby
//postcodeHome,addHome,telNoWork,telNoHome,mobile,email,oicq,msn,demo

    private int id;
    private int customerNameId;
    private String postPriv;
    private String linkmanName;
    private String linkmanSex;
    private java.util.Date birthday;
    private String hobby;
    private String postcodeHome;
    private String addHome;
    private String telNoWork;
    private String telNoHome;
    private String mobile;
    private String email;
    private String oicq;
    private String msn;
    private String demo;

    public void initDB() {
        tableName = "linkman";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new LinkmanCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE =
                "insert into " + tableName + " (customerNameId,postPriv,linkmanName,linkmanSex,birthday,hobby,postcodeHome,addHome,telNoWork,telNoHome,mobile,email,oicq,msn,demo) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set customerNameId=?, postPriv=?,linkmanName=?, linkmanSex=? ,birthday=?, hobby=? ,postcodeHome=? ,addHome=? ,telNoWork=?, telNoHome=?, mobile=? ,email=?,oicq=?, msn=?, demo=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select customerNameId,postPriv,linkmanName,linkmanSex,birthday,hobby,postcodeHome,addHome,telNoWork,telNoHome,mobile,email,oicq,msn,demo from " +
                     tableName + " where id=?";
    }

    public LinkmanDb getLinkmanDb(int id) {
        return (LinkmanDb) getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, customerNameId);
            ps.setString(2, postPriv);
            ps.setString(3, linkmanName);
            ps.setString(4, linkmanSex);
            if (birthday != null)
              ps.setDate(5, new java.sql.Date(birthday.getTime()));
            else
              ps.setDate(5, null);
            ps.setString(6, hobby);
            ps.setString(7, postcodeHome);
            ps.setString(8, addHome);
            ps.setString(9, telNoWork);
            ps.setString(10, telNoHome);
            ps.setString(11, mobile);
            ps.setString(12, email);
            ps.setString(13, oicq);
            ps.setString(14, msn);
            ps.setString(15, demo);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                LinkmanCache rc = new LinkmanCache(this);
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
                LinkmanCache rc = new LinkmanCache(this);
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
    public LinkmanDb() {
      init();
  }
    public LinkmanDb(int id) {
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
        return new LinkmanDb(pk.getIntValue());
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
        //customerNameId,postPriv,linkmanName,linkmanSex,birthday,hobby
        //postcodeHome,addHome,telNoWork,telNoHome,mobile,email,oicq,msn,demo
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                customerNameId = rs.getInt(1);
                postPriv = rs.getString(2);
                linkmanName = rs.getString(3);
                linkmanSex = rs.getString(4);
                try{
                    birthday = rs.getDate(5);
                } catch (Exception e) {
                    logger.error("load1:" + e.getMessage());
                }
                hobby = rs.getString(6);
                postcodeHome = rs.getString(7);
                addHome = rs.getString(8);
                telNoWork = rs.getString(9);
                telNoHome = rs.getString(10);
                mobile = rs.getString(11);
                email = rs.getString(12);
                oicq = rs.getString(13);
                msn = rs.getString(14) ;
                demo = rs.getString(15);
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
            ps.setInt(1, customerNameId);
            ps.setString(2, postPriv);
            ps.setString(3, linkmanName);
            ps.setString(4, linkmanSex);
            if (birthday != null)
              ps.setDate(5, new java.sql.Date(birthday.getTime()));
            else
              ps.setDate(5, null);
            if (birthday != null)
             ps.setDate(5, new java.sql.Date(birthday.getTime()));
            ps.setString(6, hobby);
            ps.setString(7, postcodeHome);
            ps.setString(8, addHome);
            ps.setString(9, telNoWork);
            ps.setString(10, telNoHome);
            ps.setString(11, mobile);
            ps.setString(12, email);
            ps.setString(13, oicq);
            ps.setString(14, msn);
            ps.setString(15, demo);
            ps.setInt(16, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                LinkmanCache rc = new LinkmanCache(this);
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
       public boolean hasLinkmanOfType(int typeId) {
           ResultSet rs = null;
           Conn conn = new Conn(connname);
           try {
               String sql = "select id from linkman where typeId=?";
               PreparedStatement ps = conn.prepareStatement(sql);
               ps.setInt(1, typeId);
               rs = conn.executePreQuery();
               if (rs != null && rs.next()) {
                   return true;
               }
           } catch (SQLException e) {
               logger.error("hasSaleOfType: " + e.getMessage());
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
                    "select id from linkman where number='" + tableName +
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
                    result.addElement(getLinkmanDb(rs.getInt(1)));
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
                    LinkmanDb ug = getLinkmanDb(rs.getInt(1));
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
   public void setDemo(String demo) {
        this.demo = demo;
    }

    private void jbInit() throws Exception {
    }


    public void setPostPriv(String postPriv) {
        this.postPriv = postPriv;
    }

    public void setLinkmanName(String linkmanName) {
        this.linkmanName = linkmanName;
    }

    public void setLinkmanSex(String linkmanSex) {
        this.linkmanSex = linkmanSex;
    }

    public void setBirthday(java.util.Date birthday) {
        this.birthday = birthday;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public void setPostcodeHome(String postcodeHome) {
        this.postcodeHome = postcodeHome;
    }

    public void setAddHome(String addHome) {
        this.addHome = addHome;
    }

    public void setTelNoWork(String telNoWork) {
        this.telNoWork = telNoWork;
    }

    public void setTelNoHome(String telNoHome) {
        this.telNoHome = telNoHome;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setOicq(String oicq) {
        this.oicq = oicq;
    }

    public void setMsn(String msn) {
        this.msn = msn;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCustomerNameId(int customerNameId) {
        this.customerNameId = customerNameId;
    }

    public String getPostPriv() {
        return postPriv;
    }

    public String getLinkmanName() {
        return linkmanName;
    }

    public String getLinkmanSex() {
        return linkmanSex;
    }
    public java.util.Date getBirthday() {
        return birthday;
    }

    public String getHobby() {
        return hobby;
    }

    public String getPostcodeHome() {
        return postcodeHome;
    }

    public String getAddHome() {
        return addHome;
    }

    public String getTelNoWork() {
        return telNoWork;
    }

    public String getTelNoHome() {
        return telNoHome;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public String getOicq() {
        return oicq;
    }

    public String getMsn() {
        return msn;
    }

    public String getDemo() {
        return demo;
    }

    public int getId() {
        return id;
    }

    public int getCustomerNameId() {
        return customerNameId;
    }
}
