package com.cloudweb.oa.controller;


import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.Result;
import com.redmoon.oa.ui.SkinMgr;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.bytebuddy.asm.Advice;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
@Api(tags = "权限管理模块")
@Controller
@RequestMapping("/admin")
public class PrivilegeController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    IPrivilegeService privilegeService;

    @Autowired
    ResponseUtil responseUtil;

    @Autowired
    IUserPrivService userPrivService;

    @Autowired
    IRolePrivService rolePrivService;

    @Autowired
    IGroupPrivService groupPrivService;

    @Autowired
    IUserService userService;

    @Autowired
    IDeptUserService deptUserService;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IRoleService roleService;

    @Autowired
    IGroupService groupService;

    @RequestMapping(value = "/listPriv")
    @ResponseBody
    public Result<Object> listPriv() throws ValidateException {
        List<Privilege> list = privilegeService.listByLicense();

        return new Result<>(list);
    }

    @ResponseBody
    @RequestMapping(value = "/setPrivs", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> setPrivs(@RequestParam(value = "tblPrivs_rowOrder") String rowOrder, String oldPrivs) throws ValidateException {
        boolean re = privilegeService.setPrivs(request, rowOrder, oldPrivs);
        return new Result<>(re);
    }

    @ResponseBody
    @RequestMapping(value = "/setPrivsList", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> setPrivsList(@RequestParam String newRowOrder, String oldPrivs) throws ValidateException {
        boolean re = privilegeService.setPrivsList(request, newRowOrder, oldPrivs);
        return new Result<>(re);
    }

//    @RequestMapping(value = "/addPriv")
//    public String addPriv(Model model) throws ValidateException {
//        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
//        return "th/admin/priv_add";
//    }

    @ResponseBody
    @RequestMapping(value = "/createPriv", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> createPriv(String priv, String desc, int layer) throws ValidateException {
        Privilege privilege = new Privilege();
        List<Privilege> maxList = privilegeService.list(new QueryWrapper<Privilege>().orderByDesc("orders"));
        if (maxList.size() > 0) {
            privilege.setOrders(maxList.get(0).getOrders() + 1);
        } else {
            privilege.setOrders(1);
        }
        privilege.setPriv(priv);
        privilege.setDescription(desc);
        privilege.setLayer(layer);
        boolean re = privilege.insert();
        return new Result<>(re);
    }

    @RequestMapping(value = "/privDetail")
    @ResponseBody
    public Result<Object> privDetail(String priv) throws ValidateException {
        JSONObject object = new JSONObject();
        Privilege privilege = privilegeService.getByPriv(priv);
        object.put("privilege", privilege);

        com.alibaba.fastjson.JSONArray arr = new JSONArray();

        JSONArray userArr = new JSONArray();
        List<UserPriv> userList = userPrivService.listByPriv(priv);
        for (UserPriv userPriv : userList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userName", userPriv.getUsername());

            User user = userService.getUser(userPriv.getUsername());
            jsonObject.put("realName", user.getRealName());

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
            jsonObject.put("deptNames", deptNames.toString());
            userArr.add(jsonObject);

            JSONObject json = new JSONObject();
            json.put("kind", ConstUtil.PRIV_TYPE_USER);
            json.put("code", userPriv.getUsername());
            json.put("name", user.getRealName());
            arr.add(json);
        }
        object.put("list", userArr);

        List<RolePriv> rpList = rolePrivService.listByRolePriv(priv);
        List<Role> roleList = new ArrayList<>();
        for (RolePriv rolePriv : rpList) {
            Role role = roleService.getRole(rolePriv.getRoleCode());
            roleList.add(role);

            JSONObject json = new JSONObject();
            json.put("kind", ConstUtil.PRIV_TYPE_ROLE);
            json.put("code", role.getCode());
            json.put("name", role.getDescription());
            arr.add(json);
        }
        object.put("roleList", roleList);

        List<GroupPriv> gpList = groupPrivService.listByPriv(priv);
        List<Group> groupList = new ArrayList<>();
        for (GroupPriv groupPriv : gpList) {
            Group group = groupService.getGroup(groupPriv.getGroupCode());
            groupList.add(group);

            JSONObject json = new JSONObject();
            json.put("kind", ConstUtil.PRIV_TYPE_USERGROUP);
            json.put("code", group.getCode());
            json.put("name", group.getDescription());
            arr.add(json);
        }
        object.put("groupList", groupList);

        object.put("allPriv", arr);

        return new Result<>(object);
    }

    @ResponseBody
    @RequestMapping(value = "/delRolePriv", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> delRolePriv(String roleCode, String priv) {
        return new Result<>(rolePrivService.del(roleCode, priv));
    }

    @ResponseBody
    @RequestMapping(value = "/delUserPriv", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> delUserPriv(String userName, String priv) {
        boolean re = userPrivService.delUserPriv(userName, priv);
        return new Result<>(re);
    }

    @ResponseBody
    @RequestMapping(value = "/delGroupPriv", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> delGroupPriv(String groupCode, String priv) {
        return new Result<>(groupPrivService.delGroupPriv(groupCode, priv));
    }

    /**
     * 置角色、用户组、用户权限，如有则保留，如无则添加，如未选中则删除
     *
     * @param roleGroupUser
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/setRoleGroupUser", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;"})
    public Result<Object> setRoleGroupUser(@RequestParam(required = true) String priv, String roleGroupUser) {
        boolean re = true;
        if (roleGroupUser != null && !"".equals(roleGroupUser)) {
            com.alibaba.fastjson.JSONArray arrAll = new JSONArray();

            List<UserPriv> userList = userPrivService.listByPriv(priv);
            for (UserPriv userPriv : userList) {
                JSONObject json = new JSONObject();
                json.put("kind", ConstUtil.PRIV_TYPE_USER);
                json.put("code", userPriv.getUsername());
                arrAll.add(json);
            }

            List<RolePriv> rpList = rolePrivService.listByRolePriv(priv);
            for (RolePriv rolePriv : rpList) {
                Role role = roleService.getRole(rolePriv.getRoleCode());
                JSONObject json = new JSONObject();
                json.put("kind", ConstUtil.PRIV_TYPE_ROLE);
                json.put("code", role.getCode());
                arrAll.add(json);
            }

            List<GroupPriv> gpList = groupPrivService.listByPriv(priv);
            for (GroupPriv groupPriv : gpList) {
                Group group = groupService.getGroup(groupPriv.getGroupCode());
                JSONObject json = new JSONObject();
                json.put("kind", ConstUtil.PRIV_TYPE_USERGROUP);
                json.put("code", group.getCode());
                json.put("name", group.getDescription());
                arrAll.add(json);
            }

            com.alibaba.fastjson.JSONArray arr = com.alibaba.fastjson.JSONArray.parseArray(roleGroupUser);
            // 删除没被选择的权限
            Iterator<Object> ir = arrAll.iterator();
            while (ir.hasNext()) {
                JSONObject privJson = (JSONObject) ir.next();
                int privKind = privJson.getIntValue("kind");
                String myCode = privJson.getString("code");
                boolean isFound = false;
                for (int i = 0; i < arr.size(); i++) {
                    com.alibaba.fastjson.JSONObject jsonObject = arr.getJSONObject(i);
                    String code = jsonObject.getString("code");
                    String kind = jsonObject.getString("kind");
                    int type = StrUtil.toInt(kind, ConstUtil.PRIV_TYPE_ROLE);
                    if (myCode.equals(code) && privKind == type) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    if (privKind == ConstUtil.PRIV_TYPE_ROLE) {
                        re = rolePrivService.del(myCode, priv);
                        ir.remove();
                    } else if (privKind == ConstUtil.PRIV_TYPE_USERGROUP) {
                        re = groupPrivService.delGroupPriv(myCode, priv);
                        ir.remove();
                    } else if (privKind == ConstUtil.PRIV_TYPE_USER) {
                        re = userPrivService.delUserPriv(myCode, priv);
                        ir.remove();
                    }
                }
            }

            for (int i = 0; i < arr.size(); i++) {
                com.alibaba.fastjson.JSONObject jsonObject = arr.getJSONObject(i);
                String code = jsonObject.getString("code");
                String kind = jsonObject.getString("kind");
                int privKind = StrUtil.toInt(kind, ConstUtil.PRIV_TYPE_ROLE);
                // 判断原来是否已存在该权限
                boolean isFound = false;
                for (Object o : arrAll) {
                    JSONObject json = (JSONObject) o;
                    if (json.getString("code").equals(code) && json.getIntValue("kind") == privKind) {
                        isFound = true;
                        break;
                    }
                }

                if (!isFound) {
                    if (privKind == ConstUtil.PRIV_TYPE_ROLE) {
                        re = rolePrivService.create(code, priv);
                    } else if (privKind == ConstUtil.PRIV_TYPE_USERGROUP) {
                        re = groupPrivService.create(code, priv);
                    } else if (privKind == ConstUtil.PRIV_TYPE_USER) {
                        re = userPrivService.create(code, priv);
                    }
                }
            }
        }

        return new Result<>(re);
    }

    @ApiOperation(value = "切换角色", notes = "切换角色")
    @ResponseBody
    @RequestMapping(value = "/switchRole", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> switchRole(String curRoleCode) {
        com.redmoon.oa.pvg.Privilege.setCurRoleCode(curRoleCode);
        return new Result<>(true);
    }

    @ApiOperation(value = "切换部门", notes = "切换部门")
    @ResponseBody
    @RequestMapping(value = "/switchDept", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> switchDept(String curDeptCode) {
        com.redmoon.oa.pvg.Privilege.setCurDeptCode(curDeptCode);
        return new Result<>(true);
    }
}
