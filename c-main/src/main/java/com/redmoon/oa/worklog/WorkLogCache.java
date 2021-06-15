package com.redmoon.oa.worklog;

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
public class WorkLogCache extends ObjectCache {
    public WorkLogCache() {
    }

    public WorkLogCache(WorkLogDb kd) {
        super(kd);
    }
}
