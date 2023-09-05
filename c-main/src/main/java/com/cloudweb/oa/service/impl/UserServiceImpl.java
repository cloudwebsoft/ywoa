package com.cloudweb.oa.service.impl;

import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.mapper.UserMapper;
import com.cloudweb.oa.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.Base64Util;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.vo.UserVO;
import com.cloudwebsoft.framework.security.AesUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-01-09
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    IGroupService userGroupService;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IUserOfRoleService userOfRoleService;

    @Autowired
    IUserOfGroupService userOfGroupService;

    @Autowired
    IUserPrivService userPrivService;

    @Autowired
    IUserSetupService userSetupService;

    @Autowired
    DozerBeanMapper dozerBeanMapper;

    @Autowired
    IDeptUserService deptUserService;

    @Autowired
    IRoleService roleService;

    @Autowired
    IOaPortalService oaPortalService;

    @Autowired
    IOaSlideMenuGroupService oaSlideMenuGroupService;

    @Autowired
    IAccountService accountService;

    @Autowired
    IUserAuthorityService userAuthorityService;

    @Autowired
    IUserDesktopSetupService userDesktopSetupService;

    @Autowired
    IUserRecentlySelectedService userRecentlySelectedService;

    @Autowired
    UserCache userCache;

    @Autowired
    IFileService fileService;

    @Autowired
    IPostUserService postUserService;

    @Override
    public User getUser(String userName) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("name", userName);
        return getOne(qw, false);
    }

    @Override
    public User getUserById(int id) {
        return getById(id);
    }

    @Override
    public User getUserByEmail(String email) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("email", email);
        return getOne(qw, false);
    }

    @Override
    public User getUserByMobile(String mobile) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("mobile", mobile);
        return getOne(qw, false);
    }

    @Override
    public User getUserByOpenId(String openId) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("open_id", openId);
        return getOne(qw, false);
    }

    @Override
    public User getUserByUin(String uin) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("uin", uin);
        return getOne(qw, false);
    }

    @Override
    public User getUserByUnionId(String openId) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("union_id", openId);
        return getOne(qw, false);
    }

    @Override
    public User getUserByRealName(String realName) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("realName", realName);
        return getOne(qw, false);
    }

    @Override
    public User getUserByLoginName(String loginName) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("login_name", loginName);
        return getOne(qw, false);
    }

    @Override
    public void updateUserUnitCode(String code, String unitCode) {
        userMapper.updateUserUnitCode(code, unitCode);
    }

    @Override
    public List<User> getRecentSelected(String userName) {
        return userMapper.getRecentSelected(userName);
    }

    @Override
    public List<User> getRecentSelectedOfUnit(String userName, String unitCode) {
        return userMapper.getRecentSelectedOfUnit(userName, unitCode);
    }

    @Override
    public List<User> getDeptUsers(String deptCode, boolean isIncludeChildren, String[] limitDeptArr, String unitCode, boolean includeRootDept) {
        String sql = "";

        StringBuilder deptForSql = new StringBuilder();
        if (limitDeptArr != null) {
            for (String dept : limitDeptArr) {
                if (!"".equals(deptForSql.toString())) {
                    deptForSql.append(",").append(StrUtil.sqlstr(dept));
                } else {
                    deptForSql.append(StrUtil.sqlstr(dept));
                }
            }
        }

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
        String orderField = showByDeptSort ? "du.orders" : "u.orders";

        if (limitDeptArr == null || includeRootDept) {
            // 如果是根部门的所有人员，要根据人员在查询出其所属的所有部门，以逗号隔开，拼成字符串(因为每个人所属的部门可能不止一个)
            if (includeRootDept) {
                if (unitCode.equals(ConstUtil.DEPT_ROOT)) {
                    sql = "select u.name,u.realName,u.gender from users u where u.isValid=1 order by u.orders desc,u.realName asc";
                } else {
                    sql = "select u.name,u.realName,u.gender from users u where u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by u.orders desc,u.realName asc";
                }
            } else {
                if (StringUtils.isEmpty(deptCode)) {
                    return new ArrayList<>();
                }
                sql = "select u.name,u.realName,u.gender,d.name from dept_user du, users u,department d where du.user_name=u.name and du.dept_code=d.code and u.isValid=1 and du.dept_code=" + StrUtil.sqlstr(deptCode) + " order by " + orderField + " desc,du.dept_code asc, du.orders asc";
            }
        } else if (isIncludeChildren || deptCode.equals(ConstUtil.DEPT_ROOT)) {
            if (unitCode.equals(ConstUtil.DEPT_ROOT)) {
                sql = "select u.name,u.realName,u.gender,d.name from dept_user du, users u,department d where du.user_name=u.name and du.dept_code=d.code and u.isValid=1 and du.dept_code in (" + deptForSql + ") order by " + orderField + " desc,du.dept_code asc, du.orders asc";
            } else {
                sql = "select u.name,u.realName,u.gender,d.name from dept_user du, users u,department d where du.user_name=u.name and du.dept_code=d.code and u.isValid=1 and du.dept_code in (" + deptForSql + ") and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by " + orderField + " desc,du.dept_code asc, du.orders asc";
            }
        } else if (!"".equals(deptCode)) { // limitDepts不为空时
            sql = "select u.name,u.realName,u.gender,d.name from dept_user du, users u,department d where du.user_name=u.name and du.dept_code=d.code and u.isValid=1 and du.dept_code=" + StrUtil.sqlstr(deptCode) + " order by " + orderField + " desc,du.dept_code asc, du.orders asc";
        } else {
            return null;
        }

        DebugUtil.d(this.getClass(), "sql", sql);
        return userMapper.listBySql(sql);
    }

    /**
     * 用于选择用户userMutiSel
     *
     * @param userName
     * @param limitDeptArr
     * @param includeRootDept
     * @return
     */
    @Override
    public List<User> searchUser(String userName, String[] limitDeptArr, boolean includeRootDept) {
        String sql = "";
        String result = "";

        StringBuilder deptForSql = new StringBuilder();
        if (limitDeptArr != null) {
            for (String dept : limitDeptArr) {
                if (!deptForSql.toString().equals("")) {
                    deptForSql.append(",");
                }
            }
        }

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
        String orderField = showByDeptSort ? "du.orders" : "u.orders";

        if (limitDeptArr == null || includeRootDept) {
            sql = "select u.name,u.realName,u.gender from users u where u.name <> 'system' and u.isValid=1 and u.realName like " + StrUtil.sqlstr("%" + userName + "%") + " order by u.orders desc,u.realName asc";
        } else {
            sql = "select u.name,u.realName,u.gender from users u,dept_user du where u.name <> 'system' and u.isValid=1 and u.realName like " + StrUtil.sqlstr("%" + userName + "%") + " and du.user_name=u.name and du.dept_code in (" + deptForSql + ") order by " + orderField + " desc,u.realName asc";
        }

        return userMapper.listBySql(sql);
    }

    /**
     * 取得属于某角色的用户
     *
     * @param roleCode
     * @param unitCode
     * @return
     */
    @Override
    public List<User> getUserOfRole(String roleCode, String unitCode) {
        String sql;
        if (roleCode.equals(ConstUtil.ROLE_MEMBER)) {
            if (unitCode.equals(ConstUtil.DEPT_ROOT)) {
                sql = "select u.name,u.realName,u.gender from users u where u.name <> 'system' and u.isValid=1 order by u.orders desc,u.realName asc";
            } else {
                sql = "select u.name,u.realName,u.gender from users u where u.name <> 'system' and u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by u.orders desc,u.realName asc";
            }
        } else {
            if (unitCode.equals(ConstUtil.DEPT_ROOT)) {
                sql = "select u.name,u.realName,u.gender from users u,user_of_role a where u.isValid=1 and u.name = a.userName and a.roleCode = " + StrUtil.sqlstr(roleCode) + " order by u.orders desc,u.realName asc";
            } else {
                sql = "select u.name,u.realName,u.gender from users u,user_of_role a where u.isValid=1 and u.name = a.userName and a.roleCode = " + StrUtil.sqlstr(roleCode) + " and unit_code=" + StrUtil.sqlstr(unitCode) + " order by u.orders desc,u.realName asc";
            }
        }

        return userMapper.listBySql(sql);
    }

    /**
     * 取得属于某用户组的用户，用于userMultiSel
     *
     * @param groupCode
     * @param unitCode
     * @return
     */
    @Override
    public List<User> getGroupUsers(String groupCode, String unitCode) {
        String sql = "";
        if (groupCode.equals(ConstUtil.GROUP_EVERYONE)) {
            if (unitCode.equals(ConstUtil.DEPT_ROOT)) {
                sql = "select u.name,u.realName,u.gender from users u where u.name <> 'system' and u.isValid=1 order by u.orders desc,u.realName asc";
            } else {
                sql = "select u.name,u.realName,u.gender from users u where u.name <> 'system' and u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by u.orders desc,u.realName asc";
            }
        } else {
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
            String orderField = showByDeptSort ? "du.orders" : "u.orders";

            QueryWrapper<Group> qw = new QueryWrapper<>();
            qw.eq("code", groupCode);
            Group group = userGroupService.getOne(qw);

            // 20220313 用户组与部门及角色不再关联
            if (false && group.getIsDept()) {
                if (group.getIsIncludeSubDept() == 1) {
                    List<Department> list = new ArrayList<>();
                    departmentService.getAllChild(list, group.getDeptCode());
                    QueryWrapper<Department> qwDept = new QueryWrapper<>();
                    qwDept.eq("code", group.getDeptCode());
                    list.add(departmentService.getOne(qwDept));

                    String allDepts = "";
                    for (Department dept : list) {
                        allDepts += (allDepts.equals("") ? "" : ",") + StrUtil.sqlstr(dept.getCode());
                    }
                    if (unitCode.equals(ConstUtil.DEPT_ROOT)) {
                        sql = "select u.name,u.realName,u.gender from users u,user_group a,dept_user du where u.isValid=1 and du.dept_code in (" + allDepts + ") and du.user_name=u.name and a.code = " + StrUtil.sqlstr(groupCode) + " order by " + orderField + " desc,u.realName";
                    } else {
                        sql = "select u.name,u.realName,u.gender from users u,user_group a,dept_user du where u.isValid=1 and du.dept_code in (" + allDepts + ") and du.user_name=u.name and a.code = " + StrUtil.sqlstr(groupCode) + " and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by " + orderField + " desc,u.realName asc";
                    }
                } else {
                    if (unitCode.equals(ConstUtil.DEPT_ROOT)) {
                        sql = "select u.name,u.realName,u.gender from users u,user_group a,dept_user du where u.isValid=1 and a.dept_code=du.dept_code and du.user_name=u.name and a.code = " + StrUtil.sqlstr(groupCode) + " order by " + orderField + " desc,u.realName asc";
                    } else {
                        sql = "select u.name,u.realName,u.gender from users u,user_of_group a,dept_user du where u.isValid=1 and a.dept_code=du.dept_code and du.user_name=u.name and a.code = " + StrUtil.sqlstr(groupCode) + " and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by " + orderField + " desc,u.realName asc";
                    }
                }
            } else {
                if (unitCode.equals(ConstUtil.DEPT_ROOT)) {
                    sql = "select u.name,u.realName,u.gender from users u,user_of_group a where u.isValid=1 and u.name = a.user_name and a.group_code = " + StrUtil.sqlstr(groupCode) + " order by u.orders desc,u.realName asc";
                } else {
                    sql = "select u.name,u.realName,u.gender from users u,user_of_group a where u.isValid=1 and u.name = a.user_name and a.group_code = " + StrUtil.sqlstr(groupCode) + " and u.unit_code=" + StrUtil.sqlstr(unitCode) + " order by u.orders desc,u.realName asc";
                }
            }
        }

        return userMapper.listBySql(sql);
    }

    /**
     * 离职 20120515 fgf create
     *
     * @return boolean
     */
    @Override
    public boolean leaveOffice(int id, String operator) {
        User user = userMapper.selectById(id);
        user.setIsValid(ConstUtil.USER_VALID_FIRED);
        boolean re = user.updateById();
        if (re) {
            String userName = user.getName();
            userCache.refreshSave(userName);

            /*
            // 20200929 不再删除权限，以便于随时恢复
            userOfRoleService.removeAllRoleOfUser(userName);
            userOfGroupService.removeAllGroupOfUser(userName);
            userPrivService.removeAllPrivOfUser(userName);
            */

            userSetupService.onUserDeleted(user, SpringUtil.getUserName());
        }

        return re;
    }

    @Override
    public String getNextPersonNo() {
        List<User> list = userMapper.getUsersHasPersonNo();
        if (list == null) {
            return "";
        }

        int pn = -1;
        String perfix = "";

        for (User user : list) {
            String num = "";
            String curPn = user.getPersonNo();
            int intPn = 0;
            try {
                intPn = Integer.parseInt(curPn) + 1;
            } catch (Exception e1) {
                if (!curPn.equals("")) {
                    if (curPn.charAt(curPn.length() - 1) < '0' || curPn
                            .charAt(curPn.length() - 1) > '9') {
                        continue;
                    }
                    String temp = "";
                    for (int i = curPn.length() - 1; i >= 0; i--) {
                        char ch = curPn.charAt(i);
                        if (temp.equals("") && ch >= '0' && ch <= '9') {
                            num = ch + num;
                        } else {
                            temp = ch + temp;
                        }
                    }
                    if (perfix.equals("")) {
                        perfix = temp;
                    }
                    intPn = StrUtil.toInt(num) + 1;
                } else {
                    intPn = 0;
                }
            }
            if (intPn > pn) {
                pn = intPn;
            }
        }

        if (pn != -1) {
            String str = String.valueOf(pn);
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            int len = cfg.getInt("personNoLen");
            return perfix + StrUtil.PadString(str, '0', len, true);
        } else {
            return "";
        }
    }

    @Override
    public boolean isPersonNoExist(String personNo) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("person_no", personNo);
        User user = userMapper.selectOne(qw);
        return user!=null;
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean create(UserVO userVO) throws IOException, ErrMsgException {
        User user = dozerBeanMapper.map(userVO, User.class);

        user.setPwdRaw(encryptPwd(user.getPwdRaw()));

        // 保存附件
        MultipartFile file = userVO.getPhoto();
        if (file != null && !file.isEmpty()) {
            String vpath = "public/users"; // 放在public目录下方便手机端获取
            String name = file.getOriginalFilename();
            String ext = StrUtil.getFileExt(name);
            String diskName = FileUpload.getRandName() + "." + ext;
            user.setPhoto(vpath + "/" + diskName);
            fileService.write(file, vpath, diskName);
        } else {
            user.setPhoto("");
        }

        if (user.getId() == null) {
            user.setId((int) SequenceManager.nextID(SequenceManager.OA_USER));
        }

        // 以ID作为用户名
        user.setName(String.valueOf(user.getId()));
        // 赋值，以便于在导入用户时获得name值
        userVO.setName(user.getName());

        Role role = roleService.getRole(ConstUtil.ROLE_MEMBER);
        user.setDiskSpaceAllowed(role.getDiskQuota());

        try {
            // 取得默认密码
            com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
            String pwdM55 = SecurityUtil.MD5(scfg.getInitPassword());
            user.setPwd(pwdM55);
            user.setPwdRaw(encryptPwd(scfg.getInitPassword()));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        boolean re = user.insert();
        if (re) {
            userCache.refreshCreate();

            // 刷新用户的权限
            userAuthorityService.refreshUserAuthority(user.getName());

            // 安排部门
            String deptCode = userVO.getDeptCode();
            String newDeptCode = deptCode;

            if (deptCode!=null && !"".equals(deptCode)) {
                // 多个部门处理
                String[] deptCodes = deptCode.split(",");
                newDeptCode = deptCodes[0];
                for(String tempCode : deptCodes) {
                    deptUserService.create(user.getName(), tempCode);
                }

                // 以排在第一的部门的单位作为用户的单位
                String firstCode = deptCodes[0];
                Department department = departmentService.getDepartment(firstCode);
                String unitCode = departmentService.getUnitOfDept(department).getCode();
                user = getUser(user.getName());
                user.setUnitCode(unitCode);
                user.updateById();
            } else {
                // 否则将该用户置于创建者所在的单位
                Privilege privilege = new Privilege();
                String unitCode = privilege.getUserUnitCode();
                user = getUser(user.getName());
                user.setUnitCode(unitCode);
                user.updateById();
            }
            if (userVO.getAccount() != null && !"".equals(userVO.getAccount())) {
                // 创建工号
                Account acc = new Account();
                acc.setName(userVO.getAccount());
                acc.setUserName(user.getName());
                if (!"".equals(deptCode)) {
                    String[] deptCodes = deptCode.split(",");
                    Department department = departmentService.getDepartment(deptCodes[0]);
                    String unitCode = departmentService.getUnitOfDept(department).getCode();
                    acc.setUnitCode(unitCode);
                } else {
                    Privilege pvg = new Privilege();
                    acc.setUnitCode(pvg.getUserUnitCode());
                }
                acc.insert();
            }

            userSetupService.create(user.getName());
            userSetupService.setMyleaders(user.getName(), userVO.getLeaderCode());

            // 创建门户，20220108，不再单独为用户创建门户
            // oaPortalService.init(user.getName());

            // 创建滑动菜单项
            //oaSlideMenuGroupService.init(user.getName());

            // 更新personbasic表
            userSetupService.onUserCreated(user, newDeptCode, SpringUtil.getUserName());
        }

        return re;
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean update(UserVO userVO) throws IOException {
        User oldUser = userCache.getUser(userVO.getName());
        // User oldUser = getUser(userVO.getName());
        User user = dozerBeanMapper.map(userVO, User.class);
        // 如果没有修改密码，则置user的密码为原来的密码
        if (StrUtil.isEmpty(userVO.getPassword())) {
            user.setPwd(oldUser.getPwd());
            user.setPwdRaw(oldUser.getPwdRaw());
        } else {
            user.setPwdRaw(encryptPwd(user.getPwdRaw()));
        }
        user.setId(oldUser.getId());

        // 保存附件
        MultipartFile file = userVO.getPhoto();
        if (file != null && !file.isEmpty()) {
            // 如果上传了照片，则检查原来是否已有照片，如有则删除
            if (!"".equals(user.getPhoto())) {
                fileService.del(user.getPhoto());
            }

            String vpath = "public/users"; // 放在public目录下方便手机端获取
            String name = file.getOriginalFilename();
            String ext = StrUtil.getFileExt(name);
            String diskName = FileUpload.getRandName() + "." + ext;
            user.setPhoto(vpath + "/" + diskName);
            fileService.write(file, vpath, diskName);
        } else {
            user.setPhoto(oldUser.getPhoto());
        }

        boolean re = updateByUserName(user);
        if (re) {
            // 置工号
            String account = userVO.getAccount();
            if (account!=null && !"".equals(account)) {
                Account acc = accountService.getAccount(account);
                if (acc!=null) {
                    if (!acc.getUserName().equals(user.getName())) {
                        // 如果用户原来已有工号，则置原有工号中对应的用户名为空
                        Account accMine = accountService.getAccountByUserName(user.getName());
                        if (accMine != null) {
                            accMine.setUserName("");
                            accountService.update(accMine, new QueryWrapper<Account>().eq("name", accMine.getName()));
                        }

                        // 工号已被其它人使用（也有可能为空）
                        acc.setUserName(user.getName());
                        acc.setUnitCode(user.getUnitCode());
                        accountService.update(acc, new QueryWrapper<Account>().eq("name", accMine.getName()));
                    }
                } else {
                    // 如果用户原来已有工号，则置原有工号中对应的用户名为空
                    Account accMine = accountService.getAccountByUserName(user.getName());
                    if (accMine != null) {
                        accMine.setUserName("");
                        accountService.update(accMine, new QueryWrapper<Account>().eq("name", accMine.getName()));
                    }

                    acc = new Account();
                    // 创建新工号
                    acc.setName(account);
                    acc.setUserName(user.getName());
                    acc.setUnitCode(user.getUnitCode());
                    acc.insert();
                }
            }

            // 置邮箱空间
            UserSetup userSetup = userSetupService.getUserSetup(user.getName());
            if (userSetup != null) {
                Long msgSpaceAllowed = userVO.getMsgSpaceAllowed();
                // 导入用户时，msgSpaceAllowed、getLeaderCode为null
                if (msgSpaceAllowed != null && msgSpaceAllowed != -1) {
                    userSetup.setMsgSpaceAllowed(msgSpaceAllowed);
                    userSetupService.update(userSetup, new QueryWrapper<UserSetup>().eq("user_name", user.getName()));
                }
                // 设置我的领导
                if (userVO.getLeaderCode() != null && !userVO.getLeaderCode().equals(userSetup.getMyleaders())) {
                    userSetup.setMyleaders(userVO.getLeaderCode());
                    userSetupService.updateByUserName(userSetup);
                }
            }
            else {
                log.warn("用户 " + userVO.getRealName() + " 的setup信息不存在");
            }

            // 取用户的部门，如果有多个，则以排在第一的为准
            String newDeptCode = userVO.getDeptCode();
            String[] deptCodes = StrUtil.split(userVO.getDeptCode(), ",");
            if (deptCodes!=null) {
                newDeptCode = deptCodes[0];
            }

            // 删除部门中的用户,然后新增部门人员信息
            List<Department> list = departmentService.getDeptsOfUser(user.getName());
            StringBuffer sbDepts = new StringBuffer();
            for (Department dept : list) {
                StrUtil.concat(sbDepts, ",", dept.getCode());
            }

            // 置用户所在的单位
            if (!StrUtil.isEmpty(userVO.getDeptCode()) && !userVO.getDeptCode().equals(sbDepts.toString())) {
                deptUserService.delOfUser(user.getName());

                int i = 0;
                for(String tempCode : deptCodes) {
                    deptUserService.create(user.getName(), tempCode);
                    if (i==0) {
                        Department department = departmentService.getDepartment(tempCode);
                        Department deptUnit = departmentService.getUnitOfDept(department);
                        user.setUnitCode(deptUnit.getCode());
                        updateByUserName(user);
                    }
                    i++;
                }
            }

            // 用户保存事件处理
            if (oldUser.getIsValid()!=1 && user.getIsValid()==1) {
                userSetupService.onUserCreated(user, newDeptCode, SpringUtil.getUserName());
            } else {
                userSetupService.onUserUpdated(user, newDeptCode, SpringUtil.getUserName());
            }
        }
        return re;
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean updateMyInfo(UserVO userVO) throws IOException {
        User user = getUser(userVO.getName());
        user.setRealName(userVO.getRealName());
        user.setGender(userVO.getGender());
        user.setIsMarriaged(userVO.getIsMarriaged());
        user.setBirthday(userVO.getBirthday());
        user.setQq(userVO.getQq());
        user.setEmail(userVO.getEmail());
        user.setMsn(userVO.getMsn());
        user.setPhone(userVO.getPhone());
        user.setMobile(userVO.getMobile());
        user.setIDCard(userVO.getIDCard());
        user.setDuty(userVO.getDuty());
        user.setParty(userVO.getParty());
        user.setPhone(userVO.getPhone());
        user.setResume(userVO.getResume());
        user.setAddress(userVO.getAddress());
        user.setPostCode(userVO.getPostCode());
        user.setHobbies(userVO.getHobbies());

        // 保存附件
        MultipartFile file = userVO.getPhoto();
        if (file != null && !file.isEmpty()) {
            // 前端如果photo为null，会传new Blob()
            if (!"blob".equals(file.getOriginalFilename())) {
                // 如果上传了照片，则检查原来是否已有照片，如有则删除
                if (!"".equals(user.getPhoto())) {
                    fileService.del(user.getPhoto());
                }

                String vpath = "public/users"; // 放在public目录下方便手机端获取
                String name = file.getOriginalFilename();
                String ext = StrUtil.getFileExt(name);
                String diskName = FileUpload.getRandName() + "." + ext;
                user.setPhoto(vpath + "/" + diskName);
                fileService.write(file, vpath, diskName);
            }
        }

        boolean re = updateByUserName(user);
        if (re) {
            // 取用户的部门，如果有多个，则以排在第一的为准
            String newDeptCode = userVO.getDeptCode();
            String[] deptCodes = StrUtil.split(userVO.getDeptCode(), ",");
            if (deptCodes!=null) {
                newDeptCode = deptCodes[0];
            }

            // 设置我的领导
            UserSetup userSetup = userSetupService.getUserSetup(userVO.getName());
            if (userVO.getLeaderCode()!=null && !userVO.getLeaderCode().equals(userSetup.getMyleaders())) {
                userSetup.setMyleaders(userVO.getLeaderCode());
                userSetupService.updateByUserName(userSetup);
            }

            // 用户保存事件处理
            userSetupService.onUserUpdated(user, newDeptCode, SpringUtil.getUserName());
        }
        return re;
    }

    /**
     * 删除用户
     * @param ary
     * @return
     * @throws ResKeyException
     * @throws ErrMsgException
     */
    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean delUsers(String[] ary) throws ResKeyException, ErrMsgException {
        boolean re = false;
        for (String str : ary) {
            int userId = StrUtil.toInt(str);
            User user = getById(userId);
            if (user == null) {
                throw new ErrMsgException("用户id:" + userId + "不存在");
            }
            String userName = user.getName();

            // 删除对接的spark、微信、钉钉信息
            userSetupService.onUserDeleted(user, SpringUtil.getUserName());

            // 删除UserSetup
            userSetupService.del(userName);

            // 删除部门中的用户
            deptUserService.delOfUser(userName);

            // 删除角色中的用户
            userOfRoleService.delOfUser(userName);

            // 删除用户组中的用户
            userOfGroupService.delOfUser(userName);

            // 删除用户的权限
            userPrivService.delOfUser(userName);

            // 删除用户所拥有的权限（包括所属角色、组及用户自己的权限）
            userAuthorityService.delOfUser(userName);

            // @task:删除用户所管理的部门

            // 删除桌面模块
            userDesktopSetupService.delOfUser(userName);

            // 删除门户
            oaPortalService.delOfUser(userName);

            // 删除滑动菜单项
            oaSlideMenuGroupService.delOfUser(userName);

            // 删除用户的工号
            accountService.delOfUser(userName);

            // 删除职位
            postUserService.delByUserName(userName);

            re = user.deleteById();

            if (re) {
                userCache.refreshDel(user.getName());

                // 删除成功之后要更新表user_recently_selected，此表用来记录最近选择的用户
                QueryWrapper<UserRecentlySelected> qwRecent = new QueryWrapper<>();
                qwRecent.eq("userName", userName);
                userRecentlySelectedService.remove(qwRecent);
            }
        }
        return re;
    }

    @Override
    public List<String> listNameBySql(String sql) {
        return userMapper.listNameBySql(sql);
    }

    @Override
    public List<User> listBySql(String sql) {
        return userMapper.listBySql(sql);
    }

    /**
     * 通讯录列表
     * @param what
     * @return
     */
    @Override
    public List<User> listForAddress(String what) {
        String sql = "select * from users where name<>'system' and name<>'admin'";
        if (!StrUtil.isEmpty(what)) {
            sql += " and (realname like " + StrUtil.sqlstr("%" + what + "%") + " or mobile like " + StrUtil.sqlstr("%" + what + "%") + ")";
        }
        return listBySql(sql);
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean reEmploryment(int id, String operator) {
        User user = getById(id);
        user.setIsValid(1);
        boolean re = user.updateById();
        if (re) {
            userCache.refreshSave(user.getName());
            userSetupService.onUserCreated(user, null, operator);
        }
        return re;
    }

    @Override
    public List<User> listAll() {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.ne("name", ConstUtil.USER_SYSTEM);
        qw.eq("isvalid", 1).orderByDesc("regDate");
        return list(qw);
    }

    @Override
    public boolean refreshOrders(String userName) {
        // 始终刷新orders字段，因为有些场景下可能仍需按orders排序
        int order = 0;
        List<UserOfRole> list = userOfRoleService.listByUserName(userName);
        for (UserOfRole userOfRole : list) {
            Role role = roleService.getRole(userOfRole.getRoleCode());
            if (role!=null) {
                if (role.getOrders() > order) {
                    order = role.getOrders();
                }
            }
        }

        Integer r = userMapper.updateUserOrder(userName, order);
        return r > 0;
    }

    /**
     * 对密码进行加密
     * @param pwd
     * @return
     */
    @Override
    public String encryptPwd(String pwd) {
        return Base64Util.encode(pwd);
    }

    @Override
    public String aesEncryptPwdRaw(String pwdRaw) {
        String pwdRawStr = Base64Util.decode(pwdRaw);
        com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
        String pwdAesKey = myconfig.getProperty("pwdAesKey");
        String pwdAesIV = myconfig.getProperty("pwdAesIV");
        try {
            return AesUtil.aesEncrypt(pwdRawStr, pwdAesKey, pwdAesIV);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return "";
    }

    /**
     * 修改密码
     * @param userName
     * @param newPwd 密码
     * @return
     * @throws ErrMsgException
     */
    @Override
    public boolean modifyPwd(String userName, String newPwd) {
        User user = getUser(userName);
        try {
            user.setPwd(SecurityUtil.MD5(newPwd));
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        user.setPwdRaw(encryptPwd(newPwd));
        updateByUserName(user);

        // userSetupService.onUserUpdated中主要是同步至微信及钉钉，仅更改密码无需同步
        /*DeptUser deptUser = deptUserService.getPrimary(userName);
        // 群机器人中自动创建帐户，但并未分配部门
        String deptCode;
        if (deptUser != null) {
            deptCode = deptUser.getDeptCode();
        }
        else {
            deptCode = "";
        }

        userSetupService.onUserUpdated(user, deptCode, SpringUtil.getUserName());*/

        return true;
    }

    @Override
    public boolean updateByUserName(User user) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("name", user.getName());
        boolean re = update(user, qw);
        if (re) {
            userCache.refreshSave(user.getName());
        }
        return re;
    }

    @Override
    public List<User> listByUnitCode(String unitCode) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("unit_code", unitCode)
                .eq("isValid", 1).orderByDesc("regDate");
        return list(qw);
    }

    @Override
    public int getValidUserCount() {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("isValid", ConstUtil.USER_VALID_WORKING);
        return userMapper.selectCount(qw);
    }

    @Override
    public boolean isPersonNoUsedByOther(String userName, String personNo) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.ne("login_name", userName)
                .eq("person_no", personNo);
        User user = getOne(qw, false);
        return user != null;
    }

    /**
     * 获取全部部门code对应部门全称的hash表,用于根据全称查询code
     * @return
     */
    public HashMap<String, String> getAllFullName() {
        HashMap<String, String> map = new HashMap<String, String>();

        Department rootDept = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
        List<Department> allList = new ArrayList<>();
        departmentService.getAllChild(allList, ConstUtil.DEPT_ROOT);
        allList.add(rootDept);
        for (Department dept : allList) {
            String code = dept.getCode();
            String name = getFullNameOfDept(code);
            map.put(name, code);
        }
        return map;
    }

    /**
     * 取得部门全称
     *
     * @param code
     * @return
     */
    public String getFullNameOfDept(String code) {
        Department dept = departmentService.getDepartment(code);
        String name = dept.getName();
        while (!dept.getParentCode().equals("-1")
                && !dept.getParentCode().equals(ConstUtil.DEPT_ROOT)) {
            dept = departmentService.getDepartment(dept.getParentCode());
            if (dept != null && !dept.getParentCode().equals("")) {
                name = dept.getName() + "-" + name;
            } else {
                return "";
            }
        }
        return name;
    }

    @Override
    public boolean importUser(com.alibaba.fastjson.JSONArray arr) throws ValidateException, IOException, ErrMsgException {
        Privilege privilege = new Privilege();
        boolean re = false;
        for (int i = 0; i < arr.size(); i++) {
            com.alibaba.fastjson.JSONObject json = arr.getJSONObject(i);

            User user = new User();
            String userName = json.getString("name");
            // name在创建时会用自定义的id代替
            // user.setName(userName);
            user.setLoginName(userName);
            user.setRealName(json.getString("realName"));
            user.setMobile(json.getString("mobile"));
            user.setGender("女".equals(json.getString("gender")));
            String personNo = json.getString("personNo");
            if ("".equals(personNo)) {
                personNo = getNextPersonNo();
            }
            user.setPersonNo(personNo);
            user.setIDCard(json.getString("idCard"));
            user.setIsMarriaged("已婚".equals(json.getString("marriaged")));
            user.setBirthday(DateUtil.parseLocalDate(json.getString("birthday"), "yyyy-MM-dd"));
            user.setEmail(json.getString("email"));
            user.setQq(json.getString("QQ"));
            user.setPhone(json.getString("phone"));
            user.setEntryDate(DateUtil.parseLocalDate(json.getString("entryDate"), "yyyy-MM-dd"));
            user.setMsn(json.getString("shortMobile"));
            user.setHobbies(json.getString("hobbies"));
            user.setAddress(json.getString("address"));

            HashMap<String, String> map = getAllFullName();

            String deptCode = "";
            String depName = "";
            String parentCode = "root";
            // 更改为支持多部门导入
            String dept = json.getString("dept");
            // 转换全角的逗号
            dept = dept.replaceAll("，", ",");
            String depts[] = dept.split(",");
            //存放多个deptcode
            String deptCodes = "";
            for (int ii = 0; ii < depts.length; ii++) {
                String newDeptName[] = depts[ii].split("\\\\");
                int depLevel = newDeptName.length;
                parentCode = "root";
                deptCode = "";
                depName = "";
                for (int j = 0; j < depLevel; j++) {
                    depName += depName.equals("") ? newDeptName[j] : ("-" + newDeptName[j]);
                    deptCode = map.get(depName);
                    if (deptCode == null || deptCode.equals("")) {
                        Department childLeaf = new Department();
                        deptCode = departmentService.generateNewNodeCode(parentCode);
                        childLeaf.setCode(deptCode);
                        childLeaf.setName(newDeptName[j]);
                        childLeaf.setDeptType(DeptDb.TYPE_DEPT);
                        childLeaf.setIsShow(1);
                        childLeaf.setParentCode(parentCode);
                        departmentService.createAnySyn(childLeaf);

                        map.put(depName, deptCode);
                    }
                    parentCode = deptCode;
                }
                if ("".equals(deptCodes)) {
                    deptCodes = deptCode;
                } else {
                    deptCodes += "," + deptCode;
                }
            }

            UserVO userVO = dozerBeanMapper.map(user, UserVO.class);
            userVO.setAccount(json.getString("accountName"));
            userVO.setDeptCode(deptCodes);
            userVO.setIsPass(1);
            User user2 = getUserByLoginName(userName);
            if (user2==null) {
                re = create(userVO);
            } else {
                userVO.setName(user2.getName());
                userVO.setIsValid(1);
                re = update(userVO);
            }

            String roleName = StrUtil.getNullStr(json.getString("roleName"));
            // -号表示不设置
            if (!roleName.startsWith("-")) {
                roleName = roleName.replaceAll("，", ",");
                String[] rNames = StrUtil.split(roleName, ",");
                String[] rCodes = null;
                if (rNames != null) {
                    IRoleService roleService = SpringUtil.getBean(IRoleService.class);
                    rCodes = new String[rNames.length];
                    for (int n = 0; n < rNames.length; n++) {
                        String roleCode;
                        Role role = roleService.getRoleByDesc(rNames[n]);
                        if (role == null) {
                            // 如果角色不存在，则创建
                            roleCode = RandomSecquenceCreator.getId(20);
                            roleService.create(roleCode, rNames[n], 0, 0, -1, privilege.getUserUnitCode(), -1, 0, "", null);
                        }
                        else {
                            roleCode = role.getCode();
                        }
                        rCodes[n] = roleCode;
                    }
                }

                IUserOfRoleService userOfRoleService = SpringUtil.getBean(IUserOfRoleService.class);
                userOfRoleService.setRoleOfUser(userVO.getName(), rCodes);
            }
        }
        return re;
    }

    @Override
    public int updateUserUnitInDept(String unitCode, String deptCode) {
        return userMapper.updateUserUnitInDept(unitCode, deptCode);
    }

    @Override
    public List<User> listByOnlineTime() {
        return userMapper.listByOnlineTime();
    }
}
