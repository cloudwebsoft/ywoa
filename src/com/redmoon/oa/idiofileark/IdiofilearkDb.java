package com.redmoon.oa.idiofileark;

/**
 * <p>Title: 社区</p>
 * <p>Description: 社区</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: 镇江云网软件技术有限公司</p>
 * @author bluewind
 * @version 1.0
 */

import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.redmoon.kit.util.*;
import com.redmoon.oa.db.*;
import com.redmoon.oa.message.*;
import com.redmoon.oa.person.*;
import com.redmoon.oa.ui.*;

public class IdiofilearkDb extends ObjectDb implements IDesktopUnit {
    int id;
    private FileUpload fileUpload;
    public String title, content, ip, dir_code;

    public static final String SENDER_SYSTEM = "系统";

    public IdiofilearkDb() {
        init();
    }

    public IdiofilearkDb(int id) {
        this.id = id;
        init();
        load();
    }

    public String getPageList(HttpServletRequest request,
                              UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList();
        return url;
    }

    public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.
                                                 Privilege();
        // System.out.println("PlanDb.java display sql=" + sql);
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageShow();
        String str = "";
        int count = uds.getCount();

        UserMgr um = new UserMgr();
        UserDb user = um.getUserDb(privilege.getUser(request));
        String inboxDirCode = Leaf.getSystemDirCode(user, Leaf.TYPE_INBOX);

