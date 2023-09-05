package com.cloudweb.oa.controller;


import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSON;
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
import com.cloudweb.oa.vo.Result;
import com.cloudweb.oa.vo.RoleVO;
import com.cloudweb.oa.vo.UserAuthorityVO;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.ui.SkinMgr;
import io.swagger.annotations.Api;
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
@Api(tags = "角色控制器")
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

//    @ResponseBody
//    @RequestMapping(value = "/admin/listRoleByDesc", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
//    public String listRoleByDesc(String roleDesc) {
//        List<Role> list = roleService.list("", "search", roleDesc, "");
//        com.alibaba.fastjson.JSONArray jsonArray = new com.alibaba.fastjson.JSONArray();
//        for (Role role : list) {
//            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
//            json.put("code", role.getCode());
//            json.put("desc", role.getDescription());
//            jsonArray.add(json);
//        }
//        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
//        jsonObject.put("ret", 1);
//        jsonObject.put("result", jsonArray);
//        return jsonObject.toString();
//    }

    @ApiOperation(value = "进入角色管理界面", notes = "进入角色管理界面", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = true, dataType = "String")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/listRole")
    @ResponseBody
    public Result<Object> listRole(@RequestParam(defaultValue = "") String searchUnitCode,
                                   @RequestParam(defaultValue = "") String op,
                                   @RequestParam(defaultValue = "") String what, @RequestParam(defaultValue = "") String kind, Model model) {
        Result<Object> result = new Result<>();
        JSONObject jsonObject = new JSONObject();
        StringBuffer sb = new StringBuffer();
        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
        departmentService.getUnitAsOptions(sb, department, department.getLayer());
        jsonObject.put("unitOpts", sb.toString());

        jsonObject.put("searchUnitCode", searchUnitCode);
        jsonObject.put("op", op);
        jsonObject.put("what", what);
        jsonObject.put("kind", kind);

        List<Role> roleList = roleService.list(searchUnitCode, op, what, kind, -1);
        List<RoleVO> roleVOList = new ArrayList<>();
        for (Role role : roleList) {
            RoleVO roleVO = dozerBeanMapper.map(role, RoleVO.class);

            Department dept = departmentService.getDepartment(roleVO.getUnitCode());
            if (dept != null) {
                roleVO.setUnitName(dept.getName());
            }

            roleVO.setDiskQuotaDesc(role.getDiskQuota() == ConstUtil.QUOTA_NOT_SET ? "未指定" : FileUtil.getSizeDesc(role.getDiskQuota()));

            roleVO.setMsgSpaceQuotaDesc(role.getMsgSpaceQuota() == ConstUtil.QUOTA_NOT_SET ? "未指定" : FileUtil.getSizeDesc(role.getMsgSpaceQuota()));

            StringBuffer sbRealNames = new StringBuffer();
            int k = 0;
            List<UserOfRole> urList = userOfRoleService.listByRoleCode(role.getCode());
            for (UserOfRole userOfRole : urList) {
                User user = userService.getUser(userOfRole.getUserName());
                if (user != null) {
                    StrUtil.concat(sbRealNames, "，", user.getRealName());
                } else {
                    StrUtil.concat(sbRealNames, "，", userOfRole.getUserName() + " 不存在");
                    log.error("用户：" + userOfRole.getUserName() + " 不存在");
                }
                k++;
                if (k == 3) {
                    break;
                }
            }
            String realNames = sbRealNames.toString();
            if (urList.size() > 3) {
                realNames += "...";
            }
            roleVO.setUserRealNames(realNames);
            roleVO.setUserCount(urList.size());

            String kindName = "";
            if (role.getKind() != null && !"".equals(role.getKind())) {
                SelectOptionDb selectOptionDb = new SelectOptionDb();
                kindName = selectOptionDb.getOptionName("role_kind", role.getKind());
            }
            roleVO.setKindName(kindName);

            roleVOList.add(roleVO);
        }
        jsonObject.put("list", roleVOList);
        jsonObject.put("isAdmin", new Privilege().isUserPrivValid(request, Privilege.ADMIN));

//        SelectDb sd = new SelectDb();
//        sd = sd.getSelectDb("role_kind");
//        StringBuffer opts = new StringBuffer();
//        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
//        jsonObject.put("vType", vType);
        result.setData(jsonObject);

        return result;
    }


    @ApiOperation(value = "更改角色的排序号", notes = "更改角色的排序号", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "角色编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "order", value = "排序号", required = true, dataType = "Integer")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/changeRoleOrder", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Role> changeRoleOrder(@RequestParam(required = true) String code, @RequestParam(required = true) Integer order) throws ValidateException {
        Result<Role> result = new Result<>();
        Role role = roleService.getRole(code);
        if (role == null) {
            result.error500("未查到该角色");
        }

        role.setOrders(order);
        if (roleService.update(role, false)) {
            result.setData(role);
        } else {
            result.error500("修改失败");
        }

        return result;
    }

    @ApiOperation(value = "更改角色的状态", notes = "更改角色的状态", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "角色编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "status", value = "启用状态", required = true, dataType = "Integer")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/changeRoleStatus", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Role> changeRoleStatus(@RequestParam(required = true) String code, @RequestParam(required = true) Boolean status) throws ValidateException {
        Result<Role> result = new Result<>();
        Role role = roleService.getRole(code);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{code});
        }
        role.setStatus(status);
        if (roleService.update(role, true)) {
            result.setData(role);
        } else {
            result.error500("修改失败");
        }
        return result;
    }

    @ApiOperation(value = "复制角色", notes = "复制角色", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "角色编码", required = true, dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/copyRole", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public Result<Role> copyRole(@RequestParam(required = true) String code) throws ValidateException {
        Result<Role> result = new Result<>();
        Role role = roleService.getRole(code);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{code});
        }

        if (roleService.copy(role)) {
            result.setData(role);
        } else {
            result.error500("修改失败");
        }

        return result;
    }

