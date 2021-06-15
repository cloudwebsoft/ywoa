package com.redmoon.forum.person;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.db.Conn;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import cn.js.fan.cache.jcs.RMCache;

public class UserCache extends ObjectCache {
    final String TOPFORUM = "TOP_FORUM";
    final String prefix = "user_nick_";

    public UserCache(UserDb user) {
        super(user);
    }

    public void refreshNick(String nick) {
        try {
            RMCache.getInstance().remove(prefix + nick, group);
        }
        catch (Exception e) {
            logger.error("refreshNick" + e.getMessage());
        }
    }

    public UserDb getUserDbByNick(String nick) {
        UserDb user = null;
        try {
            user = (UserDb) RMCache.getInstance().getFromGroup(prefix + nick,
                    group);
        } catch (Exception e) {
            logger.error("getUserDbByNick1:" + e.getMessage());
        }
        if (user == null) {
            String sql = "select name from sq_user where nick=?";
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            Conn conn = new Conn(connname);
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nick);
                rs = conn.executePreQuery();
                if (rs != null && rs.next()) {
                    UserDb ud = new UserDb();
                    ud = ud.getUser(rs.getString(1));
                    user = ud;
                    try {
                        RMCache.getInstance().putInGroup(prefix + nick, group, user);
                    }
                    catch (Exception e) {
                        logger.error("getUserDbByNick2:" + e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("getUserDbByNick3: " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
        }
        return user;
    }
}
