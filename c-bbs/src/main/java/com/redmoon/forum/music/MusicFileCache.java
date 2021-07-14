package com.redmoon.forum.music;

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
public class MusicFileCache extends ObjectCache {
    public MusicFileCache() {
        listCachable = true;
        objCachable = true;
    }

    public MusicFileCache(MusicFileDb isf) {
        super(isf);
        listCachable = true;
        objCachable = true;
    }
}
