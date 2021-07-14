package com.redmoon.oa.vehicle;

import java.sql.*;
import java.util.*;
import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

/**
 * <p>Title: </p>
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
public class VehicleApplyDb extends ObjectDb{
    public VehicleApplyDb() {
         init();
    }

    public VehicleApplyDb(int flowId) {
        this.flowId = flowId;
        init();
        load();
    }

    public VehicleApplyDb getVehicleApplyDb(int flowId) {
        return (VehicleApplyDb) getObjectDb(new Integer(flowId));
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new VehicleApplyDb(pk.getIntValue());
    }

    public void initDB() {
        this.tableName = "form_table_vehicle_apply";
        primaryKey = new PrimaryKey("flowId", PrimaryKey.TYPE_INT);
        objectCache = new VehicleApplyCache(this);

        this.QUERY_LOAD =
                "SELECT flowId,licenseNo,dept,destination,person,myresult,beginDate,endDate,kilometer,applier,reason,remark,driver FROM " +
                tableName + " WHERE flowId=?";
        this.QUERY_LIST = "SELECT flowId FROM " + tableName;
        isInitFromConfigDB = false;
    }

    public boolean create() throws ErrMsgException {
        return false;
    }

    public boolean save() throws ErrMsgException {
        return false;
    }

    public void load() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(this.QUERY_LOAD);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs.next()) {
                this.licenseNo = StrUtil.getNullStr(rs.getString("licenseNo"));
                this.beginDate= rs.getTimestamp("beginDate");
                this.endDate = rs.getTimestamp("endDate");
                this.dept = StrUtil.getNullStr(rs.getString("dept"));
                this.target = StrUtil.getNullStr(rs.getString("destination"));
                this.person = StrUtil.getNullStr(rs.getString("person"));
                this.result = StrUtil.getNullStr(rs.getString("myresult"));
                this.kilometer = StrUtil.getNullStr(rs.getString("kilometer"));
                this.applier = StrUtil.getNullStr(rs.getString("applier"));
                this.reason = StrUtil.getNullStr(rs.getString("reason"));
                this.remark = StrUtil.getNullStr(rs.getString("remark"));
                this.driver = StrUtil.getNullStr(rs.getString("driver"));
                loaded = true;
                primaryKey.setValue(new Integer(flowId));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {e.printStackTrace();}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del() throws ErrMsgException {
        return false;
    }

    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    VehicleApplyDb vd = getVehicleApplyDb(rs.getInt(1));
                    result.addElement(vd);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {e.printStackTrace();}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }


    public void setId(int flowId) {
        this.flowId = flowId;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public void setBeginDate(java.util.Date beginDate) {
        this.beginDate = beginDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setKilometer(String kilometer) {
        this.kilometer = kilometer;
    }

    public void setApplier(String applier) {
        this.applier = applier;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getFlowId() {
        return flowId;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public java.util.Date getBeginDate() {
        return beginDate;
    }

    public java.util.Date getEndDate() {
        return endDate;
    }

    public String getDept() {
        return dept;
    }

    public String getTarget() {
        return target;
    }

    public String getPerson() {
        return person;
    }

    public String getResult() {
        return result;
    }

    public String getKilometer() {
        return kilometer;
    }

    public String getApplier() {
        return applier;
    }

    public String getReason() {
        return reason;
    }

    public String getDriver() {
        return driver;
    }

    public String getRemark() {
        return remark;
    }


    private String licenseNo;
    private int flowId;
    private java.util.Date beginDate;
    private java.util.Date endDate;
    private String dept;
    private String target;
    private String person;
    private String result;
    private String kilometer;
    private String applier;
    private String reason;
    private String driver;
    private String remark;

}
