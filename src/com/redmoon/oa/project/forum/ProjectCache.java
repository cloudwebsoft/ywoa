package com.redmoon.oa.project.forum;

import cn.js.fan.base.ObjectCache;

public class ProjectCache extends ObjectCache {
    public ProjectCache() {
        super();
    }

    public ProjectCache(ProjectDb projectDb) {
        super(projectDb);
    }

}
