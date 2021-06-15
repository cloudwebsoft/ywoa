package com.redmoon.forum.plugin.group;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import com.redmoon.forum.MsgDb;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.db.ResultIterator;
import java.sql.SQLException;
import cn.js.fan.db.ResultRecord;

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
public class GroupActivityDb extends QObjectDb {
    public GroupActivityDb() {
    }

    public String getListActivitySql(long groupId) {
        String sql = "select id from plugin_group_activity where group_id=" + groupId + " order by id desc";
        return sql;
    }

    public void delGroupActivityOfUserInGroup(long groupId, String userName) throws ResKeyException {
        String sql = table.getSql("activityOfUserInGroup");
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Long(groupId), userName});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long id = rr.getLong("id");
                GroupActivityDb gad = (GroupActivityDb)getQObjectDb(new Long(id));
                gad.del();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delGroupActivityOfGroup:" + e.getMessage());
        }
    }

    public void delGroupActivityOfGroup(long groupId) throws ResKeyException {
        String sql = getListActivitySql(groupId);
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long id = rr.getLong("id");
                GroupActivityDb gad = (GroupActivityDb)getQObjectDb(new Long(id));
                gad.del();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delGroupActivityOfGroup:" + e.getMessage());
        }
    }

    public boolean onDelMsg(long msgId) throws ResKeyException {
        MsgDb md = new MsgDb();
        md.getMsgDb(msgId);

        String sql = table.getSql("groupActivityOfMsg");
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql,
                                                new Object[] {new Long(msgId)});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long id = rr.getLong("id");
                GroupActivityDb gad = (GroupActivityDb)getQObjectDb(new Long(id));
                gad.del();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("onDelMsg:" + e.getMessage());
        }
        return true;
    }
}
