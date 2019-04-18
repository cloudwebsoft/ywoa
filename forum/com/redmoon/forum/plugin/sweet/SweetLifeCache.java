package com.redmoon.forum.plugin.sweet;

import cn.js.fan.base.ObjectCache;

public class SweetLifeCache extends ObjectCache {
    public SweetLifeCache(SweetLifeDb sweetLifeDb) {
        super(sweetLifeDb);
    }

    public void setGroup() {
        group = "SWEETLIFE_";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "SWEETLIFE_COUNT_";
    }

}
