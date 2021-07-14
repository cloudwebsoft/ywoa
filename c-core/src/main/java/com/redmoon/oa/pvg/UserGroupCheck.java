package com.redmoon.oa.pvg;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import com.redmoon.oa.dept.DeptDb;

public class UserGroupCheck extends AbstractCheck {
    String code = "", desc = "";

    public UserGroupCheck() {
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isDept() {
        return dept;
    }

    public boolean isIncludeSubDept() {
        return includeSubDept;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public String getUnitCode() {
        return unitCode;
    }

    private boolean dept = false;

    public String chkCode(HttpServletRequest request) {
        code = ParamUtil.get(request, "code");
        if (code.equals("")) {
            log("编码必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(code))
            log("请勿使用' ; 等字符！");
        return code;
    }

    public String chkDeptCode(HttpServletRequest request) {
        deptCode = ParamUtil.get(request, "deptCode");
        return deptCode;
    }

    public boolean chkIncludeSubDept(HttpServletRequest request) {
        includeSubDept = ParamUtil.getInt(request, "isIncludeSubDept", 0)==1;
        if (!dept)
            includeSubDept = false;
        return includeSubDept;
    }

    public String chkDesc(HttpServletRequest request) {
        desc = ParamUtil.get(request, "desc");
        if (desc.equals("")) {
            log("描述必须填写！");
        }

        return desc;
    }

    public boolean chkIsDept(HttpServletRequest request) {
        String isDept = ParamUtil.get(request, "isDept");
        dept = isDept.equals("1");
        return dept;
    }


    public String chkUnitCode(HttpServletRequest request) {
        unitCode = ParamUtil.get(request, "unitCode");
        if (unitCode.equals("")) {
            unitCode = DeptDb.ROOTCODE;
        }
        return unitCode;
    }

    public boolean checkAdd(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        chkDesc(request);
        chkIsDept(request);
        chkIncludeSubDept(request);
        chkDeptCode(request);
        chkUnitCode(request);
        report();
        return true;
    }

    public boolean checkUpdate(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        chkDesc(request);
        chkIsDept(request);
        chkIncludeSubDept(request);
        chkDeptCode(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        report();
        return true;
    }

    public void setDept(boolean dept) {
        this.dept = dept;
    }

    public void setIncludeSubDept(boolean includeSubDept) {
        this.includeSubDept = includeSubDept;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    private boolean includeSubDept = true;
    private String deptCode;
    private String unitCode;
}
