package com.redmoon.oa.flow;

import com.redmoon.oa.pvg.RoleDb;
import java.util.Comparator;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description:连接线比较大小，起始为同一个节点 </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkflowLinkComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        WorkflowLinkDb e1 = (WorkflowLinkDb) o1;
        WorkflowLinkDb e2 = (WorkflowLinkDb) o2;

        String[] roles1 = StrUtil.split(e1.getToAction().getJobCode(), ",");
        if (roles1 == null)
            return 0;
        String roleCode1 = roles1[0];

        String[] roles2 = StrUtil.split(e2.getToAction().getJobCode(), ",");
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
}
