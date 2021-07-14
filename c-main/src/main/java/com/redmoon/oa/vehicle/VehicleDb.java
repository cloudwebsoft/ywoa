package com.redmoon.oa.vehicle;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.kit.util.*;
import com.redmoon.kit.util.FileInfo;
import java.io.File;

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
public class VehicleDb extends ObjectDb {
    private String licenseNo;

    public VehicleDb() {
        init();
    }

    public VehicleDb(String licenseNo) {
        this.licenseNo = licenseNo;
        init();
        load();
    }

    public VehicleDb getVehicleDb(String licenseNo) {
        return (VehicleDb) getObjectDb(licenseNo);
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new VehicleDb(pk.getStrValue());
    }

    public void initDB() {
        this.tableName = "vehicle";
        primaryKey = new PrimaryKey("licenseNo", PrimaryKey.TYPE_STRING);
        objectCache = new VehicleCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE licenseNo=?";
        this.QUERY_CREATE =
                "INSERT into " + tableName + " (licenseNo,engineNo,type,driver,price,image,buyDate,state,remark,brand) VALUES (?,?,?,?,?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT engineNo,type,driver,price,image,buyDate,state,remark,brand FROM " +
                tableName + " WHERE licenseNo=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET licenseNo=?,engineNo=?,type=?,driver=?,price=?,image=?,buyDate=?,state=?,remark=?,brand=? WHERE licenseNo=?";
        this.QUERY_LIST = "SELECT licenseNo FROM " + tableName;
            isInitFromConfigDB = false;
    }

    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(this.QUERY_SAVE);
            pstmt.setString(1,licenseNo);
            pstmt.setString(2,engineNo);
            pstmt.setInt(3,type);
            pstmt.setString(4,driver);
            pstmt.setString(5,price);
            pstmt.setString(6, image);
            if(buyDate != null){
                pstmt.setDate(7, new java.sql.Date(buyDate.getTime()));
            }else{
                pstmt.setDate(7, new java.sql.Date(new java.util.Date().getTime()));
            }

            pstmt.setInt(8, state);
            pstmt.setString(9, remark);
            pstmt.setString(10, brand);
            pstmt.setString(11, oldLicenseNo);
            if (conn.executePreUpdate() == 1) {
                VehicleCache mc = new VehicleCache(this);
                primaryKey.setValue(oldLicenseNo);
                mc.refreshSave(primaryKey);
                return true;
            } else
                return false;
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ErrMsgException("更新vehicle时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean save(FileUpload fu) throws ErrMsgException {
        // logger.info("save:" + fu.getFiles().size() + " image=" + image);
        if (fu.getFiles().size()>0) {
            // logger.info("save1:" + fu.getFiles().get(0));
            delImage(fu.getRealPath());
            image = writeImage(fu);
        }
        return save();
    }

    public void delImage(String realPath) {
        if (image != null && !image.equals("")) {
            try {
                File file = new File(realPath + image);
                file.delete();
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
    }

    public void load() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(this.QUERY_LOAD);
            pstmt.setString(1, licenseNo);
            rs = conn.executePreQuery();
            if (rs.next()) {
                this.engineNo = rs.getString("engineNo");
                this.type= rs.getInt("type");
                this.driver = StrUtil.getNullStr(rs.getString("driver"));
                this.price = StrUtil.getNullStr(rs.getString("price"));
                this.buyDate = rs.getDate("buyDate");
                this.state = rs.getInt("state");
                this.remark = StrUtil.getNullStr(rs.getString("remark"));
                this.image = StrUtil.getNullStr(rs.getString("image"));
                this.brand = StrUtil.getNullStr(rs.getString("brand"));
                loaded = true;
                primaryKey.setValue(licenseNo);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del() throws ErrMsgException {
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement pstmt = conn.prepareStatement(this.QUERY_DEL);
            pstmt.setString(1, licenseNo);
            if (conn.executePreUpdate()==1) {
                VehicleCache mc = new VehicleCache(this);
                mc.refreshDel(primaryKey);
                return true;
            }
            else
                return false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("删除出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del(String realPath) throws
            ErrMsgException {
        boolean re = del();
        if (re)
            delImage(realPath);
        return re;
    }


    public boolean create() throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        String[] str = null;
        try {
            conn = new Conn(connname);
            PreparedStatement pstmt = conn.prepareStatement(this.QUERY_CREATE);
            pstmt.setString(1,licenseNo);
            pstmt.setString(2,engineNo);
            pstmt.setInt(3, type);
            pstmt.setString(4, driver);
            pstmt.setString(5, price);
            image = writeImage(this.fu);
            pstmt.setString(6, image);
            if(buyDate != null){
                pstmt.setDate(7, new java.sql.Date(buyDate.getTime()));
            }else{
                pstmt.setDate(7, new java.sql.Date(new java.util.Date().getTime()));
            }
            pstmt.setInt(8, state);
            pstmt.setString(9, remark);
            pstmt.setString(10, brand);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                VehicleCache vc = new VehicleCache(this);
                vc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("插入vehicle时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public String writeImage(FileUpload fu) {
        if (fu.getRet() == fu.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0)
                fi = (FileInfo) v.get(0);
            String vpath = "";
            if (fi != null) {
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(cal.YEAR));
                String month = "" + (cal.get(cal.MONTH) + 1);
                vpath = "upfile/" +
                        fi.getExt() + "/" + year + "/" + month + "/";
                String filepath = fu.getRealPath() + vpath;
                fu.setSavePath(filepath);
                // 使用随机名称写入磁盘
                fu.writeFile(true);
                return vpath + fi.getDiskName();
            }
        }
        return "";
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
                    VehicleDb vd = getVehicleDb(rs.getString(1));
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


    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public void setEngineNo(String engineNo) {
        this.engineNo = engineNo;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setBuyDate(java.util.Date buyDate) {
        this.buyDate = buyDate;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setOldLicenseNo(String oldLicenseNo) {
        this.oldLicenseNo = oldLicenseNo;
    }

    public void setFileUpload(FileUpload fu) {
        this.fu = fu;
    }


    public String getLicenseNo() {
        return licenseNo;
    }

    public String getEngineNo() {
        return engineNo;
    }

    public int getType() {
        return type;
    }

    public String getDriver() {
        return driver;
    }

    public String getPrice() {
        return price;
    }

    public int getState() {
        return state;
    }

    public String getRemark() {
        return remark;
    }

    public String getImage() {
        return image;
    }

    public java.util.Date getBuyDate() {
        return buyDate;
    }

    public String getBrand() {
        return brand;
    }

    public String getOldLicenseNo() {
        return oldLicenseNo;
    }

    public FileUpload getFileUpload() {
        return fu;
    }

    private String engineNo;
    private int type;
    private String driver;
    private String price;
    private java.util.Date buyDate;
    private int state;
    private String remark;
    private String image;
    private FileUpload fu = null;
    private String brand;
    private String oldLicenseNo;

}
