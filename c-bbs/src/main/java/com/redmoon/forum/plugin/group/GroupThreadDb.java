package com.redmoon.forum.plugin.group;

import java.sql.SQLException;
import java.util.Vector;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ResKeyException;
import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.MsgDb;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GroupThreadDb extends QObjectDb {
    public GroupThreadDb() {
    }

    public boolean create(JdbcTemplate jt, Object[] params) throws
            ResKeyException {
        boolean re = super.create(jt, params);
        if (re) {
            // System.out.println(getClass() + " group_id=" + getLong("group_id"));
            refreshList("" + getLong("group_id"));
        }
        return re;
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        boolean re = super.del(jt);
        refreshList("" + getLong("group_id"));
        return re;
    }

    public boolean onAddReply(MsgDb md) throws ResKeyException {
        String sql = table.getSql("groupthreadofmsg");
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql,
                                                new Object[] {new Long(md.getRootid())});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long id = rr.getLong("id");

                GroupThreadDb gtd = (GroupThreadDb)getQObjectDb(new Long(id));
                gtd.set("reply_date", new java.util.Date());
                gtd.save();
                refreshList("" + getLong("group_id"));
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delThread:" + e.getMessage());
        }
        return true;
    }

    public void delThreadOfUserInGroup(long groupId, String userName) throws ResKeyException {
        String sql = table.getSql("threadOfUserInGroup");
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Long(groupId), userName});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long id = rr.getLong("id");
                GroupThreadDb gtd = (GroupThreadDb)getQObjectDb(new Long(id));
                gtd.del(jt);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delThreadOfUserInGroup:" + e.getMessage());
        }
    }

    public void delThreadOfGroup(long groupId) throws ResKeyException {
        String sql = table.getSql("threadOfGroup");
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Long(groupId)});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long id = rr.getLong("id");
                GroupThreadDb gtd = (GroupThreadDb)getQObjectDb(new Long(id));
                gtd.del(jt);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delGroupThreadOfGroup:" + e.getMessage());
        }
    }

    public Vector getGroupThreadsOfThread(long msgId) {
    	Vector v = new Vector();
        String sql = table.getSql("groupthreadofmsg");
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql,
                                                new Object[] {new Long(msgId)});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long id = rr.getLong("id");
                GroupThreadDb gtd = (GroupThreadDb)getQObjectDb(new Long(id));
                v.addElement(gtd);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delThread:" + e.getMessage());
        }    	
        return v;
    }
    
    public boolean onDelMsg(long msgId) throws ResKeyException {
        GroupUserDb gu = new GroupUserDb();
        MsgDb md = new MsgDb();
        md.getMsgDb(msgId);
        String userName = md.getName();

        String sql = table.getSql("groupthreadofmsg");
        GroupDb gd = new GroupDb();
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql,
                                                new Object[] {new Long(msgId)});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long id = rr.getLong("id");
                GroupThreadDb gtd = (GroupThreadDb)getQObjectDb(new Long(id));
                gu = gu.getGroupUserDb(gtd.getLong("group_id"), userName);

                gtd.del(jt);

                if (gu!=null) {
                    // 当从回收站清除贴子时，圈子可能已被删除
                    gu.set("msg_count", new Integer(gu.getInt("msg_count") - 1));
                    gu.set("total_count", new Integer(gu.getInt("total_count") - 1));
                    gu.save();
                }
                else
                    gu = new GroupUserDb();

                gd = (GroupDb) gd.getQObjectDb(new Long(gtd.getLong("group_id")));
                gd.set("msg_count", new Integer(gd.getInt("msg_count") + 1));
                gd.set("total_count", new Integer(gd.getInt("total_count") + 1));
                gd.save();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delThread:" + e.getMessage());
        }
        return true;
    }

    public String getListThreadSql(long groupId, String which) {
        String sql = "";
        if (which.equals("add_date")) {
            sql = "select id from " + table.getName() +
                  " where group_id=" + groupId + " order by add_date desc";
        }
        else if (which.equals("reply_date")) {
            sql = "select id from " + table.getName() +
                  " where group_id=" + groupId + " order by reply_date desc";
        }
        return sql;
    }

}
