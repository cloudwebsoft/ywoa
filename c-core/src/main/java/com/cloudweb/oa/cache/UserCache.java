package com.cloudweb.oa.cache;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.web.Global;
import com.cloudweb.oa.base.ObjCache;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.jcs.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class UserCache extends ObjCache {
    final String ROLES_PREFIX = "ROLES_";
    final String GROUPS_PREFIX = "GROUPS_";
    final String PRIVS_PREFIX = "PRIVS_";
    final String ADMIN_DEPTS_PREFIX = "ADMIN_DEPTS_";

    private static final Lock lock = new ReentrantLock();

    @Autowired
    IUserService userService;

    @Autowired
    IRoleService roleService;

    @Autowired
    IGroupService groupService;

    @Autowired
    IGroupOfRoleService groupOfRoleService;

    @Autowired
    IUserPrivService userPrivService;

    @Autowired
    IUserAdminDeptService userAdminDeptService;

    @Autowired
    IDeptUserService deptUserService;

    public User getUser(String userName) {
        return (User)getObj(this, userName);
    }

    public List<Role> getRolesWithoutLock(String userName) throws CacheException {
        boolean isNotExist = false;
        List<Role> list = (List) RMCache.getInstance().getFromGroup(ROLES_PREFIX + userName, group);
        if (null == list) {
            // 取得用户所属的角色
            list = roleService.getRolesOfUser(userName, true);
            // 取得用户所属的用户组
            List<Group> groupList = groupService.getGroupsOfUser(userName, false);
            for (Group group : groupList) {
                // 取得用户组所属的角色
                List<GroupOfRole> gorList = groupOfRoleService.listByGroupCode(group.getCode());
                // 判断用户组所属的角色是否已在list中，如果不在则加入
                for (GroupOfRole groupOfRole : gorList) {
                    boolean isFound = false;
                    for (Role role : list) {
                        if (role.getCode().equals(groupOfRole.getRoleCode())) {
                            isFound = true;
                            break;
                        }
                    }
                    if (!isFound) {
                        list.add(roleService.getRole(groupOfRole.getRoleCode()));
                    }
                }
            }

            if (list.size()==0) {
                isNotExist = true;
                // throw new RuntimeException("This data could not be empty. code=" + code);
                // 将代表null值的menu置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                Role role = new Role();
                role.setCode(ConstUtil.CACHE_NONE);
                list.add(role);
                RMCache.getInstance().putInGroup(ROLES_PREFIX + userName, group, list, ConstUtil.CACHE_NONE_EXPIRE);
            } else {
                RMCache.getInstance().putInGroup(ROLES_PREFIX + userName, group, list);
            }
        }
        else {
            if (ConstUtil.CACHE_NONE.equals(list.get(0).getCode())) {
                isNotExist = true;
            }
        }
        if (isNotExist) {
            return new ArrayList<>();
        }
        else {
            return list;
        }
    }

    /**
     * 取得用户所属的角色，不包含MEMBER角色
     * @return RoleDb[]
     */
    public List<Role> getRoles(String userName) {
        List<Role> list = null;
        try {
            list = (List<Role>) RMCache.getInstance().getFromGroup(ROLES_PREFIX + userName, group);
        } catch (Exception e) {
            log.error("getRoles:" + e.getMessage());
        }

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
                list = getRolesWithoutLock(userName);
            }
            else {
                if (lock.tryLock()) {
                    isLocked = true;
                    list = getRolesWithoutLock(userName);
                } else {
                    TimeUnit.MILLISECONDS.sleep(200);
                    list = getRoles(userName);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (CacheException e) {
            e.printStackTrace();
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

        return list;
    }

    public void refreshRoles(String userName) {
        try {
            RMCache.getInstance().remove(ROLES_PREFIX + userName, group);
        } catch (Exception e) {
            log.error("refreshRoles:" + e.getMessage());
        }
    }

    /**
     * 取得所属的用户组，不包含everyone用户组
     * @param userName
     * @return
     */
    public List<Group> getGroups(String userName) {
        List<Group> list = null;
        try {
            list = (List<Group>) RMCache.getInstance().getFromGroup(GROUPS_PREFIX + userName, group);
        } catch (Exception e) {
            log.error("getRoles:" + e.getMessage());
        }

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
                list = getGroupsWithoutLock(userName);
            }
            else {
                if (lock.tryLock()) {
                    isLocked = true;
                    list = getGroupsWithoutLock(userName);
                } else {
                    TimeUnit.MILLISECONDS.sleep(200);
                    list = getGroups(userName);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (CacheException e) {
            e.printStackTrace();
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
        return list;
    }

    public List<Group> getGroupsWithoutLock(String userName) throws CacheException {
        boolean isNotExist = false;
        List<Group> groupList = (List) RMCache.getInstance().getFromGroup(GROUPS_PREFIX + userName, group);
        if (null == groupList) {
            groupList = groupService.getGroupsOfUser(userName, true);
            // 取用户所属的部门型的用户组
            List<Group> deptGrouplist = groupService.listByIsDept(true);
            for (Group group : deptGrouplist) {
                boolean isOfGroup = false;
                if (group.getIsIncludeSubDept()==1) {
                    // 判断用户是否属于该部门型用户组，如果用户属于deptCode部门的子部门，则判定为属于该组
                    if (deptUserService.isUserBelongToDept(userName, group.getDeptCode())) {
                        isOfGroup = true;
                    }
                }
                else {
                    if (deptUserService.isUserOfDept(userName, group.getDeptCode())) {
                        isOfGroup = true;
                    }
                }
                if (isOfGroup) {
                    boolean isFound = false;
                    for (Group gp : groupList) {
                        if (gp.getCode().equals(group.getCode())) {
                            isFound = true;
                            break;
                        }
                    }
                    if (!isFound) {
                        groupList.add(group);
                    }
                }
            }

            if (groupList.size()==0) {
                isNotExist = true;
                // throw new RuntimeException("This data could not be empty. code=" + code);
                // 将代表null值的menu置于缓存，如置menu.name为 &&，且通常置到期时间为5分钟
                Group uGroup = new Group();
                uGroup.setCode(ConstUtil.CACHE_NONE);
                groupList.add(uGroup);
                RMCache.getInstance().putInGroup(GROUPS_PREFIX + userName, group, groupList, ConstUtil.CACHE_NONE_EXPIRE);
            } else {
                RMCache.getInstance().putInGroup(GROUPS_PREFIX + userName, group, groupList);
            }
        }
        else {
            if (ConstUtil.CACHE_NONE.equals(groupList.get(0).getCode())) {
                isNotExist = true;
            }
        }
        if (isNotExist) {
            return new ArrayList<>();
        }
        else {
            return groupList;
        }
    }

    public void refreshGroups(String userName) {
        try {
            RMCache.getInstance().remove(GROUPS_PREFIX + userName, group);
        } catch (Exception e) {
            log.error("refreshGroups:" + e.getMessage());
        }
    }

    public String[] getPrivs(String userName) {
        String[] pv = null;
        try {
            pv = (String[]) RMCache.getInstance().getFromGroup(PRIVS_PREFIX + userName, group);
        } catch (Exception e) {
            log.error("getRoles:" + e.getMessage());
        }

        if (null != pv) {
            // 防穿透
            if (ConstUtil.CACHE_NONE.equals(pv[0])) {
                return null;
            }
            else {
                return pv;
            }
        }

        boolean isLocked = false;
        String indentifier = "";
        // 双重检测锁可能会导致排队，性能有所损失，防止高并发时因缓存并发而导致穿透，但不适用于集群环境，集群时需使用分布式锁
        try {
            if (Global.isCluster() && Global.getInstance().isUseRedis()) {
                indentifier = distributedLock.lock(getClass().getName(), 5000, 1000);
                pv = getPrivsWithoutLock(userName);
            }
            else {
                if (lock.tryLock()) {
                    isLocked = true;
                    pv = getPrivsWithoutLock(userName);
                } else {
                    TimeUnit.MILLISECONDS.sleep(200);
                    pv = getPrivs(userName);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (CacheException e) {
            e.printStackTrace();
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
        return pv;
    }

    public String[] getPrivsWithoutLock(String userName) throws CacheException {
        boolean isNotExist = false;
        String[] pv = (String[]) RMCache.getInstance().getFromGroup(PRIVS_PREFIX + userName, group);
        if (null == pv) {
            List<UserPriv> list = userPrivService.listByUserName(userName);
            if (list.size()>0) {
                pv = new String[list.size()];
                int i = 0;
                for (UserPriv userPriv : list) {
                    pv[i] = userPriv.getPriv();
                    i++;
                }
                RMCache.getInstance().putInGroup(PRIVS_PREFIX + userName, group, pv);
            }
            else {
                isNotExist = true;
                pv = new String[]{ConstUtil.CACHE_NONE};
                RMCache.getInstance().putInGroup(PRIVS_PREFIX + userName, group, pv, ConstUtil.CACHE_NONE_EXPIRE);
            }
        }
        else {
            if (ConstUtil.CACHE_NONE.equals(pv[0])) {
                isNotExist = true;
            }
        }
        if (isNotExist) {
            return null;
        }
        else {
            return pv;
        }
    }

    public void refreshPrivs(String name) {
        try {
            RMCache.getInstance().remove(PRIVS_PREFIX + name, group);
        } catch (Exception e) {
            log.error("refreshPrivs:" + e.getMessage());
        }
    }

    public String[] getAdminDepts(String userName) {
        String[] depts = null;
        try {
            depts = (String[]) RMCache.getInstance().getFromGroup(ADMIN_DEPTS_PREFIX + userName, group);
        } catch (Exception e) {
            log.error("getRoles:" + e.getMessage());
        }

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
                depts = getAdminDeptsWithoutLock(userName);
            }
            else {
                if (lock.tryLock()) {
                    isLocked = true;
                    depts = getAdminDeptsWithoutLock(userName);
                } else {
                    TimeUnit.MILLISECONDS.sleep(200);
                    depts = getAdminDepts(userName);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (CacheException e) {
            e.printStackTrace();
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
        return depts;
    }

    public String[] getAdminDeptsWithoutLock(String userName) throws CacheException {
        boolean isNotExist = false;
        String[] depts = (String[]) RMCache.getInstance().getFromGroup(ADMIN_DEPTS_PREFIX + userName, group);
        if (null == depts) {
            List<UserAdminDept> list = userAdminDeptService.listByUserName(userName);
            if (list.size()>0) {
                depts = new String[list.size()];
                int i = 0;
                for (UserAdminDept userAdminDept : list) {
                    depts[i] = userAdminDept.getDeptCode();
                    i++;
                }
                RMCache.getInstance().putInGroup(ADMIN_DEPTS_PREFIX + userName, group, depts);
            }
            else {
                isNotExist = true;
                depts = new String[]{ConstUtil.CACHE_NONE};
                RMCache.getInstance().putInGroup(ADMIN_DEPTS_PREFIX + userName, group, depts, ConstUtil.CACHE_NONE_EXPIRE);
            }
        }
        else {
            if (ConstUtil.CACHE_NONE.equals(depts[0])) {
                isNotExist = true;
            }
        }
        if (isNotExist) {
            return null;
        }
        else {
            return depts;
        }
    }

    public void refreshAdminDepts(String userName) {
        try {
            RMCache.getInstance().remove(ADMIN_DEPTS_PREFIX + userName, group);
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
        User user = (User)obj;
        return user.getName();
    }

    @Override
    public Object getEmptyObjWithPrimaryKey(String value) {
        User user = new User();
        user.setName(ConstUtil.CACHE_NONE);
        return user;
    }

    @Override
    public Object getObjRaw(String key) {
        return userService.getUser(key);
    }
}
