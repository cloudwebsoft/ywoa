package com.redmoon.forum.plugin.witkey;

import cn.js.fan.base.ObjectCache;

public class WitkeyUserCache extends ObjectCache {
    public WitkeyUserCache() {
        super();
    }

    public WitkeyUserCache(WitkeyUserDb witkeyUserDb) {
        super(witkeyUserDb);
    }

}
