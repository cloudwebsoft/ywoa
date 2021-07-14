package com.redmoon.oa.fileark;

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
public class DocAttachmentLogCache extends ObjectCache {
    public DocAttachmentLogCache() {
    }

    public DocAttachmentLogCache(DocAttachmentLogDb dald) {
        super(dald);
    }
}
