package com.cloudweb.oa.controller;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudweb.oa.vo.UserAuthorityVO;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.dept.DeptView;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.SkinMgr;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @ApiOperation(value = "系统信息展示列表", notes = "系统信息展示列表", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "curDeptCode", value = "curDeptCode", dataType = "Integer"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/organize")
    @ResponseBody
    public Result<Object> organize(@RequestParam(value = "curDeptCode", required = false) String curDeptCode) {
        JSONObject object = new JSONObject();
        String type = ParamUtil.get(request, "type");
        object.put("type", type);
        object.put("curDeptCode", curDeptCode);

        Config cfg = Config.getInstance();
        object.put("isDeptCodeAuto", cfg.getBooleanProperty("isDeptCodeAuto"));
        object.put("isPostUsed", cfg.get("isPostUsed"));
        return new Result<>(object);
    }

//    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
//    @RequestMapping(value = "/organizeFrameList")
//    public Result<Object> organizeFrameList(Model model) {
//        object.put("skinPath", SkinMgr.getSkinPath(request, false));
//        return "th/admin/organize/organize_frame_list";
//    }

//    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
//    @RequestMapping(value = "/organizeFrameAdd")
//    public Result<Object> organizeFrameAdd(String curDeptCode, Model model) {
//        object.put("skinPath", SkinMgr.getSkinPath(request, false));
//        object.put("curDeptCode", curDeptCode);
//        return "th/admin/organize/organize_frame_add";
//    }

    @ApiOperation(value = "部门树", notes = "部门树", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "rootCode", value = "权限编码", dataType = "Integer"),
            @ApiImplicitParam(name = "pageType", value = "pageType", dataType = "Integer"),
            @ApiImplicitParam(name = "curDeptCode", value = "curDeptCode", dataType = "Integer"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/listDeptTree")
    @ResponseBody
    public Result<Object> listDeptTree(@RequestParam(value = "rootCode", required = false) String rootCode,
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
        JSONObject object = new JSONObject();

        object.put("rootCode", rootCode);
        object.put("flag", "0");

        object.put("pageType", pageType);

        object.put("listHided", StringUtils.join(listHided, ","));
        object.put("jsonData", jsonData);

        List<String> list = null;
        try {
            list = dv.getAllUnit();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        object.put("unitStr", StringUtils.join(list, ","));

        if (null == curDeptCode) {
            curDeptCode = DeptDb.ROOTCODE;
        }
        object.put("curDeptCode", curDeptCode);

        return new Result<>(object);
    }

    @ApiOperation(value = "添加用户", notes = "添加用户", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "curDeptCode", value = "部门编码", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/addUser")
    @ResponseBody
    public Result<Object> addUser(@RequestParam(value = "curDeptCode", required = false) String curDeptCode, Model model) {
        HashMap hashMap = new HashMap();
        com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        boolean isMobileRequired = false;
        if (weixinCfg.getBooleanProperty("isUse") || dingdingCfg.isUseDingDing()) {
            isMobileRequired = true;
        }
        hashMap.put("isMobileRequired", isMobileRequired);

        String defaultPwd = "", defaultPwdDesc = "";
        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        // if (scfg.isForceChangeInitPassword()) {
            defaultPwd = scfg.getInitPassword();
            defaultPwdDesc = "默认密码：" + scfg.getInitPassword();
        // }
        hashMap.put("defaultPwd", defaultPwd);
        hashMap.put("defaultPwdDesc", defaultPwdDesc);

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
        hashMap.put("curDeptCode", curDeptCode);
        hashMap.put("curDeptName", curDeptName);

        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect("user_type");

        hashMap.put("userTypeOpts", sd);

        String pn = "";
        Config cfg = Config.getInstance();
        if (cfg.getBooleanProperty("personNoAutoCreate")) {
            pn = userService.getNextPersonNo();
        }
        hashMap.put("nextPersonNo", pn);

        return new Result<>(hashMap);
    }

    @ApiOperation(value = "用户列表", notes = "用户列表", httpMethod = "GET")
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/listUser", produces = {"text/html;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> listUser() {
        JSONObject object = new JSONObject();

        String searchType = ParamUtil.get(request, "searchType"); // realName-姓名、userName-帐户、account-工号、mobile-手机、Email
        if ("".equals(searchType)) {
            searchType = "realName";
        }

        object.put("searchType", searchType);

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

        object.put("op", op);
        object.put("type", type);
        object.put("curPage", curPage);
        object.put("pageSize", pageSize);
        object.put("content", content);
        object.put("condition", condition);
        object.put("isValid", isValid);
        object.put("orderBy", orderBy);
        object.put("sort", sort);
        object.put("deptCode", deptCode);

        com.redmoon.oa.sso.Config ssocfg = new com.redmoon.oa.sso.Config();
        boolean isLarkUsed = cfg.getBooleanProperty("isLarkUsed") && ssocfg.getBooleanProperty("isUse");
        object.put("isLarkUsed", isLarkUsed);

        com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
        boolean isUseWx = weixinCfg.getBooleanProperty("isUse");
        boolean isSyncWxToOa = weixinCfg.getBooleanProperty("isSyncWxToOA");
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        boolean isUseDingDing = dingdingCfg.getBooleanProperty("isUse");
        boolean isSyncDingDingToOa = dingdingCfg.getBooleanProperty("isSyncDingDingToOA");

        object.put("isUseWx", isUseWx);
        object.put("isSyncWxToOA", isSyncWxToOa);
        object.put("isUseDingDing", isUseDingDing);
        object.put("isSyncDingDingToOA", isSyncDingDingToOa);

        Privilege pvg = new Privilege();
        String unitCode = pvg.getUserUnitCode(request);
        object.put("unitCode", unitCode);

        com.redmoon.oa.Config oacfg = new com.redmoon.oa.Config();
        boolean canDelUser = oacfg.getBooleanProperty("canDelUser");
        object.put("canDelUser", canDelUser);

        boolean isUseAccount = cfg.getBooleanProperty("isUseAccount");

        com.alibaba.fastjson.JSONArray ary = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONObject jsonChk = new com.alibaba.fastjson.JSONObject();
        jsonChk.put("type", "checkbox");
        jsonChk.put("align", "center");
        jsonChk.put("fixed", "left");
        ary.add(jsonChk);

        if (showByDeptSort && !"".equals(deptCode) && !ConstUtil.DEPT_ROOT.equals(deptCode)) {
            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            json.put("title", "序号");
            json.put("field", "deptOrder");
            json.put("width", 60);
            json.put("sort", false);
            json.put("align", "center");
            ary.add(json);
        }

        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        json.put("title", "帐号");
        json.put("field", "name");
        json.put("width", 100);
        json.put("sort", false);
        json.put("align", "center");
        ary.add(json);
        json = new com.alibaba.fastjson.JSONObject();
        json.put("title", "姓名");
        json.put("field", "realName");
        json.put("width", 100);
        json.put("sort", false);
        json.put("align", "center");
        ary.add(json);
        json = new com.alibaba.fastjson.JSONObject();
        json.put("title", "性别");
        json.put("field", "sex");
        json.put("width", 60);
        json.put("sort", false);
        json.put("align", "center");
        ary.add(json);
        json = new com.alibaba.fastjson.JSONObject();
        json.put("title", "姓名");
        json.put("field", "realName");
        json.put("width", 100);
        json.put("sort", false);
        json.put("align", "center");
        ary.add(json);

        if (isUseAccount) {
            json = new com.alibaba.fastjson.JSONObject();
            json.put("title", "工号");
            json.put("field", "account");
            json.put("width", 80);
            json.put("sort", false);
            json.put("align", "center");
            ary.add(json);
        }
        json = new com.alibaba.fastjson.JSONObject();
        json.put("title", "所属部门");
        json.put("field", "deptNames");
        json.put("width", 180);
        json.put("sort", false);
        json.put("align", "center");
        ary.add(json);

        json = new com.alibaba.fastjson.JSONObject();
        json.put("title", "角色");
        json.put("field", "roleName");
        json.put("width", 150);
        json.put("sort", false);
        json.put("align", "center");
        ary.add(json);

        json = new com.alibaba.fastjson.JSONObject();
        json.put("title", "手机号");
        json.put("field", "mobile");
        json.put("width", 130);
        json.put("sort", false);
        json.put("align", "center");
        ary.add(json);

        json = new com.alibaba.fastjson.JSONObject();
        json.put("title", "状态");
        json.put("field", "status");
        json.put("width", 60);
        json.put("sort", false);
        json.put("align", "center");
        ary.add(json);

        if (isBindMobile) {
            json = new com.alibaba.fastjson.JSONObject();
            json.put("title", "手机绑定");
            json.put("field", "isBindMobile");
            json.put("width", 100);
            json.put("sort", false);
            json.put("align", "center");
            ary.add(json);
        }
        json = new com.alibaba.fastjson.JSONObject();
        json.put("title", "操作");
        json.put("field", "colOperate");
        json.put("width", 80);
        json.put("sort", false);
        json.put("align", "center");
        json.put("fixed", "right");
        ary.add(json);

        object.put("list", ary);
        object.put("isBindMobile", isBindMobile);

        return new Result<>(object);
    }

    @ApiOperation(value = "修改用户", notes = "修改用户", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/editUser")
    @ResponseBody
    public Result<Object> editUser(@RequestParam(value = "userName", required = true) String userName) {
        HashMap map = new HashMap();
        Result<Object> result = new Result<>();
        User user = userService.getUser(userName);
        map.put("user", user);
        List<Department> list = departmentService.getDeptsOfUser(userName);

        map.put("list", list);

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isUseAccount = cfg.getBooleanProperty("isUseAccount");
        String accountName = "";
        if (isUseAccount) {
            Account account = accountService.getAccountByUserName(userName);
            if (account != null) {
                accountName = account.getName();
            }
        }
        map.put("isUseAccount", isUseAccount);
        map.put("account", accountName);

        boolean isMobileNotEmpty = false;
        com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        if (weixinCfg.getBooleanProperty("isUse") || dingdingCfg.isUseDingDing()) {
            isMobileNotEmpty = true;
        }
        map.put("isMobileNotEmpty", isMobileNotEmpty);
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect("user_type");
        map.put("userTypeOpts", sd);

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
        map.put("leaders", leaders);
        map.put("leaderNames", leaderNames);
        map.put("msgSpaceAllowed", msgSpaceAllowed);

        result.setData(map);
        return result;
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
    @ResponseBody
    public Result<Object> userPriv(String userName, Integer userId) throws ValidateException {
        if (userName==null && userId==null) {
            throw new ValidateException("用户名或用户ID不能为空");
        }

        JSONObject object = new JSONObject();

        HashMap hashMap = new HashMap<>();

        User user;
        if (userName!=null) {
            user = userService.getUser(userName);
        }
        else {
            user = userService.getById(userId);
        }
        hashMap.put("userName", user.getName());
        hashMap.put("realName", user.getRealName());
        hashMap.put("isMeAdmin", new Privilege().isUserPrivValid(request, Privilege.ADMIN));

        List<Role> roleList = roleService.getAllRolesOfUser(user.getName(), false);
        List<Group> groupList = userGroupService.getGroupsOfUser(user.getName(), false);

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

        hashMap.put("list", authorityVOList);

        return new Result<>(hashMap);
    }

    @ApiOperation(value = "进入用户角色管理界面", notes = "进入用户角色管理界面", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = true, dataType = "String")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/userSetRole")
    @ResponseBody
    public Result<Object> userSetRole(String userName) {
        HashMap map = new HashMap();

        User user = userService.getUser(userName);
        map.put("userName", user.getName());
        map.put("realName", user.getRealName());

        StringBuffer roleCodes = new StringBuffer();
        StringBuffer descs = new StringBuffer();

        List<UserOfRole> list = userOfRoleService.listByUserName(userName);
        for (UserOfRole userOfRole : list) {
            String roleCode = userOfRole.getRoleCode();
            StrUtil.concat(roleCodes, ",", roleCode);
            StrUtil.concat(descs, ",", roleService.getRole(roleCode).getDescription());
        }

        map.put("roleCodes", roleCodes.toString());
        map.put("descs", descs);

        Privilege pvg = new Privilege();
        String unitCode = pvg.getUserUnitCode(request);
        map.put("unitCode", unitCode);

        StringBuffer sbGroupRoleDesc = new StringBuffer();
        // 取得用户所有的组
        List<UserOfGroup> ugList = userOfGroupService.listByUserName(userName);
        for (UserOfGroup userOfGroup : ugList) {
            // 取得组信息
            Group group = userGroupService.getGroup(userOfGroup.getGroupCode());
            if (group == null) {
                DebugUtil.w(getClass(), "userSetRole", "用户: " + userName + " 所属的组 " + userOfGroup.getGroupCode() + " 已不存在");
            }
            else {
                List<GroupOfRole> grList = userGroupOfRoleService.listByGroupCode(group.getCode());
                for (GroupOfRole groupOfRole : grList) {
                    // 取得组所属的角色
                    Role role = roleService.getRole(groupOfRole.getRoleCode());
                    StrUtil.concat(sbGroupRoleDesc, ",", group.getDescription() + ":" + role.getDescription());
                }
            }
        }
        map.put("groupRoleDesc", sbGroupRoleDesc);

        return new Result<>(map);
    }

    @ApiOperation(value = "用户部门", notes = "用户部门", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/userAdminDept")
    @ResponseBody
    public Result<Object> userAdminDept(String userName, Model model) {
        HashMap hashMap = new HashMap();

        User user = userService.getUser(userName);
        hashMap.put("userName", userName);
        hashMap.put("realName", user.getRealName());

        StringBuffer sbDepts = new StringBuffer();
        StringBuffer sbDeptNames = new StringBuffer();
        List<UserAdminDept> list = userAdminDeptService.listByUserName(userName);
        for (UserAdminDept userAdminDept : list) {
            Department department = departmentService.getDepartment(userAdminDept.getDeptCode());
            StrUtil.concat(sbDepts, ",", department.getCode());
            StrUtil.concat(sbDeptNames, ",", department.getName());
        }
        hashMap.put("depts", sbDepts.toString());
        hashMap.put("deptNames", sbDeptNames.toString());

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
        hashMap.put("adminDeptMap", map);

        return new Result<>(hashMap);
    }


}
