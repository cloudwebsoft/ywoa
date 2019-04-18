package com.redmoon.forum;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;

/**
 *
 * <p>Title: 版主管理</p>
 *
 * <p>Description: 对版主进行管理</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class BoardManagerDb extends ObjectDb {
    private String boardCode;
    public BoardManagerDb() {
        init();
    }

    public BoardManagerDb(String boardCode, String name) {
        this.boardCode = boardCode;
        this.name = name;
        init();
        load();
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        BoardManagerCache fc = new BoardManagerCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return (BoardManagerDb)fc.getObjectDb(pk);
    }

    /**
     * 删除所有用户名为userName的版主
     * @param userName String
     * @return int
     */
    public int delManager(String userName) {
        String sql = "select boardcode from sq_boardmanager where name=?";
        ResultSet rs = null;
        int count = 0;
        Conn conn = new Conn(connname);
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs!=null) {
                while (rs.next()) {
                    getBoardManagerDb(rs.getString(1), userName).del();
                    count ++;
                }
            }
        } catch (SQLException e) {
            logger.error("delManager:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return count;
    }

    public boolean del() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setString(1, boardCode);
            ps.setString(2, name);
            rowcount = conn.executePreUpdate();
            BoardManagerCache uc = new BoardManagerCache(this);
            primaryKey.setKeyValue("boardCode", boardCode);
            primaryKey.setKeyValue("name", name);
            uc.refreshDel(primaryKey);

            BoardManagerCache bmc = new BoardManagerCache(this);
            bmc.refreshBoardManagers(boardCode);
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
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
        return new BoardManagerDb(pk.getKeyStrValue("boardCode"), pk.getKeyStrValue("name"));
    }

    public void setQueryCreate() {
        this.QUERY_CREATE = "insert into sq_boardmanager (boardcode, name, sort, is_hide, can_check) values (?,?,?,?,?)";
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, boardCode);
            ps.setString(2, name);
            ps.setInt(3, sort);
            ps.setInt(4, hide?1:0);
            ps.setInt(5, canCheck?1:0);
            rowcount = conn.executePreUpdate();
            if (rowcount>0) {
                BoardManagerCache bmc = new BoardManagerCache(this);
                bmc.refreshBoardManagers(boardCode);
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
        this.QUERY_SAVE =
            "update sq_boardmanager set sort=?,is_hide=?,can_check=? where boardcode=? and name=?";
    }

    public void setQueryDel() {
        this.QUERY_DEL = "delete from sq_boardmanager where boardcode=? and name=?";
    }

    public void setQueryLoad() {
        this.QUERY_LOAD =
            "select sort,is_hide,can_check from sq_boardmanager where boardcode=? and name=?";
    }

    public void setQueryList() {
    }

    public boolean save() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setInt(1, sort);
            ps.setInt(2, hide?1:0);
            ps.setInt(3, canCheck?1:0);
            ps.setString(4, boardCode);
            ps.setString(5, name);
            rowcount = conn.executePreUpdate();
            BoardManagerCache uc = new BoardManagerCache(this);

            primaryKey.setKeyValue("boardCode", boardCode);
            primaryKey.setKeyValue("name", name);
            uc.refreshSave(primaryKey);

            BoardManagerCache bmc = new BoardManagerCache(this);
            bmc.refreshBoardManagers(boardCode);
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public Vector getBoardManagers(String boardcode) {
        BoardManagerCache bmc = new BoardManagerCache(this);
        return bmc.getBoardManagers(boardcode);
    }

    public BoardManagerDb getBoardManagerDb(String boardCode, String name) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("boardCode", boardCode);
        pk.setKeyValue("name", name);
        return (BoardManagerDb)getObjectDb(pk.getKeys());
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setString(1, boardCode);
            ps.setString(2, name);
            primaryKey.setKeyValue("boardCode", boardCode);
            primaryKey.setKeyValue("name", name);
            rs = conn.executePreQuery();
            if (rs.next()) {
                sort = rs.getInt(1);
                hide = rs.getInt(2)==1;
                canCheck = rs.getInt(3)==1;
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
        // 注意key的顺序一定要设置
        key.put("boardCode", new KeyUnit(PrimaryKey.TYPE_STRING, 0));
        key.put("name", new KeyUnit(PrimaryKey.TYPE_STRING, 1));
        primaryKey = new PrimaryKey(key);
    }

    /**
     * 检查用户是有版主身份
     * @param name String
     * @return boolean
     */
    public boolean isUserManager(String name) {
        BoardManagerCache bmc = new BoardManagerCache(this);
        return bmc.isUserManager(name);
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getName() {
        return name;
    }

    public int getSort() {
        return sort;
    }

    public boolean isHide() {
        return hide;
    }

    private String name;
    private int sort;
    private boolean hide = false;
    private boolean canCheck = true;
    
	public boolean isCanCheck() {
		return canCheck;
	}

	public void setCanCheck(boolean canCheck) {
		this.canCheck = canCheck;
	}
}
