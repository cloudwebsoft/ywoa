package com.redmoon.oa.flow;

import cn.js.fan.base.AbstractCheck;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ErrMsgException;

public class WorkflowCheck extends AbstractCheck {
    String typeCode;
    String title;

    public WorkflowCheck() {
    }

    public String chkTypeCode(HttpServletRequest request) {
        try {
            typeCode = ParamUtil.get(request, "typeCode");
            if (typeCode.equals("") || typeCode.equals("not")) {
                log("流程类别必须填写！");
            }
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return typeCode;
    }

    public String chkTitle(HttpServletRequest request) {
        try {
            title = ParamUtil.get(request, "title");
            if (title.equals("")) {
                log("流程标题必须填写！");
            }
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return title;
    }

    public boolean checkCreate(HttpServletRequest request) throws ErrMsgException {
        init();
        chkTitle(request);
        chkTypeCode(request);
        report();
        return true;
    }
}
