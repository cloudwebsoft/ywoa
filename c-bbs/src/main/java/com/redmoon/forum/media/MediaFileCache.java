package com.redmoon.forum.media;

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
public class MediaFileCache extends ObjectCache {
    public MediaFileCache() {
        listCachable = false;
        objCachable = false;
    }

    public MediaFileCache(MediaFileDb isf) {
        super(isf);
        listCachable = false;
        objCachable = false;
    }
}
