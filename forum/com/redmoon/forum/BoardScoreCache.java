package com.redmoon.forum;

import cn.js.fan.base.ObjectCache;

public class BoardScoreCache extends ObjectCache {
    public BoardScoreCache(BoardScoreDb boardScoreDb) {
        super(boardScoreDb);
    }

    public void setGroup() {
        group = "BOARDSCORE_";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "BOARDSCORE_COUNT_";
    }
}
