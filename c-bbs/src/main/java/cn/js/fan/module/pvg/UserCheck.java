package cn.js.fan.module.pvg;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import org.json.JSONException;
import org.json.JSONObject;

public class UserCheck extends AbstractCheck {
    String name;
    String realName;
    String desc;
    String pwd;

    public UserCheck() {
    }

    public String getName() {
        return name;
    }

    public String getRealName() {
        return realName;
    }

    public String getDesc() {
        return desc;
    }

    public String getPwd() {
        return pwd;
    }

    public boolean isForegroundUser() {
        return foregroundUser;
    }

    public String chkName(HttpServletRequest request) {
        name = ParamUtil.get(request, "name");
        if (name.equals("")) {
            log("姓名必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(name))
            log("请勿使用' ; 等字符！");
        return name;
    }

    public String chkRealName(HttpServletRequest request) {
        realName = ParamUtil.get(request, "realname");
        if (realName.equals("")) {
            log("真实姓名必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(realName))
            log("请勿使用' ; 等字符！");
        return realName;
    }

    public String chkDesc(HttpServletRequest request) {
        desc = ParamUtil.get(request, "desc");
        if (desc.equals("")) {
            log("描述必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(desc))
            log("请勿使用' ; 等字符！");
        return desc;
    }

    public String chkPwd(HttpServletRequest request) {
        pwd = ParamUtil.get(request, "pwd");
        String pwd_confirm = ParamUtil.get(request, "pwd_confirm");
        if (!pwd.equals(pwd_confirm)) {
            log("密码与确认密码不一致！");
        }
        return pwd;
    }

    public boolean chkForegroundUser(HttpServletRequest request) {
        foregroundUser = ParamUtil.get(request, "isForegroundUser").equals("true");
        return foregroundUser;
    }

    public boolean checkAdd(HttpServletRequest request) throws ErrMsgException {
        init();
        chkName(request);
        chkRealName(request);
        chkDesc(request);
        chkPwd(request);
        chkForegroundUser(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkName(request);
        report();
        return true;
    }

    public boolean checkUpdate(HttpServletRequest request) throws ErrMsgException {
        init();
        chkName(request);
        chkRealName(request);
        chkDesc(request);
        chkForegroundUser(request);
        report();
        return true;
    }

    public boolean checkUpdateWithPwd(HttpServletRequest request) throws ErrMsgException {
        init();
        chkName(request);
        chkRealName(request);
        chkDesc(request);
        chkPwd(request);
        report();
        return true;
    }

    public void setForegroundUser(boolean foregroundUser) {
        this.foregroundUser = foregroundUser;
    }

    private boolean foregroundUser = false;
}
