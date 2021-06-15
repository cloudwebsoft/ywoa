package com.cloudweb.oa.controller;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.vo.UserAuthorityVO;
import com.redmoon.oa.Config;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.dept.DeptView;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.SkinMgr;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Api(tags = "组织机构管理模块")
@Controller
@RequestMapping("/admin/organize")
public class OrganizeController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IDepartmentService departmentService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IUserSetupService userSetupService;

    @Autowired
    private IPrivilegeService privilegeService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IGroupService userGroupService;

    @Autowired
    private IRolePrivService userRolePrivService;

    @Autowired
    private IGroupPrivService userGroupPrivService;

    @Autowired
    private IMenuService menuService;

    @Autowired
    private IUserPrivService userPrivService;

    @Autowired
    private IUserOfRoleService userOfRoleService;

    @Autowired
    private IUserOfGroupService userOfGroupService;

    @Autowired
    private IGroupOfRoleService userGroupOfRoleService;

    @Autowired
    private IUserAdminDeptService userAdminDeptService;

    @Autowired
    private IRoleDeptService userRoleDeptService;

    @Autowired
    private IDeptUserService deptUserService;

    @Autowired
    private IRolePrivService rolePrivService;

    @Autowired
    private  IGroupPrivService groupPrivService;

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/organize")
    public String organize(@RequestParam(value = "curDeptCode", required = false) String curDeptCode, Model model) {
        String type = ParamUtil.get(request, "type");
        model.addAttribute("type", type);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("curDeptCode", curDeptCode);
        return "th/admin/organize/organize";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/organizeFrameList")
    public String organizeFrameList(Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        return "th/admin/organize/organize_frame_list";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/organizeFrameAdd")
    public String organizeFrameAdd(String curDeptCode, Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("curDeptCode", curDeptCode);
        return "th/admin/organize/organize_frame_add";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/listDeptTree")
    public String listDeptTree(@RequestParam(value = "rootCode", required = false) String rootCode,
                               @RequestParam(required = false, defaultValue = "list") String pageType,
                               @RequestParam(required = false) String curDeptCode, Model model) {
        if (rootCode == null) {
            Privilege privilege = new Privilege();
            rootCode = privilege.getUserUnitCode(request);
        }

        DeptMgr dm = new DeptMgr();
        DeptDb leaf = dm.getDeptDb(rootCode);
        DeptView dv = new DeptView(leaf);
        ArrayList<String> listHided = dv.getAllHided();
        String jsonData = dv.getJsonString(false, true);

        model.addAttribute("rootCode", rootCode);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("flag", "0");

        model.addAttribute("pageType", pageType);

        model.addAttribute("listHided", StringUtils.join(listHided, ","));
        model.addAttribute("jsonData", jsonData);

        List<String> list = null;
        try {
            list = dv.getAllUnit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("unitStr", StringUtils.join(list, ","));

        if (null == curDeptCode) {
            curDeptCode = DeptDb.ROOTCODE;
        }
        model.addAttribute("curDeptCode", curDeptCode);

        return "th/admin/organize/dept_tree";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/addUser")
    public String addUser(@RequestParam(value = "curDeptCode", required = false) String curDeptCode, Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        boolean isMobileRequired = false;
        if (weixinCfg.getBooleanProperty("isUse") || dingdingCfg.isUseDingDing()) {
            isMobileRequired = true;
        }
        model.addAttribute("isMobileRequired", isMobileRequired);

        String defaultPwd = "", defaultPwdDesc = "";
        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        // if (scfg.isForceChangeInitPassword()) {
            defaultPwd = scfg.getInitPassword();
            defaultPwdDesc = "默认密码：" + scfg.getInitPassword();
        // }
        model.addAttribute("defaultPwd", defaultPwd);
        model.addAttribute("defaultPwdDesc", defaultPwdDesc);

        String curDeptName = "";
        if (curDeptCode != null) {
            Department department = departmentService.getDepartment(curDeptCode);
            // 可能在刚删除的部门上操作
            if (department != null) {
                curDeptName = department.getName();
                curDeptCode = "";
            }
        } else {
            curDeptCode = "";
        }
        model.addAttribute("curDeptCode", curDeptCode);
        model.addAttribute("curDeptName", curDeptName);

        String userTypeOpts = "";
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect("user_type");
        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        Iterator irType = vType.iterator();
        while (irType.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb) irType.next();
            String selected = "";
            if (sod.isDefault()) {
                selected = "selected";
            }
            String clr = "";
            if (!sod.getColor().equals("")) {
                clr = " style='color:" + sod.getColor() + "' ";
            }
            userTypeOpts += "<option value='" + sod.getValue() + "' " + selected + clr + ">" + sod.getName() + "</option>";
        }
        model.addAttribute("userTypeOpts", userTypeOpts);

        String pn = "";
        Config cfg = Config.getInstance();
        if (cfg.getBooleanProperty("personNoAutoCreate")) {
            pn = userService.getNextPersonNo();
        }
        model.addAttribute("nextPersonNo", pn);

        return "th/admin/organize/user_add";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/listUser", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String listUser(Model model) {
        String skinPath = SkinMgr.getSkinPath(request, false);
        model.addAttribute("skinPath", skinPath);

        String searchType = ParamUtil.get(request, "searchType"); // realName-姓名、userName-帐户、account-工号、mobile-手机、Email
        if ("".equals(searchType)) {
            searchType = "realName";
        }

        model.addAttribute("searchType", searchType);

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isBindMobile = cfg.getBooleanProperty("is_bind_mobile");
        boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");

        String op = ParamUtil.get(request, "op");
        String type = ParamUtil.get(request, "type");
        int curPage = ParamUtil.getInt(request, "CPages", 1);
        int pageSize = ParamUtil.getInt(request, "pageSize", 20);
        String content = ParamUtil.get(request, "content");
        content = content.replaceAll("，", ",");
        String condition = ParamUtil.get(request, "condition");
        int isValid = ParamUtil.getInt(request, "isValid", 1);    //1:在职  0：离职
        String orderBy = ParamUtil.get(request, "orderBy");
        if ("".equals(orderBy)) {
            orderBy = "regDate";
        }
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(sort)) {
            sort = "desc";
        }
        String deptCode = ParamUtil.get(request, "deptCode");

        model.addAttribute("op", op);
        model.addAttribute("type", type);
        model.addAttribute("curPage", curPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("content", content);
        model.addAttribute("condition", condition);
        model.addAttribute("isValid", isValid);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("sort", sort);
        model.addAttribute("deptCode", deptCode);

        com.redmoon.oa.sso.Config ssocfg = new com.redmoon.oa.sso.Config();
        boolean isLarkUsed = cfg.getBooleanProperty("isLarkUsed") && ssocfg.getBooleanProperty("isUse");
        model.addAttribute("isLarkUsed", isLarkUsed);

        com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
        boolean isUseWx = weixinCfg.getBooleanProperty("isUse");
        boolean isSyncWxToOa = weixinCfg.getBooleanProperty("isSyncWxToOA");
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        boolean isUseDingDing = dingdingCfg.getBooleanProperty("isUse");
        boolean isSyncDingDingToOa = dingdingCfg.getBooleanProperty("isSyncDingDingToOA");

        model.addAttribute("isUseWx", isUseWx);
        model.addAttribute("isSyncWxToOA", isSyncWxToOa);
        model.addAttribute("isUseDingDing", isUseDingDing);
        model.addAttribute("isSyncDingDingToOA", isSyncDingDingToOa);

        Privilege pvg = new Privilege();
        String unitCode = pvg.getUserUnitCode(request);
        model.addAttribute("unitCode", unitCode);

        com.redmoon.oa.Config oacfg = new com.redmoon.oa.Config();
        boolean canDelUser = oacfg.getBooleanProperty("canDelUser");
        model.addAttribute("canDelUser", canDelUser);

        boolean isUseAccount = cfg.getBooleanProperty("isUseAccount");
        StringBuffer colProps = new StringBuffer();
        if (showByDeptSort && !"".equals(deptCode) && !ConstUtil.DEPT_ROOT.equals(deptCode)) {
            colProps.append("{display: '序号', name : 'deptOrder', width : 50, sortable : true, align: 'center', hide: false, process:editCol}");
            colProps.append(",{display: '帐号', name : 'name', width : 100, sortable : true, align: 'center', hide: false}");
        } else {
            colProps.append("{display: '帐号', name : 'name', width : 100, sortable : true, align: 'center', hide: false}");
        }
        colProps.append(",{display: '姓名', name : 'realName', width : 100, sortable : true, align: 'center', hide: false}");
        colProps.append(",{display: '性别', name : 'sex', width : 50, sortable : true, align: 'center', hide: false}");
        if (isUseAccount) {
            colProps.append(",{display: '工号', name : 'account', width : 80, sortable : true, align: 'center', hide: false}");
        }
        colProps.append(",{display: '所属部门', name : 'deptNames', width : 180, sortable : true, align: 'center', hide: false}");
        colProps.append(",{display: '角色', name : 'roleName', width : 150, sortable : true, align: 'center', hide: false}");
        colProps.append(",{display: '手机号', name : 'mobile', width : 100, sortable : true, align: 'center', hide: false}");
        colProps.append(",{display: '状态', name : 'status', width : 50, sortable : true, align: 'center', hide: false}");
        if (isBindMobile) {
            colProps.append(",{display: '手机绑定', name : 'isBindMobile', width : 100, sortable : true, align: 'center', hide: false}");
        }
        colProps.append(",{display: '操作', name : 'op', width : 80, sortable : false, align: 'center', hide: false}");
        model.addAttribute("colProps", colProps);
        model.addAttribute("isBindMobile", isBindMobile);

        return "th/admin/organize/user_list";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/editUser")
    public String editUser(@RequestParam(value = "userName", required = true) String userName, Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        User user = userService.getUser(userName);
        model.addAttribute("user", user);

        String selectDeptCode = ParamUtil.get(request, "selectDeptCode");
        model.addAttribute("selectDeptCode", selectDeptCode);

        StringBuffer sbDeptCode = new StringBuffer();
        StringBuffer sbDeptName = new StringBuffer();
        List<Department> list = departmentService.getDeptsOfUser(userName);
        for (Department department : list) {
            StrUtil.concat(sbDeptCode, ",", department.getCode());
            StrUtil.concat(sbDeptName, ",", department.getName());
        }
        model.addAttribute("deptCode", sbDeptCode.toString());
        model.addAttribute("deptName", sbDeptName.toString());

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isUseAccount = cfg.getBooleanProperty("isUseAccount");
        String accountName = "";
        if (isUseAccount) {
            Account account = accountService.getAccountByUserName(userName);
            if (account != null) {
                accountName = account.getName();
            }
        }
        model.addAttribute("isUseAccount", isUseAccount);
        model.addAttribute("account", accountName);

        boolean isMobileNotEmpty = false;
        com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        if (weixinCfg.getBooleanProperty("isUse") || dingdingCfg.isUseDingDing()) {
            isMobileNotEmpty = true;
        }
        model.addAttribute("isMobileNotEmpty", isMobileNotEmpty);

        StringBuffer typeOpts = new StringBuffer();
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect("user_type");
        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        Iterator irType = vType.iterator();
        while (irType.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb) irType.next();
            String selected = "";
            if (sod.isDefault()) {
                selected = "selected";
            }
            String clr = "";
            if (!sod.getColor().equals("")) {
                clr = " style='color:" + sod.getColor() + "' ";
            }
            typeOpts.append("<option value='" + sod.getValue() + "'" + selected + clr + ">" + sod.getName() + "</option>");
        }
        model.addAttribute("userTypeOpts", typeOpts);

        long msgSpaceAllowed = 0;
        String leaders = "";
        String leaderNames = "";
        UserSetup userSetup = userSetupService.getUserSetup(user.getName());
        if (userSetup != null) {
            leaders = StrUtil.getNullStr(userSetup.getMyleaders());
            if (!leaders.equals("")) {
                String[] leadersAry = StrUtil.split(leaders, ",");
                for (int i = 0; i < leadersAry.length; i++) {
                    User myUser = userService.getUser(leadersAry[i]);
                    if (myUser == null) {
                        continue;
                    }
                    leaderNames += (leaderNames.equals("") ? "" : ",") + myUser.getRealName();
                }
            }
            msgSpaceAllowed = userSetup.getMsgSpaceAllowed();
        }
        model.addAttribute("leaders", leaders);
        model.addAttribute("leaderNames", leaderNames);
        model.addAttribute("msgSpaceAllowed", msgSpaceAllowed);

        return "th/admin/organize/user_edit";
    }

    /**
     * 将各个权限可见的菜单项存于map中
     * @param map
     * @param menu
     */
    public void addMap(Map<String, List<Menu>> map, Menu menu) {
        // 取可以看到菜单项lf的权限
        String pvgCodes = menu.getPvg();
        String[] ary = StrUtil.split(pvgCodes, ",");
        if (ary != null) {
            for (int i = 0; i < ary.length; i++) {
                if (ary[i].equals("admin")) {
                    continue;
                }
                List<Menu> menuList = map.get(ary[i]);
                if (menuList == null) {
                    menuList = new ArrayList<>();
                    menuList.add(menu);
                    map.put(ary[i], menuList);
                } else {
                    menuList.add(menu);
                }
            }
        }
    }

    @ApiOperation(value = "进入用户权限管理界面", notes = "进入用户权限管理界面，用户名或用户ID至少有一个不为空")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = false, dataType = "String"),
            @ApiImplicitParam(name = "userId", value = "用户ID", required = false, dataType = "Integer")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/userPriv")
    public String userPriv(String userName, Integer userId, Model model) throws ValidateException {
        if (userName==null && userId==null) {
            throw new ValidateException("用户名或用户ID不能为空");
        }

        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        User user;
        if (userName!=null) {
            user = userService.getUser(userName);
        }
        else {
            user = userService.getById(userId);
        }
        model.addAttribute("userName", user.getName());
        model.addAttribute("realName", user.getRealName());
        model.addAttribute("isMeAdmin", new Privilege().isUserPrivValid(request, Privilege.ADMIN));

        List<Role> roleList = roleService.getRolesOfUser(user.getName(), true);
        List<Group> groupList = userGroupService.getGroupsOfUser(user.getName(), true);

        Map<String, List<Menu>> map = new HashMap<>();
        List<Menu> menuList = menuService.getChildren(ConstUtil.MENU_ROOT);
        for (Menu menu : menuList) {
            // 置生成的链接
            menu.setRealLink(menuService.getRealLink(menu));
            menu.setFullName(menuService.getFullName(menu));
            addMap(map, menu);

            List<Menu> menuList2 = menuService.getChildren(menu.getCode());
            for (Menu menu2 : menuList2) {
                menu2.setRealLink(menuService.getRealLink(menu2));
                menu2.setFullName(menuService.getFullName(menu2));
                addMap(map, menu2);

                List<Menu> menuList3 = menuService.getChildren(menu2.getCode());
                for (Menu menu3 : menuList3) {
                    menu3.setRealLink(menuService.getRealLink(menu3));
                    menu3.setFullName(menuService.getFullName(menu3));
                    addMap(map, menu3);
                }
            }
        }

        List<UserAuthorityVO> authorityVOList = new ArrayList<>();
        List<com.cloudweb.oa.entity.Privilege> list = privilegeService.getAll();

        for (com.cloudweb.oa.entity.Privilege privilege : list) {
            UserAuthorityVO userAuthorityVO = new UserAuthorityVO();
            userAuthorityVO.setCode(privilege.getPriv());
            userAuthorityVO.setName(privilege.getDescription());
            userAuthorityVO.setLayer(privilege.getLayer());
            userAuthorityVO.setIsAdmin(privilege.getIsAdmin()==1);

            // boolean isAuthorized = pvg.isUserPrivValid(userName, privilege.getPriv());
            boolean isAuthorized = userPrivService.isUserPrivValid(userName, privilege.getPriv());
            userAuthorityVO.setAuthorized(isAuthorized);

            if (rolePrivService.isRolePrivValid(ConstUtil.ROLE_MEMBER, privilege.getPriv())) {
                userAuthorityVO.setRoleAuthorized(true);
                Role role = roleService.getRole(ConstUtil.ROLE_MEMBER);
                userAuthorityVO.getRoleList().add(role);
            }

            for (Role role : roleList) {
                if (userRolePrivService.isRolePrivValid(role.getCode(), privilege.getPriv())) {
                    userAuthorityVO.setRoleAuthorized(true);
                    userAuthorityVO.getRoleList().add(role);
                }
            }

            if (groupPrivService.isGroupPrivValid(ConstUtil.GROUP_EVERYONE, privilege.getPriv())) {
                userAuthorityVO.setGroupAuthorized(true);
                Group group = userGroupService.getGroup(ConstUtil.GROUP_EVERYONE);
                userAuthorityVO.getGroupList().add(group);
            }

            for (Group group : groupList) {
                if (userGroupPrivService.isGroupPrivValid(group.getCode(), privilege.getPriv())) {
                    userAuthorityVO.setGroupAuthorized(true);
                    userAuthorityVO.getGroupList().add(group);
                }
            }

            userAuthorityVO.setMenuList(map.get(privilege.getPriv()));

            authorityVOList.add(userAuthorityVO);
        }

        model.addAttribute("authorityVOList", authorityVOList);

        return "th/admin/organize/user_priv";
    }

    @ApiOperation(value = "进入用户角色管理界面", notes = "进入用户角色管理界面", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = true, dataType = "String")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/userSetRole")
    public String userSetRole(String userName, Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        User user = userService.getUser(userName);
        model.addAttribute("userName", user.getName());
        model.addAttribute("realName", user.getRealName());

        StringBuffer roleCodes = new StringBuffer();
        StringBuffer descs = new StringBuffer();

        List<UserOfRole> list = userOfRoleService.listByUserName(userName);
        for (UserOfRole userOfRole : list) {
            String roleCode = userOfRole.getRoleCode();
            StrUtil.concat(roleCodes, ",", roleCode);
            StrUtil.concat(descs, ",", roleService.getRole(roleCode).getDescription());
        }

        model.addAttribute("roleCodes", roleCodes.toString());
        model.addAttribute("descs", descs);

        Privilege pvg = new Privilege();
        String unitCode = pvg.getUserUnitCode(request);
        model.addAttribute("unitCode", unitCode);

        StringBuffer sbGroupRoleDesc = new StringBuffer();
        // 取得用户所有的组
        List<UserOfGroup> ugList = userOfGroupService.listByUserName(userName);
        for (UserOfGroup userOfGroup : ugList) {
            // 取得组信息
            Group group = userGroupService.getGroup(userOfGroup.getGroupCode());
            List<GroupOfRole> grList = userGroupOfRoleService.listByGroupCode(group.getCode());
            for (GroupOfRole groupOfRole : grList) {
                // 取得组所属的角色
                Role role = roleService.getRole(groupOfRole.getRoleCode());
                StrUtil.concat(sbGroupRoleDesc, ",", group.getDescription() + ":" + role.getDescription());
            }
        }
        model.addAttribute("groupRoleDesc", sbGroupRoleDesc);

        return "th/admin/organize/user_set_role";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/userAdminDept")
    public String userAdminDept(String userName, Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        User user = userService.getUser(userName);
        model.addAttribute("userName", userName);
        model.addAttribute("realName", user.getRealName());

        StringBuffer sbDepts = new StringBuffer();
        StringBuffer sbDeptNames = new StringBuffer();
        List<UserAdminDept> list = userAdminDeptService.listByUserName(userName);
        for (UserAdminDept userAdminDept : list) {
            Department department = departmentService.getDepartment(userAdminDept.getDeptCode());
            StrUtil.concat(sbDepts, ",", department.getCode());
            StrUtil.concat(sbDeptNames, ",", department.getName());
        }
        model.addAttribute("depts", sbDepts.toString());
        model.addAttribute("deptNames", sbDeptNames.toString());

        Map map = new LinkedHashMap();

        // 取得用户所属的角色能够管理的部门
        List<String> deptNameList = new ArrayList<>();
        List<UserOfRole> urList = userOfRoleService.listByUserName(userName);
        for (UserOfRole userOfRole : urList) {
            String roleCode = userOfRole.getRoleCode();
            // 取得角色能管理的部门
            List<RoleDept> rdList = userRoleDeptService.listByRoleCode(roleCode);
            for (RoleDept roleDept : rdList) {
                Department department = departmentService.getDepartment(roleDept.getDeptCode());
                deptNameList.add(department.getName());
            }

            // 如果角色可以管理所在的部门，则取得用户所在的部门
            Role role = roleService.getRole(roleCode);
            if ("1".equals(role.getIsDeptManager())) {
                // 过滤掉重复项
                List<DeptUser> duList = deptUserService.listByUserName(userName);
                for (DeptUser deptUser : duList) {
                    Department department = departmentService.getDepartment(deptUser.getDeptCode());
                    if (!deptNameList.contains(department.getName())) {
                        deptNameList.add(department.getName());
                    }
                }
            }

            String str = StringUtils.join(deptNameList, ",");
            if ("1".equals(role.getIsDeptManager())) {
                str += "(角色可管理本部门)";
            }
            else {
                str += "(角色不可管理本部门)";
            }
            map.put(role.getDescription(), str);
        }
        model.addAttribute("adminDeptMap", map);

        return "th/admin/organize/user_admin_dept";
    }


}
