package com.redmoon.oa.organization;


import java.sql.SQLException;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;

/**
 * @Description:
 * @author: mk
 * @Date: Jul 21, 201511:20:50 AM
 */
public class DeptTreeAction {
    String SUCCESS = "SUCCESS";
    private String currNodeCode;
    private String newNodeCode = "";
    private String parentNodeName = "";                //所有的父节点名称  例：全部\总经理\总经办
    private String parentNodeCode = "";
    private int deptType = 1;

    private boolean group = false;
    private boolean hide = false;
    private String description = "";
    private String shortName = "";

    public int getDeptType() {
        return deptType;
    }

    public void setDeptType(int deptType) {
        this.deptType = deptType;
    }

    public String getParentNodeCode() {
        return parentNodeCode;
    }

    public void setParentNodeCode(String parentNodeCode) {
        this.parentNodeCode = parentNodeCode;
    }

    public String getParentNodeName() {
        return parentNodeName;
    }

    public void setParentNodeName(String parentNodeName) {
        this.parentNodeName = parentNodeName;
    }

    public String getCurrNodeCode() {
        return currNodeCode;
    }

    public void setCurrNodeCode(String currNodeCode) {
        this.currNodeCode = currNodeCode;
    }

    public String getNewNodeCode() {
        return newNodeCode;
    }

    public void setNewNodeCode(String newNodeCode) {
        this.newNodeCode = newNodeCode;
    }

    /**
     * 生成部门归属  例：全部\总经理\总经办
     *
     * @param code
     * @return
     * @Description:
     */
    private void generateParentNodeName(String code) {
        DeptMgr dMgr = new DeptMgr();
        DeptDb db = dMgr.getDeptDb(code);
        if (db == null)
            return;
        String nodeName = db.getName();
        this.parentNodeName = nodeName + "\\" + this.parentNodeName;
        String parentNodeCode = db.getParentCode();
        if ("-1".equals(parentNodeCode)) {
            return;
        } else {
            generateParentNodeName(parentNodeCode);
        }
    }

    /**
     * 根据当前节点，生成子节点的code
     *
     * @param code
     * @return
     * @Description:
     */
    public void generateNewNodeCode(String code) {
        String sql = "select MAX(code) from department d where d.parentCode = " + StrUtil.sqlstr(code);
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri;
        String maxCode = "";
        try {
            ri = jt.executeQuery(sql);
            if (ri.size() > 0) {
                ResultRecord rr = (ResultRecord) ri.next();
                maxCode = rr.getString(1);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }

        int num = 0;
        if (DeptDb.ROOTCODE.equals(code)) {
            num = StrUtil.toInt(maxCode) + 1;
        } else {
            if ("".equals(maxCode) || maxCode == null) {
                maxCode = "0";
            } else {
                maxCode = maxCode.substring(code.length());
            }
            num = StrUtil.toInt(maxCode) + 1;
        }

        // 过早版本是允许夸部门移动子部门的,可能会导致当前的code已经被使用了,所以要进行检测,by 郝炜
        DeptDb dd = null;
        do {
            if (DeptDb.ROOTCODE.equals(code)) {
                this.newNodeCode = StrUtil.PadString(String.valueOf(num), '0', 4, true);
            } else {
                this.newNodeCode = code + StrUtil.PadString(String.valueOf(num), '0', 4, true);
            }
            num++;
            dd = new DeptDb(this.newNodeCode);
        } while (dd != null && dd.isLoaded());
    }

    /**
     * 获取parentCode
     *
     * @param code
     * @Description:
     */
    private void getParentCode(String code) {
        DeptMgr dMgr = new DeptMgr();
        DeptDb db = dMgr.getDeptDb(code);
        if (db == null)
            return;
        this.parentNodeCode = db.getParentCode();
    }

    /**
     * 获取部门类型   0单位  1部门
     *
     * @param code
     * @Description:
     */
    private void getDepartmentType(String code) {
        DeptMgr dMgr = new DeptMgr();
        DeptDb db = dMgr.getDeptDb(code);
        if (db == null)
            return;
        this.deptType = db.getType();
        this.group = db.isGroup();
        this.hide = db.isHide();
        this.description = db.getDescription();
        this.shortName = db.getShortName();
    }

    /**
     * 增加部门
     *
     * @return
     * @Description:
     */
    public String addDepartment() {
        generateNewNodeCode(this.currNodeCode);
        generateParentNodeName(this.currNodeCode);
        getParentCode(this.currNodeCode);
        getDepartmentType(this.currNodeCode);
        return SUCCESS;
    }

    /**
     * 修改部门
     *
     * @return
     * @Description:
     */
    public String modifyDepartment() {
        getParentCode(this.currNodeCode);
        generateParentNodeName(this.parentNodeCode);
        getDepartmentType(this.currNodeCode);
        return SUCCESS;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(boolean group) {
        this.group = group;
    }

    /**
     * @return the group
     */
    public boolean isGroup() {
        return group;
    }

    /**
     * @param hide the hide to set
     */
    public void setHide(boolean hide) {
        this.hide = hide;
    }

    /**
     * @return the hide
     */
    public boolean isHide() {
        return hide;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
