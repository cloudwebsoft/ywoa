package com.redmoon.forum.plugin.group;

import java.util.Iterator;

import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.forum.plugin.group.photo.PhotoDb;

/**
 * <p>Title: </p>
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
public class GroupUserDb extends QObjectDb {
    public static int CHECK_STATUS_NOT = 0;
    public static int CHECK_STATUS_PASSED = 1;

    public static String PRIV_TOPIC = "priv_topic";
    public static String PRIV_ALL = "priv_all";

    public GroupUserDb() {
    }

    public boolean create(JdbcTemplate jt, Object[] params) throws
            ResKeyException {
        boolean re = super.create(jt, params);
        if (re) {
            GroupDb gd = new GroupDb();
            gd = (GroupDb)gd.getQObjectDb(new Long(getLong("group_id")));
            gd.set("user_count", new Integer(gd.getInt("user_count") + 1));
            gd.save();
        }
        return re;
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        GroupDb gd = new GroupDb();
        gd = (GroupDb) gd.getQObjectDb(new Long(getLong("group_id")));
        gd.set("user_count", new Integer(gd.getInt("user_count") - 1));
        boolean re = gd.save();

        if (re) {
            GroupThreadDb gt = new GroupThreadDb();
            PhotoDb pd = new PhotoDb();
            GroupActivityDb ga = new GroupActivityDb();
            // 删除用户的贴子、图片和活动
            gt.delThreadOfUserInGroup(getLong("group_id"),
                                      getString("user_name"));
            pd.delPhotoOfUserInGroup(getLong("group_id"), getString("user_name"));
            ga.delGroupActivityOfUserInGroup(getLong("group_id"),
                                             getString("user_name"));

            re = super.del(jt);

        }

        return re;
    }

    public String getListUserSql(long groupId, String which) {
        String sql = "";
        if (which.equals("activeUser")) {
            sql = "select group_id, user_name from " + table.getName() +
                  " where group_id=" + groupId + " order by total_count desc";
        }
        else if (which.equals("newUser")) {
            sql = "select group_id, user_name from " + table.getName() +
                  " where group_id=" + groupId + " and check_status=1 order by add_date desc";
        }
        return sql;
    }

    public GroupUserDb getGroupUserDb(long groupId, String userName) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("group_id", new Long(groupId));
        pk.setKeyValue("user_name", userName);
        return (GroupUserDb)getQObjectDb(pk.getKeys());
    }

    /**
     * 删除圈子中的用户
     * @param id long
     * @throws ResKeyException
     */
    public void delUserOfGroup(long id) throws ResKeyException {
        String sql = "select group_id, user_name from " + table.getName() + " where group_id=" + id;
        Iterator ir = list(sql).iterator();
        while (ir.hasNext()) {
            GroupUserDb bu = (GroupUserDb)ir.next();
            bu.del();
        }
    }

    public Iterator getGroupUserAttend(String userName) {
        String sql = "select group_id, user_name from " + table.getName() + " where user_name=" + StrUtil.sqlstr(userName);
        long count = getQObjectCount(sql);
        return getQObjects(sql, 0, (int)count);
    }

}
