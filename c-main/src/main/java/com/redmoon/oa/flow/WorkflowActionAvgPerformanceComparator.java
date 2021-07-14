package com.redmoon.oa.flow;

import com.redmoon.oa.pvg.RoleDb;
import cn.js.fan.util.StrUtil;
import java.util.Comparator;

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
public class WorkflowActionAvgPerformanceComparator implements Comparator {
    public WorkflowActionAvgPerformanceComparator() {
    }

    public int compare(Object o1, Object o2) {
        WorkflowActionDb e1 = (WorkflowActionDb) o1;
        WorkflowActionDb e2 = (WorkflowActionDb) o2;

        if (e1.getAveragePerformance() < e2.getAveragePerformance()) { //这样比较是降序,如果把-1改成1就是升序.
            return 1;
        } else if (e1.getAveragePerformance() > e2.getAveragePerformance()) {
            return -1;
        } else {
            return 0;
        }
    }
}
