package com.redmoon.forum.plugin.witkey;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description:存放贴子的属性，msgRootId,state等 </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WitkeyUserDb extends ObjectDb {
    public WitkeyUserDb() {
        super();
    }

    public WitkeyUserDb(long msgRootId, String userName) {
        this.msgRootId = msgRootId;
        this.userName = userName;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "plugin_witkey_user";
        HashMap key = new HashMap();
        key.put("msgRootId", new KeyUnit(primaryKey.TYPE_LONG, 0));
        key.put("userName", new KeyUnit(primaryKey.TYPE_STRING, 1));
        primaryKey = new PrimaryKey(key);
        objectCache = new WitkeyUserCache(this);

        this.QUERY_CREATE = "insert into plugin_witkey_user(msg_root_id, user_name, real_name, city, tel, oicq, other_contact, add_date) values (?,?,?,?,?,?,?,?)";
        this.QUERY_SAVE = "update plugin_witkey_user set real_name=?, city=?, tel=?, oicq=?, other_contact=?, contribution_count=?, communication_count=? where msg_root_id=? and user_name=?";
        this.QUERY_DEL = "delete from plugin_witkey_user where msg_root_id=? and user_name=?";
        this.QUERY_LOAD = "select real_name, city, tel, oicq, other_contact, add_date, contribution_count, communication_count from plugin_witkey_user where msg_root_id=? and user_name=?";
        isInitFromConfigDB = false;
    }

    public boolean del() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setLong(1, msgRootId);
            ps.setString(2, userName);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (rowcount > 0) {
            WitkeyUserCache wuc = new WitkeyUserCache(this);
            primaryKey.setValue(new Long(msgRootId));
            primaryKey.setValue(userName);
            wuc.refreshDel(primaryKey);
        }
        return rowcount>0? true:false;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new WitkeyUserDb(pk.getKeyLongValue("msgRootId"), pk.getKeyStrValue("userName"));
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setLong(1, msgRootId);
            ps.setString(2, userName);
            ps.setString(3, realName);
            ps.setString(4, city);
            ps.setString(5, tel);
            ps.setString(6, oicq);
            ps.setString(7, otherContact);
            ps.setString(8, Long.toString(System.currentTimeMillis()));
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            WitkeyUserCache wuc = new WitkeyUserCache(this);
            wuc.refreshCreate();
        }
        return rowcount>0? true:false;
    }

    public boolean save() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setString(1, realName);
            ps.setString(2, city);
            ps.setString(3, tel);
            ps.setString(4, oicq);
            ps.setString(5, otherContact);
            ps.setInt(6, contributionCount);
            ps.setInt(7, communicationCount);
            ps.setLong(8, msgRootId);
            ps.setString(9, userName);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            WitkeyUserCache wuc = new WitkeyUserCache(this);
            primaryKey.setValue(new Long(msgRootId));
            primaryKey.setValue(userName);
            wuc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public WitkeyUserDb getWitkeyUserDb(long msgRootId, String userName) {
        primaryKey.setKeyValue("msgRootId", new Long(msgRootId));
        primaryKey.setKeyValue("userName", userName);
        return (WitkeyUserDb) getObjectDb(primaryKey.getKeys());
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setLong(1, msgRootId);
            ps.setString(2, userName);
            primaryKey.setValue(new Long(msgRootId));
            primaryKey.setValue(userName);
            rs = conn.executePreQuery();
            if (rs.next()) {
                realName = rs.getString(1);
                city = rs.getString(2);
                tel = StrUtil.getNullStr(rs.getString(3));
                oicq = StrUtil.getNullStr(rs.getString(4));
                otherContact = StrUtil.getNullStr(rs.getString(5));
                addDate = rs.getString(6);
                contributionCount = rs.getInt(7);
                communicationCount = rs.getInt(8);
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
    }

    public Vector list() {
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(this.QUERY_LIST);
            if (rs != null) {
                while (rs.next()) {
                    msgRootId = rs.getLong(1);
                    userName = rs.getString(2);
                    v.addElement(getWitkeyUserDb(msgRootId, userName));
                }
            }
        } catch (SQLException e) {
            logger.error("list: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    public void setMsgRootId(long msgRootId) {
        this.msgRootId = msgRootId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setOicq(String oicq) {
        this.oicq = oicq;
    }

    public void setOtherContact(String otherContact) {
        this.otherContact = otherContact;
    }

    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }

    public void setContributionCount(int contributionCount) {
        this.contributionCount = contributionCount;
    }

    public void setCommunicationCount(int communicationCount) {
        this.communicationCount = communicationCount;
    }

    public long getMsgRootId() {
        return msgRootId;
    }

    public String getUserName() {
        return userName;
    }

    public String getRealName() {
        return realName;
    }

    public String getCity() {
        return city;
    }

    public String getContact() {
        return contact;
    }

    public String getTel() {
        return tel;
    }

    public String getOicq() {
        return oicq;
    }

    public String getOtherContact() {
        return otherContact;
    }

    public String getAddDate() {
        return addDate;
    }

    public int getContributionCount() {
        return contributionCount;
    }

    public int getCommunicationCount() {
        return communicationCount;
    }

    private long msgRootId;
    private String userName;
    private String realName;
    private String city;
    private String contact;
    private String tel;
    private String oicq;
    private String otherContact;
    private String addDate;
    private int contributionCount;
    private int communicationCount;
}
