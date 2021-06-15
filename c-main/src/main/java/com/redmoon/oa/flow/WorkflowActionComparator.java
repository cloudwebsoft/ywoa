package com.redmoon.oa.flow;

import java.util.Comparator;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.pvg.RoleDb;

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
public class WorkflowActionComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        WorkflowActionDb e1 = (WorkflowActionDb) o1;
        WorkflowActionDb e2 = (WorkflowActionDb) o2;

        // if (e1.getNodeMode()==WorkflowActionDb.NODE_MODE_ROLE && e2.getNodeMode()==WorkflowActionDb.NODE_MODE_ROLE) {
        if ((e1.getNodeMode()==WorkflowActionDb.NODE_MODE_ROLE || e1.getNodeMode()==WorkflowActionDb.NODE_MODE_ROLE_SELECTED) && (e2.getNodeMode()==WorkflowActionDb.NODE_MODE_ROLE || e2.getNodeMode()==WorkflowActionDb.NODE_MODE_ROLE_SELECTED)) {
            String[] roles1 = StrUtil.split(e1.getJobCode(), ",");
            if (roles1 == null)
                return 0;
            String roleCode1 = roles1[0];

            String[] roles2 = StrUtil.split(e2.getJobCode(), ",");
            if (roles2 == null)
                return 0;
            String roleCode2 = roles2[0];

            RoleDb rd1 = new RoleDb();
            rd1 = rd1.getRoleDb(roleCode1);
            RoleDb rd2 = new RoleDb();
            rd2 = rd1.getRoleDb(roleCode2);
            if (rd1.getOrders() < rd2.getOrders()) { //这样比较是降序,如果把-1改成1就是升序.
                return 1;
            } else if (rd1.getOrders() > rd2.getOrders()) {
                return -1;
            } else {
                return 0;
            }
        }
        else
            return 0;
    }
}
