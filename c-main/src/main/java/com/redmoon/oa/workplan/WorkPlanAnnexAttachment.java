package com.redmoon.oa.workplan;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.oa.db.*;

public class WorkPlanAnnexAttachment implements java.io.Serializable {
    long id;
    long annexId;
    String name;
    String diskName;
    String visualPath;

    String connname;

    String LOAD = "SELECT annex_id, name, diskname, visualpath, orders, download_count, workplan_id FROM work_plan_annex_attach WHERE id=?";
    String SAVE = "update work_plan_annex_attach set annex_id=?, name=?, diskname=?, visualpath=?, orders=?, download_count=? WHERE id=?";

    public WorkPlanAnnexAttachment() {
        connname = Global.getDefaultDB();
    }

    public WorkPlanAnnexAttachment(long id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).error("Attachment:默认数据库名为空！");
        this.id = id;
        loadFromDb();
    }

    public boolean create() {
        id = (int)SequenceManager.nextID(SequenceManager.OA_WORKPLAN_ANNEX_ATTACHMENT);
        String sql =
            "insert into work_plan_annex_attach (annex_id,name,diskname,visualpath,orders,id,add_date,workplan_id) values (?,?,?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, annexId);
            pstmt.setString(2, name);
            pstmt.setString(3, diskName);
            pstmt.setString(4, visualPath);
            pstmt.setInt(5, orders);
            pstmt.setLong(6, id);
            pstmt.setTimestamp(7, new Timestamp(new java.util.Date().getTime()));
            pstmt.setLong(8, workplanId);
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public String getAttachmentUrl(HttpServletRequest request) {
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        String vpath = cfg.get("file_workplan");
        String attachmentBasePath = Global.getRootPath() + "/" +
                                    vpath + "/";
        return attachmentBasePath + visualPath + "/" + diskName;
    }

    /**
     * 获取流程附加信息的附件
     * @param annexId long
     * @return Vector
     */
    public Vector getAttachments(long annexId) {
        Vector v = new Vector();
        String sql = "select id from work_plan_annex_attach where annex_id=? order by add_date desc";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Long(annexId)});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                v.addElement(new WorkPlanAnnexAttachment(rr.getLong(1)));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return v;
    }

    /**
     * 删除流程附加信息的附件
     * @param annexId long
     */
    public void delAttachments(long annexId) {
        String sql = "select id from work_plan_annex_attach where annex_id=? order by add_date desc";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Long(annexId)});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                WorkPlanAnnexAttachment wfaa = new WorkPlanAnnexAttachment(rr.getLong(1));
                wfaa.del();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean del() {
        String sql = "delete from work_plan_annex_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        // 删除文件
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        String vpath = cfg.get("file_workplan");
        File fl = new File(Global.realPath + vpath + "/" + visualPath + "/" + diskName);
        fl.delete();
        return re;
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(SAVE);
            pstmt.setLong(1, annexId);
            pstmt.setString(2, name);
            pstmt.setString(3, diskName);
            pstmt.setString(4, visualPath);
            pstmt.setInt(5, orders);
            pstmt.setInt(6, downloadCount);
            pstmt.setLong(7, id);
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        return re;
    }

    public long getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAnnexId() {
        return annexId;
    }

    public void setAnnexId(long annexId) {
        this.annexId = annexId;
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

    public int getDownloadCount() {
        return downloadCount;
    }

    public long getWorkplanId() {
        return workplanId;
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

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public void setWorkplanId(long workplanId) {
        this.workplanId = workplanId;
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
                annexId = rs.getLong(1);
                name = rs.getString(2);
                diskName = rs.getString(3);
                visualPath = rs.getString(4);
                orders = rs.getInt(5);
                downloadCount = rs.getInt(6);
                workplanId = rs.getLong(7);
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("loadFromDb:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    private int orders = 0;
    private boolean loaded = false;
    private int downloadCount = 0;
    private long workplanId;
}
