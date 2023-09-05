package com.redmoon.oa.fileark;

import cn.js.fan.util.*;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.FileUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.util.Pdf2Html;
import org.apache.commons.lang3.StringUtils;
import cn.js.fan.db.Conn;
import cn.js.fan.db.SQLFilter;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.ServletContext;
import com.redmoon.oa.db.SequenceManager;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import sun.misc.BASE64Decoder;

public class DocContent implements java.io.Serializable {
    String content;
    int docId;
    int pageNum = 1;

    String connname = "";

    private static final String INSERT =
            "INSERT into doc_content (doc_id, content, page_num) VALUES (?,?,?)";
    private static final String LOAD =
            "SELECT content from doc_content WHERE doc_id=? and page_num=?";
    private static final String DEL =
            "DELETE FROM doc_content WHERE doc_id=? and page_num=?";
    private static final String SAVE =
            "UPDATE doc_content SET content=? WHERE doc_id=? and page_num=?";

    private static final String LOAD_DOCUMENT_ATTACHMENTS =
            "SELECT id FROM document_attach WHERE doc_id=? and page_num=? and is_title_image=0 order by orders";

    private static final String LOAD_DOCUMENT_TITLEIMAGES =
            "SELECT id FROM document_attach WHERE doc_id=? and page_num=? and is_title_image=1 order by orders";

