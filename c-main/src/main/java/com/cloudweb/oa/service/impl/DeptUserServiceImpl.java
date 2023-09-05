package com.cloudweb.oa.service.impl;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.*;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.mapper.DeptUserMapper;
import com.cloudweb.oa.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.redmoon.oa.Config;
import com.redmoon.oa.db.SequenceManager;
import com.cloudweb.oa.visual.PersonBasicService;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-04
 */
@Service
public class DeptUserServiceImpl extends ServiceImpl<DeptUserMapper, DeptUser> implements IDeptUserService {

    @Autowired
    DeptUserMapper deptUserMapper;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IUserService usersService;

    @Autowired
    IUserSetupService userSetupService;

    @Autowired
    PersonBasicService personBasicService;

    @Autowired
    IUserAuthorityService userAuthorityService;

    @Autowired
    UserAuthorityCache userAuthorityCache;

    @Autowired
    RoleCache roleCache;

    @Autowired
    UserCache userCache;

    @Autowired
    DeptUserCache deptUserCache;

    @Autowired
    IUserRoleDepartmentService userRoleDepartmentService;

    @Autowired
    IUserOfRoleService userOfRoleService;

    @Autowired
    DepartmentCache departmentCache;

    /**
     * 删除用户及钉钉、企业微信同步时调用
     * @param userName
     * @return
     */
    @Override
    public boolean delOfUser(String userName) {
        List<DeptUser> list = listByUserName(userName);
        for (DeptUser deptUser : list) {
            deptUserMapper.updateOrdersGreatThan(deptUser.getDeptCode(), deptUser.getOrders());
        }

        QueryWrapper<DeptUser> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        boolean re = remove(qw);
        if (re) {
            deptUserCache.refreshDeptUser(userName);

            userAuthorityService.refreshUserAuthority(userName);
            userAuthorityCache.refreshUserAuthorities(userName);
            userCache.refreshRoles(userName);
        }
        return re;
    }

    @Override
    public boolean del(DeptUser deptUser) {
        deptUserMapper.updateOrdersGreatThan(deptUser.getDeptCode(), deptUser.getOrders());

        QueryWrapper<DeptUser> qw = new QueryWrapper<>();
        qw.eq("user_name", deptUser.getUserName());
        qw.eq("dept_code", deptUser.getDeptCode());
        boolean re = remove(qw);
        if (re) {
            deptUserCache.refreshDeptUser(deptUser.getUserName());

            // 取出部门所属的角色
            List<UserRoleDepartment> list = userRoleDepartmentService.listByDeptCode(deptUser.getDeptCode());
            for (UserRoleDepartment userRoleDepartment : list) {
                // 删除用户所属的角色
                userOfRoleService.del(deptUser.getUserName(), userRoleDepartment.getRoleCode());
            }
            userAuthorityService.refreshUserAuthority(deptUser.getUserName());
            userAuthorityCache.refreshUserAuthorities(deptUser.getUserName());
            userCache.refreshRoles(deptUser.getUserName());
        }
        return re;
    }

    @Override
    public boolean create(String userName, String deptCode) {
        Integer order = deptUserMapper.getMaxOrder(deptCode);
        if (order == null) {
            order = 0;
        }
        else {
            order += 1;
        }
        int id = (int) SequenceManager.nextID(SequenceManager.OA_DEPT_USER);
        DeptUser deptUser = new DeptUser();
        deptUser.setUserName(userName);
        deptUser.setDeptCode(deptCode);
        deptUser.setId(id);
        deptUser.setOrders(order);
        boolean re = deptUser.insert();
        if (re) {
            // 取出部门所属的角色
            List<UserRoleDepartment> list = userRoleDepartmentService.listByDeptCode(deptUser.getDeptCode());
            for (UserRoleDepartment userRoleDepartment : list) {
                // 如果不屬于該角色，則添加用户的角色
                // 不能用userCache.isUserOfRole判斷，因為該方法會查出用戶所屬的角色，包括其部門所屬的角色
                // if (!userCache.isUserOfRole(deptUser.getUserName(), userRoleDepartment.getRoleCode())) {
                if (userOfRoleService.getUserOfRole(deptUser.getUserName(), userRoleDepartment.getRoleCode()) == null) {
                    userOfRoleService.create(deptUser.getUserName(), userRoleDepartment.getRoleCode());
                }
            }

            userAuthorityService.refreshUserAuthority(userName);
            userAuthorityCache.refreshUserAuthorities(userName);
            userCache.refreshRoles(userName);
        }
        return re;
    }

