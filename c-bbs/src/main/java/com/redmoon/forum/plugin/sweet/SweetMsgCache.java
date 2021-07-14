package com.redmoon.forum.plugin.sweet;

import cn.js.fan.base.ObjectCache;

public class SweetMsgCache extends ObjectCache {
    public SweetMsgCache(SweetMsgDb sweetMsgDb) {
        super(sweetMsgDb);
    }

    public void setGroup() {
        group = "SWEETMSG_";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "SWEETMSG_COUNT_";
    }
}
