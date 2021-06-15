package com.redmoon.forum;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.db.Conn;
import com.redmoon.forum.person.UserDb;
import java.sql.PreparedStatement;
import java.util.Vector;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoardManagerCache extends ObjectCache {
    final String BOARD_MANAGER = "BOARD_MGR_";
    final String BOARD_MANAGER_USER = "BOARD_MGR_USER_";

    public BoardManagerCache(BoardManagerDb bm) {
        super(bm);
    }

    public void setGroup() {
        group = "BOARDMANAGER_";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "BOARDMANAGER_COUNT_";
    }

    public void refreshBoardManagers(String boardCode) {
        try {
            rmCache.remove(BOARD_MANAGER + boardCode, group);
        }
        catch (Exception e) {
            logger.error("refreshBoardManagers:" + e.getMessage());
        }
    }

    /**
     * 判定用户是否具有版主身份
     * task:在置版主的时候，应刷新缓存
     * @param name String
     * @return boolean
     */
    public boolean isUserManager(String name) {
        Boolean isManager = null;
        try {
            isManager = (Boolean)rmCache.getFromGroup(BOARD_MANAGER_USER + name, group);

        } catch (Exception e) {
            logger.error("getBoardManagers:" + e.getMessage());
        }
        if (isManager!=null) {
            return isManager.booleanValue();
        }

        String sql =
            "select boardcode from sq_boardmanager where name=?";
        boolean re = false;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            rs = conn.executePreQuery();
            if (rs.next()) {
                re = true;
            }
            rmCache.putInGroup(BOARD_MANAGER_USER + name, group, new Boolean(re));
        } catch (Exception e) {
            logger.error("isUserManager:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public Vector getBoardManagers(String boardcode) {
        Vector managers = null;
        try {
            managers = (Vector)rmCache.getFromGroup(BOARD_MANAGER + boardcode, group);
        }
        catch (Exception e) {
            logger.error("getBoardManagers:" + e.getMessage());
        }
        if (managers!=null)
            return managers;
        managers = new Vector();
        String sql =
                "select name from sq_boardmanager where boardcode=? order by sort";
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        UserDb user = new UserDb();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, boardcode);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    managers.addElement(user.getUser(rs.getString(1)));
                }
            }
            rmCache.putInGroup(BOARD_MANAGER + boardcode, group, managers);
        } catch (Exception e) {
            logger.error("getBoardManagers:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return managers;
    }
}
