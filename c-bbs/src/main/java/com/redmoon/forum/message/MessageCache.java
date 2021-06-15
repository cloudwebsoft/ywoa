package com.redmoon.forum.message;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.db.Conn;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageCache extends ObjectCache {
    String prefix = "Receiver_";

    public MessageCache(MessageDb messageDb) {
        super(messageDb);
    }

    public void setGroup() {
        group = "MESSAGE";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "MESSAGE_COUNT_";
    }

    public int getNewMsgCount(String receiver) {
        Integer countobj = new Integer(0);
        try {
            countobj = (Integer) rmCache.getFromGroup(prefix + receiver, group);
        } catch (Exception e) {
            logger.error("getNewMsgCount:" + e.getMessage());
        }
        if (countobj != null)
            return countobj.intValue();
        String sql =
                "select count(*) from message where isreaded=0 and receiver=?";
        Conn conn = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, receiver);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                count = rs.getInt(1);
                countobj = new Integer(count);
                try {
                    rmCache.putInGroup(prefix + receiver, group, countobj);
                } catch (Exception e) {
                    logger.error("getNewMsgCount: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            logger.error("getNewMsgCount :" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return count;
    }

    public void refreshNewCountOfReceiver(String receiver) {
        try {
            rmCache.remove(prefix + receiver, group);
        }
        catch (Exception e) {
            logger.error("refreshNewCountOfReceiver: " + e.getMessage());
        }
    }
}
