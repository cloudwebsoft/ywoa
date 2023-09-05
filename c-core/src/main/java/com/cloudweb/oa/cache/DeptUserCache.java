package com.cloudweb.oa.cache;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.web.Global;
import com.cloudweb.oa.base.ObjCache;
import com.cloudweb.oa.entity.DeptUser;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.service.IUserAuthorityService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class DeptUserCache extends ObjCache {

    @Autowired
    IDeptUserService deptUserService;

    private final String PREFIX_USER = "dept_user_";

    private static final Lock lock = new ReentrantLock();

    public void refreshDeptUser(String userName) {
        try {
            RMCache.getInstance().remove(PREFIX_USER + userName, group);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("refreshUserDeptUser:" + e.getMessage());
        }
    }

    public List<DeptUser> listByUserName(String userName) {
        List<DeptUser> list = null;
        boolean isNotExist = false;
        try {
            list = (List) RMCache.getInstance().getFromGroup(PREFIX_USER + userName, group);
            if (null != list) {
                // 防穿透
                if (ConstUtil.CACHE_NONE.equals(list.get(0).getUserName())) {
                    return new ArrayList<>();
                }
                else {
                    return list;
                }
            }

            boolean isLocked = false;
            String indentifier = "";
            // 双重检测锁可能会导致排队，性能有所损失，防止高并发时因缓存并发而导致穿透，但不适用于集群环境，集群时需使用分布式锁
            try {
                if (Global.isCluster() && Global.getInstance().isUseRedis()) {
                    indentifier = distributedLock.lock(getClass().getName(), 5000, 1000);
                    list = (List) RMCache.getInstance().getFromGroup(PREFIX_USER + userName, group);
                    if (null == list) {
                        list = deptUserService.listByUserName(userName);
                        if (list.size() == 0) {
                            isNotExist = true;
                            // throw new RuntimeException("This data could not be empty. code=" + code);
                            // 将代表null值的menu置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                            DeptUser deptUser = new DeptUser();
                            deptUser.setUserName(ConstUtil.CACHE_NONE);
                            list.add(deptUser);
                            RMCache.getInstance().putInGroup(PREFIX_USER + userName, group, list, ConstUtil.CACHE_NONE_EXPIRE);
                        } else {
                            RMCache.getInstance().putInGroup(PREFIX_USER + userName, group, list);
                        }
                    }
                    else {
                        if (ConstUtil.CACHE_NONE.equals(list.get(0).getUserName())) {
                            isNotExist = true;
                        }
                    }
                }
                else {
                    if (lock.tryLock()) {
                        isLocked = true;
                        list = (List) RMCache.getInstance().getFromGroup(PREFIX_USER + userName, group);
                        if (null == list) {
                            list = deptUserService.listByUserName(userName);
                            if (list.size() == 0) {
                                isNotExist = true;
                                // throw new RuntimeException("This data could not be empty. code=" + code);
                                // 将代表null值的menu置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                                DeptUser deptUser = new DeptUser();
                                deptUser.setUserName(ConstUtil.CACHE_NONE);
                                list.add(deptUser);
                                RMCache.getInstance().putInGroup(PREFIX_USER + userName, group, list, ConstUtil.CACHE_NONE_EXPIRE);
                            } else {
                                RMCache.getInstance().putInGroup(PREFIX_USER + userName, group, list);
                            }
                        }
                        else {
                            if (ConstUtil.CACHE_NONE.equals(list.get(0).getUserName())) {
                                isNotExist = true;
                            }
                        }
                    } else {
                        TimeUnit.MILLISECONDS.sleep(200);
                        list = listByUserName(userName);
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
                        lock.unlock();
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if (isNotExist) {
            return new ArrayList<>();
        }
        else {
            return list;
        }
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public String getPrimaryKey(Object obj) {
        return null;
    }

    @Override
    public Object getEmptyObjWithPrimaryKey(String value) {
        return null;
    }

    @Override
    public Object getObjRaw(String key) {
        return null;
    }
}
