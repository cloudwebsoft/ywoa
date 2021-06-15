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

/**
 *
 * <p>Title: 积分所加挂的版块，如果type为forum，则积分挂于全部版块</p>
 *
 * <p>Description: 积分操作可以有选择地挂于某些版块上，但当type为forum，则是全局积分</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class BoardScoreDb extends ObjectDb {

    private String boardCode;

    public BoardScoreDb() {
        super();
    }

    public BoardScoreDb(String boardCode, String scoreCode) {
        this.boardCode = boardCode;
        this.scoreCode = scoreCode;
        init();
        load();
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        BoardScoreCache fc = new BoardScoreCache(this);
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
            ps.setString(2, scoreCode);
            rowcount = conn.executePreUpdate();
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
        return new BoardScoreDb(pk.getKeyStrValue("boardCode"), pk.getKeyStrValue("scoreCode"));
    }

    public void setQueryCreate() {
        this.QUERY_CREATE = "insert into sq_board_score (boardCode,scoreCode) values (?,?)";
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, boardCode);
            ps.setString(2, scoreCode);
            rowcount = conn.executePreUpdate();
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

    public void setQuerySave() {
    }

    public void setQueryDel() {
        this.QUERY_DEL = "delete from sq_board_score where boardCode=? and scoreCode=?";
    }

    public void setQueryLoad() {
        this.QUERY_LOAD =
            "select boardCode,scoreCode from sq_board_score where boardCode=? and scoreCode=?";
    }

    public void setQueryList() {
    }

    public boolean save() {
         return true;
    }

    /**
     * 当删除Leaf时，用以删除leaf所附带的score
     * @param boardCode String
     * @return Vector
     */
    public void delBoardScoreDbsOfBoard(String boardCode) {
        ResultSet rs = null;
        String sql = "select scoreCode from sq_board_score where boardCode=?";
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, boardCode);
            rs = conn.executePreQuery();
            if (rs!=null) {
                while (rs.next()) {
                    scoreCode = rs.getString(1);
                    getBoardScoreDb(boardCode, scoreCode).del();
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
    }

    public BoardScoreDb getBoardScoreDb(String boardCode, String scoreCode) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("boardCode", boardCode);
        pk.setKeyValue("scoreCode", scoreCode);
        return (BoardScoreDb)getObjectDb(pk.getKeys());
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setString(1, boardCode);
            ps.setString(2, scoreCode);
            primaryKey.setKeyValue("boardCode", boardCode);
            primaryKey.setKeyValue("scoreCode", scoreCode);
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
        key.put("boardCode", new KeyUnit(primaryKey.TYPE_STRING, 0));
        key.put("scoreCode", new KeyUnit(primaryKey.TYPE_STRING, 1));
        primaryKey = new PrimaryKey(key);
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setScoreCode(String scoreCode) {
        this.scoreCode = scoreCode;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getScoreCode() {
        return scoreCode;
    }

    public Vector list(String scoreCode) {
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        String sql = "select boardCode from sq_board_score where scoreCode=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, scoreCode);
            rs = conn.executePreQuery();
            if (rs!=null) {
                while (rs.next()) {
                    boardCode = rs.getString(1);
                    v.addElement(getBoardScoreDb(boardCode, scoreCode));
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

    private String scoreCode;

}
