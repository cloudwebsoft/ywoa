package com.redmoon.oa.address;

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
public class AddressCache extends ObjectCache {
    public AddressCache() {
    
    }

    public AddressCache(AddressDb addr) {
        super(addr);
    }
}