    @Override
    public DeptUser getDeptUser(String userName, String deptCode) {
        QueryWrapper<DeptUser> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        qw.eq("dept_code", deptCode);
        return getOne(qw, false);
    }

    /**
     * 取得主部门，即排在第一位的部门
     *
     * @param userName
     * @return
     */
    @Override
    public DeptUser getPrimary(String userName) {
        QueryWrapper<DeptUser> qw = new QueryWrapper<>();
        qw.eq("user_name", userName).orderByAsc("orders");
        return getOne(qw, false);
    }

    @Override
    public List<DeptUser> listByUserName(String userName) {
        QueryWrapper<DeptUser> qw = new QueryWrapper<>();
        qw.eq("user_name", userName).orderByAsc("orders")
                .orderByAsc("id");
        return list(qw);
    }

    /**
     * 调动部门，调动时将会脱离原来所在的部门，用户如果原来处于多个部门，都会被脱离
     *
     * @param userName
     * @param deptCodes
     * @return
     */
    @Override
    public boolean changeDeptOfUser(String userName, String deptCodes, String opUser) throws ErrMsgException {
        String[] ary = StrUtil.split(deptCodes, ",");

        String deptCode = "";
        boolean re = true;
        deptCode = ary[0];

        // 取得用户所来所在的部门
        List<DeptUser> list = listByUserName(userName);
        for (DeptUser deptUser : list) {
            boolean isFound = false;
            for (int i = 0; i < ary.length; i++) {
                if (ary[i].equals(deptUser.getDeptCode())) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                // 如果原部门不在新选的部门中，则删除
                DeptUser duOld = getDeptUser(userName, deptUser.getDeptCode());
                del(duOld);
            }
        }

        for (int i = 0; i < ary.length; i++) {
            boolean isFound = false;
            for (DeptUser deptUser : list) {
                if (ary[i].equals(deptUser.getDeptCode())) {
                    isFound = true;
                    break;
                }
            }
            // 如果新选的部门在原部门中未找到，则新增
            if (!isFound) {
                re = create(userName, ary[i]);
            }
        }

        if (ary.length >= 1) {
            deptUserCache.refreshDeptUser(userName);

            userAuthorityService.refreshUserAuthority(userName);
            userAuthorityCache.refreshUserAuthorities(userName);
            userCache.refreshRoles(userName);

            // 置为排在第一个的部门所在的单位
            Department dept = departmentService.getDepartment(deptCode);
            String unitCode = departmentService.getUnitOfDept(dept).getCode();

            User user = usersService.getUser(userName);
            user.setUnitCode(unitCode);
            user.updateById();

            // 同步人事档案信息
            personBasicService.changeDeptOfUser(userName, deptCode);

            // 更新对接的其它系统
            userSetupService.onUserUpdated(user, deptCode, SpringUtil.getUserName());
        }

        return re;
    }

    /**
     * 判断用户是否属于某个部门
     * @param userName
     * @param deptCode
     * @return
     */
    @Override
    public boolean isUserOfDept(String userName, String deptCode) {
        return getDeptUser(userName, deptCode)!=null;
    }

    /**
     * 用户是否属于某个部门或其子部门
     * @param userName String
     * @param deptCode String
     * @return boolean
     */
    @Override
    public boolean isUserBelongToDept(String userName, String deptCode) {
        List<DeptUser> list = listByUserName(userName);
        List<Department> deptList = new ArrayList<>();
        for (DeptUser deptUser : list) {
            if (deptUser.getDeptCode().equals(deptCode)) {
                return true;
            }

            // 判断用户所在的单位是否为deptCode
            Department department = departmentService.getDepartment(deptUser.getDeptCode());
            if (department == null) {
                LogUtil.getLog(getClass()).error("isUserBelongToDept: " + deptUser.getUserName() + " 的部门 " + deptUser.getDeptCode() + " 不存在");
                continue;
            }
            deptList.add(department);

            String unitCode = departmentService.getUnitOfDept(department).getCode();
            if (unitCode.equals(deptCode)) {
                return true;
            }
        }

        // 判断用户是否属于deptCode部门的子部门
        for (Department dept : deptList) {
            // 向上遍历
            String parentCode = dept.getParentCode();
            while (!parentCode.equals("-1")) {
                if (parentCode.equals(deptCode)) {
                    return true;
                }
                dept = departmentService.getDepartment(parentCode);
                if (dept==null) {
                    break;
                }
                parentCode = dept.getParentCode();
            }
        }

        return false;
    }

