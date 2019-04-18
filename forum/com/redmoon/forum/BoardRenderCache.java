package com.redmoon.forum;

import cn.js.fan.base.ObjectCache;

public class BoardRenderCache extends ObjectCache {
    public BoardRenderCache(BoardRenderDb boardRenderDb) {
        super(boardRenderDb);
    }

    public void setGroup() {
        group = "BOARDRENDER_";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "BOARDRENDER_COUNT_";
    }
}
