package com.redmoon.forum.plugin.flower;

import cn.js.fan.base.ObjectCache;

public class FlowerCache extends ObjectCache {
    public FlowerCache() {
        super();
    }

    public FlowerCache(FlowerDb rewardDb) {
        super(rewardDb);
    }

}
