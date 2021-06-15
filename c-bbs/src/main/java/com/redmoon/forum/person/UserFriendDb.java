package com.redmoon.forum.person;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.*;
import org.apache.log4j.*;

public class UserFriendDb extends ObjectDb {
    String connname;
    Logger logger = Logger.getLogger(UserFriendDb.class.getName());

    public UserFriendDb() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("UserFriendDb:connname is empty.");
        init();
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public UserFriendDb(int id){
        this.id = id;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("UserFriendDb:connname is empty.");
        load();
        init();
    }

    public void initDB() {
        objectCache = new UserFriendCache(this);
        tableName = "sq_friend";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);

        QUERY_LOAD =
            "SELECT name,friend,rq,state FROM " + tableName + " WHERE id=?";
        QUERY_SAVE =
            "update " + tableName + " set state=? WHERE id=?";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_CREATE = "insert into " + tableName + " (name,friend,rq,id,state) values (?,?,?,?,?)";
        QUERY_LIST = "select id from " + tableName + " order by rq asc";
        isInitFromConfigDB = false;
    }

    /**
     * 根据用户名和朋友名取得UserFriendDb
     * @param userName String
     * @param friendName String
     * @return UserFriendDb
     */
    public UserFriendDb getUserFriendDb(String userName, String friendName) {
        Conn conn = null;
        try {
            conn = new Conn(connname);
            // 检查用户是否已在自己的好友列表中
            String sql = "select id from sq_friend where name=? and friend=?";
            ResultSet rs = null;
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            pstmt.setString(2, friendName);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                return getUserFriendDb(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("getUserFriendDb:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;
    }

    public boolean create() throws ResKeyException {
        Conn conn = null;
        boolean re = false;
        try {
            conn = new Conn(connname);
            // 检查用户是否已在自己的好友列表中
            String sql = "select friend,state from sq_friend where name=? and friend=?";
            ResultSet rs = null;
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, friend);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                int state = rs.getInt(2);
                if (state==1) {
                    // 如果已有并且是已通过状态，则提醒“用户已经是您的好友”
                    throw new ResKeyException("res.forum.person.UserFriendDb",
                                              "err_add_repeat");
                }
                else {
                    // 如果是未通过状态，则提醒“您已经向对方发出了加为好友的申请！”
                    throw new ResKeyException("res.forum.person.UserFriendDb",
                                              "err_apply_repeat");
                }
            }

            if (rs!=null) {
                rs.close();
                rs = null;
            }

            if (pstmt!=null) {
                pstmt.close();
                pstmt = null;
            }

            pstmt = conn.prepareStatement(QUERY_CREATE);
            pstmt.setString(1, name);
            pstmt.setString(2, friend);
            pstmt.setString(3, "" + System.currentTimeMillis());
            int id = (int)SequenceMgr.nextID(SequenceMgr.SQ_FRIEND);
            pstmt.setInt(4, id);
            pstmt.setInt(5, state);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                UserFriendCache mc = new UserFriendCache(this);
                mc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new UserFriendDb(pk.getIntValue());
    }

    public int delFriendsOfUser(String userName) {
        // Based on the id in the object, get the message data from the database.
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "select id from sq_friend where name=?";
        int count = 0;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    getUserFriendDb(rs.getInt(1)).del();
                    count++;
                }
            }
        } catch (SQLException e) {
            logger.error("delFriendsOfUser:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return count;
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (re) {
                UserFriendCache bc = new UserFriendCache(this);
                primaryKey.setValue(new Integer(id));
                bc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setInt(1, state);
            pstmt.setInt(2, id);
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            Logger.getLogger(getClass()).error("save:" + e.getMessage());
        }
        finally {
            UserFriendCache ufc = new UserFriendCache(this);
            primaryKey.setValue(new Integer(id));
            ufc.refreshSave(primaryKey);
            if (conn != null) {
                conn.close(); conn = null;
            }
        }
        return re;
    }

    public void load() {
        // Based on the id in the object, get the message data from the database.
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            // "SELECT name, personNum, description, address, equipment, mydate FROM " + tableName + " WHERE id=?";
            pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (!rs.next()) {
                logger.error("load: " + id +
                             " is not found in DB.");
            } else {
                name = rs.getString(1);
                friend = rs.getString(2);
                rq = DateUtil.parse(rs.getString(3));
                state = rs.getInt(4);
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
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
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public UserFriendDb getUserFriendDb(int id) {
        return (UserFriendDb)getObjectDb(new Integer(id));
    }

    public int getId() {
        return id;
    }

    public String getFriend() {
        return friend;
    }

    public java.util.Date getRq() {
        return rq;
    }

    public int getState() {
        return state;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setRq(Date rq) {
        this.rq = rq;
    }

    private String name;
    private int id;
    private String friend;
    private java.util.Date rq;
    private int state;
    private void jbInit() throws Exception {
    }

}
