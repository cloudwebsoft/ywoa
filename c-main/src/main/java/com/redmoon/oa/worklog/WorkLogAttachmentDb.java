package com.redmoon.oa.worklog;


import java.sql.ResultSet;
import java.sql.SQLException;
import cn.js.fan.web.Global;
import cn.js.fan.db.Conn;
import java.io.File;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.util.Vector;
import com.redmoon.oa.db.SequenceManager;
import com.cloudwebsoft.framework.util.LogUtil;

public class WorkLogAttachmentDb implements Serializable {

    int id;
    int workLogId;
    String name;
    String diskName;
    String visualPath;

    String connname;

    String CREATE ="insert into work_log_attach(id,workLogId, name, diskname, visualpath, orders) values(?,?,?,?,?,?)";
    String LOAD = "SELECT workLogId, name, diskname, visualpath, orders, file_size FROM work_log_attach WHERE id=?";
    String SAVE = "update work_log_attach set workLogId=?, name=?, diskname=?, visual_path=?, orders=? WHERE id=?";
    String DEL = "delete from work_log_attach WHERE id=?";

    public WorkLogAttachmentDb() {
        connname = Global.getDefaultDB();
    }

    public WorkLogAttachmentDb(int id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("Attachment:默认数据库名为空！");
        this.id = id;
        loadFromDb();
    }

    public Vector getAttachsOfMyWork(int workLogId) {
        String sql = "select id from work_log_attach where workLogId=?"; // +noticeId;
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, workLogId);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    v.addElement(new WorkLogAttachmentDb(id));
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
                "insert into work_log_attach (id,workLogId,name,diskname,visualpath,orders,file_size) values (?,?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            id = (int)SequenceManager.nextID(SequenceManager.OA_NOTICE_ATTACH);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.setInt(2, workLogId);
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
        String sql = "delete from work_log_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
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
            pstmt.setInt(1, workLogId);
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

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
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
//String LOAD = "SELECT notice_id, name, diskname, visualpath, orders FROM oa_notice_attach WHERE id=?";
                workLogId = rs.getInt(1);
                name = rs.getString(2);
                // System.out.println("attach name=" + name);
                // System.out.println("attach fullPath=" + fullPath);
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
                } catch (SQLException e) {e.printStackTrace();}
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

	public int getWorkLogId() {
		return workLogId;
	}

	public void setWorkLogId(int workLogId) {
		this.workLogId = workLogId;
	}

}

