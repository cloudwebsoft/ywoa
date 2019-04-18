package com.redmoon.oa.emailpop3;

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
public class EmailPop3Cache extends ObjectCache {
    public EmailPop3Cache() {
    }

    public EmailPop3Cache(EmailPop3Db epd) {
        super(epd);
    }
}