    public DocContent() {
        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("DocContent:默认数据库名为空！");
        }
    }

    public DocContent(int doc_id, int page_num) {
        this.docId = doc_id;
        this.pageNum = page_num;

        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("DocContent:默认数据库名为空！");
        }
        loadFromDb(doc_id, page_num);
    }

    public void renew() {
    }

    /**
     * 创建时不保存cms_image
     * @param application
     * @param mfu
     * @param doc_id
     * @param content
     * @param pageNum
     * @return
     * @throws ErrMsgException
     */
    public boolean createWithoutFile(ServletContext application,
                                     CMSMultiFileUploadBean mfu, int doc_id,
                                     String content, int pageNum) throws
            ErrMsgException {
        String filePath = StrUtil.getNullString(mfu.getFieldValue("filepath"));

        this.docId = doc_id;
        this.content = content;
        this.pageNum = pageNum;

        Conn conn = new Conn(connname);
        boolean re = true;
        try {
            // 处理附件
            int ret = mfu.getRet();
            if (ret == FileUpload.RET_SUCCESS) {
                String path = (Global.getRealPath() + filePath + "/").replaceAll("\\\\","/");
                IFileService fileService = SpringUtil.getBean(IFileService.class);

                com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
                boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");
                boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");

                Vector attachs = mfu.getAttachments();
                int orders = getAttachmentMaxOrders(false) + 1;
                Attachment attachment = new Attachment();
                Iterator ir = attachs.iterator();
                while (ir.hasNext()) {
                    FileInfo fi = (FileInfo) ir.next();
                    fileService.write(fi, filePath);

                    attachment.setDocId(docId);
                    attachment.setName(fi.getName());
                    attachment.setDiskName(fi.getDiskName());
                    attachment.setVisualPath(filePath);
                    attachment.setSize(fi.getSize());
                    attachment.setPageNum(pageNum);
                    attachment.setOrders(orders);
                    attachment.setExt(StrUtil.getFileExt(fi.getName()));
                    attachment.setUploadDate(new java.util.Date());
                    attachment.create();

                    orders ++;

                    // 获取上传文件路径，生成预览文件
                    if (canOfficeFilePreview) {
                        if (FileUtil.isOfficeFile(fi.getDiskName())) {
                            Document.createOfficeFilePreviewHTML(path + fi.getDiskName());
                        }
                    }
                    if (canPdfFilePreview) {
                        if (FileUtil.isPdfFile(fi.getDiskName())) {
                            Pdf2Html.del(path + fi.getDiskName());
                        }
                    }
                }

                // 将标题图片写入磁盘
                attachs = mfu.getTitleImages();
                orders = getAttachmentMaxOrders(true) + 1;
                ir = attachs.iterator();
                while (ir.hasNext()) {
                    FileInfo fi = (FileInfo) ir.next();
                    fileService.write(fi, filePath);

                    attachment.setDocId(docId);
                    attachment.setName(fi.getName());
                    attachment.setDiskName(fi.getDiskName());
                    attachment.setVisualPath(filePath);
                    attachment.setSize(fi.getSize());
                    attachment.setPageNum(pageNum);
                    attachment.setOrders(orders);
                    attachment.setExt(StrUtil.getFileExt(fi.getName()));
                    attachment.setUploadDate(new java.util.Date());
                    attachment.setTitleImage(true);

                    attachment.create();

                    orders ++;
                }

                // 对标题图片排序
                String imgOrders = mfu.getFieldValue("imgOrders");
                String[] orderArr = StrUtil.split(imgOrders, ",");
                if (orderArr!=null) {
                    for (int i = 0; i< orderArr.length; i++) {
                        String imgOrder = orderArr[i];
                        if (NumberUtil.isNumeric(imgOrder)) {
                            int imgId = StrUtil.toInt(imgOrder);
                            Attachment att = new Attachment(imgId);
                            att.setOrders(i);
                            att.save();
                        }
                        else {
                            Attachment att = new Attachment();
                            att = att.getAttachmentByName(docId, imgOrder);
                            if (att!=null) {
                                att.setOrders(i);
                                att.save();
                            }
                        }
                    }
                }
            } else {
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
                String sql = "update doc_content set page_num=page_num+1 where doc_id=? and page_num>=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, docId);
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();

                sql = "update cms_images set subkey=subkey+1 where mainkey=? and subkey>=? and kind='document'";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, docId);
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();

                sql = "update document_attach set page_num=page_num+1 where doc_id=? and page_num>=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, docId);
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();
            }

            // 更新缓存
            DocContentCacheMgr dcm = new DocContentCacheMgr();
            for (int i = pageNum; i < doc.getPageCount(); i++) {
                dcm.refreshUpdate(docId, pageNum);
            }

            // 解析content，将其中的base64编码转换成文件
            content = convertBase64ToImgFile(content, filePath, docId);

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
            LogUtil.getLog(getClass()).error("createWithoutFile:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }

        return re;
    }

    public boolean create(int doc_id, String content) {
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
            if (conn != null) {
                conn.close();
                conn = null;
            }
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
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(LOAD);
            this.docId = doc_id;
            this.pageNum = page_num;
            pstmt.setInt(1, doc_id);
            pstmt.setInt(2, page_num);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    content = StrUtil.getNullStr(rs.getString(1));
                    loaded = true;
                }
            }
            pstmt.close();

            // 取得附件
            pstmt = conn.prepareStatement(LOAD_DOCUMENT_ATTACHMENTS);
            pstmt.setInt(1, docId);
            pstmt.setInt(2, pageNum);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int aid = rs.getInt(1);
                    Attachment am = new Attachment(aid);
                    attachments.addElement(am);
                }
            }

            pstmt = conn.prepareStatement(LOAD_DOCUMENT_TITLEIMAGES);
            pstmt.setInt(1, docId);
            pstmt.setInt(2, pageNum);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int aid = rs.getInt(1);
                    Attachment am = new Attachment(aid);
                    titleImages.addElement(am);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("loadFromDb: " + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
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
            String sql = "select path from cms_images where mainkey=" + docId +
                         " and kind='document' and subkey=" + pageNum;
            rs = conn.executeQuery(sql);
            if (rs != null) {
                String fpath = "";
                while (rs.next()) {
                    fpath = rs.getString(1);
                    if (fpath != null) {
                        File virtualFile = new File(Global.getRealPath() + fpath);
                        virtualFile.delete();
                    }
                }
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            // 从数据库中删除图像文件
            sql = "delete from cms_images where mainkey=" + docId +
                  " and kind='document' and subkey=" + pageNum;
            conn.executeUpdate(sql);

            // 删除附件
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");
            for (Attachment att : attachments) {
                att.del();

                // 获取上传文件路径，删除对应预览文件
                String filepath = Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();
                if (!canOfficeFilePreview) {
                    if (FileUtil.isOfficeFile(att.getDiskName())) {
                        Document.jacobFileDelete(filepath);
                    }
                }
            }

            // 从数据库中删除
            PreparedStatement pstmt = conn.prepareStatement(DEL);
            pstmt.setInt(1, docId);
            pstmt.setInt(2, pageNum);
            re = conn.executePreUpdate() == 1;
            pstmt.close();
            pstmt = null;

            // 如果该页是最后一页，则不用更新
            Document doc = new Document();
            doc = doc.getDocument(docId);
            if (pageNum != doc.getPageCount()) {
                // 更新其后的页面的页码
                sql =
                        "update doc_content set page_num=page_num-1 where doc_id=? and page_num>?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, docId);
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();

                sql = "update cms_images set subkey=subkey-1 where mainkey=? and subkey>? and kind='document'";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, docId);
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();

                sql =
                        "update document_attach set page_num=page_num-1 where doc_id=? and page_num>?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, docId);
                pstmt.setInt(2, pageNum);
                conn.executePreUpdate();
                pstmt.close();
                pstmt = null;
            }
            // 更新文章的总页数
            if (doc.getPageCount() > 1)
                doc.UpdatePageCount(doc.getPageCount() - 1);

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

    public synchronized boolean save(ServletContext application,
                                     CMSMultiFileUploadBean mfu) throws
            ErrMsgException {
        String isuploadfile = StrUtil.getNullString(mfu.getFieldValue("isuploadfile"));
        if ("false".equals(isuploadfile)) {
            return saveWithoutFile(application, mfu);
        }
        String visualPath = StrUtil.getNullString(mfu.getFieldValue("filepath"));
        ResultSet rs;
        Conn conn = new Conn(connname);
        try {
            // 如果是高级发布方式编辑，则删除原有的图片
            String editFlag = StrUtil.getNullStr(mfu.getFieldValue("editFlag"));
            if ("redmoon".equals(editFlag)) {
	            // 删除图像文件
	            String sql = "select path from cms_images where mainkey=" + docId + " and kind='document' and subkey=" + pageNum + "";
	            rs = conn.executeQuery(sql);
	            if (rs != null) {
	                String fpath = "";
	                while (rs.next()) {
	                    fpath = rs.getString(1);
	                    if (fpath != null) {
	                        File virtualFile = new File(Global.getRealPath() + fpath);
	                        virtualFile.delete();
	                    }
	                }
	            }
	
	            if (rs != null) {
	                rs.close();
	            }
                     
	            // 从数据库中删除图像
	            sql = "delete from cms_images where mainkey=" + docId + " and kind='document' and subkey=" + pageNum + "";
	            conn.executeUpdate(sql);
            }

            // 处理图片
            int ret = mfu.getRet();
            if (ret == 1) {
                IFileService fileService = SpringUtil.getBean(IFileService.class);

                Vector files = mfu.getFiles();
                java.util.Enumeration e = files.elements();
                String sql;
                while (e.hasMoreElements()) {
                    FileInfo fi = (FileInfo) e.nextElement();
                    fileService.write(fi, visualPath, false);

                    sql =
                            "insert into cms_images (id,path,mainkey,kind,subkey,ext) values (" +
                            SequenceManager.nextID(SequenceManager.CMS_IMAGES) + "," + StrUtil.sqlstr(visualPath + "/" + fi.getName()) + "," + docId +
                            ",'document'," + pageNum + "," + StrUtil.sqlstr(StrUtil.getFileExt(fi.getName())) + ")";
                    conn.executeUpdate(sql);
                }
            } else {
                throw new ErrMsgException("save:上传失败！ret=" + ret);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }

        saveWithoutFile(application, mfu);
        return true;
    }

    /**
     * 保存时，不存储cms_image
     * @param application
     * @param mfu
     * @return
     * @throws ErrMsgException
     */
    public synchronized boolean saveWithoutFile(ServletContext application,
                                                CMSMultiFileUploadBean mfu) throws ErrMsgException {
        content = StrUtil.getNullString(mfu.getFieldValue("htmlcode"));

        Conn conn = new Conn(connname);
        boolean re = true;
        try {
            String filePath = StrUtil.getNullString(mfu.getFieldValue("filepath"));

            // 将附件写入磁盘
            IFileService fileService = SpringUtil.getBean(IFileService.class);
            com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
            boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");
            boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");

            Vector attachs = mfu.getAttachments();
            int orders = getAttachmentMaxOrders(false) + 1;
            Attachment attachment = new Attachment();
            Iterator ir = attachs.iterator();
            while (ir.hasNext()) {
                FileInfo fi = (FileInfo) ir.next();
                fileService.write(fi, filePath);

                attachment.setDocId(docId);
                attachment.setName(fi.getName());
                attachment.setDiskName(fi.getDiskName());
                attachment.setVisualPath(filePath);
                attachment.setSize(fi.getSize());
                attachment.setPageNum(pageNum);
                attachment.setOrders(orders);
                attachment.setExt(StrUtil.getFileExt(fi.getName()));
                attachment.setUploadDate(new java.util.Date());
                attachment.create();

                orders ++;

                // 获取上传文件路径，生成预览文件
                String fullPath = Global.getRealPath() + filePath + "/" + fi.getDiskName();
                if (canOfficeFilePreview) {
                    if (FileUtil.isOfficeFile(fi.getDiskName())) {
                        com.redmoon.oa.fileark.Document.createOfficeFilePreviewHTML(fullPath);
                    }
                }
                if (canPdfFilePreview) {
                    if (FileUtil.isPdfFile(fi.getDiskName())) {
                        Pdf2Html.createPreviewHTML(fullPath);
                    }
                }
            }

            // 将标题图片写入磁盘
            attachs = mfu.getTitleImages();
            orders = getAttachmentMaxOrders(true) + 1;
            ir = attachs.iterator();
            while (ir.hasNext()) {
                FileInfo fi = (FileInfo) ir.next();
                fileService.write(fi, filePath);

                attachment.setDocId(docId);
                attachment.setName(fi.getName());
                attachment.setDiskName(fi.getDiskName());
                attachment.setVisualPath(filePath);
                attachment.setSize(fi.getSize());
                attachment.setPageNum(pageNum);
                attachment.setOrders(orders);
                attachment.setExt(StrUtil.getFileExt(fi.getName()));
                attachment.setUploadDate(new java.util.Date());
                attachment.setTitleImage(true);

                attachment.create();

                orders ++;
            }

            // 对标题图片排序
            String imgOrders = mfu.getFieldValue("imgOrders");
            String[] orderArr = StrUtil.split(imgOrders, ",");
            if (orderArr!=null) {
                for (int i = 0; i< orderArr.length; i++) {
                    String imgOrder = orderArr[i];
                    if (NumberUtil.isNumeric(imgOrder)) {
                        int imgId = StrUtil.toInt(imgOrder);
                        Attachment att = new Attachment(imgId);
                        att.setOrders(i);
                        att.save();
                    }
                    else {
                        String imgName = imgOrder;
                        Attachment att = new Attachment();
                        att = att.getAttachmentByName(docId, imgName);
                        if (att!=null) {
                            att.setOrders(i);
                            att.save();
                        }
                    }
                }
            }

            // 解析content，将其中的base64编码转换成文件
            content = convertBase64ToImgFile(content, filePath, docId);

            PreparedStatement pstmt = conn.prepareStatement(SAVE);
            pstmt.setString(1, content);
            pstmt.setInt(2, docId);
            pstmt.setInt(3, pageNum);
            re = conn.executePreUpdate() == 1;
            pstmt.close();

            // 更新缓存
            DocContentCacheMgr dcm = new DocContentCacheMgr();
            dcm.refreshUpdate(docId, pageNum);
        } catch (SQLException e) {
            re = false;
            LogUtil.getLog(getClass()).error(e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    public String convertBase64ToImgFile(String content, String filePath, int docId) {
        String contentTmp = content;
        Parser parser;
        try {
            parser = new Parser(content);
            parser.setEncoding("utf-8");//
            TagNameFilter filter = new TagNameFilter("img");
            NodeList nodes = parser.parse(filter);
            if (nodes == null || nodes.size() == 0) {
                return content;
            }
            else {
                StringBuilder sb = new StringBuilder();
                int lastNodeEnd = 0;
                for (int k=0; k<nodes.size(); k++) {
                    ImageTag node = (ImageTag) nodes.elementAt(k);

                    // image/png;base64,
                    String imgUrl = node.getImageURL();
                    int p = imgUrl.indexOf(",");
                    if (p != -1) {
                        String base64img = imgUrl.substring(p + 1);
                        int a = imgUrl.indexOf("/");
                        int b = imgUrl.indexOf(";");
                        String ext = imgUrl.substring(a + 1, b);

                        String diskName = FileUpload.getRandName() + "." + ext;
                        boolean re = generateImage(base64img, Global.getRealPath() + filePath + "/" + diskName);
                        if (re) {
                            node.setImageURL(filePath + "/" + diskName);
                            int s = node.getStartPosition();
                            int e = node.getEndPosition();
                            String c = contentTmp.substring(lastNodeEnd, s);
                            c += node.toHtml();
                            sb.append(c);
                            lastNodeEnd = e;

                            IFileService fileService = SpringUtil.getBean(IFileService.class);
                            fileService.upload(Global.getRealPath() + filePath + "/" + diskName, filePath, diskName);

                            File f = new File(Global.getRealPath() + filePath + "/" + diskName);
                            long size = f.length();
                            Attachment att = new Attachment();
                            att.setDocId(docId);
                            att.setName(diskName);
                            att.setDiskName(diskName);
                            att.setVisualPath(filePath);
                            att.setSize(size);
                            att.setPageNum(pageNum);
                            att.setOrders(0);
                            att.setExt(ext);
                            att.setUploadDate(new java.util.Date());
                            att.setEmbedded(true);
                            att.create();
                        }
                    }
                    else {
                        int e = node.getEndPosition();
                        String c = contentTmp.substring(lastNodeEnd, e);
                        sb.append(c);
                        lastNodeEnd = e;
                    }
                }
                sb.append(StringUtils.substring(contentTmp, lastNodeEnd));
                content = sb.toString();
            }

        } catch (ParserException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return content;
    }

    public static boolean generateImage(String imgStr, String path) {
        if (imgStr == null) {
            return false;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // 解密
            byte[] b = decoder.decodeBuffer(imgStr);
            // 处理数据
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            OutputStream out = new FileOutputStream(path);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            LogUtil.getLog(DocContent.class).error(e);
            return false;
        }
    }

    public int getDocId() {
        return docId;
    }

    public int getPageNum() {
        return pageNum;
    }

    /**
     * 取得页数
     * @param doc_id int
     * @return int -1 表示sql语句不合法
     */
    public static int getContentCount(int doc_id) {
        DocContentCacheMgr dcm = new DocContentCacheMgr();
        return dcm.getContentCount(doc_id);
    }

    public boolean create(ServletContext application,
                          CMSMultiFileUploadBean mfu, int doc_id,
                          String content, int pageNum) throws
            ErrMsgException {
        String sql;
        String visualPath = StrUtil.getNullString(mfu.getFieldValue("filepath"));
        Conn conn = new Conn(connname);
        boolean re = true;
        try {
            // 处理附件
            int ret = mfu.getRet();
            // 如果上传成功
            if (ret == FileUpload.RET_SUCCESS) {
                // 处理HTMLCODE中的文件
                Vector files = mfu.getFiles();
                IFileService fileService = SpringUtil.getBean(IFileService.class);

                java.util.Enumeration e = files.elements();
                while (e.hasMoreElements()) {
                    FileInfo fi = (FileInfo) e.nextElement();
                    fileService.write(fi, visualPath, false);
                    sql =
                            "insert into cms_images (id,path,mainkey,kind,subkey,ext) values (" +
                            SequenceManager.nextID(SequenceManager.CMS_IMAGES) + "," + StrUtil.sqlstr(visualPath + "/" + fi.getName()) + "," + docId +
                            ",'document'," + this.pageNum + "," + StrUtil.sqlstr(StrUtil.getFileExt(fi.getName())) + ")";
                    conn.executeUpdate(sql);
                }
            } else {
                throw new ErrMsgException("上传失败！ret=" + ret);
            }

            re = createWithoutFile(application, mfu, doc_id, content, pageNum);
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

    private Vector<Attachment> attachments = new Vector<>();

    public Vector getTitleImages() {
        return titleImages;
    }

    private Vector<Attachment> titleImages = new Vector<>();

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
        String sql = "update document_attach set name=? where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newname);
            pstmt.setInt(2, attachId);
            re = conn.executePreUpdate() == 1;
            if (re) {
                // 更新缓存
                DocContentCacheMgr dcm = new DocContentCacheMgr();
                dcm.refreshUpdate(docId, pageNum);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    public int getAttachmentMaxOrders(boolean isTitleImage) {
        String GETMAXORDERS = "select max(orders) from document_attach where doc_id=? and page_num=? and is_title_image=?";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        int maxorders = -1;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(GETMAXORDERS);
            pstmt.setInt(1, docId);
            pstmt.setInt(2, pageNum);
            pstmt.setInt(3, isTitleImage?1:0);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getMaxOrders:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return maxorders;
    }

    public boolean moveAttachment(int attachId, String direction) {
        Attachment attach = new Attachment(attachId);
        boolean re = false;
        int orders = attach.getOrders();
        if ("up".equals(direction)) {
            if (orders == 1)
                return true;
            else {
                Attachment upperAttach = new Attachment(orders - 1, docId, pageNum);
                if (upperAttach.isLoaded()) {
                    upperAttach.setOrders(orders);
                    upperAttach.save();
                }
                attach.setOrders(orders - 1);
                re = attach.save();
            }
        } else {
            int maxorders = getAttachmentMaxOrders(attach.isTitleImage());
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
    
    /**
     * 此方法用于采集
     * @param doc_id
     * @param content
     * @param pageNum
     * @return
     */
    public boolean create(int doc_id,
                          String content, int pageNum) {
        this.docId = doc_id;
        this.content = content;

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
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }    
    

    /**
     * 此方法用于采集
     * @return
     */
    public boolean save() {
        String sql = "update doc_content set content=? where doc_id=? and page_num=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setInt(2, docId);
            pstmt.setInt(3, pageNum);
            re = conn.executePreUpdate() == 1;
            if (re) {
                // 更新缓存
                DocContentCacheMgr dcm = new DocContentCacheMgr();
                dcm.refreshUpdate(docId, pageNum);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }
    
    public boolean isLoaded() {
    	return loaded;
    }
    
    private boolean loaded = false;

}
