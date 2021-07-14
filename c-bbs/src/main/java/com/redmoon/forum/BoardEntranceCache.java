package com.redmoon.forum;

import cn.js.fan.base.ObjectCache;

public class BoardEntranceCache extends ObjectCache {
    public BoardEntranceCache(BoardEntranceDb boardEntranceDb) {
        super(boardEntranceDb);
    }

    public void setGroup() {
        group = "BOARDENTRANCE_";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "BOARDENTRANCE_COUNT_";
    }
}
