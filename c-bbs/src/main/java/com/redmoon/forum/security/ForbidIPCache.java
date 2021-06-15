package com.redmoon.forum.security;

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
public class ForbidIPCache extends ObjectCache {
    public ForbidIPCache(ForbidIPDb fid) {
        super(fid);
    }
}
