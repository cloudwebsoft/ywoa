package com.redmoon.forum;

import java.sql.*;
import java.util.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.util.*;

/**
 * <p>Title: 与标签相关贴子的管理</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class TagMsgDb extends QObjectDb {

    public TagMsgDb() {
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        boolean re = super.del(jt);
        if (re) {
            TagDb td = new TagDb();
            td = td.getTagDb(getLong("tag_id"));
            td.set("count", new Integer(td.getInt("count") - 1));
            td.save(jt);

            // 刷新对应贴子的缓存
            MsgCache mc = new MsgCache();
            mc.refreshUpdate(getLong("msg_id"));
        }
        return re;
    }

    /**
     * 删除用户的标签
     * @param tagId long
     * @param userName String
     */
    public void delTagOfUser(long tagId, String userName) {
        String sql = "select tag_id, msg_id from " + table.getName() + " where tag_id=? and user_name=?";
        Vector v = list(new JdbcTemplate(), sql, new Object[] {new Long(tagId), userName});
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            TagMsgDb tmd = (TagMsgDb)ir.next();
            try {
                tmd.del();
            }
            catch (ResKeyException e) {
                LogUtil.getLog(getClass()).error("delTagOfUser:" + e.getMessage());
            }
        }
    }

    /**
     * 删除标签与贴子的对应记录
     * @param tagId long 被删除对应记录的标签的ID
     */
    public void delForTag(long tagId) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = new Connection(Global.getDefaultDB());
        try {
            // 清除贴子原来的标签
            String sql = "select tag_id, msg_id from " + table.getName() + " where tag_id=?";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, tagId);
            rs = conn.executePreQuery();
            while (rs.next()) {
                TagMsgDb tmd = getTagMsgDb(rs.getLong(1), rs.getLong(2));
                tmd.del();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delForMsg:" + e.getMessage());
        }
        catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error("delForMsg2:" + e.getMessage());
        }
        finally {
            if (rs!=null) {
                try { rs.close(); } catch (Exception e) {}
                rs = null;
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {}
                conn = null;
            }
        }
    }

/*
    public boolean create(long tagId, long msgId) {
        JdbcTemplate jt = new JdbcTemplate();
        boolean re = false;
        try {
            re = jt.executeUpdate(table.getQueryCreate(),
                                  new Object[] {new Long(tagId), new Long(msgId),
                                  new java.util.Date()})==1;
            if (re) {
                TagDb td = new TagDb();
                td = td.getTagDb(tagId);
                td.set("count", new Integer(td.getInt("count") + 1));
                td.save(jt);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }
        catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error("create2:" + e.getMessage());
        }
        return re;
    }
*/

    public Vector getTagMsgDbOfMsg(long msgId) {
        String sql = "select tag_id, msg_id from " + table.getName() + " where msg_id=?";
        // System.out.println(getClass() + " sql=" + sql + "  msgId=" + msgId);
        return list(new JdbcTemplate(), sql, new Object[] {new Long(msgId)});
    }

    public void createForMsg(long msgId, Vector tagNames, String userName) {
        int len = tagNames.size();
        PreparedStatement ps = null;

        Connection conn = new Connection(Global.getDefaultDB());
        try {
            ps = conn.prepareStatement(table.getQueryCreate());
            TagDb td2 = new TagDb();
            for (int i = 0; i < len; i++) {
                String tagName = (String)tagNames.elementAt(i);
                TagDb td = td2.getTagDbByName(tagName);
                if (td == null) {
                    // 如果是新标签则创建
                    td2.create(tagName, userName);
                    td = td2.getTagDbByName((String) tagNames.elementAt(i));
                }
                ps.setLong(1, td.getLong("id"));
                ps.setLong(2, msgId);
                ps.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
                ps.setString(4, userName);
                ps.addBatch();
                try {
                    td.set("count", new Integer(td.getInt("count") + 1));
                    td.save();
                }
                catch (ResKeyException e) {
                    LogUtil.getLog(getClass()).error("createForMsg1:" + e.getMessage());
                }
            }
            ps.executeBatch();

            refreshCreate();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("createForMsg2:" + e.getMessage());
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {}
                conn = null;
            }
        }
    }

    public TagMsgDb getTagMsgDb(long tagId, long msgId) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("tag_id", new Long(tagId));
        pk.setKeyValue("msg_id", new Long(msgId));
        TagMsgDb tmd = (TagMsgDb)getQObjectDb(pk.getKeys());
        return tmd;
    }

    public void delForMsg(long msgId) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = new Connection(Global.getDefaultDB());
        try {
            // 清除贴子原来的标签
            String sql = "select tag_id, msg_id from " + table.getName() + " where msg_id=?";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, msgId);
            rs = conn.executePreQuery();
            while (rs.next()) {
                TagMsgDb tmd = getTagMsgDb(rs.getLong(1), rs.getLong(2));
                tmd.del();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delForMsg:" + e.getMessage());
        }
        catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error("delForMsg2:" + e.getMessage());
        }
        finally {
            if (rs!=null) {
                try { rs.close(); } catch (Exception e) {}
                rs = null;
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {}
                conn = null;
            }
        }
    }

    public void editForMsg(long msgId, Vector tagNames, String userName) {
        if (tagNames==null)
            return;
        int len = tagNames.size();
        PreparedStatement ps = null;

        // 清除贴子原来的标签
        delForMsg(msgId);
        if (tagNames==null)
            return;
        Connection conn = new Connection(Global.getDefaultDB());
        try {
            ps = conn.prepareStatement(table.getQueryCreate());
            TagDb td2 = new TagDb();
            for (int i = 0; i < len; i++) {
                String tagName = (String)tagNames.elementAt(i);

                TagDb td = td2.getTagDbByName(tagName);
                if (td == null) {
                    // 如果是新标签则创建
                    td2.create(tagName, userName);
                    td = td2.getTagDbByName((String) tagNames.elementAt(i));
                }

                ps.setLong(1, td.getLong("id"));
                ps.setLong(2, msgId);
                ps.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
                ps.setString(4, userName);
                ps.addBatch();
                try {
                    td.set("count", new Integer(td.getInt("count") + 1));
                    td.save();
                }
                catch (ResKeyException e) {
                    LogUtil.getLog(getClass()).error("editForMsg1:" + e.getMessage());
                }
            }
            ps.executeBatch();

        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("editForMsg2:" + e.getMessage());
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {}
                conn = null;
            }
        }

        // 刷新对应贴子的缓存
        MsgCache mc = new MsgCache();
        mc.refreshUpdate(msgId);
    }

}
