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
public class SoftwareFileCache extends ObjectCache {
    public SoftwareFileCache() {
        listCachable = false;
        objCachable = false;
    }

    public SoftwareFileCache(SoftwareFileDb isf) {
        super(isf);
        listCachable = false;
        objCachable = false;
    }
}
