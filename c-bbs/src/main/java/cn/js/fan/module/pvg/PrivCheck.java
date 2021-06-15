package cn.js.fan.module.pvg;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.base.AbstractCheck;

public class PrivCheck  extends AbstractCheck {
    String priv = "", desc = "";

    public PrivCheck() {
    }

    public String getPriv() {
        return priv;
    }

    public String getDesc() {
        return desc;
    }

    public String chkPriv(HttpServletRequest request) {
        priv = ParamUtil.get(request, "priv");
        if (priv.equals("")) {
            log("编码必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(priv))
            log("请勿使用' ; 等字符！");
        return priv;
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
        chkPriv(request);
        chkDesc(request);
        report();
        return true;
    }

    public boolean checkUpdate(HttpServletRequest request) throws ErrMsgException {
        init();
        chkPriv(request);
        chkDesc(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkPriv(request);
        report();
        return true;
    }
}
