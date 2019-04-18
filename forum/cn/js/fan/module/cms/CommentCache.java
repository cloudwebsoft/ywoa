package cn.js.fan.module.cms;

import com.cloudwebsoft.framework.base.ObjectCache;

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
public class CommentCache extends ObjectCache {
    public CommentCache() {
    }

    public CommentCache(CommentDb cd) {
        super(cd);
    }

}
