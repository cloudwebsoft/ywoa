package com.cloudweb.oa.controller;


import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.annotation.SysLog;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.enums.LogLevel;
import com.cloudweb.oa.enums.LogType;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudweb.oa.vo.RoleVO;
import com.cloudweb.oa.vo.UserAuthorityVO;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.pvg.Privilege;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.dozer.DozerBeanMapper;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author fgf
 * @since 2020-02-02
 */
@Controller
@RequestMapping("/admin")
public class GroupController {
    @Autowired
    I18nUtil i18nUtil;

    @Autowired
    IGroupService groupService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    ResponseUtil responseUtil;

    @Autowired
    IRoleService roleService;

    @Autowired
    IGroupOfRoleService groupOfRoleService;

    @Autowired
    DozerBeanMapper dozerBeanMapper;

    @Autowired
    IMenuService menuService;

    @Autowired
    IGroupPrivService groupPrivService;

    @Autowired
    IPrivilegeService privilegeService;

    @Autowired
    IUserOfGroupService userOfGroupService;

    @Autowired
    IUserService userService;

    @Autowired
    IDeptUserService deptUserService;

    /**
     * 用于user_multi_sel.html
     * @param userNames
     * @param desc
     * @return
     * @throws ValidateException
     */
    @SuppressWarnings("AlibabaAvoidPatternCompileInMethod")
    @ApiOperation(value = "添加用户组", notes = "添加用户组", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userNames", value = "组编码", dataType = "String"),
            @ApiImplicitParam(name = "desc", value = "描述", dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/addUserGroup", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> addUserGroup(String userNames, @Length(min = 0, max = 50, message = "{usergroup.desc.tooLong}") String desc) throws ValidateException {
        Result<Object> result = new Result();
        String[] users = StrUtil.split(userNames, ",");
        String groupCode = String.valueOf(SequenceManager.nextID(SequenceManager.OA_USER_GROUP));

        if (Pattern.compile("'").matcher(desc).find()) {
            throw new ValidateException("#usergroup.desc.invalid");
        }

        Group group = new Group();
        group.setIsIncludeSubDept(0);
        group.setUnitCode(ConstUtil.DEPT_ROOT);
        group.setCode(groupCode);
        group.setDescription(desc);
        boolean re = group.insert();
        if (re) {
            for (String userName : users) {
                UserOfGroup userOfGroup = new UserOfGroup();
                userOfGroup.setGroupCode(groupCode);
                userOfGroup.setUserName(userName);
                userOfGroup.insert();
            }
        }else{
            result.setResult(false);
        }
        return result;
    }

    /**
     * 用于user_multi_sel.html
     * @return
     * @throws ValidateException
     */
//    @ResponseBody
//    @RequestMapping(value = "/refreshUserGroup", produces = {"text/html;", "application/json;charset=UTF-8;"})
//    public String refreshUserGroup() throws ValidateException {
//        String sql = "";
//        String result = "";
//
//        QueryWrapper<Group> qw = new QueryWrapper<>();
//        qw.orderByDesc("isSystem").orderByAsc("code");
//        List<Group> list = groupService.list(qw);
//        result = "<ul>";
//        for (Group group : list) {
//            String groupCode = group.getCode();
//            String desc = group.getDescription();
//            result += "<li onMouseOver='addLiClass(this);' onMouseOut='removeLiClass(this);' onclick='getLiGroup(this);' id='" + groupCode + "' name='" + groupCode + "'>&nbsp;&nbsp;&nbsp;&nbsp;" + desc + "</li>";
//        }
//        result += "</ul>";
//
//        JSONObject json = new JSONObject();
//        json.put("ret", "1");
//        json.put("result", result);
//        return json.toString();
//    }

    @ApiOperation(value = "用户组管理描述", notes = "用户组管理描述", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupDesc", value = "组编码", dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/listGroupByDesc", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public Result<Object> listGroupByDesc(String groupDesc) {
        List<Group> list = groupService.list("", "search", groupDesc, "");
        com.alibaba.fastjson.JSONArray jsonArray = new com.alibaba.fastjson.JSONArray();
        for (Group group : list) {
            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            json.put("code", group.getCode());
            json.put("desc", group.getDescription());
            jsonArray.add(json);
        }
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("ret", 1);
        jsonObject.put("result", jsonArray);
        return new Result<>(jsonObject);
    }

    @ApiOperation(value = "用户组管理", notes = "用户组管理", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchUnitCode", value = "单位", dataType = "String"),
            @ApiImplicitParam(name = "op", value = "操作", dataType = "String"),
            @ApiImplicitParam(name = "what", value = "名称", dataType = "String"),
            @ApiImplicitParam(name = "kind", value = "类别", dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/listGroup")
    public Result<Object> listGroup(@RequestParam(defaultValue = "") String searchUnitCode,
                            @RequestParam(defaultValue = "") String op,
                            @RequestParam(defaultValue = "") String what,
                            @RequestParam(defaultValue = "") String kind,
                            Model model) throws ValidateException {
        JSONObject object = new JSONObject();

        object.put("searchUnitCode", searchUnitCode);
        object.put("op", op);
        object.put("what", what);
        object.put("kind", kind);

        List<Group> list = groupService.list(searchUnitCode, op, what, kind);
        SelectOptionDb selectOptionDb = new SelectOptionDb();
        JSONArray jsonArr = new JSONArray();
        for (Group group : list) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", group.getCode());
            jsonObject.put("desc", group.getDescription());
            jsonObject.put("unitCode", group.getUnitCode());
            String deptName = "";
            if (group.getDeptCode()!=null && !"".equals(group.getDeptCode())) {
                Department department = departmentService.getDepartment(group.getDeptCode());
                if (department!=null) {
                    deptName = department.getName();
                }
            }
            if(!StrUtil.isEmpty(group.getKind())){
                jsonObject.put("kindName",selectOptionDb.getOptionName("usergroup_kind", group.getKind()));
            }else{
                jsonObject.put("kindName","");
            }

            jsonObject.put("deptName", deptName);
            jsonObject.put("isDept", group.getIsDept());
            jsonObject.put("isIncludeSubDept", group.getIsIncludeSubDept());
            String unitName = "";
            Department department = departmentService.getDepartment(group.getUnitCode());
            if (department!=null) {
                unitName = department.getName();
            }
            jsonObject.put("unitName", unitName);
            jsonObject.put("isSystem", group.getIsSystem());

//            String kindName = "";
//            if (group.getKind()!=null && !"".equals(group.getKind())) {
//                SelectOptionDb selectOptionDb = new SelectOptionDb();
//                kindName = selectOptionDb.getOptionName("usergroup_kind", group.getKind());
//            }
//            jsonObject.put("kindName", kindName);

            jsonArr.add(jsonObject);
        }

        object.put("list", jsonArr);

//        SelectDb sd = new SelectDb();
//        sd = sd.getSelectDb("usergroup_kind");
//        StringBuffer opts = new StringBuffer();
//        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
//        Iterator irType = vType.iterator();
//
//        object.put("kindOpts", irType);

        return new Result<>(object);
    }

    @ApiOperation(value = "删除用户组", notes = "删除用户组", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupCode", value = "组编码", dataType = "String"),
    })
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/delGroup", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> delGroup(@RequestParam(required = true) String groupCode) throws ValidateException {
        Group group = groupService.getGroup(groupCode);
        if (group == null) {
            throw new ValidateException("#group.notexist", new Object[]{groupCode});
        }
        return new Result<>(groupService.del(groupCode));
    }

//    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
//    @RequestMapping(value = "/addGroup")
//    public String addGroup(String groupCode, Model model) throws ValidateException {
//        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
//        StringBuffer sb = new StringBuffer();
//        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
//        departmentService.getUnitAsOptions(sb, department, department.getLayer());
//        model.addAttribute("unitOpts", sb.toString());
//
//        String code = RandomSecquenceCreator.getId(20);
//        model.addAttribute("code", code);
//
//        sb = new StringBuffer();
//        departmentService.getDeptAsOptions(sb, department, department.getLayer());
//        model.addAttribute("deptOpts", sb.toString());
//
//        SelectDb sd = new SelectDb();
//        sd = sd.getSelectDb("usergroup_kind");
//        StringBuffer opts = new StringBuffer();
//        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
//        Iterator irType = vType.iterator();
//        while (irType.hasNext()) {
//            SelectOptionDb sod = (SelectOptionDb) irType.next();
//            opts.append("<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>");
//        }
//        model.addAttribute("kindOpts", opts);
//
//        return "th/admin/group_add";
//    }

    @ApiOperation(value = "修改设置用户组的角色画面", notes = "修改设置用户组的角色画面", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupCode", value = "组编码", dataType = "String"),
    })
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/editGroup")
    public Result<Object> editGroup(String groupCode) throws ValidateException {
        JSONObject object = new JSONObject();
        Group group = groupService.getGroup(groupCode);
        object.put("group", group);

        return new Result<>(object);
    }

