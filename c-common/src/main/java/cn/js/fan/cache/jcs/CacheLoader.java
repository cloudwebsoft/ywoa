package cn.js.fan.cache.jcs;

import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class CacheLoader {

    public CacheLoader(String cacheName) {
        
    }

    public ICache getInstance(String cacheName) {
        ICache obj = null;
        try {
            Class newClass = Class.forName(cacheName);
            obj = (ICache)newClass.newInstance();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return obj;
    }
}
