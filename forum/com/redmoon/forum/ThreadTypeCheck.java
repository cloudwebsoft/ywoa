package com.redmoon.forum;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;

/**
 *
 * <p>Title:从request中获取版块子类别 </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ThreadTypeCheck extends AbstractCheck {
    String name;
    String link;
    int orders;
    String direction;
    String newName;

    public ThreadTypeCheck() {
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

    public String getDirection() {
        return this.direction;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public int getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public String chkName(HttpServletRequest request) {
        name = ParamUtil.get(request, "name");
        if (name.equals("")) {
            log("名称必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(name))
            log("请勿使用' ; 等字符！");
        return name;
    }

    public String chkBoardCode(HttpServletRequest request) {
        boardCode = ParamUtil.get(request, "boardCode");
        if (boardCode.equals("")) {
            log("版块编码必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(boardCode))
            log("请勿使用' ; 等字符！");
        return boardCode;
    }

    public int chkId(HttpServletRequest request) {
        try {
            id = ParamUtil.getInt(request, "id");
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return id;
    }

    public int chkDisplayOrder(HttpServletRequest request) {
        orders = ParamUtil.getInt(request, "displayOrder", 0);

        return orders;
    }

    public String chkDirection(HttpServletRequest request) {
        direction = ParamUtil.get(request, "direction");
        if (direction.equals("")) {
            log("方向必须填写！");
        }
        return direction;
    }

    public String chkColor(HttpServletRequest request) {
        color = ParamUtil.get(request, "color");
        return color;
    }

    public boolean checkAdd(HttpServletRequest request) throws ErrMsgException {
        init();
        chkName(request);
        chkBoardCode(request);
        chkColor(request);
        chkDisplayOrder(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkId(request);
        report();
        return true;
    }

    public boolean checkUpdate(HttpServletRequest request) throws ErrMsgException {
        init();
        chkName(request);
        chkId(request);
        chkColor(request);
        chkDisplayOrder(request);
        report();
        return true;
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setColor(String color) {
        this.color = color;
    }

    private String boardCode;
    private int id;
    private String color;
}
