package com.redmoon.forum.plugin.activity;

import cn.js.fan.base.ObjectCache;

public class ActivityCache extends ObjectCache {
    public ActivityCache() {
        super();
    }

    public ActivityCache(ActivityDb rewardDb) {
        super(rewardDb);
    }

}
