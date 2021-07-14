package com.redmoon.forum.plugin.debate;

import cn.js.fan.base.ObjectCache;

public class DebateCache extends ObjectCache {
    public DebateCache() {
        super();
    }

    public DebateCache(DebateDb rewardDb) {
        super(rewardDb);
    }

}
