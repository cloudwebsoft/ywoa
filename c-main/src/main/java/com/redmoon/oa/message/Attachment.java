package com.redmoon.oa.message;


import cn.js.fan.db.Conn;
import cn.js.fan.web.Global;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Attachment implements java.io.Serializable {
    int id;
    int msgId;
    String name;
    String diskName;
    String visualPath;

    String connname;

    String LOAD = "SELECT msgId, name, diskname, visualpath, orders, file_size FROM oa_message_attach WHERE id=?";
    String SAVE = "update oa_message_attach set msgId=?, name=?, diskname=?, visualpath=?, orders=?, file_size=? WHERE id=?";

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
        String sql = "insert into oa_message_attach (id,msgId,name,diskname,visualpath,orders,file_size) values (?,?,?,?,?,?,?)";
        id = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE_ATTACHMENT);
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
            pstmt.setLong(7, size);
            re = conn.executePreUpdate() == 1;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    public boolean del() {
        String sql = "delete from oa_message_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate() == 1;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            return false;
        } finally {
            conn.close();
        }
        // 删除文件
        IFileService fileService = SpringUtil.getBean(IFileService.class);
        fileService.del(visualPath, diskName);
        return re;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(SAVE);
            pstmt.setInt(1, msgId);
            pstmt.setString(2, name);
            pstmt.setString(3, diskName);
            pstmt.setString(4, visualPath);
            pstmt.setInt(5, orders);
            pstmt.setLong(6, size);
            pstmt.setInt(7, id);
            re = conn.executePreUpdate() == 1;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
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
        return Global.getRealPath() + visualPath + "/" + diskName;
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
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                msgId = rs.getInt(1);
                name = rs.getString(2);
                diskName = rs.getString(3);
                visualPath = rs.getString(4);
                orders = rs.getInt(5);
                size = rs.getLong(6);
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
            conn.close();
        }
    }

    private int orders = 0;
    private boolean loaded = false;
    private long size = 0;
}
