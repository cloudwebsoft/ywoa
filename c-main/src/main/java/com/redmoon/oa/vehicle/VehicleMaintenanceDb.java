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
public class VehicleMaintenanceDb extends ObjectDb{
    public VehicleMaintenanceDb() {
         init();
    }

    public VehicleMaintenanceDb(int id) {
        this.id = id;
        init();
        load();
    }

    public VehicleMaintenanceDb getVehicleMaintenanceDb(int id) {
        return (VehicleMaintenanceDb) getObjectDb(new Integer(id));
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new VehicleMaintenanceDb(pk.getIntValue());
    }

    public void initDB() {
        this.tableName = "vehicle_maintenance";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new VehicleMaintenanceCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT into " + tableName + " (licenseNo,beginDate,endDate,type,cause,expense,transactor,remark) VALUES (?,?,?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT id,licenseNo,beginDate,endDate,type,cause,expense,transactor,remark FROM " +
                tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET licenseNo=?,beginDate=?,endDate=?,type=?,cause=?,expense=?,transactor=?,remark=? WHERE id=?";
        this.QUERY_LIST = "SELECT id FROM " + tableName;
        isInitFromConfigDB = false;
    }

    public boolean create() throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        String[] str = null;
        try {
            conn = new Conn(connname);
            PreparedStatement pstmt = conn.prepareStatement(this.QUERY_CREATE);
            pstmt.setString(1,licenseNo);
            if(beginDate != null){
                pstmt.setDate(2,new java.sql.Date(beginDate.getTime()));
            }else{
                pstmt.setDate(2,new java.sql.Date(new java.util.Date().getTime()));
            }
            if (endDate != null) {
                pstmt.setDate(3, new java.sql.Date(endDate.getTime()));
            } else {
                pstmt.setDate(3, new java.sql.Date(new java.util.Date().getTime()));
            }

            pstmt.setInt(4,type);
            pstmt.setString(5,cause);
            pstmt.setString(6,expense);
            pstmt.setString(7,transactor);
            pstmt.setString(8,remark);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                VehicleMaintenanceCache vmc = new VehicleMaintenanceCache(this);
                vmc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("插入vehicle_maintenance时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1,licenseNo);
            pstmt.setDate(2,new java.sql.Date(beginDate.getTime()));
            pstmt.setDate(3,new java.sql.Date(endDate.getTime()));
            pstmt.setInt(4,type);
            pstmt.setString(5,cause);
            pstmt.setString(6,expense);
            pstmt.setString(7,transactor);
            pstmt.setString(8,remark);
            pstmt.setInt(9, id);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (re) {
                VehicleMaintenanceCache vmc = new VehicleMaintenanceCache(this);
                primaryKey.setValue(new Integer(id));
                vmc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void load() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(this.QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs.next()) {
                this.licenseNo = rs.getString("licenseNo");
                this.beginDate= rs.getDate("beginDate");
                this.endDate = rs.getDate("endDate");
                this.type = rs.getInt("type");
                this.cause = rs.getString("cause");
                this.expense = rs.getString("expense");
                this.transactor = rs.getString("transactor");
                this.remark = rs.getString("remark");
                loaded = true;
                primaryKey.setValue(new Integer(id));
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
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (re) {
                VehicleMaintenanceCache vmc = new VehicleMaintenanceCache(this);
                primaryKey.setValue(new Integer(id));
                vmc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
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
                    VehicleMaintenanceDb vd = getVehicleMaintenanceDb(rs.getInt(1));
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


    public void setId(int id) {
        this.id = id;
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

    public void setType(int type) {
        this.type = type;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public void setExpense(String expense) {
        this.expense = expense;
    }

    public void setTransactor(String transactor) {
        this.transactor = transactor;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getId() {
        return id;
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

    public int getType() {
        return type;
    }

    public String getCause() {
        return cause;
    }

    public String getExpense() {
        return expense;
    }

    public String getTransactor() {
        return transactor;
    }

    public String getRemark() {
        return remark;
    }

    private String licenseNo;
    private int id;
    private java.util.Date beginDate;
    private java.util.Date endDate;
    private int type;
    private String cause;
    private String expense;
    private String transactor;
    private String remark;


}
