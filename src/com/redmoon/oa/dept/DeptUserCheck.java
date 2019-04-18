package com.redmoon.oa.dept;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import com.redmoon.oa.person.*;

public class DeptUserCheck  extends AbstractCheck {
    String deptCode = "", userName = "";

    public DeptUserCheck() {
    }

    public String getDeptCode() {
        return deptCode;
    }

    public String getUserName() {
        return userName;
    }

    public int getId() {
        return id;
    }

    public String getDirection() {
        return direction;
    }

    public String getRank() {
        return rank;
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

    public String chkDeptCode(HttpServletRequest request) {
        deptCode = ParamUtil.get(request, "deptCode");
        if (deptCode.equals("")) {
            log("编码必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(deptCode))
            log("请勿使用' ; 等字符！");
        return deptCode;
    }

    public String chkUserName(HttpServletRequest request) {
        userName = ParamUtil.get(request, "userName");
        if (userName.equals("")) {
            log("名称必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(userName))
            log("请勿使用' ; 等字符！");
        return userName;
    }

    public boolean checkAdd(HttpServletRequest request) throws ErrMsgException {
        init();
        chkDeptCode(request);
        chkUserName(request);
        chkRank(request);
        
        if (!userName.equals("")) {
	        UserDb ud = new UserDb();
	        ud = ud.getUserDb(userName);
	        String[] ary = StrUtil.split(userName, ",");
	        // 当只选了单个用户时检查，如果是批量调动则不检查
	        if (ary.length==1) {
		        if (ud==null || !ud.isLoaded()) {
		            log("用户 " + userName + " 不存在！");
		        } else {
		            // 检查用户是否已处于该部门
		            DeptUserDb du = new DeptUserDb();
		            if (du.isUserOfDept(userName, deptCode)) {
		                log("用户 " + ud.getRealName() + " 已在该部门中！");
		            }
		        }
	        }
        }
        report();
        return true;
    }

    public boolean checkModify(HttpServletRequest request) throws ErrMsgException {
        init();
        chkDeptCode(request);
        chkUserName(request);
        chkRank(request);
        chkId(request);
        report();
        return true;
    }

    public boolean checkMove(HttpServletRequest request) throws ErrMsgException {
        init();
        chkId(request);
        chkDeptCode(request);
        chkDirection(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkId(request);
        report();
        return true;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String chkDirection(HttpServletRequest request) {
        direction = ParamUtil.get(request, "direction");
        if (direction.equals("")) {
            log("方向必须填写！");
        }

        return direction;
    }

    public String chkRank(HttpServletRequest request) {
        rank = ParamUtil.get(request, "rank");
        if (rank.equals("")) {
            // log("职级必须填写！");
        }
        return rank;
    }

    private int id;
    private String direction;
    private String rank;
}
