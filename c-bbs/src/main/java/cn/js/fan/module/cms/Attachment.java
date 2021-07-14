package cn.js.fan.module.cms;

import java.io.*;
import java.sql.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.*;
import org.apache.log4j.*;

public class Attachment implements java.io.Serializable {
    int id;
    int docId;
    String name;
    String fullPath;
    String diskName;
    String visualPath;

    public static final int TEMP_DOC_ID = -1; // 上传图片文件的临时id 置于doc_id字段

    String connname;

    String LOAD = "SELECT doc_id, name, fullpath, diskname, visualpath, orders, page_num,downloadCount,upload_date,file_size FROM document_attach WHERE id=?";
    String SAVE = "update document_attach set doc_id=?, name=?, fullpath=?, diskname=?, visualpath=?, orders=?, page_num=?,downloadCount=? WHERE id=?";
    Logger logger = Logger.getLogger(Attachment.class.getName());

    public Attachment() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Attachment:DB is empty！");
    }

    public Attachment(int id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Attachment:DB is empty！");
        this.id = id;
        loadFromDb();
    }

    public Attachment(int orders, int docId, int pageNum) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Attachment:DB is empty！");
        this.orders = orders;
        this.docId = docId;
        this.pageNum = pageNum;
        loadFromDbByOrders();
    }

    /**
     * 当uploadimg.jsp中上传图片时，用于创建临时图片
     * @return boolean
     */
    public boolean create() {
        String sql = "insert into document_attach (id,fullpath,doc_id,name,diskname,visualpath,page_num,upload_date,file_size) values (?,?,?,?,?,?,?,?,?)";
        id = (int) SequenceMgr.nextID(SequenceMgr.DOCUMENT_ATTACH);
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            pstmt.setString(2, fullPath);
            pstmt.setLong(3, docId);
            pstmt.setString(4, name);
            pstmt.setString(5, diskName);
            pstmt.setString(6, visualPath);
            pstmt.setInt(7, pageNum);
            pstmt.setString(8, DateUtil.toLongString(new java.util.Date()));
            pstmt.setLong(9, size);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }


    public boolean del() {
        String sql = "delete from document_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            pstmt.close();
            // 更新其后的附件的orders
            sql = "update document_attach set orders=orders-1 where doc_id=? and page_num=? and orders>?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, docId);
            pstmt.setInt(2, pageNum);
            pstmt.setInt(3, orders);
            conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        // 删除文件
        File fl = new File(Global.realPath + visualPath + "/" + diskName);
        fl.delete();
        return re;
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(SAVE);
            pstmt.setInt(1, docId);
            pstmt.setString(2, name);
            pstmt.setString(3, fullPath);
            pstmt.setString(4, diskName);
            pstmt.setString(5, visualPath);
            pstmt.setInt(6, orders);
            pstmt.setInt(7, pageNum);
            pstmt.setInt(8, downloadCount);
            pstmt.setInt(9, id);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(docId);
        }
        return re;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDocId() {
        return this.docId;
    }

    public void setDocId(int di) {
        this.docId = di;
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

    public int getPageNum() {
        return pageNum;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public java.util.Date getUploadDate() {
        return uploadDate;
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

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
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

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
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
                docId = rs.getInt(1);
                name = rs.getString(2);
                fullPath = rs.getString(3);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                orders = rs.getInt(6);
                pageNum = rs.getInt(7);
                downloadCount = rs.getInt(8);
                uploadDate = DateUtil.parse(rs.getString(9));
                size = rs.getLong(10);
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

    public void loadFromDbByOrders() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String LOADBYORDERS = "SELECT id, name, fullpath, diskname, visualpath, downloadCount, upload_date, file_size FROM document_attach WHERE orders=? and doc_id=? and page_num=?";
            pstmt = conn.prepareStatement(LOADBYORDERS);
            pstmt.setInt(1, orders);
            pstmt.setInt(2, docId);
            pstmt.setInt(3, pageNum);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                id = rs.getInt(1);
                name = rs.getString(2);
                fullPath = rs.getString(3);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                downloadCount = rs.getInt(6);
                uploadDate = DateUtil.parse(rs.getString(7));
                size = rs.getLong(8);
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

    public String getFileExt() {
        return StrUtil.getFileExt(getDiskName());
    }

    public void setUploadDate(java.util.Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public boolean delTmpAttach() {
        String sql = "delete from document_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            // 删除文件
            Config cfg = new Config();
            String attPath = cfg.getProperty("cms.file_doc_attach");

            String filepath = Global.getRealPath() + attPath + "/" +
                              visualPath + "/" + diskName;

            File fl = new File(filepath);
            fl.delete();
        }
        return re;
    }

    private int orders = 0;
    private int pageNum = 1;
    private boolean loaded = false;
    private int downloadCount = 0;
    private java.util.Date uploadDate;
    private long size = 0;
}
