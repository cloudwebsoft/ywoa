package com.redmoon.oa.emailpop3;


import java.io.*;
import java.sql.*;

import cn.js.fan.db.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;

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
    Logger logger = Logger.getLogger(Attachment.class.getName());

    public Attachment() {
        connname = Global.getDefaultDB();
    }

    public Attachment(int id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Attachment:默认数据库名为空！");
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
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
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
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            logger.error("del:" + e.getMessage());
            return false;
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        // 删除文件
        File fl = new File(Global.getRealPath() + visualPath + "/" + diskName);
        fl.delete();
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
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
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
            // System.out.println("attach id=" + id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                emailId = rs.getInt(1);
                name = rs.getString(2);
                // System.out.println("attach name=" + name);
                fullPath = rs.getString(3);
                // System.out.println("attach fullPath=" + fullPath);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                orders = rs.getInt(6);
                fileSize = rs.getLong(7);
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    private int orders = 0;
    private boolean loaded = false;
    private long fileSize = 0;
}
