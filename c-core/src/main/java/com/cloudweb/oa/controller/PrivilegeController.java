package com.cloudweb.oa.controller;


import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.redmoon.oa.ui.SkinMgr;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
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
    public String listPriv(Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        List<Privilege> list = privilegeService.listByLicense();

        StringBuffer oldPrivs = new StringBuffer();
        for (Privilege privilege : list) {
            StrUtil.concat(oldPrivs, ",", privilege.getPriv());
        }
        model.addAttribute("oldPrivs", oldPrivs);

        JSONArray arr = new JSONArray();
        for (Privilege privilege : list) {
            int layer = privilege.getLayer();
            String desc = privilege.getDescription();
            String desc2 = "";
            if (layer==2) {
                desc2 = desc;
                desc = "";
            }

            JSONObject json = new JSONObject();
            json.put("desc", desc);
            json.put("desc2", desc2);
            json.put("privX", privilege.getPriv());
            json.put("priv", privilege.getPriv());
            json.put("isSystem", privilege.getIsSystem()?"是":"否");
            arr.add(json);
        }
        model.addAttribute("jsonArr", arr.toString());

        return "th/admin/priv";
    }

    @ResponseBody
    @RequestMapping(value = "/setPrivs", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public JSONObject setPrivs(@RequestParam(value="tblPrivs_rowOrder") String rowOrder, String oldPrivs) throws ValidateException {
        boolean re = privilegeService.setPrivs(request, rowOrder, oldPrivs);
        return responseUtil.getResultJson(re);
    }

    @RequestMapping(value = "/addPriv")
    public String addPriv(Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        return "th/admin/priv_add";
    }

    @ResponseBody
    @RequestMapping(value = "/createPriv", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public JSONObject createPriv(String priv, String desc, int layer) throws ValidateException {
        Privilege privilege = new Privilege();
        privilege.setPriv(priv);
        privilege.setDescription(desc);
        privilege.setLayer(layer);
        boolean re = privilege.insert();
        return responseUtil.getResultJson(re);
    }

    @RequestMapping(value = "/privDetail")
    public String privDetail(String priv, Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        Privilege privilege = privilegeService.getByPriv(priv);
        model.addAttribute("privilege", privilege);

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
        }
        model.addAttribute("userArr", userArr);

        List<RolePriv> rpList = rolePrivService.listByRolePriv(priv);
        List<Role> roleList = new ArrayList<>();
        for (RolePriv rolePriv : rpList) {
            Role role = roleService.getRole(rolePriv.getRoleCode());
            roleList.add(role);
        }
        model.addAttribute("roleList", roleList);

        List<GroupPriv> gpList = groupPrivService.listByPriv(priv);
        List<Group> groupList = new ArrayList<>();
        for (GroupPriv groupPriv : gpList) {
            Group group = groupService.getGroup(groupPriv.getGroupCode());
            groupList.add(group);
        }
        model.addAttribute("groupList", groupList);

        return "th/admin/priv_detail";
    }

    @ResponseBody
    @RequestMapping(value = "/admin/delRolePriv", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public JSONObject delRolePriv(String roleCode, String priv) {
        return responseUtil.getResultJson(rolePrivService.del(roleCode, priv));
    }

    @ResponseBody
    @RequestMapping(value = "/delUserPriv", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public JSONObject delUserPriv(String userName, String priv) {
        return responseUtil.getResultJson(userPrivService.delUserPriv(userName, priv));
    }

    @ResponseBody
    @RequestMapping(value = "/delGroupPriv", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public JSONObject delGroupPriv(String groupCode, String priv) {
        return responseUtil.getResultJson(groupPrivService.delGroupPriv(groupCode, priv));
    }
}
