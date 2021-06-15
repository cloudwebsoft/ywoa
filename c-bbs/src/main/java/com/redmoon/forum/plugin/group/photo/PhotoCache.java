package com.redmoon.forum.plugin.group.photo;

import com.cloudwebsoft.framework.base.*;

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
public class PhotoCache extends ObjectCache {
    public PhotoCache() {
        super();
    }

    public PhotoCache(PhotoDb ld) {
        super(ld);
    }
}
