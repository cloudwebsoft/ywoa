package com.cloudweb.oa.controller;


import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.entity.DeptUser;
import com.cloudweb.oa.entity.Log;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.service.ILogService;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.SkinMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author fgf
 * @since 2020-03-24
 */
@Controller
@RequestMapping("/admin")
public class LogController {
    @Autowired
    ILogService logService;

    @Autowired
    IUserService userService;

    @Autowired
    IDeptUserService deptUserService;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    ResponseUtil responseUtil;

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/listLog")
    public String listLog(Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
        StringBuffer sb = new StringBuffer();
        departmentService.getDeptAsOptions(sb, department, department.getLayer());
        model.addAttribute("deptOpts", sb.toString());
        return "th/admin/log_list";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/getLogList", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public String getLogList(@RequestParam(defaultValue = "") String op,
                                 @RequestParam(defaultValue = "") String userName,
                                 @RequestParam(defaultValue = "") String logType,
                                 @RequestParam(defaultValue = "") String userAction,
                                 @RequestParam(defaultValue = "-1") int device,
                                 @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date beginDate,
                                 @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date endDate,
                                 @RequestParam(defaultValue = "") String deptCode,
                                 @RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "20") int pageSize
    ) {
        JSONObject json = new JSONObject();

        JSONArray arr = new JSONArray();
        PageHelper.startPage(pageNum, pageSize); // 分页
        List<Log> list = logService.list(userName, op, logType, userAction,device, beginDate, endDate, deptCode);
        PageInfo<Log> pageInfo = new PageInfo<>(list);
        for (Log log : list) {
            JSONObject jsonObj = (JSONObject) JSON.toJSON(log);

            Date logDate = log.getLogDate();
            jsonObj.put("logDate", DateUtil.format(logDate, "yyyy-MM-dd HH:mm:ss"));

            if (log.getDevice()==ConstUtil.DEVICE_PC) {
                jsonObj.put("device", "电脑");
            }
            else {
                jsonObj.put("device", "手机");
            }

            jsonObj.put("logTypeDesc", logService.getTypeDesc(log.getLogType()));

            User user = userService.getUser(log.getUserName());
            if (user!=null) {
                jsonObj.put("realName", user.getRealName());

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
                    jsonObj.put("deptName", deptNames.toString());
                }
            }
            else {
                jsonObj.put("realName", "");
                jsonObj.put("deptName", "");
            }
            arr.add(jsonObj);
        }

        json.put("data", arr);
        json.put("total", pageInfo.getTotal());
        return json.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/delLog", produces={"text/html;charset=UTF-8;","application/json;charset=UTF-8;"})
    @ResponseBody
    public JSONObject delLog(String[] ids) {
        boolean re = true;
        for (String strId : ids) {
            long id = StrUtil.toLong(strId);
            re = logService.removeById(id);
        }
        return responseUtil.getResultJson(re);
    }
}
