package com.redmoon.forum;

import java.io.*;
import java.sql.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.person.*;
import org.apache.log4j.*;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 *
 * <p>Title: 贴子附件对象</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Attachment implements Serializable {
    long id;
    long msgId;
    String name;
    String fullPath;
    String diskName;
    String visualPath;

    public static final long TEMP_MSG_ID = -1; // 上传图片文件的临时id 置于msgId字段

    String connname;

    String LOAD = "SELECT msgId, name, fullpath, diskname, visualpath, orders, DOWNLOAD_COUNT, UPLOAD_DATE, FILE_SIZE, USER_NAME, is_remote, ext, att_desc FROM sq_message_attach WHERE id=?";
    String SAVE = "update sq_message_attach set msgId=?, name=?, fullpath=?, diskname=?, visualpath=?, orders=?, DOWNLOAD_COUNT=?,att_desc=? WHERE id=?";

    public Attachment(long id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            Logger.getLogger(getClass()).info("Attachment:connname is empty.");
        this.id = id;
        loadFromDb();
    }

    public Attachment() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            Logger.getLogger(getClass()).info("Attachment:connname is empty.");
    }

    public Attachment(int orders, long msgId) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            Logger.getLogger(getClass()).info("Attachment:connname is empty.");
        this.orders = orders;
        this.msgId = msgId;
        loadFromDbByOrders();
    }

    public boolean create() {
        String sql = "insert into sq_message_attach (id,fullpath,msgId,name,diskname,visualpath,orders,UPLOAD_DATE,FILE_SIZE,DOWNLOAD_COUNT,USER_NAME,is_remote,ext, att_desc) values (?,?,?,?,?,?,?,?,?,0,?,?,?,?)";
        id = SequenceMgr.nextID(SequenceMgr.SQ_MESSAGE_ATTACH);
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            pstmt.setString(2, fullPath);
            pstmt.setLong(3, msgId);
            pstmt.setString(4, name);
            pstmt.setString(5, diskName);
            pstmt.setString(6, visualPath);
            pstmt.setInt(7, orders);
            pstmt.setString(8, "" + uploadDate.getTime());
            pstmt.setLong(9, size);
            pstmt.setString(10, userName);
            pstmt.setInt(11, remote?1:0);
            pstmt.setString(12, ext);
            pstmt.setString(13, desc);
            re = conn.executePreUpdate() == 1 ? true : false;
        }
        catch (SQLException e) {
            Logger.getLogger(getClass()).error("create:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        return re;
    }

    public Attachment getAttachment(long id) {
        return new Attachment(id);
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public boolean delTmpAttach() {
        String sql = "delete from sq_message_attach where id=?";
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
            Config cfg = Config.getInstance();
            boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
            if (isFtpUsed && remote) {
                FTPUtil ftp = new FTPUtil();
                if (isFtpUsed) {
                    boolean retFtp = ftp.connect(cfg.getProperty(
                            "forum.ftpServer"),
                                     cfg.getIntProperty("forum.ftpPort"),
                                      cfg.getProperty("forum.ftpUser"),
                                      cfg.getProperty("forum.ftpPwd"), true);
                    if (!retFtp) {
                        ftp.close();
                        LogUtil.getLog(getClass()).error("del:" + ftp.getReplyMessage());
                    }
                    else {
                        ftp.del(visualPath + "/" + diskName);
                        ftp.close();
                    }
                }
            } else {
                // 删除文件
                String path = Global.getRealPath() +
                              Config.getInstance().getAttachmentPath() + "/" + visualPath + "/" +
                              diskName;
                File fl = new File(path);
                fl.delete();
            }
        }
        return re;
    }

    public boolean del() {
        String sql = "delete from sq_message_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            re = conn.executePreUpdate()==1?true:false;
            // logger.info("del: re=" + re);
            pstmt.close();
            // 更新其后的附件的orders
            sql = "update sq_message_attach set orders=orders-1 where msgId=? and orders>?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, msgId);
            pstmt.setInt(2, orders);
            conn.executePreUpdate();
        }
        catch (SQLException e) {
            Logger.getLogger(getClass()).error("del:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        if (re) {
            Config cfg = Config.getInstance();
            boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
            if (isFtpUsed && remote) {
                FTPUtil ftp = new FTPUtil();
                if (isFtpUsed) {
                    boolean retFtp = ftp.connect(cfg.getProperty(
                            "forum.ftpServer"),
                                     cfg.getIntProperty("forum.ftpPort"),
                                      cfg.getProperty("forum.ftpUser"),
                                      cfg.getProperty("forum.ftpPwd"), true);
                    if (!retFtp) {
                        ftp.close();
                        LogUtil.getLog(getClass()).error("del:" + ftp.getReplyMessage());
                    }
                    else {
                        ftp.del(visualPath + "/" + diskName);
                        ftp.close();
                    }
                }
            }
            else {
                // 删除文件
                File fl = new File(Global.realPath +
                                   com.redmoon.forum.Config.getInstance().
                                   getAttachmentPath() + "/" + visualPath + "/" +
                                   diskName);
                // System.out.println(getClass() + " path=" + Global.realPath + visualPath + "/" + diskName);
                fl.delete();
            }
            // 更新用户已用的磁盘空间
            if (!userName.equals("")) {
                MsgDb md = new MsgDb();
                md = md.getMsgDb(msgId);
                if (md.isLoaded()) {
                    UserDb ud = new UserDb();
                    ud = ud.getUser(md.getName());
                    long u = ud.getDiskSpaceUsed() - size;
                    // logger.info("del:u=" + u);
                    if (u < 0)
                        u = 0;
                    ud.setDiskSpaceUsed(u);
                    ud.save();
                }
            }
        }
        return re;
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(SAVE);
            pstmt.setLong(1, msgId);
            pstmt.setString(2, name);
            pstmt.setString(3, fullPath);
            pstmt.setString(4, diskName);
            pstmt.setString(5, visualPath);
            pstmt.setInt(6, orders);
            pstmt.setInt(7, downloadCount);
            pstmt.setString(8, desc);
            pstmt.setLong(9, id);
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            Logger.getLogger(getClass()).error("save:" + e.getMessage());
        }
        finally {
            MsgCache mc = new MsgCache();
            mc.refreshUpdate(msgId);
            if (conn!=null) {
                conn.close(); conn = null;
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

    public int getDownloadCount() {
        return downloadCount;
    }

    public java.util.Date getUploadDate() {
        return uploadDate;
    }

    public long getSize() {
        return size;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isRemote() {
        return remote;
    }

    public String getExt() {
        return ext;
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

    public void setUploadDate(java.util.Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public void setExt(String ext) {
        this.ext = ext;
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
                msgId = rs.getInt(1);
                name = rs.getString(2);
                fullPath = rs.getString(3);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                orders = rs.getInt(6);
                downloadCount = rs.getInt(7);
                uploadDate = DateUtil.parse(rs.getString(8));
                size = rs.getLong(9);
                userName = StrUtil.getNullStr(rs.getString(10));
                remote = rs.getInt(11)==1;
                ext = rs.getString(12);
                desc = StrUtil.getNullStr(rs.getString(13));
                loaded = true;
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("loadFormDb:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (pstmt!=null) {
                try {
                    pstmt.close();
                }
                catch (Exception e) {}
                pstmt = null;

            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public String getIcon() {
        String ext = StrUtil.getFileExt(diskName);
        if (ext == null)
            return "default.gif";
        if (ext.equals("doc") || ext.equals("xls") || ext.equals("gif") ||
            ext.equals("jpg"))
            return ext + ".gif";
        if (ext.equals("jpeg"))
            return "jpg.gif";
        if (ext.equals("exe"))
            return "exe.gif";
        if (ext.equals("rar"))
            return "rar.gif";
        return "default.gif";
    }

    public void loadFromDbByOrders() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String LOADBYORDERS = "SELECT id, name, fullpath, diskname, visualpath, msgId, DOWNLOAD_COUNT, UPLOAD_DATE, FILE_SIZE, USER_NAME, is_remote, ext, att_desc FROM sq_message_attach WHERE orders=? and msgId=?";
            pstmt = conn.prepareStatement(LOADBYORDERS);
            pstmt.setInt(1, orders);
            pstmt.setLong(2, msgId);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                id = rs.getInt(1);
                name = rs.getString(2);
                fullPath = rs.getString(3);
                diskName = rs.getString(4);
                visualPath = rs.getString(5);
                msgId = rs.getInt(6);
                downloadCount = rs.getInt(7);
                uploadDate = DateUtil.parse(rs.getString(8));
                size = rs.getLong(9);
                userName = StrUtil.getNullStr(rs.getString(10));
                remote = rs.getInt(11)==1;
                ext = rs.getString(12);
                desc = StrUtil.getNullStr(rs.getString(13));
                loaded = true;
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error(e.getMessage());
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
    private int downloadCount = 0;
    private java.util.Date uploadDate;
    private long size = 0;
    private String userName;
    private boolean remote = false;
    private String ext;
    private String desc;

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
