package com.redmoon.oa.account;

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
public class AccountCache extends ObjectCache {
    public AccountCache() {
    }

    public AccountCache(AccountDb ad) {
     super(ad);
    }
}
