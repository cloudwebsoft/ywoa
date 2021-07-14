package cn.js.fan.module.cms;

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
public class ImgStoreFileCache extends ObjectCache {
    public ImgStoreFileCache() {
        listCachable = false;
        objCachable = false;
    }

    public ImgStoreFileCache(ImgStoreFileDb isf) {
        super(isf);
        listCachable = false;
        objCachable = false;
    }
}
