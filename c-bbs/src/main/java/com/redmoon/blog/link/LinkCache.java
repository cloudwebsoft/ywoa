package com.redmoon.blog.link;

import cn.js.fan.base.*;

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
public class LinkCache extends ObjectCache {
    public LinkCache() {
        super();
    }

    public LinkCache(LinkDb ld) {
        super(ld);
    }

    /**
     * setGroup
     *
     * @todo Implement this cn.js.fan.base.ObjectCache method
     */
    public void setGroup() {
        group = "LINK";
    }

    /**
     * setGroupCount
     *
     * @todo Implement this cn.js.fan.base.ObjectCache method
     */
    public void setGroupCount() {
        this.COUNT_GROUP_NAME = "LINKCOUNT";
    }
}
