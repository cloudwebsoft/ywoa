package com.redmoon.oa.meeting;

import java.sql.*;
import cn.js.fan.db.*;
import cn.js.fan.web.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.base.ObjectDb;
import com.cloudwebsoft.framework.util.LogUtil;

public class BoardroomDb extends ObjectDb {
    String connname;
    // Logger LogUtil.getLog(getClass()) = Logger.getLogger(BoardroomDb.class.getName());

    public BoardroomDb() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("FlowTypeDb:默认数据库名为空！");
        isInitFromConfigDB = false;
        init();
    }

    public BoardroomDb(int id){
        isInitFromConfigDB = false;
        this.id = id;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("FlowTypeDb:默认数据库名为空！");
        load();
        init();
    }

    public void initDB() {
        objectCache = new BoardroomCache(this);
        tableName = "boardroom";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);

        QUERY_LOAD =
            "SELECT name, personNum, description, address, equipment, mydate FROM " + tableName + " WHERE id=?";
        QUERY_SAVE =
            "update " + tableName + " set name=?, personNum=?, description=?, address=?, equipment=? where id=?";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_CREATE = "insert into " + tableName + " (name,personNum,description,address,equipment,mydate) values (?,?,?,?,?,?)";
        QUERY_LIST = "select id from " + tableName + " order by mydate asc";
        isInitFromConfigDB = false;
    }

    public boolean create() throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        try {
            conn = new Conn(connname);
            PreparedStatement pstmt = conn.prepareStatement(this.QUERY_CREATE);
            pstmt.setString(1, name);
            pstmt.setInt(2, personNum);
            pstmt.setString(3, description);
            pstmt.setString(4, address);
            pstmt.setString(5, equipment);
            pstmt.setTimestamp(6, new Timestamp(new java.util.Date().getTime()));
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                BoardroomCache mc = new BoardroomCache(this);
                mc.refreshCreate();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            throw new ErrMsgException("插入时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new BoardroomDb(pk.getIntValue());
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
                BoardroomCache bc = new BoardroomCache(this);
                primaryKey.setValue(new Integer(id));
                bc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
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
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            // "update " + tableName + " set name=?, personNum, description=?, address=?, equipment=? where id=?";
            pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, name);
            pstmt.setInt(2, personNum);
            pstmt.setString(3, description);
            pstmt.setString(4, address);
            pstmt.setString(5, equipment);
            pstmt.setInt(6, id);
            re = conn.executePreUpdate()>0?true:false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void load() {
        // Based on the id in the object, get the message data from the database.
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // "SELECT name, personNum, description, address, equipment, mydate FROM " + tableName + " WHERE id=?";
            pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (!rs.next()) {
                LogUtil.getLog(getClass()).error("load:流程类型 " + id +
                             " 在数据库中未找到.");
            } else {
                name = rs.getString(1);
                personNum = rs.getInt(2);
                description = rs.getString(3);
                address = rs.getString(4);
                equipment = rs.getString(5);
                mydate = rs.getDate(6);
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPersonNum(int personNum) {
        this.personNum = personNum;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public void setMydate(Date mydate) {
        this.mydate = mydate;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPersonNum() {
        return personNum;
    }

    public String getEquipment() {
        return equipment;
    }

    public Date getMydate() {
        return mydate;
    }

    public String getAddress() {
        return address;
    }

    public BoardroomDb getBoardroomDb(int id) {
        return (BoardroomDb)getObjectDb(new Integer(id));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String name;
    private String description;
    private int id;
    private int personNum;
    private String equipment;
    private Date mydate;
    private String address;

}
