package com.redmoon.oa.exam;
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
public class QuestionCache extends ObjectCache{
    public QuestionCache() {
        super();
    }
    public QuestionCache(QuestionDb id) {
        super(id);
    }
}
