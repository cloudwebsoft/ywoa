package com.redmoon.forum.plugin.witkey;

import cn.js.fan.base.ObjectCache;

public class WitkeyCache extends ObjectCache {
    public WitkeyCache() {
        super();
    }

    public WitkeyCache(WitkeyDb witkeyDb) {
        super(witkeyDb);
    }

}
