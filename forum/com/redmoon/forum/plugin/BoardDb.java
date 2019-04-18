package com.redmoon.forum.plugin;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

public class BoardDb extends ObjectDb {

    public BoardDb() {
        init();
    }

    public BoardDb(String pluginCode, String boardCode) {
        this.pluginCode = pluginCode;
        this.boardCode = boardCode;
        init();
        load();
    }

    public boolean create(String pluginCode, String boardCode, String rule) {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, pluginCode);
            ps.setString(2, boardCode);
            ps.setString(3, rule);
            rowcount = conn.executePreUpdate();
            BoardCache rbc = new BoardCache(this);
            rbc.refreshCreate();

            PluginMgr.reload();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount > 0 ? true : false;
    }

    public boolean del() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setString(1, pluginCode);
            ps.setString(2, boardCode);
            rowcount = conn.executePreUpdate();
            BoardCache sc = new BoardCache(this);
            sc.refreshDel(primaryKey);

            PluginMgr.reload();
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount > 0 ? true : false;
    }

    public BoardDb getBoardDb(String pluginCode, String boardCode) {
        primaryKey.setKeyValue("pluginCode", pluginCode);
        primaryKey.setKeyValue("boardCode", boardCode);
        return (BoardDb)getObjectDb(primaryKey.getKeys());
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new BoardDb(pk.getKeyStrValue("pluginCode"), pk.getKeyStrValue("boardCode"));
    }

    public boolean save() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, boardRule);
            ps.setString(2, pluginCode);
            ps.setString(3, boardCode);
            rowcount = conn.executePreUpdate();
            BoardCache uc = new BoardCache(this);
            primaryKey.setKeyValue("pluginCode", pluginCode);
            primaryKey.setKeyValue("boardCode", boardCode);
            uc.refreshSave(primaryKey);
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

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setString(1, pluginCode);
            ps.setString(2, boardCode);
            rs = conn.executePreQuery();
            if (rs.next()) {
                boardRule = StrUtil.getNullStr(rs.getString(1));
                primaryKey.setKeyValue("pluginCode", pluginCode);
                primaryKey.setKeyValue("boardCode", boardCode);
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

    public void setBoardRule(String boardRule) {
        this.boardRule = boardRule;
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    public String getBoardRule() {
        return boardRule;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getPluginCode() {
        return pluginCode;
    }

    private String boardRule; // 本版规则
    private String boardCode;

    /**
     * 判断版块上是否有插件
     * @param pluginCode 插件编码
     * @param boardCode 版块编码
     * @return
     */
    public boolean isPluginBoard(String pluginCode, String boardCode) {
        BoardDb sb = getBoardDb(pluginCode, boardCode);
        // System.out.println(getClass() + " pluginCode=" + pluginCode + " boardCode=" + boardCode + " sb.isLoaded()=" + sb.isLoaded());

        if (sb.isLoaded())
            return true;
        else
            return false;
    }

    public void initDB() {
        tableName = "plugin_board";
        HashMap key = new HashMap();
        // 注意key的顺序一定要设置
        key.put("pluginCode", new KeyUnit(PrimaryKey.TYPE_STRING, 0));
        key.put("boardCode", new KeyUnit(PrimaryKey.TYPE_STRING, 1));
        primaryKey = new PrimaryKey(key);
        objectCache = new BoardCache(this);

        this.QUERY_CREATE = "insert into plugin_board (pluginCode, boardCode, boardRule) values (?, ?, ?)";
        this.QUERY_SAVE = "update plugin_board set boardRule=? where pluginCode=? and boardCode=?";
        this.QUERY_DEL = "delete from plugin_board where pluginCode=? and boardCode=?";
        this.QUERY_LOAD =
            "select boardRule from plugin_board where pluginCode=? and boardCode=?";
        this.QUERY_LIST = "select pluginCode, boardCode from plugin_board";
        isInitFromConfigDB = false;
    }

    private String pluginCode;
}
