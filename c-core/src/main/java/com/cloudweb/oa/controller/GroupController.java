package com.cloudweb.oa.controller;


import cn.js.fan.util.RandomSecquenceCreator;
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
import com.cloudweb.oa.vo.RoleVO;
import com.cloudweb.oa.vo.UserAuthorityVO;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.pvg.Privilege;
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
    @RequestMapping(value = "/addUserGroup", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String addUserGroup(String userNames, @Length(min = 0, max = 50, message = "{usergroup.desc.tooLong}") String desc) throws ValidateException {
        JSONObject json = new JSONObject();
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
            json.put("ret", "1");
            json.put("result", i18nUtil.get("info_op_success"));
        } else {
            json.put("ret", "2");
            json.put("result", i18nUtil.get("info_op_fail"));
        }
        return json.toString();
    }

    /**
     * 用于user_multi_sel.html
     * @return
     * @throws ValidateException
     */
    @ResponseBody
    @RequestMapping(value = "/refreshUserGroup", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String refreshUserGroup() throws ValidateException {
        String sql = "";
        String result = "";

        QueryWrapper<Group> qw = new QueryWrapper<>();
        qw.orderByDesc("isSystem").orderByAsc("code");
        List<Group> list = groupService.list(qw);
        result = "<ul>";
        for (Group group : list) {
            String groupCode = group.getCode();
            String desc = group.getDescription();
            result += "<li onMouseOver='addLiClass(this);' onMouseOut='removeLiClass(this);' onclick='getLiGroup(this);' id='" + groupCode + "' name='" + groupCode + "'>&nbsp;&nbsp;&nbsp;&nbsp;" + desc + "</li>";
        }
        result += "</ul>";

        JSONObject json = new JSONObject();
        json.put("ret", "1");
        json.put("result", result);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/listGroupByDesc", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String listGroupByDesc(String groupDesc) {
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
        return jsonObject.toString();
    }

    @RequestMapping(value = "/listGroup")
    public String listGroup(@RequestParam(defaultValue = "") String searchUnitCode,
                            @RequestParam(defaultValue = "") String op,
                            @RequestParam(defaultValue = "") String what,
                            @RequestParam(defaultValue = "") String kind,
                            Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        model.addAttribute("searchUnitCode", searchUnitCode);
        model.addAttribute("op", op);
        model.addAttribute("what", what);
        model.addAttribute("kind", kind);

        List<Group> list = groupService.list(searchUnitCode, op, what, kind);

        JSONArray jsonArr = new JSONArray();
        for (Group group : list) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", group.getCode());
            jsonObject.put("desc", group.getDescription());
            String deptName = "";
            if (group.getDeptCode()!=null && !"".equals(group.getDeptCode())) {
                Department department = departmentService.getDepartment(group.getDeptCode());
                if (department!=null) {
                    deptName = department.getName();
                }
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

            String kindName = "";
            if (group.getKind()!=null && !"".equals(group.getKind())) {
                SelectOptionDb selectOptionDb = new SelectOptionDb();
                kindName = selectOptionDb.getOptionName("usergroup_kind", group.getKind());
            }
            jsonObject.put("kindName", kindName);

            jsonArr.add(jsonObject);
        }

        model.addAttribute("jsonArr", jsonArr);

        SelectDb sd = new SelectDb();
        sd = sd.getSelectDb("usergroup_kind");
        StringBuffer opts = new StringBuffer();
        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        Iterator irType = vType.iterator();
        while (irType.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb) irType.next();
            opts.append("<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>");
        }
        model.addAttribute("kindOpts", opts);

        return "th/admin/group_list";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/delGroup", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String delGroup(@RequestParam(required = true) String groupCode) throws ValidateException {
        Group group = groupService.getGroup(groupCode);
        if (group == null) {
            throw new ValidateException("#group.notexist", new Object[]{groupCode});
        }
        return responseUtil.getResultJson(groupService.del(groupCode)).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/addGroup")
    public String addGroup(String groupCode, Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        StringBuffer sb = new StringBuffer();
        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
        departmentService.getUnitAsOptions(sb, department, department.getLayer());
        model.addAttribute("unitOpts", sb.toString());

        String code = RandomSecquenceCreator.getId(20);
        model.addAttribute("code", code);

        sb = new StringBuffer();
        departmentService.getDeptAsOptions(sb, department, department.getLayer());
        model.addAttribute("deptOpts", sb.toString());

        SelectDb sd = new SelectDb();
        sd = sd.getSelectDb("usergroup_kind");
        StringBuffer opts = new StringBuffer();
        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        Iterator irType = vType.iterator();
        while (irType.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb) irType.next();
            opts.append("<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>");
        }
        model.addAttribute("kindOpts", opts);

        return "th/admin/group_add";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/editGroup")
    public String editGroup(String groupCode, String tabIdOpener, Model model) throws ValidateException {
        Group group = groupService.getGroup(groupCode);
        model.addAttribute("group", group);

        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        StringBuffer sb = new StringBuffer();
        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
        departmentService.getUnitAsOptions(sb, department, department.getLayer());
        model.addAttribute("unitOpts", sb.toString());

        sb = new StringBuffer();
        departmentService.getDeptAsOptions(sb, department, department.getLayer());
        model.addAttribute("deptOpts", sb.toString());

        SelectDb sd = new SelectDb();
        sd = sd.getSelectDb("usergroup_kind");
        StringBuffer opts = new StringBuffer();
        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        Iterator irType = vType.iterator();
        while (irType.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb) irType.next();
            opts.append("<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>");
        }
        model.addAttribute("kindOpts", opts);

        model.addAttribute("tabIdOpener", tabIdOpener);

        return "th/admin/group_edit";
    }

    /**
     * 进入设置用户组的角色画面
     * @param groupCode
     * @param tabIdOpener
     * @param model
     * @return
     * @throws ValidateException
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/groupRole")
    public String groupRole(String groupCode, String tabIdOpener, Model model) throws ValidateException {
        Group group = groupService.getGroup(groupCode);
        model.addAttribute("group", group);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

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

        model.addAttribute("roleVOList", roleVOList);
        return "th/admin/group_role";
    }

    /**
     * 创建用户组
     * @param code
     * @param desc
     * @param isDept
     * @param isIncludeSubDept
     * @param unitCode
     * @param model
     * @return
     * @throws ValidateException
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/createGroup", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String createGroup(String code, String desc, Integer isDept, @RequestParam(defaultValue = "0") Integer isIncludeSubDept,
                              String unitCode,  @RequestParam(defaultValue = "") String kind, Model model) throws ValidateException {

        return responseUtil.getResultJson(groupService.create(code, desc, isDept, isIncludeSubDept, unitCode, kind)).toString();
    }

    /**
     * 编辑用户组
     * @param code
     * @param desc
     * @param deptCode
     * @param isDept
     * @param isIncludeSubDept
     * @param unitCode
     * @param model
     * @return
     * @throws ValidateException
     */
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/updateGroup", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String updateGroup(String code, String desc, String deptCode, Integer isDept, @RequestParam(defaultValue = "0") Integer isIncludeSubDept, String unitCode, String kind, Model model) throws ValidateException {

        return responseUtil.getResultJson(groupService.update(code, desc, deptCode, isDept, isIncludeSubDept, unitCode, kind)).toString();
    }

    /**
     * 置用户组所属的角色
     * @param groupCode
     * @param roleCode
     * @param model
     * @return
     * @throws ValidateException
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/setGroupOfRole", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String setGroupOfRole(String groupCode, String[] roleCode, Model model) throws ValidateException {

        return responseUtil.getResultJson(groupOfRoleService.setGroupOfRole(groupCode, roleCode)).toString();
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

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/groupPriv")
    public String groupPriv(String groupCode, Model model) throws ValidateException {
        Group group = groupService.getGroup(groupCode);
        if (group == null) {
            throw new ValidateException("#group.notexist", new Object[]{groupCode});
        }

        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("group", group);
        model.addAttribute("isMeAdmin", new Privilege().isUserPrivValid(request, Privilege.ADMIN));

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

        model.addAttribute("authorityVOList", authorityVOList);

        return "th/admin/group_priv";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/setGroupPrivs", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public JSONObject setGroupPrivs(String groupCode, String[] priv) throws ValidateException {
        Group group = groupService.getGroup(groupCode);
        if (group == null) {
            throw new ValidateException("#group.notexist", new Object[]{groupCode});
        }

        return responseUtil.getResultJson(groupPrivService.setPrivs(groupCode, priv));
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/listUserOfGroup")
    public String listUserOfGroup(String groupCode, Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        Group group = groupService.getGroup(groupCode);
        if (group == null) {
            throw new ValidateException("#group.notexist", new Object[]{groupCode});
        }
        model.addAttribute("group", group);

        StringBuffer sbUserOpts = new StringBuffer();
        List<UserOfGroup> list = userOfGroupService.listByGroupCode(groupCode);
        JSONArray jsonArray = new JSONArray();
        for (UserOfGroup userOfGroup : list) {
            JSONObject json = new JSONObject();
            json.put("name", userOfGroup.getUserName());

            User user = userService.getUser(userOfGroup.getUserName());
            json.put("realName", user.getRealName());

            sbUserOpts.append("<option value='" + user.getName() + "'>" + user.getRealName() + "</option>");


            json.put("gender", user.getGender()?"女":"男");

            StringBuffer deptNames = new StringBuffer();
            List<DeptUser> duList = deptUserService.listByUserName(user.getName());
            for (DeptUser du : duList) {
                Department dept = departmentService.getDepartment(du.getDeptCode());
                String deptName;
                if (!dept.getParentCode().equals(ConstUtil.DEPT_ROOT) && !dept.getCode().equals(ConstUtil.DEPT_ROOT)) {
                    Department parentDept = departmentService.getDepartment(dept.getParentCode());
                    deptName = parentDept.getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dept.getName();
                } else {
                    deptName = dept.getName();
                }
                StrUtil.concat(deptNames, "，", deptName);
            }
            json.put("deptNames", deptNames.toString());

            jsonArray.add(json);
        }

        model.addAttribute("userOpts", sbUserOpts.toString());
        model.addAttribute("jsonAry", jsonArray);
        model.addAttribute("unitCode", new Privilege().getUserUnitCode());

        return "th/admin/group_user_list";
    }

    @SysLog(type = LogType.AUTHORIZE, action = "将用户组${roleDesc}赋予给用户${userRealNames}", remark="将用户组${roleDesc}赋予给用户${userRealNames}", debug = true, level = LogLevel.NORMAL)
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/addGroupUser")
    @ResponseBody
    public JSONObject addGroupUser(@RequestParam(required = true)String groupCode, String groupDesc,
                                  @NotEmpty(message = "{user.select.none}") @RequestParam(required = true) String userNames, String userRealNames) {
        String[] userAry = StrUtil.split(userNames, ",");
        return responseUtil.getResultJson(userOfGroupService.create(groupCode, userAry));
    }

    @SysLog(type = LogType.AUTHORIZE, action = "将用户组${groupDesc}中的用户${userNames}删除", remark="将用户组${groupDesc}中的用户${userNames}删除", debug = true, level = LogLevel.NORMAL)
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/delGroupUserBatch")
    @ResponseBody
    public JSONObject delGroupUserBatch(@RequestParam(required = true)String groupCode, String groupDesc,
                                       @NotEmpty(message = "{user.select.none}") @RequestParam(required = true) String userNames) {
        String[] userAry = StrUtil.split(userNames, ",");
        return responseUtil.getResultJson(userOfGroupService.del(groupCode, userAry));
    }
}
