package com.cloudweb.oa.controller;


import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.Account;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.entity.DeptUser;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.IAccountService;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.AccountVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.SkinMgr;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author fgf
 * @since 2020-02-09
 */
@Api(tags = "工号管理模块")
@Slf4j
@Validated
@Controller
@RequestMapping("/admin")
public class AccountController {
    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IAccountService accountService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    IUserService userService;

    @Autowired
    IDeptUserService deptUserService;

    @Autowired
    ResponseUtil responseUtil;

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/listAccount")
    public String listAccount(String op, String by, String what, String searchUnitCode, Model model) {
        Privilege pvg = new Privilege();
        StringBuffer sb = new StringBuffer();
        Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
        departmentService.getUnitAsOptions(sb, department, department.getLayer());
        model.addAttribute("unitOpts", sb.toString());

        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("op", op);
        model.addAttribute("what", what);
        model.addAttribute("searchUnitCode", searchUnitCode);

        model.addAttribute("unitCode", pvg.getUserUnitCode());

        model.addAttribute("by", by);
        return "th/admin/account_list";
    }

    @RequestMapping(value = "/getAccountList", produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String getAccountList(@RequestParam(defaultValue = "") String op,
                                          @RequestParam(defaultValue = "") String by,
                                          @RequestParam(defaultValue = "") String what,
                                          @RequestParam(defaultValue = "") String searchUnitCode,
                                          @RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "20") int pageSize
    ) {
        Privilege pvg = new Privilege();
        String unitCode = pvg.getUserUnitCode();
        String userName = pvg.getUser();

        List<AccountVO> resultList = new ArrayList<>();
        PageHelper.startPage(pageNum, pageSize); // 分页
        List<Account> list = accountService.list(userName, op, by, what,searchUnitCode, unitCode, pageNum, pageSize);
        PageInfo<Account> pageInfo = new PageInfo<>(list);
        for (Account acc : list) {
            AccountVO accountVO = new AccountVO();
            accountVO.setAccount(acc.getName());
            User user = userService.getUser(acc.getUserName());
            if (user!=null) {
                accountVO.setRealName(user.getRealName());

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
                    accountVO.setDeptName(deptNames.toString());
                }
                accountVO.setOp("");
            }
            else {
                accountVO.setOp("");
                accountVO.setRealName("");
                accountVO.setDeptName("");
            }
            resultList.add(accountVO);
        }

        JSONObject json = new JSONObject();
        json.put("data", resultList);
        json.put("total", pageInfo.getTotal());
        return json.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/addAccount")
    public String addAccount(Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        Privilege pvg = new Privilege();
        model.addAttribute("unitCode", pvg.getUserUnitCode());

        return "th/admin/account_add";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/createAccount", produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String createAccount(String name, String userName) throws ValidateException {
        Account account = accountService.getAccount(name);
        if (account!=null) {
            throw new ValidateException("#user.account.exist");
        };
        account = new Account();
        account.setName(name);
        account.setUserName(userName);
        return responseUtil.getResultJson(account.insert()).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/checkNotExist", produces={"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String checkNotExist(String name) {
        Account account = accountService.getAccount(name);
        boolean re = account==null;
        JSONObject jsonObject = new JSONObject();
        if (re) {
            jsonObject.put("valid", true);
        }
        else {
            jsonObject.put("valid", false);
        }
        return jsonObject.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/delAccount", produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String delAccount(String account) {
        return responseUtil.getResultJson(accountService.del(account)).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/editAccount")
    public String editAccount(String name, String tabIdOpener, Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        Account account = accountService.getAccount(name);
        model.addAttribute("account", account);

        User user = userService.getUser(account.getUserName());
        model.addAttribute("realName", user.getRealName());

        model.addAttribute("tabIdOpener", tabIdOpener);
        return "th/admin/account_edit";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/updateAccount", produces={"text/html;charset=UTF-8;","application/json;"})
    @ResponseBody
    public String updateAccount(String name, String userName) throws ValidateException {
        Account account = new Account();
        account.setName(name);
        account.setUserName(userName);
        return responseUtil.getResultJson(accountService.update(account)).toString();
    }
}