        String sql = "select id from oa_idiofileark where dir_code=" +
                     StrUtil.sqlstr(inboxDirCode) +
                     " order by add_date desc";
        str += "<ul>";
        Iterator msgir = list(sql, 0, count - 1).iterator();
        while (msgir.hasNext()) {
            IdiofilearkDb md = (IdiofilearkDb) msgir.next();
            id = md.getId();
            title = md.getTitle();
            addDate = md.getAddDate();
            str += "<li><a href='" + url + "?id=" + md.getId() + "'>";
            str += title;
            // str += "(" + sender + ")";
            str += "  [" + DateUtil.format(addDate, "yy-MM-dd") + "]";
            str += "</a></li>";
        }
        str += "</ul>";
        return str;
    }

    /**
     * 将文件保存至自定义目录
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param userName String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean addDoc(ServletContext application,
                          HttpServletRequest request,
                          String userName) throws
            ErrMsgException {
        IdiofilearkForm mf = new IdiofilearkForm(application, request, this);
        mf.checkAddDoc();
        fileUpload = mf.getFileUpload();
        create(mf.fileUpload, userName);
        return true;
    }

    public boolean modifyDoc(ServletContext application,
                             HttpServletRequest request
            ) throws
            ErrMsgException {
        IdiofilearkForm mf = new IdiofilearkForm(application, request, this);
        mf.checkModifyDoc();
        fileUpload = mf.getFileUpload();
        save(mf.fileUpload);
        return true;
    }

    public boolean del(String[] ids) throws ErrMsgException {
        int len = ids.length;
        String str = "";
        for (int i = 0; i < len; i++)
            if (str.equals(""))
                str += ids[i];
            else
                str += "," + ids[i];
        str = "(" + str + ")";
        String sql = "select id from oa_idiofileark where id in " + str;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            ResultSet rs = conn.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    getIdiofilearkDb(rs.getInt(1)).del();
                }
            }
        } catch (Exception e) {
            logger.error("delMsg:" + e.getMessage());
            throw new ErrMsgException("删除失败！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public IdiofilearkDb getIdiofilearkDb(int id) {
        return (IdiofilearkDb) getObjectDb(new Integer(id));
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getIp() {
        return ip;
    }

    public int getId() {
        return id;
    }

    public Vector getIdioAttachments() {
        return IdioAttachments;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public java.util.Date getAddDate() {
        return addDate;
    }

    public String getDirCode() {
        return dir_code;
    }

    public boolean create(FileUpload fu, String userName) throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        // logger.info("create:toUser=" + toUser);
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            id = (int) SequenceManager.nextID(SequenceManager.OA_IDIOMESSAGE);
            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, ip);
            ps.setTimestamp(5, new Timestamp((new java.util.Date()).getTime()));
            ps.setString(6, dir_code);
            ps.setString(7, userName);
            re = conn.executePreUpdate() == 1 ? true : false;
            IdiofilearkCache mc = new IdiofilearkCache(this);
            mc.refreshCreate();

            if (re) {
                // 将msg的id与上传的临时图片文件相关联
                String[] tmpAttachIds = fu.getFieldValues("tmpAttachId");
                if (tmpAttachIds != null) {
                    int len = tmpAttachIds.length;
                    for (int k = 0; k < len; k++) {
                        IdioAttachment att = new IdioAttachment(Integer.parseInt(
                                tmpAttachIds[
                                k]));
                        // att.setMsgId(id);
                        //  att.save();

                        String fullPath = Global.realPath + att.getVisualPath() +
                                          "/" +
                                          att.getDiskName();
                        String newName = RandomSecquenceCreator.getId() + "." +
                                         StrUtil.getFileExt(att.getDiskName());

                        String newFullPath = Global.realPath +
                                             att.getVisualPath() +
                                             "/" + newName;
                        FileUtil.CopyFile(fullPath, newFullPath);
                        
                        IdioAttachment att2 = new IdioAttachment();
                        att2.setFullPath(newFullPath);
                        att2.setMsgId(id);
                        att2.setFullPath(fullPath);
                        att2.setName(att.getName());
                        att2.setDiskName(newName);
                        att2.setVisualPath(att.getVisualPath());
                        att2.setUploadDate(att.getUploadDate());
                        re = att2.create();
                        //修改Ccntent中图片名称
                        changeContent(att.getDiskName(), newName);
                    }
                    // 删除上传的临时图片文件
                    del(tmpAttachIds);
                }

                if (fu.getRet() == fu.RET_SUCCESS) {
                    // 置保存路径
                    Calendar cal = Calendar.getInstance();
                    String year = "" + (cal.get(cal.YEAR));
                    String month = "" + (cal.get(cal.MONTH) + 1);

                    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                    String vpath = cfg.get("file_message") + "/" + year + "/" +
                                   month + "/";
                    String filepath = fu.getRealPath() + vpath;
                    fu.setSavePath(filepath);
                    // 使用随机名称写入磁盘
                    fu.writeFile(true);
                    Vector v = fu.getFiles();
                    FileInfo fi = null;
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        fi = (FileInfo) ir.next();
                        IdioAttachment att = new IdioAttachment();
                        att.setFullPath(filepath + fi.getDiskName());
                        att.setMsgId(id);
                        att.setName(fi.getName());
                        att.setDiskName(fi.getDiskName());
                        att.setVisualPath(vpath);
                        re = att.create();
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("create: " + StrUtil.trace(e));
            throw new ErrMsgException("数据库操作错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean save(FileUpload fu) throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        String sql = "update oa_idiofileark set title=?,content=? where id=?";
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setInt(3, id);
            re = conn.executePreUpdate() == 1 ? true : false;

            IdiofilearkCache uc = new IdiofilearkCache(this);
            primaryKey.setValue(new Integer(id));
            uc.refreshSave(primaryKey);

            if (re) {
                // 将msg的id与上传的临时图片文件相关联
                String[] tmpAttachIds = fu.getFieldValues("tmpAttachId");
                if (tmpAttachIds != null) {
                    int len = tmpAttachIds.length;
                    for (int k = 0; k < len; k++) {
                        IdioAttachment att = new IdioAttachment(Integer.parseInt(
                                tmpAttachIds[
                                k]));
                        att.setMsgId(id);
                        att.save();
                    }
                }

                if (fu.getRet() == fu.RET_SUCCESS) {
                    // 置保存路径
                    Calendar cal = Calendar.getInstance();
                    String year = "" + (cal.get(cal.YEAR));
                    String month = "" + (cal.get(cal.MONTH) + 1);

                    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                    String vpath = cfg.get("file_message") + "/" + year + "/" +
                                   month + "/";
                    String filepath = fu.getRealPath() + vpath;
                    fu.setSavePath(filepath);
                    // 使用随机名称写入磁盘
                    fu.writeFile(true);
                    Vector v = fu.getFiles();
                    FileInfo fi = null;
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        fi = (FileInfo) ir.next();
                        IdioAttachment att = new IdioAttachment();
                        att.setFullPath(filepath + fi.getDiskName());
                        att.setMsgId(id);
                        att.setName(fi.getName());
                        att.setDiskName(fi.getDiskName());
                        att.setVisualPath(vpath);
                        re = att.create();
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("save: " + e.getMessage());
            throw new ErrMsgException("数据库操作错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        IdiofilearkCache uc = new IdiofilearkCache(this);
        primaryKey.setValue(primaryKeyValue);
        return (IdiofilearkDb) uc.getObjectDb(primaryKey);
    }

    public synchronized boolean del() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;

            if (re) {
                IdiofilearkCache mc = new IdiofilearkCache(this);
                mc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            // 删除附件
            if (IdioAttachments != null) {
                Iterator ir = IdioAttachments.iterator();
                while (ir.hasNext()) {
                    IdioAttachment att = (IdioAttachment) ir.next();
                    att.del();
                }
            }

            IdiofilearkCache uc = new IdiofilearkCache(this);
            primaryKey.setValue(new Integer(id));
            uc.refreshDel(primaryKey);

        }
        return re;
    }

    public int getObjectCount(String sql) {
        IdiofilearkCache uc = new IdiofilearkCache(this);
        return uc.getObjectCount(sql);
    }

    public Object[] getObjectBlock(String query, int startIndex) {
        IdiofilearkCache dcm = new IdiofilearkCache(this);
        return dcm.getObjectBlock(query, startIndex);
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new IdiofilearkDb(pk.getIntValue());
    }

    public void setQueryCreate() {
        this.QUERY_CREATE =
                "insert into oa_idiofileark (id,title,content,ip,add_date,dir_code,user_name) values (?,?,?,?,?,?,?)";
    }

    public void setQuerySave() {
        this.QUERY_SAVE =
                "update oa_idiofileark set content=? where id=?";
    }

    public void setQueryDel() {
        this.QUERY_DEL = "delete from oa_idiofileark where id=?";
    }

    public void setQueryLoad() {
        QUERY_LOAD = "select title,content,add_date,ip,dir_code from oa_idiofileark where id=?";
    }

    public void setQueryList() {
        QUERY_LIST = "select id from oa_idiofileark order by add_date desc";
    }

    public synchronized boolean save() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, content);
            ps.setInt(2, id);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            logger.error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            IdiofilearkCache uc = new IdiofilearkCache(this);
            primaryKey.setValue(new Integer(id));
            uc.refreshSave(primaryKey);
        }
        return re;
    }

    public void setPrimaryKey() {
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
    }

    public synchronized void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                title = rs.getString(1);
                content = StrUtil.getNullStr(rs.getString(2));
                addDate = rs.getTimestamp(3);
                ip = rs.getString(4);
                dir_code = rs.getString(5);
                loaded = true;
                primaryKey.setValue(new Integer(id));

                String LOAD_DOCUMENT_IdioAttachmentS =
                        "SELECT id FROM oa_idiofileark_attach WHERE msgId=? order by orders";
                IdioAttachments = new Vector();
                pstmt = conn.prepareStatement(LOAD_DOCUMENT_IdioAttachmentS);
                pstmt.setInt(1, id);
                rs = conn.executePreQuery();
                if (rs != null) {
                    while (rs.next()) {
                        int aid = rs.getInt(1);
                        IdioAttachment am = new IdioAttachment(aid);
                        IdioAttachments.addElement(am);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void setIdioAttachments(Vector IdioAttachments) {
        this.IdioAttachments = IdioAttachments;
    }

    public IdioAttachment getIdioAttachment(int attId) {
        Iterator ir = getIdioAttachments().iterator();
        while (ir.hasNext()) {
            IdioAttachment at = (IdioAttachment) ir.next();
            if (at.getId() == attId)
                return at;
        }
        return null;
    }

    /**
     * 删除上传的临时图片文件
     * @param fu FileUpload
     * @return boolean
     */
    public boolean delTempFile(FileUpload fu) {
        String[] tmpAttachIds = fu.getFieldValues("tmpAttachId");
        int len = tmpAttachIds.length;
        for (int i = 0; i < len; i++) {
            int attId = Integer.parseInt(tmpAttachIds[i]);
            IdioAttachment att = new IdioAttachment(attId);
            if (att != null) {
                att.delTmpAttach();
            }
        }
        return true;
    }

    /**
     * 删除目录下的所有文章
     * @param code String
     * @throws ErrMsgException
     */
    public void delByDirCode(String code) throws ErrMsgException {
        Vector v = getIdiofilearkByDirCode(code);
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            IdiofilearkDb ifd = (IdiofilearkDb) ir.next();
            ifd.del();
        }
    }

    /**
     * 取得目录下的所有文章
     * @param code String
     * @return Vector
     */
    public Vector getIdiofilearkByDirCode(String code) {
        Vector v = new Vector();
        String sql = "select id from oa_idiofileark where dir_code=" +
                     StrUtil.sqlstr(code);
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sql);
            if (rs != null) {
               while (rs.next()) {
                   v.addElement(getIdiofilearkDb(rs.getInt(1)));
               }
            }
        } catch (SQLException e) {
            logger.error("getIdiofilearkByDirCode:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     * 找出s1中有而s2中没有的元素
     * @param s1 String[]
     * @param s2 String[]
     * @return String[]
     */
    public Vector getDiffElement(String[] s1, String[] s2) {
        Vector v = new Vector();
        boolean canAdd = true;
        if (s1 != null) {
            int len1 = s1.length;
            if (s2 != null) {
                int len2 = s2.length;
                for (int k = 0; k < len1; k++) {
                    for (int j = 0; j < len2; j++) {
                        if (s1[k].equals(s2[j])) {
                            canAdd = false;
                            break;
                        }
                    }
                    if (canAdd) {
                        v.add(s1[k]);
                    } else {
                        canAdd = true;
                    }
                }
            } else {
                for (int k = 0; k < len1; k++) {
                    v.add(s1[k]);
                }
            }
        }
        return v;
    }

    public synchronized boolean changeDirCode(String dirCode, String idStr) {
        this.id = StrUtil.toInt(idStr);
        this.dir_code = dirCode;
        String sql = "update oa_idiofileark set dir_code=? where id=?";
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, dir_code);
            ps.setInt(2, id);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error("changeDirCode:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            IdiofilearkCache uc = new IdiofilearkCache(this);
            primaryKey.setValue(new Integer(id));
            uc.refreshSave(primaryKey);
        }
        return re;
    }

    /**
     *  根据传入的新文件名将content中包含的文件名更新
     * @param unModifiedDiskName String
     * @param modifiedDiskName String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean changeContent(String unModifiedDiskName,
                                 String modifiedDiskName) throws
            ErrMsgException {
        String oldContent = content;
        content = content.replaceAll(unModifiedDiskName, modifiedDiskName);
        if (content.equals(oldContent)) {
            return true;
        }
        return save();
    }

    /**
     * 将短消息中的文件转存至个人文件柜中
     * @param msg IdiofilearkDb
     * @param transmitDirCode String
     * @return boolean
     */
    public boolean TransmitMsgToidiofileark(MessageDb msg, String transmitDirCode) throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement("insert into oa_idiofileark (id,title,content,ip,add_date,dir_code) values (?,?,?,?,?,?)");
            id = (int) SequenceManager.nextID(SequenceManager.OA_IDIOMESSAGE);
            ps.setInt(1, id);
            ps.setString(2, msg.getTitle());
            ps.setString(3, msg.getContent());
            ps.setString(4, msg.getIp());
            addDate = DateUtil.parse(msg.getRq(), "yy-MM-dd HH:mm");
            ps.setTimestamp(5, new Timestamp(addDate.getTime()));
            ps.setString(6, transmitDirCode);
            re = conn.executePreUpdate() == 1 ? true : false;
            IdiofilearkCache mc = new IdiofilearkCache(this);
            // mc.refreshNewCountOfReceiver(receiver); // 20071124 revise
            //mc.refreshNewCountOfReceiver(toUser);
            mc.refreshCreate();
            // 将消息中的附件转移到文件柜中
            String sql = "insert into oa_idiofileark_attach (id,msgId,name,diskname,visualpath,orders) values (?,?,?,?,?,?)";
            Iterator ir = msg.getAttachments().iterator();
            while (ir.hasNext()) {
                Attachment att = (Attachment) ir.next();
                String fullPath = Global.realPath + att.getVisualPath() +
                                  "/" + att.getDiskName();
                String newName = RandomSecquenceCreator.getId() + "." +
                                 StrUtil.getFileExt(att.getDiskName());

                String newFullPath = Global.realPath +
                                     att.getVisualPath() + "/" +
                                     newName;
                FileUtil.CopyFile(fullPath, newFullPath);

                int attachmentId = (int) SequenceManager.nextID(SequenceManager.OA_MESSAGE_IdioAttachment);
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, attachmentId);
                pstmt.setInt(2, id);
                pstmt.setString(3, att.getName());
                pstmt.setString(4, newName);
                pstmt.setString(5, att.getVisualPath());
                pstmt.setInt(6, att.getOrders());
                re = conn.executePreUpdate() == 1 ? true : false;
            }

        } catch (SQLException e) {
            logger.error("transmitMsgToidiofileark: " + e.getMessage());
            throw new ErrMsgException("数据库操作错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDirCode(String dir_code) {
        this.dir_code = dir_code;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setAddDate(java.util.Date addDate) {
        this.addDate = addDate;
    }

    private Vector IdioAttachments;
    private java.util.Date addDate;
}
