package com.redmoon.oa.meeting;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;

public class BoardroomForm extends AbstractForm {
    BoardroomDb bd = new BoardroomDb();

    public BoardroomForm() {
    }

    public BoardroomDb getBoardroomDb() {
        return bd;
    }

    public String chkAddress(HttpServletRequest request) {
        String address = ParamUtil.get(request, "address");
        if (address.equals("")) {
            log("位置必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(address))
            log("请勿使用' ; 等字符！");
        bd.setAddress(address);
        return address;
    }

    public String chkEquipment(HttpServletRequest request) {
        String equipment = ParamUtil.get(request, "equipment");
        if (equipment.equals("")) {
            // log("装备必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(equipment))
            log("请勿使用' ; 等字符！");
        bd.setEquipment(equipment);
        return equipment;
    }

    public String chkName(HttpServletRequest request) {
        String name = ParamUtil.get(request, "name");
        if (name.equals("")) {
            log("名称必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(name))
            log("请勿使用' ; 等字符！");
        bd.setName(name);
        return name;
    }

    public int chkId(HttpServletRequest request) {
        String strId = ParamUtil.get(request, "id");
        if (!StrUtil.isNumeric(strId))
            log("id 必须为数字！");
        else {
            bd.setId(Integer.parseInt(strId));
        }
        return bd.getId();
    }

    public int chkPersonNum(HttpServletRequest request) {
        String strPersonNum = ParamUtil.get(request, "personNum");
        if (!StrUtil.isNumeric(strPersonNum))
            log("人数必须为数字！");
        else {
            bd.setPersonNum(Integer.parseInt(strPersonNum));
        }
        return bd.getPersonNum();
    }

    public String chkDesc(HttpServletRequest request) {
        String desc = ParamUtil.get(request, "description");
        bd.setDescription(desc);
        return desc;
    }

    public boolean checkCreate(HttpServletRequest request) throws ErrMsgException {
        init();
        chkName(request);
        chkPersonNum(request);
        chkDesc(request);
        chkAddress(request);
        chkEquipment(request);
        report();
        return true;
    }

    public boolean checkModify(HttpServletRequest request) throws ErrMsgException {
        init();
        chkId(request);
        chkName(request);
        chkPersonNum(request);
        chkDesc(request);
        chkAddress(request);
        chkEquipment(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkId(request);
        report();
        return true;
    }

}
