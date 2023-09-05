package com.cloudweb.oa.controller;


import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.vo.Result;
import io.swagger.annotations.ApiOperation;
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
    public Result<Object> add(String currNodeCode) {
        JSONObject json = departmentService.getAddDepartmentData(currNodeCode);
        return new Result<>(json);
    }

    @ApiOperation(value = "获取编码", notes = "获取编码", httpMethod = "GET")
    @RequestMapping(value = "/getNewCode", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> getNewCode(String parentCode) throws ValidateException {
        return new Result<>(departmentService.getAddDepartmentData(parentCode));
    }

    private boolean checkNameChar(String name) {
        if (name != null) {
            int len = name.length();
            for (int i = 0; i < len; i++) {
                char ch = name.charAt(i);
                if (ch == 34) {
                    return false;
                }
            }
        }
        return true;
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "edit", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> edit(@NotEmpty(message = "{dept.code.null}") String code) {

        Department department = departmentService.getDepartment(code);
        String parentNodeName = "";
        if (!ConstUtil.DEPT_ROOT.equals(code)) {
            Department deptParent = departmentService.getDepartment(department.getParentCode());
            parentNodeName = deptParent.getName();
        }

        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(departmentService.getDepartment(code));
        jsonObject.put("parentNodeName", parentNodeName);
        return new Result<>(jsonObject);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "create", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> create(@NotEmpty(message = "{dept.code.null}") String code,
                         @NotEmpty(message = "{dept.name.null}") String name,
                         String description, Integer deptType, Integer show,
                         Integer isGroup, Integer isHide,
                         @Length(min = 0, max = 45, message = "{dept.shortName.tooLong}") String shortName,
                         @NotEmpty(message = "{dept.parentCode.null}") String parentCode
    ) throws ValidateException {
        if (!checkNameChar(name)) {
            throw new ValidateException("#dept.name.invalid");
        }

        if (code.length() > 50) {
            throw new ValidateException("#dept.layer.12");
        }

        if (!StrUtil.isSimpleCode(code)) {
            throw new ValidateException("#dept.code.invalid");
        }

        if (!checkNameChar(shortName)) {
            throw new ValidateException("#dept.deptName.invalid");
        }

        // 检查编码是否有重复
        Department department = departmentService.getDepartment(code);
        if (department != null) {
            throw new ValidateException("#dept.code.dupl");
        }

        department = new Department();
        department.setCode(code);
        department.setName(name);
        department.setIsGroup(isGroup);
        department.setIsHide(isHide);
        department.setIsShow(show);
        department.setShortName(shortName);
        department.setParentCode(parentCode);
        department.setDeptType(deptType);
        department.setDescription(description);

        boolean re = departmentService.createAnySyn(department);

        return new Result<>(re);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "save", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> save(@Valid @RequestBody Department department, BindingResult result) throws ValidateException {
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

        return new Result<>(re);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "del", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> del(@NotEmpty(message = "{dept.code.null}") String code) throws ValidateException {
        boolean re = departmentService.del(code, SpringUtil.getUserName());

        return new Result<>(re);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "move", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> move(@NotEmpty(message = "{dept.code.null}") String code, String parentCode, int position) throws ValidateException {
        if (ConstUtil.DEPT_ROOT.equals(code)) {
            throw new ValidateException("根节点不能移动");
        }
        if ("#".equals(parentCode)) {
            throw new ValidateException("不能与根节点平级");
        }
        departmentService.move(code, parentCode, position);
        return new Result<>();
    }

    @ResponseBody
    @RequestMapping(value = "getDepartments", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> getDepartments(String parentCode) {
        List<Department> list = departmentService.getDepartments(parentCode);
        return new Result<>(list);
    }

    @ResponseBody
    @RequestMapping(value = "getUnitTree", produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> getUnitTree() {
        List<Department> list = departmentService.getUnitTree();
        return new Result<>(list);
    }
}
