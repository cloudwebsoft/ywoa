package com.redmoon.oa.idiofileark;


import java.io.File;
import java.sql.*;
import java.util.Date;

import cn.js.fan.db.Conn;
import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import com.redmoon.oa.db.SequenceManager;

public class IdioAttachment implements java.io.Serializable {
    int id;
    int msgId;
    String name;
    String fullPath;
    String diskName;
    String visualPath;

    String connname;

    public static final int TEMP_MSG_ID = -1; // 上传图片文件的临时id，置于msgId字段

    public static final String LOAD = "SELECT msgId, name, fullpath, diskname, visualpath, orders, upload_date FROM oa_idiofileark_attach WHERE id=?";
    public static final String SAVE = "update oa_idiofileark_attach set msgId=?, name=?, fullpath=?, diskname=?, visualpath=?, orders=? WHERE id=?";

    transient Logger logger = Logger.getLogger(IdioAttachment.class.getName());

    public IdioAttachment() {
        connname = Global.getDefaultDB();
    }

    public IdioAttachment(int id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("IdioAttachment:默认数据库名为空！");
        this.id = id;
        loadFromDb();
    }

    public boolean create() {
        String sql =
            "insert into oa_idiofileark_attach (id,msgId,name,diskname,visualpath,orders,upload_date) values (?,?,?,?,?,?,?)";
        id = (int)SequenceManager.nextID(SequenceManager.OA_MESSAGE_IdioAttachment);
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.setInt(2, msgId);
            pstmt.setString(3, name);
            pstmt.setString(4, diskName);
            pstmt.setString(5, visualPath);
            pstmt.setInt(6, orders);
            uploadDate = new java.util.Date();
            pstmt.setTimestamp(7, new Timestamp(uploadDate.getTime()));
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
        String sql = "delete from oa_idiofileark_attach where id=?";
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
            pstmt.setInt(1, msgId);
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

    public int getMsgId() {
        return this.msgId;
    }

    public void setMsgId(int di) {
        this.msgId = di;
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

    public void setVisualPath(String vp) {
        this.visualPath = vp;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
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
                msgId = rs.getInt(1);
                name = rs.getString(2);
                // System.out.println("attach name=" + name);
                fullPath = rs.getString(3);
                // System.out.println("attach fullPath=" + fullPath);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                orders = rs.getInt(6);
                uploadDate = rs.getTimestamp(7);
                loaded = true;
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

    public boolean delTmpAttach() {
    String sql = "delete from oa_idiofileark_attach where msgId ="+ TEMP_MSG_ID +" and id=?";
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

/**
    public boolean delTmpAttach() {
        String sql = "delete from oa_idiofileark_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("delTmpAttach:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            // 删除文件
            String filepath = Global.getRealPath() + "/" +
                              visualPath + "/" + diskName;
            File fl = new File(filepath);
            fl.delete();
        }
        return re;
    }
*/
    private int orders = 0;
    private boolean loaded = false;

    private java.util.Date uploadDate;

	public java.util.Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(java.util.Date uploadDate) {
		this.uploadDate = uploadDate;
	}
}
