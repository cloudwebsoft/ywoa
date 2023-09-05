package cn.js.fan.cache.jcs;

import cn.js.fan.cache.CacheFactory;
import cn.js.fan.web.Global;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import com.cloudwebsoft.framework.base.IThreadContext;
import org.apache.commons.jcs3.access.exception.CacheException;

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

    /**
     * 默认缓存2小时
     */
    private final static int DEFAULT_EXPIRE_SECOND = 7200;
    private final static int RANDOM_MAX_SECOND = 3600;

    boolean canCache;

    private static Object initLock = new Object();

    Vector cacheMgrs = new Vector();

    public RMCache() {
        canCache = Global.useCache;
        // 判断是否启用redis缓存
        boolean isUserRedis = Global.getInstance().isUseRedis();
        if (canCache) {
            if (isUserRedis) {
                SysProperties sysProperties = SpringUtil.getBean(SysProperties.class);
                if (sysProperties.isRedisCluster()) {
                    iCache = CacheFactory.getInstance(CacheFactory.CACHE_REDIS_CLUSTER);
                } else {
                    iCache = CacheFactory.getInstance(CacheFactory.CACHE_REDIS);
                }
            } else {
                iCache = CacheFactory.getInstance(CacheFactory.CACHE_JCS);
            }
        }
    }

    public static RMCache getInstance() {
        if (instance == null) {
            synchronized (initLock) {
                if (instance == null) {
                    instance = new RMCache();
                }
            }
        }

        return instance;
    }

    public static void refresh() {
        instance = null;
    }

    @Override
    public Object get(Object name) {
        if (!canCache) {
            return null;
        }
        return iCache.get(name);
    }

    @Override
    public void put(Object name, Object obj) throws CacheException {
        if (!canCache) {
            return;
        }
        // 加入默认随机超时时间，以防雪崩
        iCache.put(name, obj, DEFAULT_EXPIRE_SECOND + random(RANDOM_MAX_SECOND));

        // 为在流程事务中回退时刷新不同步的缓存，以免数据库中回退了，但是缓存却是更新后的状态
        IThreadContext iThreadContext = SpringUtil.getBean(IThreadContext.class);
        iThreadContext.addCacheKey(name);
    }

    @Override
    public void put(Object name, Object obj, int expireSeconds) throws CacheException {
        if (!canCache) {
            return;
        }
        iCache.put(name, obj, expireSeconds);

        IThreadContext iThreadContext = SpringUtil.getBean(IThreadContext.class);
        iThreadContext.addCacheKey(name);
    }

    @Override
    public void putInGroup(Object name, String groupName, Object value) throws CacheException {
        if (!canCache) {
            return;
        }
        iCache.putInGroup(name, groupName, value, DEFAULT_EXPIRE_SECOND + random(RANDOM_MAX_SECOND));

        IThreadContext iThreadContext = SpringUtil.getBean(IThreadContext.class);
        iThreadContext.addCacheKey(name, groupName);
    }

    @Override
    public void putInGroup(Object name, String groupName, Object value, int expireSeconds) throws CacheException {
        if (!canCache) {
            return;
        }
        iCache.putInGroup(name, groupName, value, expireSeconds);

        IThreadContext iThreadContext = SpringUtil.getBean(IThreadContext.class);
        iThreadContext.addCacheKey(name, groupName);
    }

    @Override
    public void remove(Object name) throws CacheException {
        if (!canCache) {
            return;
        }
        iCache.remove(name);
    }

    @Override
    public void remove(Object name, String groupName) throws CacheException {
        if (!canCache) {
            return;
        }
        iCache.remove(name, groupName);
    }

    @Override
    public void invalidateGroup(String groupName) throws CacheException {
        if (!canCache) {
            return;
        }
        iCache.invalidateGroup(groupName);
    }

    @Override
    public Object getFromGroup(Object name, String group) throws CacheException {
        if (!canCache) {
            return null;
        }
        return iCache.getFromGroup(name, group);
    }

    public void setCanCache(boolean b) {
        this.canCache = b;
    }

    public boolean getCanCache() {
        return this.canCache;
    }

    @Override
    public void clear() throws CacheException {
        if (canCache) {
            iCache.clear();
        }
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

    public int random(int maxNum) {
        double result = Math.random() * maxNum;
        return (int) result;
    }

}
