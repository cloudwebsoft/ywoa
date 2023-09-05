package com.cloudweb.oa.controller;


import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.SkinMgr;
import org.apache.commons.lang3.StringUtils;
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
 *  前端控制器
 * </p>
 *
 * @author fgf
 * @since 2022-02-19
 */
@Controller
@RequestMapping("/admin")
public class UserRolePostController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IPostService postService;

    @Autowired
    IRoleService roleService;

    @Autowired
    ResponseUtil responseUtil;

    @Autowired
    IUserRolePostService userRolePostService;

    @Autowired
    IPostUserService postUserService;

    @Autowired
    UserCache userCache;

    /**
     * 职位穿梭框
     * @param roleCode
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping("/rolePostTransferPage")
    @ResponseBody
    public Result<Object> postTransferPage(@RequestParam(required = true) String roleCode) {
        JSONObject object = new JSONObject();
        Role role = roleService.getRole(roleCode);
        JSONArray ary = new JSONArray();
        List<Post> list = postService.list();
        for (Post post : list) {
            JSONObject json = new JSONObject();
            json.put("id", String.valueOf(post.getId()));
            Department department = departmentService.getDepartment(post.getDeptCode());
            String fullDeptName = departmentService.getFullNameOfDept(department);
            json.put("name", post.getName());
            json.put("fullDeptName", fullDeptName);
            ary.add(json);
        }

        object.put("list", ary);
        List<UserRolePost> rolePostList = userRolePostService.listRolePost("", role.getCode(), "");
        object.put("rolePostList", rolePostList);
        return new Result<>(object);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping(value = "/rolePostUpdate", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> rolePostUpdate(@RequestParam(required = true) String roleCode, @RequestParam(required = true) String ids) {
        String[] ary = StrUtil.split(ids, ",");
        return new Result<>(userRolePostService.update(roleCode, ary));
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping(value = "/rolePostDel", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> rolePostDel(@RequestParam(required = true) String ids) {
        String[] ary = StrUtil.split(ids, ",");
        return new Result<>(userRolePostService.del(ary));
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping(value = "/rolePostList", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> rolePostList(@RequestParam(defaultValue = "") String op,
                               @RequestParam(required = true) String roleCode,
                               @RequestParam(defaultValue = "") String name,
                               @RequestParam(defaultValue = "1") int pageNum,
                               @RequestParam(defaultValue = "20") int pageSize
    ) {
        JSONObject json = new JSONObject();

        JSONArray arr = new JSONArray();
        PageHelper.startPage(pageNum, pageSize); // 分页
        List<UserRolePost> list = userRolePostService.listRolePost(op, roleCode, name);
        PageInfo<UserRolePost> pageInfo = new PageInfo<>(list);
        for (UserRolePost userRolePost : list) {
            JSONObject jsonObj = new JSONObject();
            Post post = postService.getById(userRolePost.getPostId());
            jsonObj.put("id", userRolePost.getId());
            jsonObj.put("name", post.getName());
            jsonObj.put("description", post.getDescription());
            jsonObj.put("orders", post.getOrders());
            Department department = departmentService.getDepartment(post.getDeptCode());
            jsonObj.put("fullDeptName", departmentService.getFullNameOfDept(department));

            // 取出成员
            List<PostUser> postUserList = postUserService.listByPostId(post.getId());
            jsonObj.put("member", postUserList);

            arr.add(jsonObj);
        }

        json.put("roleCode", roleCode);
        json.put("list", arr);
        json.put("total", pageInfo.getTotal());
        json.put("page", pageInfo.getPageNum());
        return  new Result<>(json);
    }
}