package com.redmoon.forum;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import java.util.Iterator;
import java.util.Vector;

/**
 *
 * <p>Title: 版块子类别</p>
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
public class ThreadTypeDb extends ObjectDb {
    String name;

    public static final int THREAD_TYPE_NONE = 0; // 无子类别，不用-1，因为伪静态页面中要根据 - 解析

    public ThreadTypeDb() {
        init();
    }

    public ThreadTypeDb(int id) {
        this.id = id;
        init();
        load();
    }

    public void initDB() {
        this.tableName = "sq_thread_type";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new ThreadTypeCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT INTO " + tableName +
                " (id,board_code,display_order,name,color) VALUES (?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT board_code,display_order,name,color FROM " + tableName +
                " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName +
                " SET name=?,color=?,display_order=? WHERE id=?";
        this.QUERY_LIST = "select id from sq_thread_type";
        isInitFromConfigDB = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new ThreadTypeDb(pk.getIntValue());
    }

    public int getId() {
        return id;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getColor() {
        return color;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Vector getThreadTypesOfBoard(String boardCode) {
        ThreadTypeCache ttc = new ThreadTypeCache(this);
        return ttc.getThreadTypesOfBoard(boardCode);
    }

    public void delThreadTypesOfBoard(String boardCode) {
    	Iterator ir = getThreadTypesOfBoard(boardCode).iterator();
    	while (ir.hasNext()) {
    		ThreadTypeDb ttd = (ThreadTypeDb)ir.next();
    		ttd.del();
    	}
    }
    
    public boolean create(String name, String boardCode, String color, int displayOrder) {
        Conn conn = new Conn(connname);
        boolean re = false;
        id = (int) SequenceMgr.nextID(SequenceMgr.THREAD_TYPE_ID);
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
            pstmt.setInt(1, id);
            pstmt.setString(2, boardCode);
            pstmt.setInt(3, displayOrder);
            pstmt.setString(4, name);
            pstmt.setString(5, color);
            re = conn.executePreUpdate() == 1 ? true : false;
            ThreadTypeCache uc = new ThreadTypeCache(this);
            uc.refreshCreate();
            uc.refreshThreadTypesOfBoard(boardCode);
        } catch (Exception e) {
            logger.error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public ThreadTypeDb getThreadTypeDb(int id) {
        return (ThreadTypeDb) getObjectDb(new Integer(id));
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, name);
            pstmt.setString(2, color);
            pstmt.setInt(3, displayOrder);
            pstmt.setInt(4, id);
            re = conn.executePreUpdate() == 1 ? true : false;

            ThreadTypeCache uc = new ThreadTypeCache(this);
            primaryKey.setValue(new Integer(this.id));
            uc.refreshSave(primaryKey);
            uc.refreshThreadTypesOfBoard(boardCode);
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            primaryKey.setValue(new Integer(id));
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    boardCode = rs.getString(1);
                    displayOrder = rs.getInt(2);
                    name = rs.getString(3);
                    color = StrUtil.getNullStr(rs.getString(4));
                    loaded = true;
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
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

    public boolean del() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            String sql = "delete from " + tableName + " where id=" + id;
            re = conn.executeUpdate(sql)==1;
            sql = "select id from " + tableName +
                    " where board_code=" + StrUtil.sqlstr(boardCode) + " and display_order>" +
                          displayOrder;
            ResultSet rs = conn.executeQuery(sql);
            while (rs.next()) {
                ThreadTypeDb ttd = getThreadTypeDb(rs.getInt(1));
                ttd.setDisplayOrder(ttd.getDisplayOrder() - 1);
                ttd.save();
            }

            ThreadTypeCache uc = new ThreadTypeCache(this);
            primaryKey.setValue(new Integer(id));
            uc.refreshDel(primaryKey);
            uc.refreshThreadTypesOfBoard(boardCode);
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

    public int getMaxOrders() {
        String GETMAXORDERS =
                "select max(display_order) from " + tableName +
                " where board_code=?";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        int maxorders = -1;
        try {
            // 更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(GETMAXORDERS);
            pstmt.setString(1, boardCode);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return maxorders;
    }

    private int id;
    private int displayOrder = 0;
    private String boardCode;
    private String color;

}
