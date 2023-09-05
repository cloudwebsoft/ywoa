package com.cloudweb.oa.controller;

import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.service.IRoleService;
import com.cloudweb.oa.service.IUserRoleDepartmentService;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.redmoon.oa.Config;
import com.redmoon.oa.ui.SkinMgr;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 角色关联的部门 前端控制器
 * </p>
 *
 * @author fgf
 * @since 2022-03-13
 */
@Controller
@RequestMapping("/admin")
public class UserRoleDepartmentController {

    @Autowired
    IRoleService roleService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    IUserRoleDepartmentService userRoleDepartmentService;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    ResponseUtil responseUtil;

    @RequestMapping(value = "/roleDepartmentListPage")
    @ResponseBody
    public Result<Object> roleDepartmentListPage( String roleCode) {
        JSONObject json = new JSONObject();
        Role role = roleService.getRole(roleCode);
        json.put("role", role);
        return new Result<>(json);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping(value = "/roleDepartmentList", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> roleDepartmentList(@RequestParam(defaultValue = "") String op,
                                             @RequestParam(required = true) String roleCode,
                                             @RequestParam(defaultValue = "") String name,
                                             @RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "20") int pageSize
    ) {
        JSONObject json = new JSONObject();

        JSONArray arr = new JSONArray();
        PageHelper.startPage(pageNum, pageSize); // 分页
        List<UserRoleDepartment> list = userRoleDepartmentService.list(op, roleCode, name);
        PageInfo<UserRoleDepartment> pageInfo = new PageInfo<>(list);
        for (UserRoleDepartment userRoleDepartment : list) {
            JSONObject jsonObj = new JSONObject();
            Department department = departmentService.getDepartment(userRoleDepartment.getDeptCode());
            jsonObj.put("id", userRoleDepartment.getId());
            if (department == null) {
                jsonObj.put("name", "不存在");
                jsonObj.put("description", "");
            }
            else {
                jsonObj.put("name", department.getName());
                jsonObj.put("description", department.getDescription());
            }
            jsonObj.put("roleCode", roleCode);
            jsonObj.put("deptCode", userRoleDepartment.getDeptCode());
            jsonObj.put("fullDeptName", departmentService.getFullNameOfDept(department));
            jsonObj.put("include", userRoleDepartment.getInclude());
            arr.add(jsonObj);
        }

        json.put("roleCode", roleCode);
        json.put("list", arr);
        json.put("total", pageInfo.getTotal());
        json.put("page", pageInfo.getPageNum());
        return new Result<>(json);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping(value = "/create", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> create(@RequestParam(required = true) String roleCode, @RequestParam(required = true)String deptCodes) {
        String[] ary = StrUtil.split(deptCodes, ",");
        return new Result<>(userRoleDepartmentService.create(roleCode, ary));
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping(value = "/del", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> del(@RequestParam(required = true) String ids) {
        String[] ary = StrUtil.split(ids, ",");
        return new Result<>(userRoleDepartmentService.del(ary));
    }

    @ApiOperation(value = "更改是否包含子部门", notes = "更改是否包含子部门", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "角色下的部门ID", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "include", value = "是否包含子部门", required = true, dataType = "Boolean")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/changeRoleDepartmentStatus", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> changeRoleDepartmentStatus(@RequestParam(required = true) Integer id, @RequestParam(required = true) Boolean include) throws ValidateException {
        UserRoleDepartment userRoleDepartment = userRoleDepartmentService.getById(id);
        if (userRoleDepartment == null) {
            throw new ValidateException("#notexist", new Object[]{id});
        }
        boolean isIncludeOld = userRoleDepartment.getInclude();
        userRoleDepartment.setInclude(include);
        return new Result<>(userRoleDepartmentService.update(userRoleDepartment, true, isIncludeOld));
    }
}

