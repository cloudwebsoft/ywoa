package com.cloudweb.oa.controller;


import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.annotation.SysLog;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.enums.LogLevel;
import com.cloudweb.oa.enums.LogType;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.FileUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.RoleVO;
import com.cloudweb.oa.vo.UserAuthorityVO;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.Config;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.ui.SkinMgr;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dozer.DozerBeanMapper;
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
import java.sql.SQLException;
import java.util.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
@Slf4j
@Controller
public class RoleController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IRoleService roleService;

    @Autowired
    DozerBeanMapper dozerBeanMapper;

    @Autowired
    IUserOfRoleService userOfRoleService;

    @Autowired
    IUserService userService;

    @Autowired
    ResponseUtil responseUtil;

    @Autowired
    IAccountService accountService;

    @Autowired
    IDeptUserService deptUserService;

    @Autowired
    IRoleDeptService roleDeptService;

    @Autowired
    IMenuService menuService;

    @Autowired
    IPrivilegeService privilegeService;

    @Autowired
    IRolePrivService rolePrivService;

    @ResponseBody
    @RequestMapping(value = "/admin/listRoleByDesc", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String listRoleByDesc(String roleDesc) {
        List<Role> list = roleService.list("", "search", roleDesc, "");
        com.alibaba.fastjson.JSONArray jsonArray = new com.alibaba.fastjson.JSONArray();
        for (Role role : list) {
            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            json.put("code", role.getCode());
            json.put("desc", role.getDescription());
            jsonArray.add(json);
        }
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("ret", 1);
        jsonObject.put("result", jsonArray);
        return jsonObject.toString();
    }

    @ApiOperation(value = "进入角色管理界面", notes = "进入角色管理界面", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = true, dataType = "String")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/listRole")
    public String listRole(@RequestParam(defaultValue = "") String searchUnitCode,
                           @RequestParam(defaultValue = "") String op,
                           @RequestParam(defaultValue = "") String what, @RequestParam(defaultValue = "") String kind, Model model) {
        StringBuffer sb = new StringBuffer();
        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
        departmentService.getUnitAsOptions(sb, department, department.getLayer());
        model.addAttribute("unitOpts", sb.toString());

        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("searchUnitCode", searchUnitCode);
        model.addAttribute("op", op);
        model.addAttribute("what", what);
        model.addAttribute("kind", kind);

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isNetdiskUsed = cfg.getBooleanProperty("isNetdiskUsed");
        model.addAttribute("isNetdiskUsed", isNetdiskUsed);

        List<Role> roleList = roleService.list(searchUnitCode, op, what, kind);
        List<RoleVO> roleVOList = new ArrayList();
        for (Role role : roleList) {
            RoleVO roleVO = dozerBeanMapper.map(role, RoleVO.class);

            Department dept = departmentService.getDepartment(roleVO.getUnitCode());
            if (dept != null) {
                roleVO.setUnitName(dept.getName());
            }

            roleVO.setDiskQuotaDesc(role.getDiskQuota() == ConstUtil.QUOTA_NOT_SET ? "未指定" : "" + FileUtil.getSizeDesc(role.getDiskQuota()));

            roleVO.setMsgSpaceQuotaDesc(role.getMsgSpaceQuota() == ConstUtil.QUOTA_NOT_SET ? "未指定" : "" + FileUtil.getSizeDesc(role.getMsgSpaceQuota()));

            StringBuffer sbRealNames = new StringBuffer();
            int k = 0;
            List<UserOfRole> urList = userOfRoleService.listByRoleCode(role.getCode());
            for (UserOfRole userOfRole : urList) {
                User user = userService.getUser(userOfRole.getUserName());
                if (user != null) {
                    StrUtil.concat(sbRealNames, "，", user.getRealName());
                }
                else {
                    StrUtil.concat(sbRealNames, "，", userOfRole.getUserName() + " 不存在");
                    log.error("用户：" + userOfRole.getUserName() + " 不存在");
                }
                k++;
                if (k == 3) {
                    break;
                }
            }
            roleVO.setUserRealNames(sbRealNames.toString());

            String kindName = "";
            if (role.getKind()!=null && !"".equals(role.getKind())) {
                SelectOptionDb selectOptionDb = new SelectOptionDb();
                kindName = selectOptionDb.getOptionName("role_kind", role.getKind());
            }
            roleVO.setKindName(kindName);

            roleVOList.add(roleVO);
        }
        model.addAttribute("roleVOList", roleVOList);
        model.addAttribute("isAdmin", new Privilege().isUserPrivValid(request, Privilege.ADMIN));

        SelectDb sd = new SelectDb();
        sd = sd.getSelectDb("role_kind");
        StringBuffer opts = new StringBuffer();
        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        Iterator irType = vType.iterator();
        while (irType.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb) irType.next();
            opts.append("<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>");
        }
        model.addAttribute("kindOpts", opts);

        return "th/admin/role_list";
    }

    @ApiOperation(value = "更改角色的排序号", notes = "更改角色的排序号", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "角色编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "order", value = "排序号", required = true, dataType = "Integer")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/changeRoleOrder", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String changeRoleOrder(@RequestParam(required = true) String code, @RequestParam(required = true) Integer order) throws ValidateException {
        Role role = roleService.getRole(code);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{code});
        }

        role.setOrders(order);
        return responseUtil.getResultJson(roleService.update(role)).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/copyRole", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String copyRole(@RequestParam(required = true) String code) throws ValidateException {
        Role role = roleService.getRole(code);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{code});
        }

        return responseUtil.getResultJson(roleService.copy(role)).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/addRole")
    public String addRole(Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        String code = RandomSecquenceCreator.getId(20);
        model.addAttribute("code", code);

        Config cfg = Config.getInstance();
        boolean isNetdiskUsed = cfg.getBooleanProperty("isNetdiskUsed");
        model.addAttribute("isNetdiskUsed", isNetdiskUsed);

        Privilege pvg = new Privilege();
        boolean isAdmin = pvg.isUserPrivValid(request, Privilege.ADMIN);
        model.addAttribute("isAdmin", isAdmin);

        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
        model.addAttribute("unitName", department.getName());

        StringBuffer sb = new StringBuffer();
        departmentService.getUnitAsOptions(sb, department, department.getLayer());
        model.addAttribute("unitOpts", sb.toString());

        SelectDb sd = new SelectDb();
        sd = sd.getSelectDb("role_kind");
        StringBuffer opts = new StringBuffer();
        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        Iterator irType = vType.iterator();
        while (irType.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb) irType.next();
            opts.append("<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>");
        }
        model.addAttribute("kindOpts", opts);

        model.addAttribute("isPlatForm", License.getInstance().isPlatform());

        return "th/admin/role_add";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/createRole", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public String createRole(@RequestParam(required = true) String code,
                             String desc, @RequestParam(defaultValue = "0")Integer isSystem, @RequestParam(defaultValue = "1") Integer orders, @RequestParam(defaultValue = "-1")Long diskQuota,
                             String unitCode, @RequestParam(defaultValue = "-1") Long msgSpaceQuota, @RequestParam(defaultValue = "0") Integer isDeptManager, String kind
    ) throws ValidateException {
        boolean re = roleService.create(code, desc, isSystem, orders, diskQuota, unitCode, msgSpaceQuota, isDeptManager, kind);
        return responseUtil.getResultJson(re).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/editRole")
    public String editRole(String roleCode, String tabIdOpener, Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("tabIdOpener", tabIdOpener);

        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }
        model.addAttribute("role", role);

        Config cfg = Config.getInstance();
        boolean isNetdiskUsed = cfg.getBooleanProperty("isNetdiskUsed");
        model.addAttribute("isNetdiskUsed", isNetdiskUsed);

        Privilege pvg = new Privilege();
        boolean isAdmin = pvg.isUserPrivValid(request, Privilege.ADMIN);
        model.addAttribute("isAdmin", isAdmin);

        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
        model.addAttribute("unitName", department.getName());

        StringBuffer sb = new StringBuffer();
        departmentService.getUnitAsOptions(sb, department, department.getLayer());
        model.addAttribute("unitOpts", sb.toString());

        SelectDb sd = new SelectDb();
        sd = sd.getSelectDb("role_kind");
        StringBuffer opts = new StringBuffer();
        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        Iterator irType = vType.iterator();
        while (irType.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb) irType.next();
            opts.append("<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>");
        }
        model.addAttribute("kindOpts", opts);

        model.addAttribute("isPlatForm", License.getInstance().isPlatform());

        return "th/admin/role_edit";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/updateRole", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String updateRole(@RequestParam(required = true) String code,
                             String desc, @RequestParam(defaultValue = "0")Integer isSystem, @RequestParam(defaultValue = "1") Integer orders, @RequestParam(defaultValue = "-1")Long diskQuota,
                             String unitCode, @RequestParam(defaultValue = "-1") Long msgSpaceQuota, @RequestParam(defaultValue = "0") Integer isDeptManager, String kind
    ) throws ValidateException {

        Role role = new Role();
        role.setCode(code);
        role.setDescription(desc);
        role.setIsSystem(isSystem==1);
        role.setOrders(orders);
        role.setDiskQuota(diskQuota);
        role.setUnitCode(unitCode);
        role.setMsgSpaceQuota(msgSpaceQuota);
        role.setIsDeptManager(String.valueOf(isDeptManager));
        role.setKind(kind);

        return responseUtil.getResultJson(roleService.update(role)).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/delRole", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String delRole(@RequestParam(required = true) String code) throws ValidateException {
        Role role = roleService.getRole(code);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{code});
        }
        return responseUtil.getResultJson(roleService.del(role)).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/listUserOfRole")
    public String listUserOfRole(String roleCode, Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }
        model.addAttribute("role", role);

        com.redmoon.oa.Config oacfg = new com.redmoon.oa.Config();
        boolean isUseAccount = oacfg.getBooleanProperty("isUseAccount");
        model.addAttribute("isUseAccount", isUseAccount);

        int k=1;
        boolean isNeedSort = false;
        StringBuffer sbUserOpts = new StringBuffer();
        List<UserOfRole> list = userOfRoleService.listByRoleCode(roleCode);
        JSONArray jsonArray = new JSONArray();
        for (UserOfRole userOfRole : list) {
            JSONObject json = new JSONObject();
            json.put("name", userOfRole.getUserName());

            User user = userService.getUser(userOfRole.getUserName());
            json.put("realName", user.getRealName());

            sbUserOpts.append("<option value='" + user.getName() + "'>" + user.getRealName() + "</option>");

            Account acc = accountService.getAccountByUserName(user.getName());
            json.put("account", acc!=null ? acc.getName() : "");

            json.put("gender", user.getGender()?"女":"男");

            String depts = userOfRole.getDepts();
            if (depts==null || "".equals(depts)) {
                depts = "{}";
            }
            com.alibaba.fastjson.JSONObject deptsJson = com.alibaba.fastjson.JSONObject.parseObject(depts);

            List<DeptUser> duList = deptUserService.listByUserName(user.getName());
            /*StringBuffer deptNames = new StringBuffer();
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
            json.put("deptNames", deptNames.toString());*/

            JSONArray arr = new JSONArray();
            for (DeptUser du : duList) {
                Department dept = departmentService.getDepartment(du.getDeptCode());
                String deptName;
                if (!dept.getParentCode().equals(ConstUtil.DEPT_ROOT) && !dept.getCode().equals(ConstUtil.DEPT_ROOT)) {
                    Department parentDept = departmentService.getDepartment(dept.getParentCode());
                    deptName = parentDept.getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dept.getName();
                } else {
                    deptName = dept.getName();
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("deptName", deptName);

                int roleOfDept = ConstUtil.ROLE_OF_DEPT_DEFAULT;
                if (deptsJson.containsKey(dept.getCode())) {
                    roleOfDept = deptsJson.getIntValue(dept.getCode());
                }
                jsonObject.put("roleOfDept", roleOfDept);
                jsonObject.put("deptCode", dept.getCode());
                arr.add(jsonObject);
            }
            json.put("deptJson", arr);

            jsonArray.add(json);

            if (!isNeedSort && k != userOfRole.getOrders()) {
                isNeedSort = true;
            }
            k++;
        }

        // 如果需要重排，则直接在此重新排序
        if (isNeedSort) {
            userOfRoleService.sortUser(roleCode);
        }

        model.addAttribute("isNeedSort", isNeedSort);
        model.addAttribute("userOpts", sbUserOpts.toString());
        model.addAttribute("jsonAry", jsonArray);
        model.addAttribute("unitCode", new Privilege().getUserUnitCode());

        return "th/admin/role_user_list";
    }

    @SysLog(type = LogType.AUTHORIZE, action = "将角色${roleDesc}赋予给用户${userRealNames}", remark="将角色${roleDesc}赋予给用户${userRealNames}", debug = true, level = LogLevel.NORMAL)
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/addRoleUser")
    @ResponseBody
    public JSONObject addRoleUser(@RequestParam(required = true)String roleCode, String roleDesc,
                                  @NotEmpty(message = "{user.select.none}") @RequestParam(required = true) String userNames, String userRealNames) {
        String[] userAry = StrUtil.split(userNames, ",");
        return responseUtil.getResultJson(userOfRoleService.create(roleCode, userAry));
    }

    @SysLog(type = LogType.AUTHORIZE, action = "将角色${roleDesc}中的用户${userNames}删除", remark="将角色${roleDesc}中的用户${userNames}删除", debug = true, level = LogLevel.NORMAL)
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/delRoleUserBatch")
    @ResponseBody
    public JSONObject delRoleUserBatch(@RequestParam(required = true)String roleCode, String roleDesc,
                                       @NotEmpty(message = "{user.select.none}") @RequestParam(required = true) String userNames) {
        String[] userAry = StrUtil.split(userNames, ",");
        return responseUtil.getResultJson(userOfRoleService.delBatch(roleCode, userAry));
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/moveRoleUser")
    @ResponseBody
    public JSONObject moveRoleUser(@RequestParam(required = true)String roleCode, String direction,
                                       @NotEmpty(message = "{user.name.notempty}") @RequestParam(required = true) String userName) throws ErrMsgException {
        return responseUtil.getResultJson(userOfRoleService.move(roleCode, userName, direction));
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/moveToRoleUser")
    @ResponseBody
    public JSONObject moveToRoleUser(@RequestParam(required = true)String roleCode, String targetUser,
                                   @NotEmpty(message = "{user.name.notempty}") @RequestParam(required = true) String userName, Integer pos) throws ErrMsgException, ValidateException {
        UserOfRole userOfRole = userOfRoleService.getUserOfRole(userName, roleCode);
        if (userOfRole==null) {
            throw new ValidateException("角色中的用户不存在");
        }
        return responseUtil.getResultJson(userOfRoleService.moveTo(userOfRole, targetUser, pos));
    }

    @RequestMapping(value = "/admin/roleAdminDept")
    public String roleAdminDept(String roleCode, Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }
        model.addAttribute("role", role);

        // 取得角色能管理的部门
        StringBuffer sbDepts = new StringBuffer();
        StringBuffer sbDeptNames = new StringBuffer();
        List<RoleDept> rdList = roleDeptService.listByRoleCode(roleCode);
        for (RoleDept roleDept : rdList) {
            Department department = departmentService.getDepartment(roleDept.getDeptCode());
            StrUtil.concat(sbDepts, ",", department.getCode());
            StrUtil.concat(sbDeptNames, ",", department.getName());
        }
        model.addAttribute("depts", sbDepts.toString());
        model.addAttribute("deptNames", sbDeptNames.toString());

        return "th/admin/role_admin_dept";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/setRoleAdminDept")
    @ResponseBody
    public JSONObject setRoleAdminDept(@RequestParam(required = true)String roleCode, String roleDesc, String deptCodes, String deptNames) throws ValidateException {
        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }
        return responseUtil.getResultJson(roleDeptService.setRoleDepts(roleCode, roleDesc, deptCodes, deptNames));
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
    @RequestMapping(value = "/admin/rolePriv")
    public String rolePriv(String roleCode, Model model) throws ValidateException {
        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }

        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("role", role);
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

            boolean isAuthorized = rolePrivService.getRolePriv(roleCode, privilege.getPriv()) != null;
            userAuthorityVO.setAuthorized(isAuthorized);

            userAuthorityVO.setMenuList(map.get(privilege.getPriv()));

            authorityVOList.add(userAuthorityVO);
        }

        model.addAttribute("authorityVOList", authorityVOList);

        return "th/admin/role_priv";
    }

    @ResponseBody
    @RequestMapping(value = "/admin/setRolePrivs", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public JSONObject setRolePrivs(String roleCode, String[] priv) throws ValidateException {
        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }

        return responseUtil.getResultJson(rolePrivService.setPrivs(roleCode, priv));
    }

    @RequestMapping(value = "/admin/roleMenu")
    public String roleMenu(String roleCode, Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }
        model.addAttribute("role", role);
        model.addAttribute("menuJsonStr", menuService.getJsonTreeString(roleCode));

        return "th/admin/role_menu";
    }

    /**
     * 置用户角色在其兼职部门中是否有效
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/admin/setUserRoleOfDept", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String setUserRoleOfDept(String userName, String roleCode, String deptCode, int roleOfDept) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        UserOfRole userOfRole = userOfRoleService.getUserOfRole(userName, roleCode);
        if (userOfRole!=null) {
            String depts = userOfRole.getDepts();
            if (StringUtils.isEmpty(depts)) {
                depts = "{}";
            }
            com.alibaba.fastjson.JSONObject deptsJson = com.alibaba.fastjson.JSONObject.parseObject(depts);
            if (deptsJson.containsKey(deptCode)) {
                // 如果为2表示默认在该部门中拥有角色
                if (roleOfDept== ConstUtil.ROLE_OF_DEPT_DEFAULT) {
                    deptsJson.remove(deptCode);
                }
                else {
                    deptsJson.put(deptCode, String.valueOf(roleOfDept));
                }
            }
            else {
                deptsJson.put(deptCode, String.valueOf(roleOfDept));
            }
            userOfRole.setDepts(deptsJson.toString());
            boolean re = userOfRoleService.update(userOfRole);
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功");
            }
            else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        }
        else {
            json.put("ret", 0);
            json.put("msg", "记录不存在");
        }

        return json.toString();
    }

    @RequestMapping(value = "/admin/getRoleDescs", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public String getRoleDescs(String roleCodes) throws ValidateException {
        String[] ary = StrUtil.split(roleCodes, ",");
        String str = "";
        if (ary!=null) {
            RoleDb rd = new RoleDb();
            for (int i=0; i<ary.length; i++) {
                rd = rd.getRoleDb(ary[i]);
                if ("".equals(str)) {
                    str = rd.getDesc();
                }
                else {
                    str += "," + rd.getDesc();
                }
            }
        }
        return str;
    }
}
