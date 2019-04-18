package com.redmoon.forum.plugin.refer;

import cn.js.fan.base.ObjectCache;

public class ReferCache extends ObjectCache {
    public ReferCache() {
        super();
    }

    public ReferCache(ReferDb referDb) {
        super(referDb);
    }

}
