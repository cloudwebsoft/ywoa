package com.redmoon.oa.notice;

import java.io.File;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import cn.js.fan.db.Conn;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;

public class NoticeAttachmentDb implements Serializable {

    long id;
    long noticeId;
    String name;
    String diskName;
    String visualPath;

    String connname;

    //String CREATE ="insert into oa_notice_attach(id,notice_id, name, diskname, visualpath, orders) values(?,?,?,?,?,?)";
    String LOAD = "SELECT notice_id, name, diskname, visualpath, orders, file_size FROM oa_notice_attach WHERE id=?";
    String SAVE = "update oa_notice_attach set notice_id=?, name=?, diskname=?, visual_path=?, orders=? WHERE id=?";
    String DEL = "delete from oa_notice_attach WHERE id=?";

    public NoticeAttachmentDb() {
        connname = Global.getDefaultDB();
    }

    public NoticeAttachmentDb(long id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
        this.id = id;
        loadFromDb();
    }

    public Vector getAttachsOfNotice(long noticeId) {
        String sql = "select id from oa_notice_attach where notice_id=?"; // +noticeId;
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, noticeId);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    long id = rs.getLong(1);
                    v.addElement(new NoticeAttachmentDb(id));
                }
                return v;
            }
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            return v;
    }

    public boolean create() {
        String sql =
                "insert into oa_notice_attach (id,notice_id,name,diskname,visualpath,orders,file_size) values (?,?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            id = (long)SequenceManager.nextID(SequenceManager.OA_NOTICE_ATTACH);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            pstmt.setLong(2, noticeId);
            pstmt.setString(3, name);
            pstmt.setString(4, diskName);
            pstmt.setString(5, visualPath);
            pstmt.setInt(6, orders);
            pstmt.setLong(7, size);
            re = conn.executePreUpdate() >= 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean del() {
        String sql = "delete from oa_notice_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            re = conn.executePreUpdate() >= 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            // 删除文件
            File fl = new File(Global.getRealPath() + visualPath + "/" + diskName);
            fl.delete();
        }
        return re;
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(SAVE);
            pstmt.setLong(1, noticeId);
            pstmt.setString(2, name);
            pstmt.setString(3, diskName);
            pstmt.setString(4, visualPath);
            pstmt.setInt(5, orders);
            pstmt.setLong(6, id);
            re = conn.executePreUpdate() >= 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getNoticeId() {
        return this.noticeId;
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

    public void setNoticeId(long noticeId) {
        this.noticeId = noticeId;
    }

    public void loadFromDb() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(LOAD);
            pstmt.setLong(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                noticeId = rs.getLong(1);
                name = rs.getString(2);
                diskName = rs.getString(3);
                visualPath = rs.getString(4);
                orders = rs.getInt(5);
                size = rs.getLong(6);
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
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
    
    private long size = 0;

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

}
