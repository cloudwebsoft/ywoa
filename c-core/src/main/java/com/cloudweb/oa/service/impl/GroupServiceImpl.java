package com.cloudweb.oa.service.impl;

import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.GroupCache;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.mapper.GroupMapper;
import com.cloudweb.oa.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.oa.pvg.Privilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-02
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {

    @Autowired
    IUserOfGroupService userOfGroupService;

    @Autowired
    IUserService userService;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IDeptUserService deptUserService;

    @Autowired
    GroupCache groupCache;

    @Autowired
    GroupMapper groupMapper;

    @Override
    public Group getGroup(String code) {
        return getOne(new QueryWrapper<Group>().eq("code", code));
    }

    @Override
    public List<Group> getAll() {
        QueryWrapper<Group> qw = new QueryWrapper<>();
        qw.orderByDesc("isSystem");
        qw.orderByAsc("unit_code");
        qw.orderByAsc("description");
        return list(qw);
    }

    @Override
    public List<Group> listByIsDept(boolean isDept) {
        QueryWrapper<Group> qw = new QueryWrapper<>();
        qw.eq("is_dept", isDept?1:0)
                .orderByDesc("isSystem")
                .orderByAsc("code");
        return list(qw);
    }

    @Override
    public List<Group> listByUnitCode(String unitCode) {
        QueryWrapper<Group> qw = new QueryWrapper<>();
        qw.eq("unit_code", unitCode)
                .orderByDesc("isSystem")
                .orderByAsc("code");
        return list(qw);
    }

    @Override
    public boolean del(String groupCode) {
        QueryWrapper<Group> qw = new QueryWrapper<>();
        qw.eq("code", groupCode);
        boolean re = remove(qw);
        if (re) {
            groupCache.refreshDel(groupCode);

            // 删除用户组中的用户
            List<UserOfGroup> list = userOfGroupService.listByGroupCode(groupCode);
            for (UserOfGroup userOfGroup : list) {
                userOfGroupService.del(userOfGroup.getGroupCode(), new String[]{userOfGroup.getUserName()});
            }
        }
        return re;
    }

    @Override
    public boolean create(String code, String desc, Integer isDept, Integer isIncludeSubDept, String unitCode, String kind) {
        Group group = new Group();
        group.setCode(code);
        group.setDescription(desc);
//        group.setIsDept(isDept==1);
        group.setIsIncludeSubDept(isIncludeSubDept);
        group.setUnitCode(unitCode);
        group.setKind(kind);
        boolean re = group.insert();
        if (re) {
            groupCache.refreshAll();
        }
        return re;
    }

    @Override
    public boolean update(String code, String desc, String deptCode, Integer isDept, Integer isIncludeSubDept, String unitCode, String kind) {
        Group group = new Group();
        group.setCode(code);
        group.setDescription(desc);
        group.setDeptCode(deptCode);
        group.setIsDept(isDept==1);
        group.setIsIncludeSubDept(isIncludeSubDept);
        group.setUnitCode(unitCode);
        group.setKind(kind);
        QueryWrapper<Group> qw = new QueryWrapper<>();
        qw.eq("code", code);
        boolean re = update(group, qw);
        if (re) {
            groupCache.refreshSave(code);
        }
        return re;
    }

    /**
     * 取出用户所在的所有部门组，含部门型用户组
     * @param userName
     * @return
     */
    @Override
    public List<Group> getAllGroupsOfUser(String userName) {
        List<Group> groupList = getGroupsOfUser(userName, true);

        // 取用户所属的部门型的用户组
        // 20220313 用户组与角色不再关联
        /*List<Group> deptGrouplist = listByIsDept(true);
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
        }*/
        return groupList;
    }

    @Override
    public List<Group> getGroupsOfUser(String userName, boolean isWithSystem) {
        List<UserOfGroup> list = userOfGroupService.listByUserName(userName);
        List<Group> groupList = new ArrayList<>();
        for (UserOfGroup userOfGroup : list) {
            Group group = getGroup(userOfGroup.getGroupCode());
            if (group == null) {
                continue;
            }
            if (group.getIsSystem()) {
                if (isWithSystem) {
                    groupList.add(group);
                }
            }
            else {
                groupList.add(group);
            }
        }
        return groupList;
    }

    @Override
    public List<User> getAllUserOfGroup(String groupCode) {
        if (groupCode.equals(ConstUtil.GROUP_EVERYONE)) {
            return userService.listAll();
        }

        List<User> list = new ArrayList<>();
        List<UserOfGroup> urList = userOfGroupService.listByGroupCode(groupCode);
        for (UserOfGroup userOfGroup : urList) {
            User user = userService.getUser(userOfGroup.getUserName());
            if (user.getIsValid() == 1) {
                list.add(user);
            }
        }

        // 20220313 用户组与部门及角色不再关联
        /*Group group = getGroup(groupCode);
        if (group.getIsDept()) {
            // 取得部门(或者及子部门)中的用户
            List<Department> deptList = new ArrayList<>();
            deptList.add(departmentService.getDepartment(group.getDeptCode()));

            if (group.getIsIncludeSubDept()==1) {
                departmentService.getAllChild(deptList, group.getDeptCode());
            }

            for (Department dept : deptList) {
                List<DeptUser> deptUserList = deptUserService.listByDeptCode(dept.getCode());
                for (DeptUser deptUser : deptUserList) {
                    boolean isFound = false;

                    // 去除用户是否重复
                    for (User user : list) {
                        if (user.getName().equals(deptUser.getUserName())) {
                            isFound = true;
                            break;
                        }
                    }
                    if (!isFound) {
                        User user = userService.getUser(deptUser.getUserName());
                        if (user.getIsValid()==1) {
                            list.add(user);
                        }
                    }
                }
            }
        }*/
        return list.stream().filter(distinctByKey(User::getName)).collect(Collectors.toList());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Override
    public List<Group> list(String searchUnitCode, String op, String what, String kind) {
        String sql;
        if ("".equals(searchUnitCode)) {
            com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
            if (pvg.isUserPrivValid(pvg.getUser(), Privilege.ADMIN)) {
                sql = "select * from user_group where 1=1";
            } else {
                String unitCode = pvg.getUserUnitCode();
                sql = "select * from user_group where unit_code=" + StrUtil.sqlstr(unitCode);
            }
        } else {
            sql = "select * from user_group where unit_code=" + StrUtil.sqlstr(searchUnitCode);
        }

        if ("search".equals(op)) {
            if (!"".equals(what)) {
                sql += " and description like " + StrUtil.sqlstr("%" + what + "%");
            }
            if (!"".equals(kind)) {
                sql += " and kind=" + StrUtil.sqlstr(kind);
            }
        }

        sql += " order by isSystem desc, unit_code asc, description asc";
        return groupMapper.listBySql(sql);
    }
}
