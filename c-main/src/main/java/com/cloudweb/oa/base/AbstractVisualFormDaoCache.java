package com.cloudweb.oa.base;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.web.Global;
import com.cloudweb.oa.dcs.DistributedLock;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.sys.DebugUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Slf4j
public abstract class AbstractVisualFormDaoCache {

    @Lazy
    @Autowired
    protected DistributedLock distributedLock;

    public AbstractVisualFormDaoCache() {
        init();
    }

    public void init() {
    }

    public String getGroup(String formCode) {
        return AbstractVisualFormDaoCache.class.getName() + "_" + formCode;
    }

    public abstract Lock getLock();

    public abstract String getEmptyFlag(IFormDAO obj);

    public abstract IFormDAO getEmptyFormDao(String formCode, long id, String emptyFlag);

    public abstract IFormDAO getFormDaoRaw(String formCode, long id);

    public IFormDAO getFormDao(AbstractVisualFormDaoCache objCache, String formCode, long id) {
        long t = System.currentTimeMillis();
        String key = getKey(formCode, id);
        String group = getGroup(formCode);
        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "getFormDao", "formCode=" + formCode + " id=" + id + " t1=" + (double) (System.currentTimeMillis() - t) / 1000);
        }
        IFormDAO obj = null;
        try {
            obj = (IFormDAO)RMCache.getInstance().getFromGroup(key, group);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "getFormDao", " loaded=" + (null != obj) + " t2=" + (double)(System.currentTimeMillis() - t)/1000);
        }

        if (null != obj) {
            // 防穿透
            if (ConstUtil.CACHE_NONE.equals(objCache.getEmptyFlag(obj))) {
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
            if (Global.isCluster() && Global.getInstance().isUseRedis()) {
                indentifier = distributedLock.lock(getClass().getName(), 5000, 1000);
                obj = (IFormDAO) RMCache.getInstance().getFromGroup(key, group);
                if (null == obj) {
                    obj = objCache.getFormDaoRaw(formCode, id);
                    if (!obj.isLoaded()) {
                        // 防穿透，将代表null值的对象置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                        log.info("getObj key=" + key);
                        isNotExist = true;
                        obj = objCache.getEmptyFormDao(formCode, id, ConstUtil.CACHE_NONE);
                        RMCache.getInstance().putInGroup(key, group, obj, ConstUtil.CACHE_NONE_EXPIRE);
                    } else {
                        RMCache.getInstance().putInGroup(key, group, obj);
                    }
                }
                else {
                    if (ConstUtil.CACHE_NONE.equals(objCache.getEmptyFlag(obj))) {
                        isNotExist = true;
                    }
                }
            }
            else {
                if (objCache.getLock().tryLock()) {
                    isLocked = true;
                    obj = (IFormDAO) RMCache.getInstance().getFromGroup(key, group);
                    if (null == obj) {
                        obj = objCache.getFormDaoRaw(formCode, id);
                        if (!obj.isLoaded()) {
                            // 防穿透，将代表null值的对象置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                            isNotExist = true;
                            obj = objCache.getEmptyFormDao(formCode, id, ConstUtil.CACHE_NONE);
                            RMCache.getInstance().putInGroup(key, group, obj, ConstUtil.CACHE_NONE_EXPIRE);
                        } else {
                            RMCache.getInstance().putInGroup(key, group, obj);
                        }
                    }
                    else {
                        if (ConstUtil.CACHE_NONE.equals(objCache.getEmptyFlag(obj))) {
                            isNotExist = true;
                        }
                    }
                } else {
                    TimeUnit.MILLISECONDS.sleep(200);
                    obj = getFormDao(objCache, formCode, id);
                }
            }
        } catch (InterruptedException | CacheException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (Global.isCluster() && Global.getInstance().isUseRedis()) {
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

    public void refreshCreate(String formCode) {
        refreshAll(formCode);
    }

    public void refreshAll(String formCode) {
        try {
            RMCache.getInstance().invalidateGroup(getGroup(formCode));
        }
        catch (CacheException e) {
            log.error(e.getMessage());
        }
    }

    public void removeFromCache(IFormDAO iFormDAO) {
        removeFromCache(iFormDAO.getFormCode(), iFormDAO.getId());
    }

    public static String getKey(String formCode, long id) {
        // LogUtil.getLog(AbstractVisualFormDaoCache.class.getName()).info("getKey=" + formCode + "|" + id + " group=" + getGroup(formCode));
        return formCode + "|" + id;
    }

    public void removeFromCache(String formCode, long id) {
        try {
            RMCache.getInstance().remove(getKey(formCode, id), getGroup(formCode));
        } catch (CacheException e) {
            log.error(e.getMessage());
        }
    }

    public void refreshSave(IFormDAO iFormDAO) {
        removeFromCache(iFormDAO);
    }

    public void refreshDel(IFormDAO iFormDAO) {
        removeFromCache(iFormDAO);
    }
}