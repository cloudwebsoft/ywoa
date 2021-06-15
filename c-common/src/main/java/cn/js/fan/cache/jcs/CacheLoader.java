package cn.js.fan.cache.jcs;

import org.apache.log4j.Logger;

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
    private Logger logger;

    public CacheLoader(String cacheName) {
        logger = Logger.getLogger("CacheLoader");
    }

    public ICache getInstance(String cacheName) {
        ICache obj = null;
        try {
            Class newClass = Class.forName(cacheName);
            obj = (ICache)newClass.newInstance();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return obj;
    }
}
