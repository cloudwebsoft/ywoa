package com.cloudweb.oa.cache;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.web.Global;
import com.cloudweb.oa.base.ObjCache;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.service.IDepartmentService;
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
public class DepartmentCache extends ObjCache {

    @Autowired
    IDepartmentService departmentService;

    private final String cachePrix = "ch_";

    private static final Lock lock = new ReentrantLock();

    public DepartmentCache() {
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public String getPrimaryKey(Object obj) {
        Department department = (Department)obj;
        return department.getCode();
    }

    @Override
    public Object getEmptyObjWithPrimaryKey(String value) {
        Department department = new Department();
        department.setCode(value);
        return department;
    }

    @Override
    public Object getObjRaw(String key) {
        return departmentService.getDepartment(key);
    }

    public void removeAllFromCache() {
        try {
            RMCache.getInstance().invalidateGroup(group);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void refreshChildren(String parentCode) {
        try {
            RMCache.getInstance().remove(cachePrix + parentCode, group);
        } catch (CacheException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    public List<Department> getChildren(String parentCode) {
        List<Department> list = null;
        boolean isNotExist = false;
        try {
            list = (List) RMCache.getInstance().getFromGroup(cachePrix + parentCode, group);
            if (null != list) {
                // 防穿透
                if (ConstUtil.CACHE_NONE.equals(list.get(0).getCode())) {
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
                    list = (List) RMCache.getInstance().getFromGroup(cachePrix + parentCode, group);
                    if (null == list) {
                        list = departmentService.getChildren(parentCode);
                        if (list.size()==0) {
                            isNotExist = true;
                            // throw new RuntimeException("This data could not be empty. code=" + code);
                            // 将代表null值的menu置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                            Department department = new Department();
                            department.setCode(ConstUtil.CACHE_NONE);
                            list.add(department);
                            RMCache.getInstance().putInGroup(cachePrix + parentCode, group, list, ConstUtil.CACHE_NONE_EXPIRE);
                        } else {
                            RMCache.getInstance().putInGroup(cachePrix + parentCode, group, list);
                        }
                    }
                    else {
                        if (ConstUtil.CACHE_NONE.equals(list.get(0).getCode())) {
                            isNotExist = true;
                        }
                    }
                }
                else {
                    if (lock.tryLock()) {
                        isLocked = true;
                        list = (List) RMCache.getInstance().getFromGroup(cachePrix + parentCode, group);
                        if (null == list) {
                            list = departmentService.getChildren(parentCode);
                            if (list.size()==0) {
                                isNotExist = true;
                                // throw new RuntimeException("This data could not be empty. code=" + code);
                                // 将代表null值的menu置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                                Department department = new Department();
                                department.setCode(ConstUtil.CACHE_NONE);
                                list.add(department);
                                RMCache.getInstance().putInGroup(cachePrix + parentCode, group, list, ConstUtil.CACHE_NONE_EXPIRE);
                            } else {
                                RMCache.getInstance().putInGroup(cachePrix + parentCode, group, list);
                            }
                        }
                        else {
                            if (ConstUtil.CACHE_NONE.equals(list.get(0).getCode())) {
                                isNotExist = true;
                            }
                        }
                    } else {
                        TimeUnit.MILLISECONDS.sleep(200);
                        list = getChildren(parentCode);
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

    public Department getDepartment(String code) {
        if (code == null) {
            return null;
        }
        Department department = null;
        try {
            department = (Department) RMCache.getInstance().getFromGroup(code, group);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if (department != null) {
            return department;
        }

        department = departmentService.getDepartment(code);
        if (department!=null) {
            try {
                RMCache.getInstance().putInGroup(code, group, department);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return department;
    }
}