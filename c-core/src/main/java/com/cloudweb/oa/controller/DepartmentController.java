package com.cloudweb.oa.controller;


import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author fgf
 * @since 2020-01-30
 */
@RestController
@Validated
@RequestMapping("/department")
public class DepartmentController {
    @Autowired
    IDepartmentService departmentService;

    @Autowired
    @Qualifier(value = "validMessageSource")
    private MessageSource validMessageSource;

    @Autowired
    @Qualifier(value = "messageSource")
    private MessageSource messageSource;

    @Autowired
    private I18nUtil i18nUtil;

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "add", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String add(String currNodeCode) {
        JSONObject json = departmentService.getAddDepartmentData(currNodeCode);
        return json.toString();
    }

    private boolean checkNameChar(String name) {
        int len = name.length();
        for (int i = 0; i < len; i++) {
            char ch = name.charAt(i);
            if (ch == 34) {
                return false;
            }
        }
        return true;
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "edit", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String edit(@NotEmpty(message = "{dept.code.null}") String code) {

        Department department = departmentService.getDepartment(code);
        Department deptParent = departmentService.getDepartment(department.getParentCode());

        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(departmentService.getDepartment(code));
        jsonObject.put("parentNodeName", deptParent.getName());
        return jsonObject.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "create", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String create(@NotEmpty(message = "{dept.code.null}") String code,
                         @NotEmpty(message = "{dept.name.null}") String name,
                         String description,
                         int deptType,
                         int show,
                         int isGroup, int isHide,
                         @Length(min = 0, max = 45, message = "{dept.shortName.tooLong}") String shortName,
                         @NotEmpty(message = "{dept.parentCode.null}") String parentCode
    ) throws ValidateException {
        if (!checkNameChar(name)) {
            System.out.println(validMessageSource.getMessage("dept.name.invalid", null, null));
            System.out.println(messageSource.getMessage("test.name.invalid", null, null));
            throw new ValidateException("#dept.name.invalid");
        }

        if (code.length() > 50) {
            throw new ValidateException("#dept.layer.12");
        }

        if (!StrUtil.isSimpleCode(code)) {
            throw new ValidateException("#");
        }

        if (!checkNameChar(shortName)) {
            throw new ValidateException("#dept.deptName.invalid");
        }

        Department department = new Department();
        department.setCode(code);
        department.setName(name);
        department.setIsGroup(isGroup);
        department.setIsHide(isHide);
        department.setIsShow(show);
        department.setShortName(shortName);
        department.setParentCode(parentCode);
        department.setDeptType(deptType);
        department.setDescription(description);

        JSONObject json = new JSONObject();
        boolean re = departmentService.createAnySyn(department);
        if (re) {
            json.put("ret", 1);
            json.put("msg", i18nUtil.get("info_op_success"));
        } else {
            json.put("ret", 0);
            json.put("msg", i18nUtil.get("info_op_fail"));
        }
        return json.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "save", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String save(@Valid @RequestBody Department department, BindingResult result) throws ValidateException {
        // 注意此处result实际上因被GlobalExceptionHandler拦截，其中没有error
        if (!checkNameChar(department.getShortName())) {
            result.rejectValue("shortName", "dept.shortName.invalid", "名称中不能含有\"字符");
        }

        if (result.hasErrors()) {
            StringBuffer sb = new StringBuffer();
            List<ObjectError> ls = result.getAllErrors();
            for (int i = 0; i < ls.size(); i++) {
                ObjectError error = ls.get(i);
                sb.append(error.getDefaultMessage() + "\r\n");
            }

            throw new ValidateException(sb.toString());
        }

        boolean re = departmentService.update(department);
        JSONObject json = new JSONObject();
        if (re) {
            json.put("ret", 1);
            json.put("msg", i18nUtil.get("info_op_success"));
        } else {
            json.put("ret", 0);
            json.put("msg", i18nUtil.get("info_op_fail"));
        }
        return json.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "del", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String del(@NotEmpty(message = "{dept.code.null}") String code) throws ValidateException {
        JSONObject json = new JSONObject();
        boolean re = departmentService.del(code, SpringUtil.getUserName());
        if (re) {
            json.put("ret", 1);
            json.put("msg", i18nUtil.get("info_op_success"));
        } else {
            json.put("ret", 0);
            json.put("msg", i18nUtil.get("info_op_fail"));
        }
        return json.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "move", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String move(@NotEmpty(message = "{dept.code.null}") String code, String parentCode, int position) throws ValidateException {
        JSONObject json = new JSONObject();
        if (ConstUtil.DEPT_ROOT.equals(code)) {
            throw new ValidateException("根节点不能移动");
        }
        if ("#".equals(parentCode)) {
            throw new ValidateException("不能与根节点平级");
        }

        departmentService.move(code, parentCode, position);
        json.put("ret", 1);
        json.put("msg", i18nUtil.get("info_op_success"));

        return json.toString();
    }
}
