package com.redmoon.oa.emailpop3;

import cn.js.fan.db.Conn;
import cn.js.fan.web.Global;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Attachment {
    int id;
    int emailId;
    String name;
    String fullPath;
    String diskName;
    String visualPath;

    String connname;

    String LOAD = "SELECT emailId, name, fullpath, diskname, visualpath, orders, file_size FROM email_attach WHERE id=?";
    String SAVE = "update email_attach set emailId=?, name=?, fullpath=?, diskname=?, visualpath=?, orders=? WHERE id=?";

    public Attachment() {
        connname = Global.getDefaultDB();
    }

    public Attachment(int id) {
        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
        }
        this.id = id;
        loadFromDb();
    }

    public boolean create() {
        String sql =
            "insert into email_attach (fullpath,emailId,name,diskname,visualpath,orders, file_size) values (?,?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullPath);
            pstmt.setInt(2, emailId);
            pstmt.setString(3, name);
            pstmt.setString(4, diskName);
            pstmt.setString(5, visualPath);
            pstmt.setInt(6, orders);
            pstmt.setLong(7, fileSize);
            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        finally {
            conn.close();
        }
        return re;
    }

    public boolean del() {
        String sql = "delete from email_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
            return false;
        }
        finally {
            conn.close();
        }

        // 删除文件
        IFileService fileService = SpringUtil.getBean(IFileService.class);
        fileService.del(visualPath, diskName);

        return re;
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(SAVE);
            pstmt.setInt(1, emailId);
            pstmt.setString(2, name);
            pstmt.setString(3, fullPath);
            pstmt.setString(4, diskName);
            pstmt.setString(5, visualPath);
            pstmt.setInt(6, orders);
            pstmt.setInt(7, id);
            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        finally {
            conn.close();
        }
        return re;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int geteEailId() {
        return this.emailId;
    }

    public void setEmailId(int id) {
        this.emailId = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiskName() {
        return this.diskName;
    }

    public void setDiskName(String dn) {
        this.diskName = dn;
    }

    public String getFullPath() {
        return this.fullPath;
    }

    public void setFullPath(String f) {
        this.fullPath = f;
    }

    public String getVisualPath() {
        return this.visualPath;
    }

    public int getOrders() {
        return orders;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setVisualPath(String vp) {
        this.visualPath = vp;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void loadFromDb() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                emailId = rs.getInt(1);
                name = rs.getString(2);
                fullPath = rs.getString(3);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                orders = rs.getInt(6);
                fileSize = rs.getLong(7);
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }
            conn.close();
        }
    }

    private int orders = 0;
    private boolean loaded = false;
    private long fileSize = 0;
}
