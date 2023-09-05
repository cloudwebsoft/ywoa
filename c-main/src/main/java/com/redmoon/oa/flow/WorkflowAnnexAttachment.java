package com.redmoon.oa.flow;

import java.io.File;
import java.sql.*;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.*;
import cn.js.fan.web.Global;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.person.UserDb;

public class WorkflowAnnexAttachment implements java.io.Serializable {
    long id;
    long annexId;
    String name;
    String diskName;
    String visualPath;

    String connname;

    String LOAD = "SELECT annex_id, name, diskname, visualpath, orders, download_count, file_size FROM flow_annex_attach WHERE id=?";
    String SAVE = "update flow_annex_attach set annex_id=?, name=?, diskname=?, visualpath=?, orders=?, download_count=? WHERE id=?";

    public WorkflowAnnexAttachment() {
        connname = Global.getDefaultDB();
    }

    public WorkflowAnnexAttachment(long id) {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            LogUtil.getLog(getClass()).error("Attachment:默认数据库名为空！");
        }
        this.id = id;
        loadFromDb();
    }

    public boolean create() {
        id = (int)SequenceManager.nextID(SequenceManager.OA_FLOW_ANNEX_ATTACHMENT);
        String sql =
            "insert into flow_annex_attach (annex_id,name,diskname,visualpath,orders,id,add_date, file_size) values (?,?,?,?,?,?,?,?)";
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
            pstmt.setLong(8, size);
            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }
        finally {
            conn.close();
        }
        return re;
    }

    public String getAttachmentUrl(HttpServletRequest request) {
        return request.getContextPath() + "/flow/downloadAnnexAttachment.do?id=" + id;
    }

    /**
     * 获取流程附加信息的附件
     * @param annexId long
     * @return Vector
     */
    public Vector getAttachments(long annexId) {
        Vector v = new Vector();
        String sql = "select id from flow_annex_attach where annex_id=? order by add_date desc";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Long(annexId)});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                v.addElement(new WorkflowAnnexAttachment(rr.getLong(1)));
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return v;
    }
    
    /**
     * 获取流程所以的附加信息的附件
     * @param flowId 流程ID
     * @return Vector
     */
    public Vector<WorkflowAnnexAttachment> getAllAttachments(int flowId) {
        Vector<WorkflowAnnexAttachment> v = new Vector<>();
        String sql = "select f.id from flow_annex_attach f,flow_annex a where annex_id=a.id and a.flow_id=? order by f.add_date desc";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {(long) flowId});
            while (ri.hasNext()) {
                ResultRecord rr = ri.next();
                v.addElement(new WorkflowAnnexAttachment(rr.getLong(1)));
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return v;
    }

    /**
     * 删除流程附加信息的附件
     * @param annexId long
     */
    public void delAttachments(long annexId) {
        String sql = "select id from flow_annex_attach where annex_id=? order by add_date desc";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {annexId});
            while (ri.hasNext()) {
                ResultRecord rr = ri.next();
                WorkflowAnnexAttachment wfaa = new WorkflowAnnexAttachment(rr.getLong(1));
                wfaa.del();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    public boolean del() {
        String sql = "delete from flow_annex_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
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
            pstmt.setLong(1, annexId);
            pstmt.setString(2, name);
            pstmt.setString(3, diskName);
            pstmt.setString(4, visualPath);
            pstmt.setInt(5, orders);
            pstmt.setInt(6, downloadCount);
            pstmt.setLong(7, id);
            re = conn.executePreUpdate() == 1;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        }
        finally {
            conn.close();
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

    public long getSize() {
        return size;
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

    public void setSize(long size) {
        this.size = size;
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
                size = rs.getLong(7);
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
    private long size;
}
