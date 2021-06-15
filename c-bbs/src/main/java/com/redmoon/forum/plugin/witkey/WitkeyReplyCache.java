package com.redmoon.forum.plugin.witkey;

import cn.js.fan.base.ObjectCache;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WitkeyReplyCache extends ObjectCache{
    public WitkeyReplyCache() {
        super();
    }

    public WitkeyReplyCache(WitkeyReplyDb witkeyReplyDb) {
        super(witkeyReplyDb);
    }

}
