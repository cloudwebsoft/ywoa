package com.cloudweb.oa.base;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.web.Global;
import com.cloudweb.oa.dcs.DistributedLock;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Slf4j
public abstract class AbstractFlowFormDaoCache {

    @Lazy
    @Autowired
    protected DistributedLock distributedLock;

    public AbstractFlowFormDaoCache() {
        init();
    }

    public void init() {
    }

    public String getGroup(String formCode) {
        return AbstractFlowFormDaoCache.class.getName() + "_" + formCode;
    }

    public abstract Lock getLock();

    public abstract String getEmptyFlag(IFormDAO obj);

    public abstract IFormDAO getEmptyFormDao(int flowId, String formCode, String emptyFlag);

    public abstract IFormDAO getFormDaoRaw(int flowId, String formCode);

    public IFormDAO getFormDao(AbstractFlowFormDaoCache objCache, int flowId, String formCode) {
        String key = getKey(flowId, formCode);
        String group = getGroup(formCode);
        IFormDAO obj = null;
        try {
            obj = (IFormDAO)RMCache.getInstance().getFromGroup(key, group);
        } catch (Exception e) {
            log.error(e.getMessage());
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
                    obj = objCache.getFormDaoRaw(flowId, formCode);
                    if (!obj.isLoaded()) {
                        // 防穿透，将代表null值的对象置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                        log.info("getObj key=" + key);
                        isNotExist = true;
                        obj = objCache.getEmptyFormDao(flowId, formCode, ConstUtil.CACHE_NONE);
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
                        obj = objCache.getFormDaoRaw(flowId, formCode);
                        if (!obj.isLoaded()) {
                            // 防穿透，将代表null值的对象置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                            isNotExist = true;
                            obj = objCache.getEmptyFormDao(flowId, formCode, ConstUtil.CACHE_NONE);
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
                    obj = getFormDao(objCache, flowId, formCode);
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
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void removeFromCache(IFormDAO iFormDAO) {
        removeFromCache(iFormDAO.getFlowId(), iFormDAO.getFormCode());
    }

    public static String getKey(int flowId, String formCode) {
        return flowId + "|" + formCode;
    }

    public void removeFromCache(int flowId, String formCode) {
        try {
            RMCache.getInstance().remove(getKey(flowId, formCode), getGroup(formCode));
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