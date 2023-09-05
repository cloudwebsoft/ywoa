package com.cloudweb.oa.cache;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.web.Global;
import com.cloudweb.oa.base.ObjCache;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.RoleDept;
import com.cloudweb.oa.service.IRoleDeptService;
import com.cloudweb.oa.service.IRoleService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class RoleCache extends ObjCache {
    final String ADMINDEPTSPREFIX = "ROLE_ADMINDEPTS_";

    @Autowired
    IRoleService roleService;

    @Autowired
    IRoleDeptService roleDeptService;

    private static final Lock lock = new ReentrantLock();

    public Role getRole(String code) {
        return (Role)getObj(this, code);
    }

    public String[] getAdminDepts(String roleCode) {
        String[] depts = null;
        boolean isNotExist = false;
        try {
            long t = System.currentTimeMillis();
            depts = (String[]) RMCache.getInstance().getFromGroup(ADMINDEPTSPREFIX + roleCode, group);

            if (null != depts) {
                // 防穿透
                if (ConstUtil.CACHE_NONE.equals(depts[0])) {
                    return null;
                }
                else {
                    return depts;
                }
            }

            boolean isLocked = false;
            String indentifier = "";
            // 双重检测锁可能会导致排队，性能有所损失，防止高并发时因缓存并发而导致穿透，但不适用于集群环境，集群时需使用分布式锁
            try {
                if (Global.isCluster() && Global.getInstance().isUseRedis()) {
                    indentifier = distributedLock.lock(getClass().getName(), 5000, 1000);
                    depts = (String[]) RMCache.getInstance().getFromGroup(ADMINDEPTSPREFIX + roleCode, group);
                    if (null == depts) {
                        List<RoleDept> list = roleDeptService.listByRoleCode(roleCode);
                        if (list.size()>0) {
                            depts = new String[list.size()];
                            int i = 0;
                            for (RoleDept roleDept : list) {
                                depts[i] = roleDept.getDeptCode();
                                i++;
                            }
                        }

                        if (null == depts) {
                            isNotExist = true;
                            // throw new RuntimeException("This data could not be empty. code=" + code);
                            // 将代表null值的menu置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                            depts = new String[]{ConstUtil.CACHE_NONE};
                            RMCache.getInstance().putInGroup(ADMINDEPTSPREFIX + roleCode, group, depts, ConstUtil.CACHE_NONE_EXPIRE);
                        } else {
                            RMCache.getInstance().putInGroup(ADMINDEPTSPREFIX + roleCode, group, depts);
                        }
                    }
                    else {
                        if (ConstUtil.CACHE_NONE.equals(depts[0])) {
                            isNotExist = true;
                        }
                    }
                }
                else {
                    if (lock.tryLock()) {
                        isLocked = true;
                        depts = (String[]) RMCache.getInstance().getFromGroup(ADMINDEPTSPREFIX + roleCode, group);
                        if (null == depts) {
                            List<RoleDept> list = roleDeptService.listByRoleCode(roleCode);
                            if (list.size()>0) {
                                depts = new String[list.size()];
                                int i = 0;
                                for (RoleDept roleDept : list) {
                                    depts[i] = roleDept.getDeptCode();
                                    i++;
                                }
                            }

                            if (null == depts) {
                                isNotExist = true;
                                // throw new RuntimeException("This data could not be empty. code=" + code);
                                // 将代表null值的menu置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                                depts = new String[]{ConstUtil.CACHE_NONE};
                                RMCache.getInstance().putInGroup(ADMINDEPTSPREFIX + roleCode, group, depts, ConstUtil.CACHE_NONE_EXPIRE);
                            } else {
                                RMCache.getInstance().putInGroup(ADMINDEPTSPREFIX + roleCode, group, depts);
                            }
                        }
                        else {
                            if (ConstUtil.CACHE_NONE.equals(depts[0])) {
                                isNotExist = true;
                            }
                        }
                    } else {
                        TimeUnit.MILLISECONDS.sleep(200);
                        depts = getAdminDepts(roleCode);
                    }
                }
            } catch (InterruptedException e) {
                LogUtil.getLog(getClass()).error(e);
            } catch (CacheException e) {
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
            return null;
        }
        else {
            return depts;
        }
    }

    public void refreshAdminDepts(String roleCode) {
        try {
            RMCache.getInstance().remove(ADMINDEPTSPREFIX + roleCode, group);
        } catch (Exception e) {
            log.error("refreshAdminDepts:" + e.getMessage());
        }
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public String getPrimaryKey(Object obj) {
        Role role = (Role)obj;
        return role.getCode();
    }

    @Override
    public Object getEmptyObjWithPrimaryKey(String value) {
        Role role = new Role();
        role.setCode(value);
        return role;
    }

    @Override
    public Object getObjRaw(String key) {
        return roleService.getRole(key);
    }
}
