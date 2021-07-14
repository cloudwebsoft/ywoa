package com.redmoon.forum.plugin.entrance;

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
public class VIPCardCache extends ObjectCache {
    public VIPCardCache() {
        super();
    }

    public VIPCardCache(VIPCardDb vtu) {
        super(vtu);
    }
}
