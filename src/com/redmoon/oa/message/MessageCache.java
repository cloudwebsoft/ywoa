package com.redmoon.oa.message;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.db.Conn;
import cn.js.fan.web.Global;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageCache extends ObjectCache {
    String prefix = "OA_Receiver_";

    public MessageCache(MessageDb messageDb) {
        super(messageDb);
    }

    public void setGroup() {
        group = "OA_MESSAGE";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "OA_MESSAGE_COUNT_";
    }

    public int getNewMsgCount(String receiver,String sql) {
        Integer countobj = new Integer(0);
        try {
            countobj = (Integer) rmCache.getFromGroup(prefix + receiver, group);
        } catch (Exception e) {
            logger.error("getNewMsgCount:" + e.getMessage());
        }
        if (countobj != null)
            return countobj.intValue();

       
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
		} catch (Exception e) {
			logger.error("refreshNewCountOfReceiver: " + e.getMessage());
		}
    }
}