    /**
     * 列出部门中的用户，并根据设置的排序方式进行排序
     * @param deptCode
     * @return
     */
    @Override
    public List<DeptUser> listByDeptCode(String deptCode) {
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
        String orderField = showByDeptSort ? "du.orders" : "u.orders";

        return deptUserMapper.listByDeptCode(deptCode, orderField);
    }

    /**
     * 取出部门及其子部门下的所有的用户
     * @param deptCode
     * @return
     */
    @Override
    public List<DeptUser> listAllByDeptCode(String deptCode) {
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
        String orderField = showByDeptSort ? "du.orders" : "u.orders";
        List<DeptUser> list = new ArrayList<>();
        listAllByDeptCode(deptCode, orderField, list);
        return list;
    }

    public void listAllByDeptCode(String deptCode, String orderField, List<DeptUser> list) {
        list.addAll(deptUserMapper.listByDeptCode(deptCode, orderField));
        List<Department> children = departmentCache.getChildren(deptCode);
        for (Department department : children) {
            listAllByDeptCode(department.getCode(), orderField, list);
        }
    }

    @Override
    public List<String> listUserNameInDepts(String deptCodes) {
        return deptUserMapper.listUserNameInDepts(deptCodes);
    }

    @Override
    public List<Integer> listIdBySql(String sql) {
        return deptUserMapper.listIdBySql(sql);
    }

    /**
     * 同步所有用户的单位
     * @Description:
     */
    @Override
    public void syncUnit() {
        List<String> unitCodeList = departmentService.getAllUnit();

        Config cfg = Config.getInstance();
        boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");

        for (String unitCode : unitCodeList) {
            List<Department> deptList = new ArrayList<>();
            departmentService.getAllChild(deptList, unitCode);

            for (Department dept : deptList) {
                usersService.updateUserUnitInDept(unitCode, dept.getCode());
                if (isArchiveUserSynAccount) {
                    personBasicService.updateUserUnitInDept(unitCode, dept.getCode());
                }
            }
        }
        try {
            // 清缓存
            RMCache rmcache = RMCache.getInstance();
            rmcache.clear();
        } catch (CacheException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    /**
     * 组织机构
     * @param op
     * @param realName
     * @param mobile
     * @param email
     * @param depts
     * @param orderField
     * @return
     */
    @Override
    public ListResult listBySearch(String op, String realName, String mobile, String email, String depts, String phone, String orderField, int curPage, int pageSize) {
        String sql;
        if ("".equals(op)) {
            if (!"".equals(depts)) {
                sql = "select du.* from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + depts + ") order by du.DEPT_CODE asc, " + orderField + " asc";
            }
            else {
                sql = "select du.* from dept_user du, users u where du.user_name=u.name and u.isValid=1 order by du.DEPT_CODE asc, " + orderField + " asc";
            }
        }
        else {
            if (!"".equals(depts)) {
                sql = "select du.* from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + depts + ")";
            }
            else {
                sql = "select du.* from dept_user du, users u where du.user_name=u.name and u.isValid=1";
            }
            if (!"".equals(realName)) {
                sql += " and realname like " + StrUtil.sqlstr("%" + realName + "%");
            }
            if (!"".equals(mobile)) {
                sql += " and mobile like " + StrUtil.sqlstr("%" + mobile + "%");
            }
            if (!"".equals(email)) {
                sql += " and email like " + StrUtil.sqlstr("%" + email + "%");
            }
            if (!"".equals(phone)) {
                sql += " and phone like " + StrUtil.sqlstr("%" + phone + "%");
            }
            sql += " order by du.DEPT_CODE asc, " + orderField + " asc";
        }

        PageHelper.startPage(curPage, pageSize); // 分页查询
        List<DeptUser> list = deptUserMapper.listBySql(sql);
        PageInfo<DeptUser> pageInfo = new PageInfo<>(list);

        ListResult lr = new ListResult();
        Vector<DeptUser> v = new Vector<>();
        v.addAll(list);
        lr.setResult(v);
        lr.setTotal(pageInfo.getTotal());
        return lr;
    }
}
