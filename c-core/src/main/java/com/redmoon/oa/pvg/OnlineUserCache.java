package com.redmoon.oa.pvg;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.util.StrUtil;
import java.util.Calendar;

public class OnlineUserCache extends ObjectCache {
    public OnlineUserCache(OnlineUserDb ou) {
        super(ou);
    }

    public int getAllCount() {
        int count = getObjectCount(OnlineUserDb.ALLCOUNTSQL);
        return count;
    }

}
