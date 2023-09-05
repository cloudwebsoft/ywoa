package com.redmoon.oa.ui.menu;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.*;
import java.sql.*;
import java.util.Iterator;
import cn.js.fan.util.*;
import com.redmoon.oa.person.UserDb;
import com.cloudwebsoft.framework.util.LogUtil;

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
public class SlideMenuGroupDb extends QObjectDb {
    public SlideMenuGroupDb() {
    }

    /**
     * 取得下一个排序号
     * @param userName String
     * @return int
     */
    public int getNextOrders(String userName) {
        String sql = "select max(orders) from " + getTable().getName() + " where user_name=?";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql, new Object[] {userName});
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
        if (ri.hasNext()) {
            ResultRecord rr = (ResultRecord)ri.next();
            return rr.getInt(1) + 1;
        }
        return 0;
    }

    /**
     * 删除某用户的菜单组
     * @param userName String
     */
    public void deleteOfUser(String userName) {
        String sql = "select id from " + getTable().getName() + " where user_name=? order by orders";
        SlideMenuDb smd = new SlideMenuDb();
        Iterator ir = list(sql, new Object[] {userName}).iterator();
        while (ir.hasNext()) {
            SlideMenuGroupDb smgd = (SlideMenuGroupDb) ir.next();
            smd.deleteOfGroup(smgd.getLong("id"));
            try {
                smgd.del();
            } catch (ResKeyException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }
        }

    }

    /**
     * 初始化某用户的滑动菜单
     * @param userName String
     */
    public void init(String userName) {
        // 删除用户的菜单组
        deleteOfUser(userName);

        String sql = "select id from " + getTable().getName() + " where user_name=? order by orders";
        SlideMenuDb smd = new SlideMenuDb();

        // 创建组
        SlideMenuGroupDb smgd2 = new SlideMenuGroupDb();
        SlideMenuDb smd2 = new SlideMenuDb();
        String sql2 = "select id from " + smd2.getTable().getName() + " where group_id=? order by orders";
        String sqlGetGroupId = "select id from " + getTable().getName() + " where user_name=? and orders=?";
        Iterator ir = list(sql, new Object[]{UserDb.SYSTEM}).iterator();
        while (ir.hasNext()) {
            SlideMenuGroupDb smgd = (SlideMenuGroupDb)ir.next();
            // 取出groupId
            long groupId = -1;

            try {
                int orders = smgd.getInt("orders");
                smgd2.create(new JdbcTemplate(), new Object[] {
                    userName, smgd.getString("name"), new Integer(orders)
                });

                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sqlGetGroupId, new Object[]{userName, new Integer(orders)});
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord)ri.next();
                    groupId = rr.getLong(1);
                }
            } catch (ResKeyException ex1) {
                ex1.printStackTrace();
            } catch (SQLException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }

            Iterator ir2 = smd.list(sql2, new Object[]{smgd.getLong("id")}).iterator();
            while (ir2.hasNext()) {
                smd = (SlideMenuDb)ir2.next();
                // 创建菜单图标
                try {
                    // LogUtil.getLog(getClass()).info("code=" + smd.getString("code") + " groupid=" + smgd.getLong("id") + " orders=" + smd.getInt("orders"));
                    smd2.create(new JdbcTemplate(), new Object[] {
                        smd.getString("code"), new Integer(smd.getInt("orders")), new Long(groupId)
                    });
                } catch (ResKeyException ex2) {
                    ex2.printStackTrace();
                }

            }
        }
    }
}
