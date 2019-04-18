package com.redmoon.forum.plugin.sweet;

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
public class SweetUserInfoCache extends ObjectCache {
    public SweetUserInfoCache() {
    }

    public SweetUserInfoCache(SweetUserInfoDb sui) {
        super(sui);
    }

    /**
     * setGroup
     *
     * @todo Implement this cn.js.fan.base.ObjectCache method
     */
    public void setGroup() {
        group = "SweetUserInfo";
    }

    /**
     * setGroupCount
     *
     * @todo Implement this cn.js.fan.base.ObjectCache method
     */
    public void setGroupCount() {
        this.COUNT_GROUP_NAME = "SweetUserInfoCount";
    }
}
