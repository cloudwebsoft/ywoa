package com.redmoon.forum.person;

import cn.js.fan.base.ObjectCache;

public class MasterCache extends ObjectCache {
    public MasterCache(MasterDb md) {
        super(md);
    }

    public void setGroup() {
         group = "MASTER_";
     }

     public void setGroupCount() {
         COUNT_GROUP_NAME = "MASTER_COUNT_";
    }
}
