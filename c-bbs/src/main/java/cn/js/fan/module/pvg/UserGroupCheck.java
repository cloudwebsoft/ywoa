package cn.js.fan.module.pvg;

import cn.js.fan.base.*;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.security.SecurityUtil;

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

    public String chkCode(HttpServletRequest request) {
        code = ParamUtil.get(request, "code");
        if (code.equals("")) {
            log("编码必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(code))
            log("请勿使用' ; 等字符！");
        return code;
    }

    public String chkDesc(HttpServletRequest request) {
        desc = ParamUtil.get(request, "desc");
        if (desc.equals("")) {
            log("描述必须填写！");
        }

        return desc;
    }

    public boolean checkAdd(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        chkDesc(request);
        report();
        return true;
    }

    public boolean checkUpdate(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        chkDesc(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        report();
        return true;
    }
}
