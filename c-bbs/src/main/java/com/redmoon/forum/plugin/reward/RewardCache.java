package com.redmoon.forum.plugin.reward;

import cn.js.fan.base.ObjectCache;

public class RewardCache extends ObjectCache {
    public RewardCache() {
        super();
    }

    public RewardCache(RewardDb rewardDb) {
        super(rewardDb);
    }

}