    /**
     * 进入设置用户组的角色画面
     * @param groupCode
     * @return
     * @throws ValidateException
     */
    @ApiOperation(value = "进入设置用户组的角色画面", notes = "进入设置用户组的角色画面", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupCode", value = "组编码", dataType = "String"),
    })
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/groupRole")
    public Result<Object> groupRole(String groupCode) throws ValidateException {
        JSONObject object = new JSONObject();
        Group group = groupService.getGroup(groupCode);
        object.put("group", group);

        List<Role> roleList = roleService.getRolesOfUnit(group.getUnitCode(), true);

        List<RoleVO> roleVOList = new ArrayList<>();
        List<GroupOfRole> groupOfRoleList = groupOfRoleService.listByGroupCode(groupCode);
        for (Role role : roleList) {
            if (role.getCode().equals(ConstUtil.ROLE_MEMBER)) {
                continue;
            }

            RoleVO roleVO = dozerBeanMapper.map(role, RoleVO.class);

            boolean isRole = false;
            for (GroupOfRole groupOfRole : groupOfRoleList) {
                if (role.getCode().equals(groupOfRole.getRoleCode())) {
                    isRole = true;
                    break;
                }
            }
            if (isRole) {
                roleVO.setChecked(true);
            }
            else {
                roleVO.setChecked(false);
            }
            roleVOList.add(roleVO);
        }

        object.put("roleVOList", roleVOList);
        return new Result<>(object);
    }

    /**
     * 创建用户组
     * @param code
     * @param desc
     * @param isDept
     * @param isIncludeSubDept
     * @param unitCode
     * @return
     * @throws ValidateException
     */
    @ApiOperation(value = "创建用户组", notes = "创建用户组", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "编码", dataType = "String"),
            @ApiImplicitParam(name = "desc", value = "描述", dataType = "String"),
            @ApiImplicitParam(name = "isDept", value = "是否部门", dataType = "Integer"),
            @ApiImplicitParam(name = "isIncludeSubDept", value = "是否为部门组", dataType = "Integer"),
            @ApiImplicitParam(name = "unitCode", value = "单位编码", dataType = "String"),
            @ApiImplicitParam(name = "kind", value = "类别", dataType = "String"),
    })
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/createGroup", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> createGroup(String code, String desc, Integer isDept, @RequestParam(defaultValue = "0") Integer isIncludeSubDept,
                              String unitCode,  @RequestParam(defaultValue = "") String kind) throws ValidateException {

        return new Result<>(groupService.create(code, desc, isDept, isIncludeSubDept, unitCode, kind));
    }

    /**
     * 编辑用户组
     * @param code
     * @param desc
     * @param deptCode
     * @param isDept
     * @param isIncludeSubDept
     * @param unitCode
     * @return
     * @throws ValidateException
     */
    @ApiOperation(value = "编辑用户组", notes = "编辑用户组", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "编码", dataType = "String"),
            @ApiImplicitParam(name = "desc", value = "描述", dataType = "String"),
            @ApiImplicitParam(name = "deptCode", value = "部门编码", dataType = "String"),
            @ApiImplicitParam(name = "isDept", value = "是否部门", dataType = "Integer"),
            @ApiImplicitParam(name = "isIncludeSubDept", value = "是否为部门组", dataType = "Integer"),
            @ApiImplicitParam(name = "unitCode", value = "单位编码", dataType = "String"),
            @ApiImplicitParam(name = "kind", value = "类别", dataType = "String"),
    })
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/updateGroup", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> updateGroup(String code, String desc, String deptCode, Integer isDept, @RequestParam(defaultValue = "0") Integer isIncludeSubDept, String unitCode, String kind) throws ValidateException {

        return new Result<>(groupService.update(code, desc, deptCode, isDept, isIncludeSubDept, unitCode, kind));
    }

    /**
     * 置用户组所属的角色
     * @param groupCode
     * @param roleCode
     * @return
     * @throws ValidateException
     */
    @ApiOperation(value = "置用户组所属的角色", notes = "置用户组所属的角色", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupCode", value = "分组编码", dataType = "String"),
            @ApiImplicitParam(name = "roleCode", value = "角色编码", dataType = "Arrray"),
    })
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/setGroupOfRole", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> setGroupOfRole(String groupCode, String[] roleCode) throws ValidateException {

        return new Result<>(groupOfRoleService.setGroupOfRole(groupCode, roleCode));
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

    @ApiOperation(value = "用户组权限", notes = "用户组权限", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupCode", value = "分组编码", dataType = "String"),
    })
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/groupPriv")
    public Result<Object> groupPriv(String groupCode) throws ValidateException {
        JSONObject object = new JSONObject();
        Group group = groupService.getGroup(groupCode);
        if (group == null) {
            throw new ValidateException("#group.notexist", new Object[]{groupCode});
        }

        object.put("group", group);
        object.put("isMeAdmin", new Privilege().isUserPrivValid(request, Privilege.ADMIN));

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

            boolean isAuthorized = groupPrivService.getGroupPriv(groupCode, privilege.getPriv()) != null;
            userAuthorityVO.setAuthorized(isAuthorized);

            userAuthorityVO.setMenuList(map.get(privilege.getPriv()));

            authorityVOList.add(userAuthorityVO);
        }

        object.put("list", authorityVOList);

        return new Result<>(object);
    }

    @ApiOperation(value = "设置用户组权限", notes = "设置用户组权限", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupCode", value = "分组编码", dataType = "String"),
            @ApiImplicitParam(name = "priv", value = "权限", dataType = "Array"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/setGroupPrivs", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> setGroupPrivs(String groupCode, String privs) throws ValidateException {
        String[] priv = privs.split(",");
        Group group = groupService.getGroup(groupCode);
        if (group == null) {
            throw new ValidateException("#group.notexist", new Object[]{groupCode});
        }
        return new Result<>(groupPrivService.setPrivs(groupCode, priv));
    }

    @ApiOperation(value = "用户组列表", notes = "用户组列表", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupCode", value = "分组编码", dataType = "String"),
    })
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/listUserOfGroup")
    public Result<Object> listUserOfGroup(String groupCode) throws ValidateException {
        JSONObject object = new JSONObject();

        Group group = groupService.getGroup(groupCode);
        if (group == null) {
            throw new ValidateException("#group.notexist", new Object[]{groupCode});
        }
        object.put("group", group);

//        StringBuffer sbUserOpts = new StringBuffer();
        List<UserOfGroup> list = userOfGroupService.listByGroupCode(groupCode);
        JSONArray jsonArray = new JSONArray();
        for (UserOfGroup userOfGroup : list) {
            JSONObject json = new JSONObject();
            User user = userService.getUser(userOfGroup.getUserName());
            if (user == null) {
                // 删除垃圾数据
                LogUtil.getLog(getClass()).error("用户: " + userOfGroup.getUserName() + " 不存在");
                userOfGroupService.del(userOfGroup.getGroupCode(), new String[] {userOfGroup.getUserName()});
                continue;
            }
            json.put("name", user.getLoginName());
            json.put("realName", user.getRealName());

            json.put("gender", user.getGender()?"女":"男");

            StringBuffer deptNames = new StringBuffer();
            List<DeptUser> duList = deptUserService.listByUserName(user.getName());
            for (DeptUser du : duList) {
                Department dept = departmentService.getDepartment(du.getDeptCode());
                String deptName;
                if (!dept.getParentCode().equals(ConstUtil.DEPT_ROOT) && !dept.getCode().equals(ConstUtil.DEPT_ROOT)) {
                    Department parentDept = departmentService.getDepartment(dept.getParentCode());
                    deptName = parentDept.getName() + dept.getName();
                } else {
                    deptName = dept.getName();
                }
                StrUtil.concat(deptNames, "，", deptName);
            }
            json.put("deptNames", deptNames.toString());

            jsonArray.add(json);
        }

        object.put("list", jsonArray);
        object.put("unitCode", new Privilege().getUserUnitCode());

        return new Result<>(object);
    }

    @ApiOperation(value = "用户组管理", notes = "用户组管理", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupCode", value = "分组编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "groupDesc", value = "分组描述", dataType = "String"),
            @ApiImplicitParam(name = "userNames", value = "userNames", required = true, dataType = "String"),
            @ApiImplicitParam(name = "userRealNames", value = "userRealNames", dataType = "String"),
    })
    @SysLog(type = LogType.AUTHORIZE, action = "将用户组${roleDesc}赋予给用户${userRealNames}", remark="将用户组${roleDesc}赋予给用户${userRealNames}", debug = true, level = LogLevel.NORMAL)
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/addGroupUser")
    @ResponseBody
    public Result<Object> addGroupUser(@RequestParam(required = true)String groupCode, String groupDesc,
                                  @NotEmpty(message = "{user.select.none}") @RequestParam(required = true) String userNames, String userRealNames) {
        String[] userAry = StrUtil.split(userNames, ",");
        return new Result<>(userOfGroupService.create(groupCode, userAry));
    }

    @ApiOperation(value = "用户组删除", notes = "用户组删除", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupCode", value = "分组编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "groupDesc", value = "分组描述", dataType = "String"),
            @ApiImplicitParam(name = "userNames", value = "userNames", required = true, dataType = "String"),
            @ApiImplicitParam(name = "userRealNames", value = "userRealNames", dataType = "String"),
    })
    @SysLog(type = LogType.AUTHORIZE, action = "将用户组${groupDesc}中的用户${userNames}删除", remark="将用户组${groupDesc}中的用户${userNames}删除", debug = true, level = LogLevel.NORMAL)
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/delGroupUserBatch")
    @ResponseBody
    public Result<Object> delGroupUserBatch(@RequestParam(required = true)String groupCode, String groupDesc,
                                       @NotEmpty(message = "{user.select.none}") @RequestParam(required = true) String userNames) {
        String[] userAry = StrUtil.split(userNames, ",");
        return new Result<>(userOfGroupService.del(groupCode, userAry));
    }
}
