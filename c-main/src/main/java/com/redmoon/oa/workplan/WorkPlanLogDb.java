package com.redmoon.oa.workplan;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.*;
import cn.js.fan.db.SQLFilter;
import java.util.Vector;
import java.util.Iterator;

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
public class WorkPlanLogDb extends QObjectDb {
    public WorkPlanLogDb() {
    }

    public static void log(String userName, int workplanId, String remark) {
        WorkPlanDb wpd = new WorkPlanDb();
        wpd = wpd.getWorkPlanDb(workplanId);
        // 如果未审核通过，则不记录
        if (wpd.getCheckStatus()==WorkPlanDb.CHECK_STATUS_NOT)
        	return;
        
        // 检查测10分钟内是否有变动，如果有，则删除上一条，记录这一条，以免频繁改变引起的问题
        WorkPlanLogDb wpld = new WorkPlanLogDb();
        java.util.Date d = DateUtil.addMinuteDate(new java.util.Date(), 10);
        String sql = "select id from " + wpld.getTable().getName() + " where user_name=? and workplan_id=? and create_date>=?";
        Vector v = wpld.list(sql, new Object[]{userName, new Integer(workplanId), d});
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            wpld = (WorkPlanLogDb)ir.next();
            try {
                wpld.del();
            } catch (ResKeyException ex1) {
                ex1.printStackTrace();
            }
        }

        try {
            wpld.create(new JdbcTemplate(), new Object[] {
                new Integer(workplanId), userName, new java.util.Date(), wpd.getGantt(), remark
            });
        } catch (ResKeyException ex) {
            ex.printStackTrace();
        }
    }
}
