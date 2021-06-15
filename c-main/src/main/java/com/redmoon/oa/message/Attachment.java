package com.redmoon.oa.message;


import cn.js.fan.db.Conn;
import cn.js.fan.web.Global;
import com.redmoon.oa.db.SequenceManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Attachment implements java.io.Serializable {
    int id;
    int msgId;
    String name;
   // String fullPath;
    String diskName;
    String visualPath;

    String connname;

    String LOAD = "SELECT msgId, name, diskname, visualpath, orders, file_size FROM oa_message_attach WHERE id=?";
    String SAVE = "update oa_message_attach set msgId=?, name=?, diskname=?, visualpath=?, orders=?, file_size=? WHERE id=?";
    transient Logger logger = Logger.getLogger(Attachment.class.getName());

    public Attachment() {
        connname = Global.getDefaultDB();
    }

    public Attachment(int id) {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            logger.info("Attachment:默认数据库名为空！");
        }
        this.id = id;
        loadFromDb();
    }

    public boolean create() {
        String sql =
            "insert into oa_message_attach (id,msgId,name,diskname,visualpath,orders,file_size) values (?,?,?,?,?,?,?)";
        id = (int)SequenceManager.nextID(SequenceManager.OA_MESSAGE_ATTACHMENT);
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
    	String fullPath = Global.realPath + this.visualPath + this.diskName;
        String sql = "delete from oa_message_attach where id=?";
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
        File fl = new File(fullPath);
        re = fl.delete();
        
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
        return Global.getRealPath() + visualPath + "/" + diskName;
    }

   // public void setFullPath(String f) {
    //    this.fullPath = f;
   // }

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
                // System.out.println("attach fullPath=" + fullPath);
                diskName = rs.getString(3);
                visualPath = rs.getString(4);
                orders = rs.getInt(5);
                size = rs.getLong(6);
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

    private int orders = 0;
    private boolean loaded = false;
    private long size = 0;
}