//    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
//    @RequestMapping(value = "/admin/addRole")
//    public String addRole(Model model) {
//        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
//
//        String code = RandomSecquenceCreator.getId(20);
//        model.addAttribute("code", code);
//
//        Privilege pvg = new Privilege();
//        boolean isAdmin = pvg.isUserPrivValid(request, Privilege.ADMIN);
//        model.addAttribute("isAdmin", isAdmin);
//
//        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
//        model.addAttribute("unitName", department.getName());
//
//        StringBuffer sb = new StringBuffer();
//        departmentService.getUnitAsOptions(sb, department, department.getLayer());
//        model.addAttribute("unitOpts", sb.toString());
//
//        SelectDb sd = new SelectDb();
//        sd = sd.getSelectDb("role_kind");
//        StringBuffer opts = new StringBuffer();
//        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
//        Iterator irType = vType.iterator();
//        while (irType.hasNext()) {
//            SelectOptionDb sod = (SelectOptionDb) irType.next();
//            opts.append("<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>");
//        }
//        model.addAttribute("kindOpts", opts);
//
//        model.addAttribute("isPlatForm", License.getInstance().isPlatform());
//
//        return "th/admin/role_add";
//    }

    @ApiOperation(value = "创建角色", notes = "创建角色", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "用户编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "isSystem", value = "是否系统", dataType = "Integer"),
            @ApiImplicitParam(name = "orders", value = "序号",  dataType = "String"),
            @ApiImplicitParam(name = "unitCode", value = "单位编码",  dataType = "String"),
            @ApiImplicitParam(name = "isDeptManager", value = "是否管理本部门",  dataType = "String"),
            @ApiImplicitParam(name = "kind", value = "类别",  dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/createRole", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Role> createRole(@RequestParam(required = true) String code,
                                   String desc, @RequestParam(defaultValue = "0") Integer isSystem, @RequestParam(defaultValue = "1") Integer orders, @RequestParam(defaultValue = "-1") Long diskQuota,
                                   String unitCode, @RequestParam(defaultValue = "-1") Long msgSpaceQuota, @RequestParam(defaultValue = "0") Integer isDeptManager, String kind
    ) throws ValidateException {
        Result<Role> result = new Result<>();
		Role role = roleService.getRole(code);
        if (role != null) {
            return new Result<>(false, "存在相同编码的角色: " + role.getDescription());
        }
        boolean re = roleService.create(code, desc, isSystem, orders, diskQuota, unitCode, msgSpaceQuota, isDeptManager, kind, null);
        if (!re) {
            result.error500("操作失败");
        }
        return result;
    }

//    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
//    @RequestMapping(value = "/admin/editRole")
//    public String editRole(String roleCode, String tabIdOpener, Model model) throws ValidateException {
//        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
//        model.addAttribute("tabIdOpener", tabIdOpener);
//
//        Role role = roleService.getRole(roleCode);
//        if (role == null) {
//            throw new ValidateException("#role.notexist", new Object[]{roleCode});
//        }
//        model.addAttribute("role", role);
//
//        Config cfg = Config.getInstance();
//
//        Privilege pvg = new Privilege();
//        boolean isAdmin = pvg.isUserPrivValid(request, Privilege.ADMIN);
//        model.addAttribute("isAdmin", isAdmin);
//
//        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
//        model.addAttribute("unitName", department.getName());
//
//        StringBuffer sb = new StringBuffer();
//        departmentService.getUnitAsOptions(sb, department, department.getLayer());
//        model.addAttribute("unitOpts", sb.toString());
//
//        SelectDb sd = new SelectDb();
//        sd = sd.getSelectDb("role_kind");
//        StringBuffer opts = new StringBuffer();
//        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
//        Iterator irType = vType.iterator();
//        while (irType.hasNext()) {
//            SelectOptionDb sod = (SelectOptionDb) irType.next();
//            opts.append("<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>");
//        }
//        model.addAttribute("kindOpts", opts);
//
//        model.addAttribute("isPlatForm", License.getInstance().isPlatform());
//
//        boolean isPostUsed = cfg.getBooleanProperty("isPostUsed");
//        model.addAttribute("isPostUsed", isPostUsed);
//
//        return "th/admin/role_edit";
//    }

    @ApiOperation(value = "修改角色", notes = "修改角色", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "用户编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "isSystem", value = "是否系统", dataType = "Integer"),
            @ApiImplicitParam(name = "orders", value = "序号",  dataType = "String"),
            @ApiImplicitParam(name = "unitCode", value = "单位编码",  dataType = "String"),
            @ApiImplicitParam(name = "isDeptManager", value = "是否管理本部门",  dataType = "String"),
            @ApiImplicitParam(name = "kind", value = "类别",  dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/updateRole", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public Result<Role> updateRole(@RequestParam(required = true) String code,
                                   String desc, @RequestParam(defaultValue = "0") Integer isSystem, @RequestParam(defaultValue = "1") Integer orders, @RequestParam(defaultValue = "-1") Long diskQuota,
                                   String unitCode, @RequestParam(defaultValue = "-1") Long msgSpaceQuota, @RequestParam(defaultValue = "0") Integer isDeptManager, String kind
    ) throws ValidateException {

        Result<Role> result = new Result<>();
        Role role = new Role();
        role.setCode(code);
        role.setDescription(desc);
        role.setIsSystem(isSystem == 1);
        role.setOrders(orders);
        role.setDiskQuota(diskQuota);
        role.setUnitCode(unitCode);
        role.setMsgSpaceQuota(msgSpaceQuota);
        role.setIsDeptManager(String.valueOf(isDeptManager));
        role.setKind(kind);

        boolean re = roleService.update(role, false);

        if (!re) {
            result.error500("修改失败");
        }

        return result;
    }

    @ApiOperation(value = "删除角色", notes = "删除角色", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "用户编码", required = true, dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/delRole", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public Result<Object> delRole(@RequestParam(required = true) String code) throws ValidateException {
        Role role = roleService.getRole(code);
        if (role == null) {
            return new Result<>(false, "对象不存在");
        }
        return new Result<>(roleService.del(role));
    }

//    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
//    @RequestMapping(value = "/admin/listUserOfRole")
//    public String listUserOfRole(String roleCode, Model model) throws ValidateException {
//        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
//
//        Role role = roleService.getRole(roleCode);
//        if (role == null) {
//            throw new ValidateException("#role.notexist", new Object[]{roleCode});
//        }
//        model.addAttribute("role", role);
//
//        com.redmoon.oa.Config oacfg = new com.redmoon.oa.Config();
//        boolean isUseAccount = oacfg.getBooleanProperty("isUseAccount");
//        model.addAttribute("isUseAccount", isUseAccount);
//
//        int k=1;
//        boolean isNeedSort = false;
//        StringBuffer sbUserOpts = new StringBuffer();
//        List<UserOfRole> list = userOfRoleService.listByRoleCode(roleCode);
//        JSONArray jsonArray = new JSONArray();
//        for (UserOfRole userOfRole : list) {
//            JSONObject json = new JSONObject();
//            json.put("name", userOfRole.getUserName());
//
//            User user = userService.getUser(userOfRole.getUserName());
//            json.put("realName", user.getRealName());
//
//            sbUserOpts.append("<option value='" + user.getName() + "'>" + user.getRealName() + "</option>");
//
//            Account acc = accountService.getAccountByUserName(user.getName());
//            json.put("account", acc!=null ? acc.getName() : "");
//
//            json.put("gender", user.getGender()?"女":"男");
//
//            String depts = userOfRole.getDepts();
//            if (depts==null || "".equals(depts)) {
//                depts = "{}";
//            }
//            com.alibaba.fastjson.JSONObject deptsJson = com.alibaba.fastjson.JSONObject.parseObject(depts);
//
//            List<DeptUser> duList = deptUserService.listByUserName(user.getName());
//            /*StringBuffer deptNames = new StringBuffer();
//            for (DeptUser du : duList) {
//                Department dept = departmentService.getDepartment(du.getDeptCode());
//                String deptName;
//                if (!dept.getParentCode().equals(ConstUtil.DEPT_ROOT) && !dept.getCode().equals(ConstUtil.DEPT_ROOT)) {
//                    Department parentDept = departmentService.getDepartment(dept.getParentCode());
//                    deptName = parentDept.getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dept.getName();
//                } else {
//                    deptName = dept.getName();
//                }
//                StrUtil.concat(deptNames, "，", deptName);
//            }
//            json.put("deptNames", deptNames.toString());*/
//
//            JSONArray arr = new JSONArray();
//            for (DeptUser du : duList) {
//                Department dept = departmentService.getDepartment(du.getDeptCode());
//                String deptName;
//                if (!dept.getParentCode().equals(ConstUtil.DEPT_ROOT) && !dept.getCode().equals(ConstUtil.DEPT_ROOT)) {
//                    Department parentDept = departmentService.getDepartment(dept.getParentCode());
//                    deptName = parentDept.getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dept.getName();
//                } else {
//                    deptName = dept.getName();
//                }
//
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("deptName", deptName);
//
//                int roleOfDept = ConstUtil.ROLE_OF_DEPT_DEFAULT;
//                if (deptsJson.containsKey(dept.getCode())) {
//                    roleOfDept = deptsJson.getIntValue(dept.getCode());
//                }
//                jsonObject.put("roleOfDept", roleOfDept);
//                jsonObject.put("deptCode", dept.getCode());
//                arr.add(jsonObject);
//            }
//            json.put("deptJson", arr);
//
//            jsonArray.add(json);
//
//            if (!isNeedSort && k != userOfRole.getOrders()) {
//                isNeedSort = true;
//            }
//            k++;
//        }
//
//        // 如果需要重排，则直接在此重新排序
//        if (isNeedSort) {
//            userOfRoleService.sortUser(roleCode);
//        }
//
//        model.addAttribute("isNeedSort", isNeedSort);
//        model.addAttribute("userOpts", sbUserOpts.toString());
//        model.addAttribute("jsonAry", jsonArray);
//        model.addAttribute("unitCode", new Privilege().getUserUnitCode());
//        boolean isPostUsed = Config.getInstance().getBooleanProperty("isPostUsed");
//        model.addAttribute("isPostUsed", isPostUsed);
//        return "th/admin/role_user_list";
//    }

    @ApiOperation(value = "将角色${roleDesc}赋予给用户${userRealNames}", notes = "将角色${roleDesc}赋予给用户${userRealNames}", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "roleDesc", value = "角色描述", dataType = "String"),
            @ApiImplicitParam(name = "userNames", value = "用户名称",  dataType = "String"),
            @ApiImplicitParam(name = "userRealNames", value = "用户真实名称",  dataType = "String"),
    })
    @SysLog(type = LogType.AUTHORIZE, action = "将角色${roleDesc}赋予给用户${userRealNames}", remark = "将角色${roleDesc}赋予给用户${userRealNames}", debug = true, level = LogLevel.NORMAL)
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/addRoleUser")
    @ResponseBody
    public Result<Role> addRoleUser(@RequestParam(required = true) String roleCode, String roleDesc,
                                    @NotEmpty(message = "{user.select.none}") @RequestParam(required = true) String userNames, String userRealNames) {
        String[] userAry = StrUtil.split(userNames, ",");
        boolean ok = userOfRoleService.create(roleCode, userAry);

        return new Result<>(ok);
    }

    @ApiOperation(value = "将角色${roleDesc}中的用户${userNames}删除", notes = "将角色${roleDesc}中的用户${userNames}删除", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "roleDesc", value = "角色描述", dataType = "String"),
            @ApiImplicitParam(name = "userNames", value = "用户名称",  dataType = "String"),
    })
    @SysLog(type = LogType.AUTHORIZE, action = "将角色${roleDesc}中的用户${userNames}删除", remark = "将角色${roleDesc}中的用户${userNames}删除", debug = true, level = LogLevel.NORMAL)
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/delRoleUserBatch")
    @ResponseBody
    public Result<Role> delRoleUserBatch(@RequestParam(required = true) String roleCode, String roleDesc,
                                         @NotEmpty(message = "{user.select.none}") @RequestParam(required = true) String userNames) {
        String[] userAry = StrUtil.split(userNames, ",");
        boolean ok = userOfRoleService.delBatch(roleCode, userAry);
        return new Result<>(ok);
    }

    @ApiOperation(value = "删除用户角色", notes = "删除用户角色", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "direction", value = "描述", dataType = "String"),
            @ApiImplicitParam(name = "userName", value = "用户名称",  dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/moveRoleUser")
    @ResponseBody
    public Result<Role> moveRoleUser(@RequestParam(required = true) String roleCode, String direction,
                                     @NotEmpty(message = "{user.name.notempty}") @RequestParam(required = true) String userName) throws ErrMsgException {
        boolean ok = userOfRoleService.move(roleCode, userName, direction);
        return new Result<>(ok);
    }

    @ApiOperation(value = "删除用户角色", notes = "删除用户角色", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "direction", value = "描述", dataType = "String"),
            @ApiImplicitParam(name = "userName", value = "用户名称",  dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/moveToRoleUser")
    @ResponseBody
    public Result<Role> moveToRoleUser(@RequestParam(required = true) String roleCode, String targetUser,
                                       @NotEmpty(message = "{user.name.notempty}") @RequestParam(required = true) String userName, Integer pos) throws ErrMsgException, ValidateException {
        Result<Role> result = new Result<>();
        UserOfRole userOfRole = userOfRoleService.getUserOfRole(userName, roleCode);
        if (userOfRole == null) {
            result.error500("角色中的用户不存在");
        } else {
            userOfRoleService.moveTo(userOfRole, targetUser, pos);
        }
        return result;
    }

    @ApiOperation(value = "用户管理的部门", notes = "用户管理的部门", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", required = true, dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/admin/roleAdminDept", method = RequestMethod.GET, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<JSONObject> roleAdminDept(String roleCode) throws ValidateException {
        Result<JSONObject> result = new Result<>();
        JSONObject object = new JSONObject();
        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }
        object.put("role", role);

        // 取得角色能管理的部门
        StringBuffer sbDepts = new StringBuffer();
        StringBuffer sbDeptNames = new StringBuffer();
        List<RoleDept> rdList = roleDeptService.listByRoleCode(roleCode);
        for (RoleDept roleDept : rdList) {
            Department department = departmentService.getDepartment(roleDept.getDeptCode());
            StrUtil.concat(sbDepts, ",", department.getCode());
            StrUtil.concat(sbDeptNames, ",", department.getName());
        }
        object.put("depts", sbDepts.toString());
        object.put("deptNames", sbDeptNames.toString());
        boolean isPostUsed = Config.getInstance().getBooleanProperty("isPostUsed");
        object.put("isPostUsed", isPostUsed);
        result.setData(object);
        return result;
    }

    @ApiOperation(value = "用户管理的部门", notes = "用户管理的部门", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "roleDesc", value = "角色描述",  dataType = "String"),
            @ApiImplicitParam(name = "deptCodes", value = "部门编码",  dataType = "String"),
            @ApiImplicitParam(name = "deptNames", value = "部门名称",  dataType = "String"),

    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/setRoleAdminDept")
    @ResponseBody
    public Result<Role> setRoleAdminDept(@RequestParam(required = true) String roleCode, String roleDesc, String deptCodes, String deptNames) throws ValidateException {
        Result<Role> result = new Result<>();
        Role role = roleService.getRole(roleCode);
        if (role == null) {
            result.error500("角色不存在");
        } else {
            roleDeptService.setRoleDepts(roleCode, roleDesc, deptCodes, deptNames);
        }
        return result;
    }

    /**
     * 将各个权限可见的菜单项存于map中
     *
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

    @ApiOperation(value = "角色权限", notes = "角色权限", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", required = true, dataType = "String"),

    })
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/rolePriv")
    public Result<JSONObject> rolePriv(String roleCode) throws ValidateException {
        Result<JSONObject> result = new Result<>();
        JSONObject object = new JSONObject();
        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }

        object.put("role", role);
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
            userAuthorityVO.setIsAdmin(privilege.getIsAdmin() == 1);

            boolean isAuthorized = rolePrivService.getRolePriv(roleCode, privilege.getPriv()) != null;
            userAuthorityVO.setAuthorized(isAuthorized);

            userAuthorityVO.setMenuList(map.get(privilege.getPriv()));

            authorityVOList.add(userAuthorityVO);
        }

        object.put("list", authorityVOList);
        boolean isPostUsed = Config.getInstance().getBooleanProperty("isPostUsed");
        object.put("isPostUsed", isPostUsed);
        result.setData(object);
        return result;
    }

    @ApiOperation(value = "设置角色权限", notes = "设置角色权限", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "priv", value = "角色priv",  dataType = "String"),
    })

    @ResponseBody
    @RequestMapping(value = "/admin/setRolePrivs", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<JSONObject> setRolePrivs(String roleCode, String privs) throws ValidateException {
        String[] priv = privs.split(",");
        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }
        return new Result<>(rolePrivService.setPrivs(roleCode, priv));
    }

    @ApiOperation(value = "角色菜单", notes = "角色菜单,当菜单为链接型，type = 1,没有配置权限,priv = ''，则为默认选中状态且不可更改", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", required = true, dataType = "String"),
    })
    @RequestMapping(value = "/admin/roleMenu")
    @ResponseBody
    public Result<Object> roleMenu(String roleCode) throws ValidateException {
        Result<Object> result = new Result<>();
        JSONObject object = new JSONObject();
        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }
        object.put("role", role);
        JSONArray array = JSON.parseArray(menuService.getJsonTreeString(roleCode));
        object.put("list", array);
        boolean isPostUsed = Config.getInstance().getBooleanProperty("isPostUsed");
        object.put("isPostUsed", isPostUsed);
        result.setData(object);
        return result;
    }

    /**
     * 置用户角色在其兼职部门中是否有效
     *
     * @return
     */
    @ApiOperation(value = "置用户角色在其兼职部门中是否有效", notes = "置用户角色在其兼职部门中是否有效", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名称", required = true, dataType = "String"),
            @ApiImplicitParam(name = "roleCode", value = "角色编码", dataType = "String"),
            @ApiImplicitParam(name = "deptCode", value = "部门编码", dataType = "String"),
            @ApiImplicitParam(name = "roleOfDept", value = "部门角色", dataType = "Integer"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/admin/setUserRoleOfDept", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Role> setUserRoleOfDept(String userName, String roleCode, String deptCode, int roleOfDept) {
        Result<Role> result = new Result<>();
        UserOfRole userOfRole = userOfRoleService.getUserOfRole(userName, roleCode);
        if (userOfRole != null) {
            String depts = userOfRole.getDepts();
            if (StringUtils.isEmpty(depts)) {
                depts = "{}";
            }
            com.alibaba.fastjson.JSONObject deptsJson = com.alibaba.fastjson.JSONObject.parseObject(depts);
            if (deptsJson.containsKey(deptCode)) {
                // 如果为2表示默认在该部门中拥有角色
                if (roleOfDept == ConstUtil.ROLE_OF_DEPT_DEFAULT) {
                    deptsJson.remove(deptCode);
                } else {
                    deptsJson.put(deptCode, String.valueOf(roleOfDept));
                }
            } else {
                deptsJson.put(deptCode, String.valueOf(roleOfDept));
            }
            userOfRole.setDepts(deptsJson.toString());
            boolean re = userOfRoleService.update(userOfRole);
            if (!re) {
                result.error500("操作失败");
            }
        } else {
            result.error500("记录不存在");
        }

        return result;
    }


    @ApiOperation(value = "获取角色描述", notes = "获取角色描述", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/getRoleDescs", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public String getRoleDescs(String roleCodes) throws ValidateException {
        String[] ary = StrUtil.split(roleCodes, ",");
        String str = "";
        if (ary != null) {
            RoleDb rd = new RoleDb();
            for (int i = 0; i < ary.length; i++) {
                rd = rd.getRoleDb(ary[i]);
                if ("".equals(str)) {
                    str = rd.getDesc();
                } else {
                    str += "," + rd.getDesc();
                }
            }
        }
        return str;
    }

    @ApiOperation(value = "获取单位", notes = "获取单位", httpMethod = "GET")
    @RequestMapping(value = "/admin/getUnit", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> getUnit() throws ValidateException {
        Result<Object> result = new Result<>();
        JSONObject jsonObject = new JSONObject();
        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
        jsonObject.put("unitName", department.getName());
        List<JSONObject> list = departmentService.getUnits(department, department.getLayer());
        jsonObject.put("list", list);
        result.setData(jsonObject);
        return result;
    }

    @ApiOperation(value = "获取编码", notes = "获取编码", httpMethod = "GET")
    @RequestMapping(value = "/admin/getCode", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> getCode() throws ValidateException {
        String code = RandomSecquenceCreator.getId(20);
        return new Result<>(code);
    }

    @ApiOperation(value = "获取角色用户", notes = "获取角色用户", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", required = true, dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/listUserOfRole")
    @ResponseBody
    public Result<Object> listUserOfRole(String roleCode) throws ValidateException {
        Result<Object> result = new Result<>();
        JSONObject object = new JSONObject();
        Role role = roleService.getRole(roleCode);
        if (role == null) {
            throw new ValidateException("#role.notexist", new Object[]{roleCode});
        }
        object.put("role", role);

        com.redmoon.oa.Config oacfg = new com.redmoon.oa.Config();
        boolean isUseAccount = oacfg.getBooleanProperty("isUseAccount");
        object.put("isUseAccount", isUseAccount);

        int k = 1;
        boolean isNeedSort = false;
        List<UserOfRole> list = userOfRoleService.listByRoleCode(roleCode);
        JSONArray jsonArray = new JSONArray();
        for (UserOfRole userOfRole : list) {
            JSONObject json = new JSONObject();
            User user = userService.getUser(userOfRole.getUserName());
            if (user == null) {
                // 删除垃圾数据
                LogUtil.getLog(getClass()).error("用户: " + userOfRole.getUserName() + " 不存在");
                userOfRoleService.del(userOfRole.getUserName(), userOfRole.getRoleCode());
                continue;
            }
            json.put("name", user.getLoginName());
            json.put("realName", user.getRealName());

            Account acc = accountService.getAccountByUserName(user.getName());
            json.put("account", acc != null ? acc.getName() : "");

            json.put("gender", user.getGender() ? "女" : "男");

            String depts = userOfRole.getDepts();
            if (depts == null || "".equals(depts)) {
                depts = "{}";
            }
            com.alibaba.fastjson.JSONObject deptsJson = com.alibaba.fastjson.JSONObject.parseObject(depts);

            List<DeptUser> duList = deptUserService.listByUserName(user.getName());
            JSONArray arr = new JSONArray();
            for (DeptUser du : duList) {
                Department dept = departmentService.getDepartment(du.getDeptCode());
                String deptName = "";
                if (dept != null) {
                    if (!dept.getParentCode().equals(ConstUtil.DEPT_ROOT) && !dept.getCode().equals(ConstUtil.DEPT_ROOT)) {
                        Department parentDept = departmentService.getDepartment(dept.getParentCode());
                        deptName = parentDept.getName() + dept.getName();
                    } else {
                        deptName = dept.getName();
                    }
                } else {
                    LogUtil.getLog(getClass()).warn("用户: " + user.getRealName() + " 的部门: " + du.getDeptCode() + " 不存在");
                    continue;
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("deptName", deptName);

                int roleOfDept = ConstUtil.ROLE_OF_DEPT_DEFAULT;
                if (deptsJson.containsKey(du.getDeptCode())) {
                    roleOfDept = deptsJson.getIntValue(du.getDeptCode());
                }
                jsonObject.put("roleOfDept", roleOfDept);
                jsonObject.put("deptCode", du.getDeptCode());
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

        object.put("isNeedSort", isNeedSort);
        object.put("list", jsonArray);
        object.put("unitCode", new Privilege().getUserUnitCode());
        boolean isPostUsed = Config.getInstance().getBooleanProperty("isPostUsed");
        object.put("isPostUsed", isPostUsed);
        result.setData(object);
        return result;
    }
}
