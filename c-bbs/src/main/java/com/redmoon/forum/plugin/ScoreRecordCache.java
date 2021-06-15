package com.redmoon.forum.plugin;

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
public class ScoreRecordCache extends ObjectCache{
    public ScoreRecordCache() {
    }

    public ScoreRecordCache(ScoreRecordDb srd) {
         super(srd);
    }
}
