package com.redmoon.forum.plugin.witkey;

import cn.js.fan.base.ObjectCache;

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
public class WitkeyEvaluationCache extends ObjectCache{
    public WitkeyEvaluationCache() {
        super();
    }

    public WitkeyEvaluationCache(WitkeyEvaluationDb witkeyEvaluationDb) {
        super(witkeyEvaluationDb);
    }
}
