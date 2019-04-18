package cn.js.fan.cache.jcs;

import cn.js.fan.cache.CacheFactory;
import cn.js.fan.web.Global;
import com.redmoon.oa.Config;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Vector;

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
public class RMCache implements ICache {
    private static ICache iCache;
    private static RMCache instance;
    boolean canCache = true;

    private static Object initLock = new Object();

    String cacheName = "RMCache";
    transient Logger logger;
    Vector cacheMgrs = new Vector();

    public RMCache() {
        logger = Logger.getLogger(RMCache.class.getName());
        canCache = Global.useCache;
        // 判断是否启用redis缓存
        com.redmoon.oa.Config config = new com.redmoon.oa.Config();
        boolean isUserRedis = config.getBooleanProperty("isUseRedis");
        if (canCache) {
            if (isUserRedis){
                iCache = CacheFactory.getInstance(CacheFactory.CACHE_REDIS);
            }else {
                iCache = CacheFactory.getInstance(CacheFactory.CACHE_JCS);
            }
        }
    }

    public static RMCache getInstance() {
        if (instance == null) {
            synchronized(initLock) {
                if (instance == null)
                    instance = new RMCache();
            }
        }

        return instance;
    }

    public Object get(Object name) {
        if (!canCache)
            return null;
        Object obj = iCache.get(name);
        //logger.info("RMCache get: " + name + " = " + obj);
        return obj;
    }

    public void put(Object name, Object obj) throws CacheException {
        if (!canCache)
            return;
        iCache.put(name, obj);
    }

    public void remove(Object name) throws CacheException {
        if (!canCache)
            return;
        iCache.remove(name);
    }

    public void putInGroup(Object name, String groupName, Object value) throws CacheException {
        if (!canCache)
            return;
        iCache.putInGroup(name, groupName, value);
    }

    public void remove(Object name, String groupName) throws CacheException {
        if (!canCache)
            return;
        iCache.remove(name, groupName);
    }

    public void invalidateGroup(String groupName) throws CacheException {
        if (!canCache)
            return;
        iCache.invalidateGroup(groupName);
    }

    public Object getFromGroup(Object name, String group) throws CacheException {
        if (!canCache)
            return null;
        Object obj = iCache.getFromGroup(name, group);

        //logger.info("getFromGroup: " + obj);
        return obj;
    }

    public void setCanCache(boolean b) {
        this.canCache = b;
    }

    public boolean getCanCache() {
        return this.canCache;
    }

    public void clear() throws CacheException {
        if (canCache)
            iCache.clear();
    }

    /**
     * refresh CacheMgr already registed
     */
    public void timer() {
       Iterator ir = cacheMgrs.iterator();
       while (ir.hasNext()) {
           ICacheMgr icm = (ICacheMgr) ir.next();
           icm.timer();
       }
    }

    public synchronized void regist(ICacheMgr icm) {
        cacheMgrs.addElement(icm);
    }

    public Vector getCacheMgrs() {
        return cacheMgrs;
    }


}
