package com.redmoon.forum.person;

import com.cloudwebsoft.framework.base.ObjectCache;

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
public class UserGroupCache extends ObjectCache {
    public UserGroupCache() {
    }

    public UserGroupCache(UserGroupDb ugd) {
        super(ugd);
    }
}
