package com.cloudweb.oa.base;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.web.Global;
import com.cloudweb.oa.dcs.DistributedLock;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Slf4j
public abstract class ObjCache {

    @Lazy
    @Autowired
    protected DistributedLock distributedLock;

    protected String group;

    public ObjCache() {
        init();
    }

    public void init() {
        group = this.getClass().getName();
    }

    public abstract Lock getLock();

    public abstract String getPrimaryKey(Object obj);

    public abstract Object getEmptyObjWithPrimaryKey(String value);

    public abstract Object getObjRaw(String key);

    public Object getObj(ObjCache objCache, String key) {
        Object obj = null;
        try {
            obj = RMCache.getInstance().getFromGroup(key, group);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        if (null != obj) {
            // 防穿透
            if (ConstUtil.CACHE_NONE.equals(objCache.getPrimaryKey(obj))) {
                return null;
            }
            else {
                return obj;
            }
        }

        boolean isLocked = false;
        boolean isNotExist = false;
        String indentifier = "";
        // 双重检测锁可能会导致排队，性能有所损失，防止高并发时因缓存并发而导致穿透，但不适用于集群环境，集群时需使用分布式锁
        try {
            if (Global.getInstance().isUseRedis()) {
                indentifier = distributedLock.lock(getClass().getName(), 5000, 1000);
                obj = RMCache.getInstance().getFromGroup(key, group);
                if (null == obj) {
                    obj = objCache.getObjRaw(key);
                    if (null == obj) {
                        // throw new RuntimeException("This data could not be empty. code=" + code);
                        // 将代表null值的menu置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                        log.info("getObj key=" + key);
                        isNotExist = true;
                        obj = objCache.getEmptyObjWithPrimaryKey(ConstUtil.CACHE_NONE);
                        RMCache.getInstance().putInGroup(key, group, obj, ConstUtil.CACHE_NONE_EXPIRE);
                    } else {
                        RMCache.getInstance().putInGroup(key, group, obj);
                    }
                }
                else {
                    if (ConstUtil.CACHE_NONE.equals(objCache.getPrimaryKey(obj))) {
                        isNotExist = true;
                    }
                }
            }
            else {
                if (objCache.getLock().tryLock()) {
                    isLocked = true;
                    obj = RMCache.getInstance().getFromGroup(key, group);
                    if (null == obj) {
                        obj = objCache.getObjRaw(key);
                        if (null == obj) {
                            // throw new RuntimeException("This data could not be empty. code=" + code);
                            // 将代表null值的menu置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                            isNotExist = true;
                            obj = objCache.getEmptyObjWithPrimaryKey(ConstUtil.CACHE_NONE);
                            RMCache.getInstance().putInGroup(key, group, obj, ConstUtil.CACHE_NONE_EXPIRE);
                        } else {
                            RMCache.getInstance().putInGroup(key, group, obj);
                        }
                    }
                    else {
                        if (ConstUtil.CACHE_NONE.equals(objCache.getPrimaryKey(obj))) {
                            isNotExist = true;
                        }
                    }
                } else {
                    TimeUnit.MILLISECONDS.sleep(200);
                    obj = getObj(objCache, key);
                }
            }
        } catch (InterruptedException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (CacheException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (Global.getInstance().isUseRedis()) {
                distributedLock.unlock(getClass().getName(), indentifier);
            }
            else {
                if (isLocked) {
                    objCache.getLock().unlock();
                }
            }
        }
        if (isNotExist) {
            return null;
        }
        else {
            return obj;
        }
    }

    public String getGroup() {
        return group;
    }

    public void refreshCreate() {
        refreshAll();
    }

    public void refreshAll() {
        try {
            RMCache.getInstance().invalidateGroup(group);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void removeFromCache(String key) {
        try {
            RMCache.getInstance().remove(key, group);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void refreshSave(String key) {
        removeFromCache(key);
    }

    public void refreshDel(String key) {
        try {
            refreshAll();
        }
        catch (Exception e) {
            log.error("refreshDel:" + e.getMessage());
        }
    }
}