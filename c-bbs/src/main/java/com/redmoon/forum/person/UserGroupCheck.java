package com.redmoon.forum.person;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

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

    public int getDisplayOrder() {
        return displayOrder;
    }

    public String getIpBegin() {
        return ipBegin;
    }

    public String getIpEnd() {
        return ipEnd;
    }

    public boolean isGuest() {
        return guest;
    }

    public String chkCode(HttpServletRequest request) {
        code = ParamUtil.get(request, "code");
        if (!StrUtil.isSimpleCode(code)) {
            log(SkinUtil.LoadString(request, "err_simple_code"));
        }
        if (code.equalsIgnoreCase(UserGroupDb.ALL) || code.equalsIgnoreCase(UserGroupDb.EVERYONE) || code.equalsIgnoreCase(UserGroupDb.GUEST)) {
            log("The code can not be all,everyone or guest.");
        }
        return code;
    }

    public String chkDesc(HttpServletRequest request) {
        desc = ParamUtil.get(request, "desc");
        if (desc.equals("")) {
            log(SkinUtil.LoadString(request, "res.forum.person.UserGroupDb", "need_desc"));
        }

        return desc;
    }

    public String chkIpBegin(HttpServletRequest request) {
        ipBegin = ParamUtil.get(request, "ipBegin");
        guest = ParamUtil.getInt(request, "isGuest", 0)==1;

        if (guest && !StrUtil.isValidIP(ipBegin)) {
                log(SkinUtil.LoadString(request, "res.label.forum.admin.forbidip", "begin_ip_format_invalid"));
        }
        return ipBegin;
    }

    public String chkIpEnd(HttpServletRequest request) {
        ipEnd = ParamUtil.get(request, "ipEnd");
        guest = ParamUtil.getInt(request, "isGuest", 0)==1;

        if (guest && !StrUtil.isValidIP(ipEnd)) {
            log(SkinUtil.LoadString(request, "res.label.forum.admin.forbidip",
                                    "end_ip_format_invalid"));
        }
        return ipEnd;
    }

    public boolean chkGuest(HttpServletRequest request) {
        guest = ParamUtil.getInt(request, "isGuest", 0)==1;
        return guest;
    }

    public int chkDisplayOrder(HttpServletRequest request) throws ErrMsgException {
        displayOrder = ParamUtil.getInt(request, "displayOrder");
        return displayOrder;
    }

    public boolean checkAdd(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        chkDesc(request);
        chkDisplayOrder(request);
        chkIpBegin(request);
        chkIpEnd(request);
        chkGuest(request);
        report();
        return true;
    }

    public boolean checkUpdate(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        chkDesc(request);
        chkDisplayOrder(request);
        chkIpBegin(request);
        chkIpEnd(request);
        chkGuest(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        report();
        return true;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setIpBegin(String ipBegin) {
        this.ipBegin = ipBegin;
    }

    public void setIpEnd(String ipEnd) {
        this.ipEnd = ipEnd;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    private int displayOrder = 0;
    private String ipBegin;
    private String ipEnd;
    private boolean guest = false;
}
