package com.redmoon.forum;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.Conn;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import cn.js.fan.db.KeyUnit;
import java.util.Vector;
import com.redmoon.forum.plugin.EntranceMgr;

/**
 *
 * <p>Title: 通行证</p>
 *
 * <p>Description:管理对通行证的数据库操作 </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class BoardEntranceDb extends ObjectDb {

    private String boardCode;

    public BoardEntranceDb() {
        super();
    }

    public BoardEntranceDb(String boardCode, String entranceCode) {
        this.boardCode = boardCode;
        this.entranceCode = entranceCode;
        init();
        load();
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        BoardEntranceCache fc = new BoardEntranceCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return fc.getObjectDb(pk);
    }

    public boolean del() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setString(1, boardCode);
            ps.setString(2, entranceCode);
            rowcount = conn.executePreUpdate();
            if (rowcount>0) {
                BoardEntranceCache bec = new BoardEntranceCache(this);
                // 不能使用下面的用法，会丢失KeyUnit的顺序
                /*
                HashMap keys = new HashMap();
                keys.put("boardCode", new KeyUnit(boardCode));
                keys.put("entranceCode", new KeyUnit(entranceCode));
                primaryKey.setValue(keys);
                */
                primaryKey.setKeyValue("boardCode", boardCode);
                primaryKey.setKeyValue("entranceCode", entranceCode);
                bec.refreshDel(primaryKey);
                // 刷新通行证，目的是为了刷新版块上所挂的通行证getAllEntranceUnitOfBoard
                EntranceMgr.reload();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public int getObjectCount(String sql) {
        return 0;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new BoardEntranceDb(pk.getKeyStrValue("boardCode"), pk.getKeyStrValue("entranceCode"));
    }

    public void setQueryCreate() {
        this.QUERY_CREATE = "insert into sq_board_entrance (boardCode,entranceCode) values (?,?)";
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, boardCode);
            ps.setString(2, entranceCode);
            rowcount = conn.executePreUpdate();
            if (rowcount>0) {
                BoardEntranceCache bec = new BoardEntranceCache(this);
                primaryKey.setKeyValue("boardCode", boardCode);
                primaryKey.setKeyValue("entranceCode", entranceCode);
                bec.refreshCreate();
                // 刷新通行证，目的是为了刷新版块上所挂的通行证getAllEntranceUnitOfBoard
                EntranceMgr.reload();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public void setQuerySave() {
    }

    public void setQueryDel() {
        this.QUERY_DEL = "delete from sq_board_entrance where boardCode=? and entranceCode=?";
    }

    public void setQueryLoad() {
        this.QUERY_LOAD =
            "select boardCode,entranceCode from sq_board_entrance where boardCode=? and entranceCode=?";
    }

    public void setQueryList() {
    }

    public boolean save() {
         return true;
    }

    public BoardEntranceDb getBoardEntranceDb(String boardCode, String entranceCode) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("boardCode", boardCode);
        pk.setKeyValue("entranceCode", entranceCode);
        return (BoardEntranceDb)getObjectDb(pk.getKeys());
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setString(1, boardCode);
            ps.setString(2, entranceCode);
            primaryKey.setKeyValue("boardCode", boardCode);
            primaryKey.setKeyValue("entranceCode", entranceCode);
            rs = conn.executePreQuery();
            if (rs.next()) {
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

    public Object[] getObjectBlock(String query, int startIndex) {
        return null;
    }

    public void setPrimaryKey() {
        HashMap key = new HashMap();
        // 注意初始化时key的顺序一定要设置
        key.put("boardCode", new KeyUnit(primaryKey.TYPE_STRING, 0));
        key.put("entraceCode", new KeyUnit(primaryKey.TYPE_STRING, 1));
        primaryKey = new PrimaryKey(key);
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setEntranceCode(String entranceCode) {
        this.entranceCode = entranceCode;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getEntranceCode() {
        return entranceCode;
    }

    public Vector list(String entranceCode) {
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        String sql = "select boardCode from sq_board_entrance where entranceCode=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, entranceCode);
            rs = conn.executePreQuery();
            if (rs!=null) {
                while (rs.next()) {
                    boardCode = rs.getString(1);
                    v.addElement(getBoardEntranceDb(boardCode, entranceCode));
                }
            }
        }
        catch (SQLException e) {
            logger.error("list: " + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    private String entranceCode;

}
