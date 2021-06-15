package com.redmoon.forum.plugin.sweet;

import cn.js.fan.base.ObjectCache;

public class SweetUserCache extends ObjectCache {
    public SweetUserCache(SweetUserDb sweetUserDb) {
        super(sweetUserDb);
    }

    public void setGroup() {
        group = "SWEETUSER_";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "SWEETUSER_COUNT_";
    }
}
