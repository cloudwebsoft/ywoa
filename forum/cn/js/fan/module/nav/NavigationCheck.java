package cn.js.fan.module.nav;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;

public class NavigationCheck extends AbstractCheck {
    String name;
    String link;
    int orders;
    String direction;
    String newName;

    public NavigationCheck() {
    }

    public String getName() {
        return name;
    }

    public String getNewName() {
        return newName;
    }

    public int getOrders() {
        return orders;
    }

    public String getLink() {
        return link;
    }

    public String getDirection() {
        return this.direction;
    }

    public String getColor() {
        return color;
    }

    public String getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    private String color = "";

    public String chkName(HttpServletRequest request) {
        name = ParamUtil.get(request, "name");
        if (name.equals("")) {
            log("名称必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(name))
            log("请勿使用' ; 等字符！");
        return name;
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

    public String chkColor(HttpServletRequest request) {
        color = ParamUtil.get(request, "color");
        if (!SecurityUtil.isValidSqlParam(color))
            log("请勿使用' ; 等字符！");
        return color;
    }

    public String chkTarget(HttpServletRequest request) {
        target = ParamUtil.get(request, "target");
        // if (!SecurityUtil.isValidSqlParam(target))
        //    log("请勿使用' ; 等字符！");
        return target;
    }

    public String chkType(HttpServletRequest request) throws ErrMsgException {
        type = ParamUtil.get(request, "type");
        return type;
    }

    public String chkNewName(HttpServletRequest request) {
        newName = ParamUtil.get(request, "newname");
        if (newName.equals("")) {
            log("名称必须填写！");
        }
        // if (!SecurityUtil.isValidSqlParam(newName))
        //    log("请勿使用' ; 等字符！");
        return newName;
    }

    public String chkLink(HttpServletRequest request) {
        link = ParamUtil.get(request, "link");
        if (link.equals("")) {
            log("链接必须填写！");
        }
        // if (!SecurityUtil.isValidSqlParam(link))
        //    log("请勿使用' ; 等字符！");
        return link;
    }

    public int chkOrders(HttpServletRequest request) {
        try {
            orders = ParamUtil.getInt(request, "orders");
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return orders;
    }

    public String chkDirection(HttpServletRequest request) {
        direction = ParamUtil.get(request, "direction");
        if (direction.equals("")) {
            log("链接必须填写！");
        }
        return direction;
    }

    public boolean checkAdd(HttpServletRequest request) throws ErrMsgException {
        init();
        chkName(request);
        chkLink(request);
        chkColor(request);
        chkTarget(request);
        chkType(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        chkType(request);
        report();
        return true;
    }

    public boolean checkUpdate(HttpServletRequest request) throws ErrMsgException {
        init();
        chkName(request);
        chkLink(request);
        chkNewName(request);
        chkColor(request);
        chkTarget(request);
        chkType(request);
        chkCode(request);
        report();
        return true;
    }

    public boolean checkMove(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        chkDirection(request);
        chkType(request);
        report();
        return true;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private String target;
    private String type;
    private String code;

}
