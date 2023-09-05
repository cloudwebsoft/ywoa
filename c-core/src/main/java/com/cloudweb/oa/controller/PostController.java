package com.cloudweb.oa.controller;


import cn.js.fan.db.ListResult;
import cn.js.fan.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.vo.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.redmoon.oa.Config;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.SkinMgr;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author fgf
 * @since 2022-02-15
 */
@Controller
@RequestMapping("/admin")
public class PostController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    IPostService postService;

    @Autowired
    ResponseUtil responseUtil;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IPostUserService postUserService;

    @Autowired
    UserCache userCache;

    @Autowired
    IDeptUserService deptUserService;

    @Autowired
    IPostExcludedService postExcludedService;

    @Autowired
    IRoleService roleService;

//    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
//    @RequestMapping("/organize/postFrameList")
//    public String postFrameList(Model model, String curDeptCode) {
//        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
//        model.addAttribute("curDeptCode", curDeptCode);
//        return "th/admin/organize/post_frame_list";
//    }

//    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
//    @RequestMapping("/organize/postListPage")
//    public String postListPage(Model model, String curDeptCode) {
//        model.addAttribute("curDeptCode", curDeptCode);
//        String skinPath = SkinMgr.getSkinPath(request, false);
//        model.addAttribute("skinPath", skinPath);
//        return "th/admin/organize/post_list";
//    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping("/organize/postEditPage")
    @ResponseBody
    public Result<Object> postEditPage(Integer id, @RequestParam(defaultValue = "") String tabIdOpener) {
        JSONObject object = new JSONObject();
        Post post = postService.getById(id);
        object.put("post", post);
        StringBuffer sb = new StringBuffer();
        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
        departmentService.getDeptAsOptions(sb, department, department.getLayer());
        // object.put("deptOpts", sb);
        // object.put("tabIdOpener", tabIdOpener);
        object.put("id", id);

        StringBuilder postsExcluded = new StringBuilder();
        List<Integer> listExcluded = postExcludedService.listByPostId(id);
        JSONArray arr = new JSONArray();
        for (Integer idExc : listExcluded) {
            Post postExc = postService.getById(idExc);
            JSONObject json = new JSONObject();
            json.put("value", postExc.getId());
            json.put("name", postExc.getName());
            department = departmentService.getDepartment(postExc.getDeptCode());
            if (department == null) {
                json.put("fullDeptName", "部门:" + postExc.getDeptCode() + "不存在");
            }
            else {
                json.put("fullDeptName", departmentService.getFullNameOfDept(department));
            }
            arr.add(json);

            StrUtil.concat(postsExcluded, ",", String.valueOf(postExc.getId()));
        }
        object.put("aryExcluded", arr);
        return new Result<>(object);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping("/organize/postTransferPage")
    @ResponseBody
    public Result<Object> postTransferPage( @RequestParam(required = true) Integer id) {
        JSONObject object = new JSONObject();
        Privilege pvg = new Privilege();
        String unitCode = pvg.getUserUnitCode(request);
        JSONArray ary = new JSONArray();
        List<Post> list = postService.listByUnitCode(unitCode);
        for (Post post : list) {
            JSONObject json = new JSONObject();
            // 过滤掉本职位
            if (post.getId() == id.intValue()) {
                continue;
            }
            json.put("id", String.valueOf(post.getId()));
            Department department = departmentService.getDepartment(post.getDeptCode());
            String fullDeptName = departmentService.getFullNameOfDept(department);
            json.put("name", post.getName());
            json.put("fullDeptName", fullDeptName);
            ary.add(json);
        }

        object.put("ary", ary);

        List<Integer> listExcluded = postExcludedService.listByPostId(id);
        object.put("postsSelected", StringUtils.join(listExcluded, ","));
        return new Result<>(object);
    }
	
	
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping("/organize/postFlowTransferPageBack")
    public String postFlowTransferPageBack(Model model, @RequestParam(required = true) String postCodes) {
        String skinPath = SkinMgr.getSkinPath(request, false);
        model.addAttribute("skinPath", skinPath);

        Privilege pvg = new Privilege();
        String unitCode = pvg.getUserUnitCode(request);
        JSONArray ary = new JSONArray();
        List<Post> list = postService.listByUnitCode(unitCode);
        for (Post post : list) {
            JSONObject json = new JSONObject();
            json.put("id", String.valueOf(post.getId()));
            Department department = departmentService.getDepartment(post.getDeptCode());
            String fullDeptName = "";
            if (department!=null) {
                fullDeptName = departmentService.getFullNameOfDept(department);
            }
            else {
                fullDeptName = "部门不存在";
            }
            json.put("name", post.getName());
            json.put("fullDeptName", fullDeptName);
            ary.add(json);
        }

        model.addAttribute("ary", ary);

        model.addAttribute("postsSelected", StringUtils.join(postCodes, ","));
        return "th/admin/organize/post_transfer";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping("/organize/postFlowTransferPage")
    @ResponseBody
    public Result<Object> postFlowTransferPage(@RequestParam(required = true) String postCodes) {
        JSONObject object = new JSONObject();
        Privilege pvg = new Privilege();
        String unitCode = pvg.getUserUnitCode(request);
        JSONArray ary = new JSONArray();
        List<Post> list = postService.listByUnitCode(unitCode);
        for (Post post : list) {
            JSONObject json = new JSONObject();
            json.put("id", String.valueOf(post.getId()));
            Department department = departmentService.getDepartment(post.getDeptCode());
            String fullDeptName = "";
            if (department!=null) {
                fullDeptName = departmentService.getFullNameOfDept(department);
            }
            else {
                fullDeptName = "部门不存在";
            }
            json.put("name", post.getName());
            json.put("fullDeptName", fullDeptName);
            ary.add(json);
        }

        object.put("ary", ary);

        object.put("postsSelected", StringUtils.join(postCodes, ","));
        return new Result<>(object);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping("/organize/postUserTransferPage")
    @ResponseBody
    public Result<Object> postUserTransferPage(@RequestParam(required = true) String id) {
        JSONObject object = new JSONObject();

        Post post = postService.getById(id);

        // 取职位中的用户
        List<String> userList = new ArrayList<>();
        List<PostUser> postUserList = postUserService.listByPostId(Integer.parseInt(id));
        for (PostUser postUser : postUserList) {
            userList.add(StrUtil.sqlstr(postUser.getUserName()));
        }
        object.put("postUsers", StringUtils.join(userList, ","));

        // 取部门中的所有用户
        JSONArray deptUserAry = new JSONArray();
        List<DeptUser> deptUserList = deptUserService.listByDeptCode(post.getDeptCode());
        for (DeptUser deptUser : deptUserList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userName", deptUser.getUserName());
            User user = userCache.getUser(deptUser.getUserName());
            jsonObject.put("realName", user.getRealName());
            deptUserAry.add(jsonObject);
        }
        object.put("deptUserAry", deptUserAry);

        return new Result<>(object);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping(value = "/organize/postList", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> postList(@RequestParam(defaultValue = "") String op,
                             @RequestParam(required = true) String deptCode,
                             @RequestParam(defaultValue = "") String name,
                             @RequestParam(defaultValue = "1") int pageNum,
                             @RequestParam(defaultValue = "20") int pageSize
    ) {
        JSONObject json = new JSONObject();

        JSONArray arr = new JSONArray();
        PageHelper.startPage(pageNum, pageSize); // 分页
        List<Post> list = postService.list(op, deptCode, name);
        PageInfo<Post> pageInfo = new PageInfo<>(list);
        for (Post post : list) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("id", post.getId());
            jsonObj.put("name", post.getName());
            jsonObj.put("description", post.getDescription());
            jsonObj.put("orders", post.getOrders());
            jsonObj.put("numLimited", post.getNumLimited());
            jsonObj.put("deptCode", post.getDeptCode());

            Department department = departmentService.getDepartment(post.getDeptCode());
            if (department == null) {
                jsonObj.put("fullDeptName", "不存在");
            }
            else {
                jsonObj.put("fullDeptName", departmentService.getFullNameOfDept(department));
            }

            int maxMemberCount = 20;
            StringBuilder sb = new StringBuilder();
            int k = 0;
            List<PostUser> postUserList = postUserService.listByPostId(post.getId());
            for (PostUser postUser : postUserList) {
                if (k >= maxMemberCount) {
                    break;
                }
                User user = userCache.getUser(postUser.getUserName());
                if (user != null) {
                    StrUtil.concat(sb, ",", user.getRealName());
                }
                else {
                    StrUtil.concat(sb, ",", postUser.getUserName() + "不存在");
                }
                k++;
            }
            if (postUserList.size() > maxMemberCount) {
                sb.append(",...");
            }
            jsonObj.put("member", sb.toString());

            arr.add(jsonObj);
        }

        json.put("deptCode", deptCode);
        json.put("errCode", 0);
        json.put("list", arr);
        json.put("total", pageInfo.getTotal());
        json.put("page", pageInfo.getPageNum());
        return new Result<>(json);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping(value = "/organize/postCreate", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> postCreate(@RequestParam(required = true) String deptCode,
                           @RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "1") String description
    ) {
        // 判断是否存在重复
        if (postService.isExist(deptCode, name)) {
            throw new ValidationException("同一部门下岗位名称不能重复");
        }
        return new Result<>(postService.create(deptCode, name, description, null, true));
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping(value = "/organize/postUpdate", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> postUpdate(@RequestParam(required = true) Integer id,
                             @RequestParam(required = true) String deptCode,
                             @RequestParam(defaultValue = "") String name,
                             @RequestParam(defaultValue = "0") Integer orders,
                             String description,
                             @RequestParam(defaultValue = "false") Boolean limited,
                             @RequestParam(defaultValue = "0") Integer numLimited,
                             @RequestParam(defaultValue = "false") Boolean exclusive,
                             String postsExcluded,
                             Integer status
                             ) {
        Post post = postService.getById(id);
        post.setCreateDate(DateUtil.toLocalDateTime(new Date()));
        post.setName(name);
        post.setDescription(description);
        post.setDeptCode(deptCode);
        post.setExcluded(exclusive);
        post.setLimited(limited);
        post.setNumLimited(numLimited);
        post.setCreator(SpringUtil.getUserName());
        post.setOrders(orders);
        if (status == null) {
            status = 0;
        }
        post.setStatus(status==1);

        if (!post.getDeptCode().equals(deptCode)) {
            Department department = departmentService.getDepartment(deptCode);
            String unitCode = departmentService.getUnitOfDept(department).getCode();
            post.setUnitCode(unitCode);
        }

        String[] ary = StrUtil.split(postsExcluded, ",");
        return new Result<>(postService.update(post, ary));
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping(value = "/organize/postDel", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> postDel(@RequestParam(required = true) String ids) {
        String[] ary = StrUtil.split(ids, ",");
        boolean re = false;
        for (String strId : ary) {
            re = postService.del(StrUtil.toInt(strId, -1));
        }
        return new Result<>(re);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping(value = "/organize/postUserUpdate", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> postUserUpdate(@RequestParam(required = true) Integer postId,
                             @RequestParam(defaultValue = "") String userNames
    ) throws ErrMsgException {
        String[] ary = StrUtil.split(userNames, ",");
        if (ary == null) {
            return new Result<>(true);
        }
        return new Result<>(postUserService.update(postId, ary));
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin', 'admin.unit')")
    @RequestMapping("/rolePostListPage")
    public Result<Object> rolePostListPage(Model model, String roleCode) {
        JSONObject json = new JSONObject();
        Role role = roleService.getRole(roleCode);
        json.put("role", role);
        json.put("isPostUsed", Config.getInstance().getBooleanProperty("isPostUsed"));
        return new Result<>();
    }

}

