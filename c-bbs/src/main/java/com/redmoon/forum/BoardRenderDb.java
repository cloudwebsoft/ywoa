package com.redmoon.forum;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.Conn;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import com.redmoon.forum.plugin.base.IPluginRender;
import com.redmoon.forum.plugin.RenderMgr;
import com.redmoon.forum.plugin.PluginMgr;
import com.redmoon.forum.plugin.RenderUnit;

import java.util.Vector;
import java.util.Iterator;
import com.redmoon.forum.plugin.PluginUnit;
import cn.js.fan.util.StrUtil;

/**
 *
 * <p>Title: 控制版块贴子的显示</p>
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
public class BoardRenderDb extends ObjectDb {
    public static final String CODE_DEFAULT = "default";

    public BoardRenderDb() {
        init();
    }

    public BoardRenderDb(String boardCode) {
        this.boardCode = boardCode;
        init();
        load();
    }

    public boolean setBoardRender(String boardCode, String renderCode) {
        BoardRenderDb br = getBoardRenderDb(boardCode);
        if (br.isLoaded()) {
            // logger.info("setBoardRender: boardCode=" + boardCode + " loaded=" + br.isLoaded());
            br.setRenderCode(renderCode);
            return br.save();
        }
        else {
            br.setBoardCode(boardCode);
            br.setRenderCode(renderCode);
            return br.create();
        }
    }

    public BoardRenderDb getBoardRenderDb(String boardCode) {
        return (BoardRenderDb)getObjectDb(boardCode);
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        BoardRenderCache fc = new BoardRenderCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return (BoardRenderDb)fc.getObjectDb(pk);
    }

    public boolean del() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setString(1, boardCode);
            rowcount = conn.executePreUpdate();
            BoardRenderCache uc = new BoardRenderCache(this);
            primaryKey.setValue(boardCode);
            uc.refreshDel(primaryKey);
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
        return new BoardRenderDb(pk.getStrValue());
    }

    public void setQueryCreate() {
        this.QUERY_CREATE = "insert into sq_boardrender (boardCode,renderCode) values (?,?)";
    }

    public void setQuerySave() {
        this.QUERY_SAVE =
            "update sq_boardrender set renderCode=? where boardCode=?";
    }

    public void setQueryDel() {
        this.QUERY_DEL = "delete from sq_boardrender where boardCode=?";
    }

    public void setQueryLoad() {
        this.QUERY_LOAD =
            "select renderCode from sq_boardrender where boardCode=?";
    }

    public void setQueryList() {
        this.QUERY_LIST = "select boardCode,renderCode from sq_boardrender";
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, boardCode);
            ps.setString(2, renderCode);
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

    public boolean save() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setString(1, renderCode);
            ps.setString(2, boardCode);
            rowcount = conn.executePreUpdate();
            BoardRenderCache uc = new BoardRenderCache(this);
            primaryKey.setValue(boardCode);
            uc.refreshSave(primaryKey);
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

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setString(1, boardCode);
            primaryKey.setValue(boardCode);
            rs = conn.executePreQuery();
            if (rs.next()) {
                renderCode = rs.getString(1);
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
        primaryKey = new PrimaryKey("boardCode", PrimaryKey.TYPE_STRING);
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setRenderCode(String renderCode) {
        this.renderCode = renderCode;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getRenderCode() {
        return renderCode;
    }

    public IPluginRender getRender() {
        IPluginRender ipr = null;
        String pluginRenderCode = "";
        // 得到插件所带的render
        if (renderCode.equals(CODE_DEFAULT)) {
            PluginMgr pm = new PluginMgr();
            Vector vplugin = pm.getAllPluginUnitOfBoard(boardCode);
            if (vplugin.size()>0) {
                Iterator irpluginnote = vplugin.iterator();
                while (irpluginnote.hasNext()) {
                    PluginUnit pu = (PluginUnit) irpluginnote.next();
                    pluginRenderCode = StrUtil.getNullStr(pu.getRenderCode());
                    if (pluginRenderCode.equals("") || pluginRenderCode.equals(CODE_DEFAULT)) {
                        continue;
                    }
                    else
                        break;
                }
            }
        }
        if (pluginRenderCode.equals("") || pluginRenderCode.equalsIgnoreCase(CODE_DEFAULT)) {
            RenderMgr rm = new RenderMgr();
            // logger.info("getRender:" + renderCode);
            ipr = rm.getRenderUnit(renderCode).getRender();
        }
        else {
            RenderMgr rm = new RenderMgr();
            RenderUnit ru = rm.getRenderUnit(pluginRenderCode);
            if (ru==null) {
            	throw new IllegalArgumentException("Render plugin: " + pluginRenderCode + " is not exist！");
            }
            ipr = ru.getRender();
        }
        return ipr;
    }

    private String boardCode;
    private String renderCode = CODE_DEFAULT;
}
