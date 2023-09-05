package com.redmoon.oa.flow;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import cn.js.fan.db.Conn;
import cn.js.fan.db.SQLFilter;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import cn.js.fan.web.Global;
import com.redmoon.kit.util.FileInfo;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.util.Pdf2Html;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.ServletContext;

public class DocContent implements java.io.Serializable {
    String content;
    int docId;
    int pageNum = 1;

    String connname = "";

    private static final String INSERT =
            "INSERT into flow_doc_content (doc_id, content, page_num) VALUES (?,?,?)";
    private static final String LOAD =
            "SELECT content from flow_doc_content WHERE doc_id=? and page_num=?";
    private static final String DEL =
            "DELETE FROM flow_doc_content WHERE doc_id=? and page_num=?";
    private static final String SAVE =
            "UPDATE flow_doc_content SET content=? WHERE doc_id=? and page_num=?";

    private static final String LOAD_DOCUMENT_ATTACHMENTS =
            "SELECT id FROM flow_document_attach WHERE doc_id=? and page_num=? order by orders";

    public DocContent() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("DocContent:默认数据库名为空！");
    }

    public DocContent(int doc_id, int page_num) {
        this.docId = doc_id;
        this.pageNum = page_num;

        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("DocContent:默认数据库名为空！");
        loadFromDb(doc_id, page_num);
    }

    public void renew() {
    }

    public boolean createWithoutFile(ServletContext application,
                                     CMSMultiFileUploadBean mfu, int doc_id,
                                     String content, int pageNum) throws
            ErrMsgException {
        String FilePath = StrUtil.getNullString(mfu.getFieldValue("filepath"));
        // 因为mysql JDBC的原因，使得使用下句后路径会变为d:oaupfile/...
        // String tempAttachFilePath = application.getRealPath("/") + FilePath +
        //                             "/";
        String tempAttachFilePath = Global.getRealPath() + FilePath +
                                    "/";

        mfu.setSavePath(tempAttachFilePath); // 取得目录
        File f = new File(tempAttachFilePath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }

        this.docId = doc_id;
        this.content = content;
        this.pageNum = pageNum;

        Conn conn = new Conn(connname);
        boolean re = true;
        try {
            // 处理附件
            int ret = mfu.getRet();
            String sisDdxc = StrUtil.getNullString(mfu.getFieldValue("isDdxc"));
            // 断点续传
            if (sisDdxc.equals("true")) {
                String[] attachFileNames = mfu.getFieldValues("attachFileName");
                String[] clientFilePaths = mfu.getFieldValues("clientFilePath");

                int len = 0;
                if (attachFileNames != null) {
                    len = attachFileNames.length;
                }
                String sql = "";
                int orders = 1;
                for (int i = 0; i < len; i++) {
                    String filepath = mfu.getSavePath() + attachFileNames[i];
                    String name = FileUpload.getUploadFileName(clientFilePaths[i]);
                    sql =
                            "insert into flow_document_attach (fullpath,doc_id,name,diskname,visualpath,page_num,orders) values (" +
                            StrUtil.sqlstr(filepath) + "," + docId + "," +
                            StrUtil.sqlstr(name) + "," +
                            StrUtil.sqlstr(attachFileNames[i]) + "," +
                            StrUtil.sqlstr(FilePath) + "," + pageNum + "," + orders + ")";
                    conn.executeUpdate(sql);
                    orders ++;
                }
            } else {
                if (ret == FileUpload.RET_SUCCESS) {
                    String filepath = "";
                    String sql = "";
                    // 处理附件
                    mfu.writeAttachment(true); // 用随机名称命名文件
                    Vector attachs = mfu.getAttachments();
                    Iterator ir = attachs.iterator();
                    sql = "";
                    while (ir.hasNext()) {
                        FileInfo fi = (FileInfo) ir.next();
                        filepath = mfu.getSavePath() + fi.getDiskName();
                        sql =
                                "insert into flow_document_attach (fullpath,doc_id,name,diskname,visualpath,page_num) values (" +
                                StrUtil.sqlstr(filepath) + "," + docId + "," +
                                StrUtil.sqlstr(fi.getName()) + "," +
                                StrUtil.sqlstr(fi.getDiskName()) + "," +
                                StrUtil.sqlstr(FilePath) + "," + pageNum + ")";
                        conn.executeUpdate(sql);
                    }
                } else
                    throw new ErrMsgException("上传失败！ret=" + ret);
            }
            // 如果页面是从中间某页后插入，而不是被加至末尾
            Document doc = new Document();
            doc = doc.getDocument(docId);
            PreparedStatement pstmt = null;
            // pageNum=1的时候，是插入新文章，而不是从中间某页后插入新页
            if (pageNum <= doc.getPageCount() && pageNum != 1) {
                // LogUtil.getLog(getClass()).info("pageNum=" + pageNum);
                // 更新其后的页面的页码
                String sql =
                        "update flow_doc_content set page_num=page_num+1 where doc_id=? and page_num>=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, docId);
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();
                pstmt = null;

                sql = "update flow_cms_images set subkey=subkey+1 where mainkey=? and subkey>=? and kind='document'";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, docId);
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();
                pstmt = null;

                sql =
                        "update flow_document_attach set page_num=page_num+1 where doc_id=? and page_num>=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, docId);
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();
                pstmt = null;
            }

            // 更新缓存
            DocContentCacheMgr dcm = new DocContentCacheMgr();
            for (int i = pageNum; i < doc.getPageCount(); i++) {
                dcm.refreshUpdate(docId, pageNum);
            }

            pstmt = conn.prepareStatement(INSERT);
            pstmt.setInt(1, doc_id);
            pstmt.setString(2, content);
            pstmt.setInt(3, pageNum);
            re = conn.executePreUpdate() == 1;

            if (re) {
                // 更新缓存
                dcm.refreshCreate(docId);
            }

            // 更新文章的总页数
            if (pageNum != 1) {
                doc.UpdatePageCount(doc.getPageCount() + 1);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            conn.close();
        }

        return re;
    }

    public boolean create(int doc_id,
                          String content) {
        this.docId = doc_id;
        this.content = content;
        this.pageNum = 1;

        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(INSERT);
            pstmt.setInt(1, doc_id);
            pstmt.setString(2, content);
            pstmt.setInt(3, pageNum);
            re = conn.executePreUpdate() == 1;

            if (re) {
                // 更新缓存
                DocContentCacheMgr dcm = new DocContentCacheMgr();
                dcm.refreshCreate(docId);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DocContent getDocContent(int doc_id, int page_num) {
        DocContentCacheMgr dccm = new DocContentCacheMgr();
        return dccm.getDocContent(doc_id, page_num);
    }

    public void loadFromDb(int doc_id, int page_num) {
        Conn conn = new Conn(connname);
        ResultSet rs;
        PreparedStatement pstmt;
        try {
            pstmt = conn.prepareStatement(LOAD);
            this.docId = doc_id;
            this.pageNum = page_num;
            pstmt.setInt(1, doc_id);
            pstmt.setInt(2, page_num);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    content = rs.getString(1);
                }
            }
            pstmt.close();
            pstmt = null;

            // 取得附件
            attachments = new Vector();
            pstmt = conn.prepareStatement(LOAD_DOCUMENT_ATTACHMENTS);
            // LogUtil.getLog(getClass()).info(LOAD_DOCUMENT_ATTACHMENTS);
            pstmt.setInt(1, docId);
            pstmt.setInt(2, pageNum);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int aid = rs.getInt(1);
                    // LogUtil.getLog(getClass()).info("aid=" + aid);
                    Attachment am = new Attachment(aid);
                    attachments.addElement(am);
                    // LogUtil.getLog(getClass()).info("att name=" + am.getName());
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("loadFromDb: " + e.getMessage());
        } finally {
            conn.close();
        }
    }

    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        ResultSet rs = null;
        try {
            // 从磁盘删除图像文件
            String sql = "select path from flow_cms_images where mainkey=" + SQLFilter.sqlstr(String.valueOf(docId)) +
                         " and kind='document' and subkey=" + pageNum;
            rs = conn.executeQuery(sql);
            if (rs != null) {
                String fpath = "";
                while (rs.next()) {
                    fpath = rs.getString(1);
                    if (fpath != null) {
                        File virtualFile = new File(fpath);
                        virtualFile.delete();
                    }
                }
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            // 从数据库中删除图像文件
            sql = "delete from flow_cms_images where mainkey=" + SQLFilter.sqlstr(String.valueOf(docId))  +
                  " and kind='document' and subkey=" + pageNum;
            conn.executeUpdate(sql);

            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");
            // 从磁盘删除附件
            sql = "select visualpath, diskname from flow_document_attach where doc_id=" + docId +
                  " and page_num=" + pageNum;
            rs = conn.executeQuery(sql);
            if (rs != null) {
                String fpath = "";
                while (rs.next()) {
                    fpath = rs.getString(1);
                    String diskName = rs.getString(2);
                    fpath = Global.getRealPath() + fpath + "/" + diskName;
                    File virtualFile = new File(fpath);
                    virtualFile.delete();

                    // 删除预览文件
                    if (canPdfFilePreview) {
                        if ("pdf".equalsIgnoreCase(StrUtil.getFileExt(diskName))) {
                            Pdf2Html.del(fpath);
                        }
                    }
                }
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            // 从数据库中删除附件
            sql = "delete from flow_document_attach where doc_id=" + docId +
                  " and page_num=" + pageNum;
            conn.executeUpdate(sql);

            // 从数据库中删除
            PreparedStatement pstmt = conn.prepareStatement(DEL);
            pstmt.setInt(1, docId);
            pstmt.setInt(2, pageNum);
            re = conn.executePreUpdate() == 1;
            pstmt.close();

            // 如果该页是最后一页，则不用更新
            Document doc = new Document();
            doc = doc.getDocument(docId);
            if (pageNum != doc.getPageCount()) {
                // 更新其后的页面的页码
                sql =
                        "update flow_doc_content set page_num=page_num-1 where doc_id=? and page_num>?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, docId);
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();

                sql = "update flow_cms_images set subkey=subkey-1 where mainkey=? and subkey>? and kind='document'";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, SQLFilter.sqlstr(String.valueOf(docId)) );
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();

                sql =
                        "update flow_document_attach set page_num=page_num-1 where doc_id=? and page_num>?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, docId);
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();
            }
            // 更新文章的总页数
            if (doc.getPageCount() > 1) {
                doc.UpdatePageCount(doc.getPageCount() - 1);
            }

            re = true;
            // 更新缓存
            DocContentCacheMgr dcm = new DocContentCacheMgr();
            dcm.refreshDel(docId, pageNum);

            for (int i = pageNum + 1; i < doc.getPageCount(); i++) {
                dcm.refreshUpdate(docId, pageNum);
            }

        } catch (SQLException e) {
            re = false;
            LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {LogUtil.getLog(getClass()).error(e);}
            }
            conn.close();
        }
        return re;
    }

    public int getDocId() {
        return docId;
    }

    public int getPageNum() {
        return pageNum;
    }

    public boolean save() {
        boolean re = false;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(SAVE);
            pstmt.setString(1, content);
            pstmt.setInt(2, docId);
            pstmt.setInt(3, pageNum);
            re = conn.executePreUpdate() == 1;

            // 更新缓存
            DocContentCacheMgr dcm = new DocContentCacheMgr();
            dcm.refreshUpdate(docId, pageNum);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    private Vector attachments;

    public Vector getAttachments() {
        return attachments;
    }

    public synchronized boolean delAttachment(int attachId) {
        Attachment am = new Attachment(attachId);
        boolean re = am.del();
        // 更新缓存
        if (re) {
            // 更新缓存
            DocContentCacheMgr dcm = new DocContentCacheMgr();
            dcm.refreshUpdate(docId, pageNum);
        }
        return re;
    }

    public synchronized boolean updateAttachmentName(int attachId,
            String newname) {
        String sql = "update flow_document_attach set name=? where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newname);
            pstmt.setInt(2, attachId);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                // 更新缓存
                DocContentCacheMgr dcm = new DocContentCacheMgr();
                dcm.refreshUpdate(docId, pageNum);
            }
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

    public int getAttachmentMaxOrders() {
        String GETMAXORDERS =
                "select max(orders) from flow_document_attach where doc_id=? and page_num=?";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        int maxorders = -1;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(GETMAXORDERS);
            pstmt.setInt(1, docId);
            pstmt.setInt(2, pageNum);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getMaxOrders:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return maxorders;
    }

    public boolean moveAttachment(int attachId, String direction) {
        Attachment attach = new Attachment(attachId);
        boolean re = false;
        int orders = attach.getOrders();
        if (direction.equals("up")) {
            if (orders == 1)
                return true;
            else {
                Attachment upperAttach = new Attachment(orders - 1, docId,
                        pageNum);
                if (upperAttach.isLoaded()) {
                    upperAttach.setOrders(orders);
                    upperAttach.save();
                }
                attach.setOrders(orders - 1);
                re = attach.save();
            }
        } else {
            int maxorders = getAttachmentMaxOrders();
            if (orders == maxorders) {
                return true;
            } else {
                Attachment lowerAttach = new Attachment(orders + 1, docId, pageNum);
                if (lowerAttach.isLoaded()) {
                    lowerAttach.setOrders(orders);
                    lowerAttach.save();
                }
                attach.setOrders(orders + 1);
                re = attach.save();
            }
        }
        // 更新缓存
        DocContentCacheMgr dcm = new DocContentCacheMgr();
        dcm.refreshUpdate(docId, pageNum);
        return re;
    }

}
