package com.redmoon.forum.plugin.sweet;

import cn.js.fan.base.ObjectCache;

public class SweetCache extends ObjectCache {
    public SweetCache(SweetDb sweetDb) {
        super(sweetDb);
    }

    public void setGroup() {
        group = "SWEET_";
    }

    public SweetDb getSweetDb(String name) {
        SweetDb sd = null;
        try {
            sd = (SweetDb)rmCache.getFromGroup(name, group);
        }
        catch(Exception e) {
            logger.error(e.getMessage());
        }
        if (sd==null) {
            sd = new SweetDb(name);
            if (sd.isLoaded()) {
                try {
                    rmCache.putInGroup(name, group, sd);
                }
                catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return sd;
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "SWEET_COUNT_";
    }
}
