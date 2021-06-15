package com.redmoon.forum.plugin.present;

import cn.js.fan.base.ObjectCache;

public class PresentCache extends ObjectCache {
    public PresentCache() {
        super();
    }

    public PresentCache(PresentDb rewardDb) {
        super(rewardDb);
    }

}
