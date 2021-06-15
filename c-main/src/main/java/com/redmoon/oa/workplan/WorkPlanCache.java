package com.redmoon.oa.workplan;

import cn.js.fan.base.*;

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
public class WorkPlanCache extends ObjectCache {
    public WorkPlanCache() {
    }

    public WorkPlanCache(WorkPlanDb wpt) {
        super(wpt);
    }
}
