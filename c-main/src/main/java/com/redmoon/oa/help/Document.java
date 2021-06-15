package com.redmoon.oa.help;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

import com.cloudwebsoft.framework.util.*;
import com.redmoon.kit.util.*;
import com.redmoon.oa.db.*;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.*;
import com.redmoon.oa.pvg.*;
import com.redmoon.oa.ui.*;
import org.apache.log4j.*;
import org.htmlparser.*;
import org.htmlparser.filters.*;
import org.htmlparser.nodes.*;
import org.htmlparser.tags.*;
import org.htmlparser.util.*;

import com.redmoon.oa.fileark.plugin.base.IPluginDocument;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Document implements java.io.Serializable, ITagSupport, IDesktopUnit, IPluginDocument {
    public static final int NOTEMPLATE = -1;
    String connname = "";
    int id = -1;
    String title;
    String content;
    String date;
    String class1;
    java.util.Date modifiedDate;
    String summary;
    boolean isHome = false;
    int examine = 0;

    public static final int EXAMINE_NOT = 0; // 未审核
    public static final int EXAMINE_NOTPASS = 1; // 未通过
    public static final int EXAMINE_PASS = 2; //　审核通过

    public static final int EXAMINE_DUSTBIN = 10; // 删除至回收站
    
    public static final int LEVEL_TOP = 100;
    
    public static final int TYPE_DOC = 0;
    public static final int TYPE_VOTE = 1;

    private static  int IS_JACOB_SUPPORTED = -1;

    transient Logger logger = Logger.getLogger(Document.class.getName());

    private static final String INSERT_DOCUMENT =
            "INSERT into help_document (id, title, class1, type, voteoption, voteresult, nick, keywords, isrelateshow, can_comment, hit, template_id, parent_code, examine, isNew, author, flowTypeCode, color, isBold, expire_date,modifiedDate,createDate, doc_level, kind) VALUES (?,?,?,?,?,?,?,?,?,?,0,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String LOAD_DOCUMENT =
            "SELECT title, class1, modifiedDate, can_comment,summary,ishome,type,voteOption,voteResult,examine,nick,keywords,isrelateshow,hit,template_id,page_count,parent_code,isNew,author,flowTypeCode,color,isBold,expire_date,orgAddr,createDate,doc_level,kind FROM help_document WHERE id=?";

    private static final String DEL_DOCUMENT =
            "delete FROM help_document WHERE id=?";
    private static final String SAVE_DOCUMENT =
            "UPDATE help_document SET title=?, can_comment=?, ishome=?, modifiedDate=?,examine=?,keywords=?,isrelateshow=?,template_id=?,class1=?,isNew=?,author=?,flowTypeCode=?,parent_code=?,color=?,isBold=?,expire_date=?,doc_level=?,kind=? WHERE id=?";
    private static final String SAVE_SUMMARY =
            "UPDATE help_document SET summary=? WHERE id=?";
    private static final String SAVE_HIT =
            "UPDATE help_document SET hit=? WHERE id=?";

    public Document() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Document:默认数据库名为空！");
    }

    /**
     * 从数据库中取出数据
     * @param id int
     * @throws ErrMsgException
     */
    public Document(int id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Document:默认数据库名为空！");
        this.id = id;
        loadFromDB();
    }

    public void renew() {
        if (logger==null)
            logger = Logger.getLogger(Document.class.getName());
    }

    /**
     * 当directory结点的类型为文章时，根据code的值取得文章ID，如果文章不存，则创建文章
     * @param code String
     */
    public int getIDOrCreateByCode(String code, String nick) {
        int myid = getFirstIDByCode(code);
        if (myid != -1) {
            this.id = myid;
            loadFromDB();
        } else { // 文章不存在
            content = " "; // TEXT 类型字段，必须加一空格符，否则在读出时会出错
            Leaf leaf = new Leaf();
            leaf = leaf.getLeaf(code);
            title = leaf.getName();
            create(code, title, content, 0, "", "", nick, leaf.getTemplateId(), nick);
            this.id = getFirstIDByCode(code);
            // 更改目录中的doc_id
            //logger.info("id=" + id);
            leaf.setDocID(id);
            leaf.update();
        }
        return id;
    }

    public void delDocumentByDirCode(String code) throws ErrMsgException {
        Vector v = getDocumentsByDirCode(code);
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            Document doc = (Document) ir.next();
            doc.del();
        }
    }

    public Vector getDocumentsByDirCode(String code) {
        Vector v = new Vector();
        String sql = "select id from help_document where class1=" +
                     StrUtil.sqlstr(code);
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sql);
            if (rs != null) {
               while (rs.next()) {
                   v.addElement(getDocument(rs.getInt(1)));
               }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     * 当确定dirCode为文章型结点时，取得其对应的文章，但是当节点对应的文章还没创建时，会出错
     * @param dirCode String
     * @return Document
     */
    public Document getDocumentByDirCode(String dirCode) {
        Leaf leaf = new Leaf();
        leaf = leaf.getLeaf(dirCode);
        //logger.info("dirCode=" + dirCode);

        if (leaf != null && leaf.isLoaded() &&
            leaf.getType() == leaf.TYPE_DOCUMENT) {
            int id = leaf.getDocID();
            return getDocument(id);
        } else
            return null; // throw new ErrMsgException("该结点不是文章型节点或者文章尚未创建！");

    }

    /**
     * 取得结点为code的文章的ID，只取第一个
     * @param code String
     * @return int -1未取得
     */
    public int getFirstIDByCode(String code) {
        String sql = "select id from help_document where class1=" +
                     StrUtil.sqlstr(code);
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sql);
            if (rs != null && rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return -1;
    }

    public String getSummary() {
        return this.summary;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public boolean getIsHome() {
        return this.isHome;
    }

    public void setIsHome(boolean h) {
        this.isHome = h;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setVoteOption(String voteOption) {
        this.voteOption = voteOption;
    }

    public void setVoteResult(String voteResult) {
        this.voteResult = voteResult;
    }

    public String getContent(int pageNum) {
        DocContent dc = new DocContent();
        dc = dc.getDocContent(id, pageNum);
        if (dc!=null)
            return dc.getContent();
        else
            return null;
    }

    public String getPageList(HttpServletRequest request, UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList();
        return url + StrUtil.UrlEncode(uds.getModuleItem());
    }

    public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
        Privilege privilege = new Privilege();
        String dir_code = uds.getModuleItem();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dir_code);
        if (lf==null)
            return "";
        if (!dir_code.equals("")) {
            LeafPriv lp = new LeafPriv(dir_code);
            // if (!lp.canUserSee(privilege.getUser(request))) {
            if (!lp.canUserSee(request)) {
                return SkinUtil.LoadString(request, "pvg_invalid");
            }
        }
        int count = uds.getCount();
        String sql = "select id from help_document where class1=" + StrUtil.sqlstr(dir_code) + " and examine=" + Document.EXAMINE_PASS + " order by modifiedDate desc";
        Iterator ir = list(sql, count).iterator();
        String str = "<ul>";
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageShow();
        while (ir.hasNext()) {
            Document doc = (Document) ir.next();

            String t = StrUtil.getLeft(doc.getTitle(), uds.getWordCount());

            if (DateUtil.compare(new java.util.Date(), doc.getExpireDate()) == 2) {
                str += "<li><a href='" + url + "?id=" + doc.getID() + "'>";
                if (doc.isBold())
                    str += "<B>";
                if (!doc.getColor().equals("")) {
                    str += "<font color='" + doc.getColor() + "'>";
                }

                str += StrUtil.toHtml(t);
                if (!doc.getColor().equals(""))
                    str += "</font>";
                if (doc.isBold())
                    str += "</B>";
                str += "  [" + DateUtil.format(doc.getModifiedDate(),"MM-dd") + "]</a></li>";
            } else {
                str += "<li><a href='" + url + "?id=" + doc.getID() + "'>" +
                        StrUtil.toHtml(t) + "  [" +
                        DateUtil.format(doc.getModifiedDate(),"MM-dd") + "]</a></li>";
            }
        }
        str += "</ul>";
        return str;
    }

    /**
     * 获取前count个数据
     * @param sql String
     * @param count int
     * @return Vector
     */
    public Vector list(String sql, int count) {
        ResultSet rs = null;
        Vector result = new Vector();
        Conn conn = new Conn(connname);
        try {
            conn.setMaxRows(count); // 尽量减少内存的使用
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return result;
            } else {
                // defines the number of rows that will be read from the database when the ResultSet needs more rows
                rs.setFetchSize(count); // rs一次从POOL中所获取的记录数
                while (rs.next()) {
                    int id = rs.getInt(1);
                    Document doc = getDocument(id);
                    result.addElement(doc);
                }
            }
        } catch (SQLException e) {
            logger.error("list: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    public String RenderContent(HttpServletRequest request, int pageNum) {
        DocContent dc = new DocContent();
        dc = dc.getDocContent(id, pageNum);
        if (dc != null) {
            return dc.getContent();
        } else
            return null;
    }

    public DocContent getDocContent(int pageNum) {
        DocContent dc = new DocContent();
        return dc.getDocContent(id, pageNum);
    }

    public boolean isCanComment() {
        return canComment;
    }

    public String getDirCode() {
        return class1;
    }

    public synchronized boolean UpdateWithoutFile(ServletContext application,CMSMultiFileUploadBean mfu) throws
            ErrMsgException {
        //取得表单中域的信息
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));
        author = StrUtil.getNullString(mfu.getFieldValue("author"));
        title = StrUtil.getNullString(mfu.getFieldValue("title"));
        //logger.info("FilePath=" + FilePath);
        String strCanComment = StrUtil.getNullStr(mfu.getFieldValue(
                "canComment"));
        if (strCanComment.equals(""))
            canComment = false;
        else if (strCanComment.equals("1"))
            canComment = true;
        String strIsHome = StrUtil.getNullString(mfu.getFieldValue("isHome"));
        if (strIsHome.equals(""))
            isHome = false;
        else if (strIsHome.equals("false"))
            isHome = false;
        else if (strIsHome.equals("true"))
            isHome = true;
        else
            isHome = false;
        String strexamine = mfu.getFieldValue("examine");
        int oldexamine = examine;
        examine = Integer.parseInt(strexamine);
        String strisnew = StrUtil.getNullStr(mfu.getFieldValue("isNew"));
        if (StrUtil.isNumeric(strisnew))
            isNew = Integer.parseInt(strisnew);
        else
            isNew = 0;

        keywords = StrUtil.getNullStr(mfu.getFieldValue("keywords"));
        String strisRelateShow = StrUtil.getNullStr(mfu.getFieldValue("isRelateShow"));
        int intisRelateShow = 0;
        if (StrUtil.isNumeric(strisRelateShow)) {
            intisRelateShow = Integer.parseInt(strisRelateShow);
            if (intisRelateShow==1)
                isRelateShow = true;
        }

        flowTypeCode = StrUtil.getNullString(mfu.getFieldValue("flowTypeCode"));
        if (!dir_code.equals(class1)) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(dir_code);
            parentCode = lf.getParentCode();
        }
        color = StrUtil.getNullStr(mfu.getFieldValue("color"));
        bold = StrUtil.getNullStr(mfu.getFieldValue("isBold")).equals("true");
        expireDate = DateUtil.parse(mfu.getFieldValue("expireDate"), "yyyy-MM-dd");

        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            //更新文件内容
            pstmt = conn.prepareStatement(SAVE_DOCUMENT);
            pstmt.setString(1, title);
            pstmt.setInt(2, canComment ? 1 : 0);
            pstmt.setBoolean(3, isHome);
            pstmt.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
            pstmt.setInt(5, examine);
            pstmt.setString(6, keywords);
            pstmt.setInt(7, intisRelateShow);
            pstmt.setInt(8, templateId);
            pstmt.setString(9, dir_code);
            pstmt.setInt(10, isNew);
            pstmt.setString(11, author);
            pstmt.setString(12, flowTypeCode);
            pstmt.setString(13, parentCode);
            pstmt.setString(14, color);
            pstmt.setInt(15, bold?1:0);
            if (expireDate == null)
                pstmt.setTimestamp(16, null);
            else
                pstmt.setTimestamp(16, new Timestamp(expireDate.getTime()));
            pstmt.setInt(17, level);
            pstmt.setString(18, kind);
            pstmt.setInt(19, id);
            conn.executePreUpdate();
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            if (oldexamine==examine) {
                dcm.refreshUpdate(id);
            }
            else {
                dcm.refreshUpdate(id, class1, parentCode);
            }

            // 如果是更改了类别
            if (!dir_code.equals(class1)) {
                dcm.refreshChangeDirCode(class1, dir_code);
                class1 = dir_code;
            }

            // 更新内容
            DocContent dc = new DocContent();
            dc = dc.getDocContent(id, 1);
            dc.saveWithoutFile(application, mfu);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("服务器内部错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public synchronized boolean UpdateIsHome(boolean isHome) throws
            ErrMsgException {
        String sql = "update help_document set isHome=? where id=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            //更新文件内容
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, isHome ? 1 : 0);
            pstmt.setInt(2, id);
            conn.executePreUpdate();
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("服务器内部错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public synchronized boolean increaseHit() throws
            ErrMsgException {
        hit ++;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            //更新文件内容
            pstmt = conn.prepareStatement(SAVE_HIT);
            pstmt.setInt(1, hit);
            pstmt.setInt(2, id);
            conn.executePreUpdate();
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("服务器内部错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public synchronized boolean UpdateSummaryWithoutFile(ServletContext application,
                                                CMSMultiFileUploadBean mfu) throws
            ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = true;
        try {
            //取得表单中域的信息
            String idstr = StrUtil.getNullString(mfu.getFieldValue("id"));
            if (!StrUtil.isNumeric(idstr))
                throw new ErrMsgException("标识id=" + idstr + "非法，必须为数字！");
            id = Integer.parseInt(idstr);
            summary = StrUtil.getNullString(mfu.getFieldValue("htmlcode"));

            PreparedStatement pstmt = null; //更新文件内容
            pstmt = conn.prepareStatement(SAVE_SUMMARY);
            pstmt.setString(1, summary);
            pstmt.setInt(2, id);
            re = conn.executePreUpdate()==1?true:false;
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id);
        } catch (SQLException e) {
            re = false;
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public synchronized boolean UpdateSummary(ServletContext application, CMSMultiFileUploadBean mfu) throws
            ErrMsgException {
        String isuploadfile = StrUtil.getNullString(mfu.getFieldValue(
                "isuploadfile"));
        // logger.info("filepath=" + mfu.getFieldValue("filepath"));
        if (isuploadfile.equals("false"))
            return UpdateSummaryWithoutFile(application, mfu);

        String FilePath = StrUtil.getNullString(mfu.getFieldValue("filepath"));
        //String tempAttachFilePath = application.getRealPath("/") + FilePath + "/";
        String tempAttachFilePath = Global.getRealPath() + FilePath + "/";
        mfu.setSavePath(tempAttachFilePath); //取得目录
        File f = new File(tempAttachFilePath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }

        boolean re = false;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            // 删除图像文件
            String sql = "select path from cms_images where mainkey=" + id +
                         " and kind='document' and subkey=" + 0 + "";

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
            //从数据库中删除图像
            sql = "delete from cms_images where mainkey=" + id +
                  " and kind='document' and subkey=" + 0 + "";
            conn.executeUpdate(sql);

            // 处理图片
            int ret = mfu.getRet();
            if (ret == 1) {
                mfu.writeFile(false);
                Vector files = mfu.getFiles();
                // logger.info("files size=" + files.size());
                java.util.Enumeration e = files.elements();
                String filepath = "";
                sql = "";
                while (e.hasMoreElements()) {
                    FileInfo fi = (FileInfo) e.nextElement();
                    filepath = FilePath + "/" + fi.getName();
                    sql = "insert into cms_images (path,mainkey,kind,subkey) values (" +
                            StrUtil.sqlstr(filepath) + "," + id +
                            ",'document'," + 0 + ")";
                    conn.executeUpdate(sql);
                }
            } else
                throw new ErrMsgException("上传失败！ret=" + ret);
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {e.printStackTrace();}
                rs = null;
            }

            re = UpdateSummaryWithoutFile(application, mfu);
        } catch (Exception e) {
            logger.error("UpdateSummary:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return re;
    }

    public boolean updateOrgAddr() {
        String sql = "update help_document set orgAddr=? where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orgAddr);
            pstmt.setInt(2, id);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                // 更新缓存
                DocCacheMgr dcm = new DocCacheMgr();
                dcm.refreshUpdate(id);
            }
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

    public synchronized boolean updateTemplateId() {
        String sql = "update help_document set template_id=? where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, templateId);
            pstmt.setInt(2, id);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                // 更新缓存
                DocCacheMgr dcm = new DocCacheMgr();
                dcm.refreshUpdate(id);
            }
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

    public synchronized boolean Update(ServletContext application,
                                       CMSMultiFileUploadBean mfu) throws
            ErrMsgException {
        String isuploadfile = StrUtil.getNullString(mfu.getFieldValue(
                "isuploadfile"));
        // logger.info("filepath=" + mfu.getFieldValue("filepath"));
        if (isuploadfile.equals("false"))
            return UpdateWithoutFile(application, mfu);

        // 取得表单中域的信息
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));
        author = StrUtil.getNullString(mfu.getFieldValue("author"));
        title = StrUtil.getNullString(mfu.getFieldValue("title"));
        String strIsHome = StrUtil.getNullString(mfu.getFieldValue("isHome"));
        if (strIsHome.equals(""))
            isHome = false;
        else if (strIsHome.equals("false"))
            isHome = false;
        else if (strIsHome.equals("true"))
            isHome = true;
        else
            isHome = false;
        String strexamine = mfu.getFieldValue("examine");
        int oldexamine = examine;
        examine = Integer.parseInt(strexamine);
        keywords = StrUtil.getNullStr(mfu.getFieldValue("keywords"));
        String strisRelateShow = StrUtil.getNullStr(mfu.getFieldValue("isRelateShow"));
        int intisRelateShow = 0;
        if (StrUtil.isNumeric(strisRelateShow)) {
            intisRelateShow = Integer.parseInt(strisRelateShow);
            if (intisRelateShow==1)
                isRelateShow = true;
        }

        String strisnew = StrUtil.getNullStr(mfu.getFieldValue("isNew"));
        if (StrUtil.isNumeric(strisnew))
            isNew = Integer.parseInt(strisnew);
        else
            isNew = 0;
        String strCanComment = StrUtil.getNullStr(mfu.getFieldValue(
                "canComment"));
        if (strCanComment.equals(""))
            canComment = false;
        else if (strCanComment.equals("1"))
            canComment = true;

        flowTypeCode = StrUtil.getNullStr(mfu.getFieldValue("flowTypeCode"));

        color = StrUtil.getNullStr(mfu.getFieldValue("color"));
        bold = StrUtil.getNullStr(mfu.getFieldValue("isBold")).equals("true");
        expireDate = DateUtil.parse(mfu.getFieldValue("expireDate"), "yyyy-MM-dd");

        if (!dir_code.equals(class1)) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(dir_code);
            parentCode = lf.getParentCode();
        }
        
        level = StrUtil.toInt(mfu.getFieldValue("level"), 0);
        kind = StrUtil.getNullStr(mfu.getFieldValue("kind"));

        Conn conn = new Conn(connname);
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            // 更新文件内容
            pstmt = conn.prepareStatement(SAVE_DOCUMENT);
            pstmt.setString(1, title);
            pstmt.setInt(2, canComment ? 1 : 0);
            pstmt.setBoolean(3, isHome);
            pstmt.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
            pstmt.setInt(5, examine);
            pstmt.setString(6, keywords);
            pstmt.setInt(7, intisRelateShow);
            pstmt.setInt(8, templateId);
            pstmt.setString(9, dir_code);
            pstmt.setInt(10, isNew);
            pstmt.setString(11, author);
            pstmt.setString(12, flowTypeCode);
            pstmt.setString(13, parentCode);
            pstmt.setString(14, color);
            pstmt.setInt(15, bold?1:0);
            if (expireDate==null)
                pstmt.setTimestamp(16, null);
            else
                pstmt.setTimestamp(16, new Timestamp(expireDate.getTime()));
            pstmt.setInt(17, level);
            pstmt.setString(18, kind);
            pstmt.setInt(19, id);
            conn.executePreUpdate();
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            if (oldexamine==examine) {
                dcm.refreshUpdate(id);
            }
            else {
                dcm.refreshUpdate(id, class1, parentCode);
            }
            // 如果是更改了类别
            if (!dir_code.equals(class1)) {
                dcm.refreshChangeDirCode(class1, dir_code);
                class1 = dir_code;
            }

            // 更新第一页的内容
            DocContent dc = new DocContent();
            dc = dc.getDocContent(id, 1);
            dc.save(application, mfu);
            
            // 将doc的id与上传的临时图片文件相关联，当fwebedit_new.jsp方式上传时，才会可能有临时图片文件
            String[] tmpAttachIds = mfu.getFieldValues("tmpAttachId");
            if (tmpAttachIds != null) {
                int len = tmpAttachIds.length;
                for (int k = 0; k < len; k++) {
                    Attachment att = new Attachment(Integer.parseInt(tmpAttachIds[
                            k]));
                    att.setDocId(id);
                    att.setPageNum(1);
                    att.save();
                }
            }            
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("服务器内部错！");
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
        return true;
    }

    public boolean create(ServletContext application,
                          CMSMultiFileUploadBean mfu, String nick) throws
            ErrMsgException {
        String isuploadfile = StrUtil.getNullString(mfu.getFieldValue(
                "isuploadfile"));

        // 取得表单中域的信息
        author = StrUtil.getNullString(mfu.getFieldValue("author"));
        title = StrUtil.getNullString(mfu.getFieldValue("title"));
        content = StrUtil.getNullString(mfu.getFieldValue("htmlcode"));
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));
        keywords = StrUtil.getNullStr(mfu.getFieldValue("keywords"));
        String strisRelateShow = StrUtil.getNullStr(mfu.getFieldValue("isRelateShow"));
        int intisRelateShow = 0;
        if (StrUtil.isNumeric(strisRelateShow)) {
            intisRelateShow = Integer.parseInt(strisRelateShow);
            if (intisRelateShow==1)
                isRelateShow = true;
        }

        String strexamine = StrUtil.getNullStr(mfu.getFieldValue("examine"));
        if (StrUtil.isNumeric(strexamine)) {
            examine = Integer.parseInt(strexamine);
        }
        else
            examine = 0;

        String strisnew = StrUtil.getNullStr(mfu.getFieldValue("isNew"));
        if (StrUtil.isNumeric(strisnew))
            isNew = Integer.parseInt(strisnew);
        else
            isNew = 0;

        flowTypeCode = StrUtil.getNullStr(mfu.getFieldValue("flowTypeCode"));

        // 检查目录节点中是否允许插入文章
        Directory dir = new Directory();
        Leaf lf = dir.getLeaf(dir_code);
        if (lf==null || !lf.isLoaded()) {
            throw new ErrMsgException("节点：" + dir_code + "不存在！");
        }
        if (lf.getType() == 0)
            throw new ErrMsgException("对不起，该目录不包含具体内容，请选择正确的目录项！");
        if (lf.getType() == 1) {
            if (getFirstIDByCode(dir_code) != -1)
                throw new ErrMsgException("该目录节点为文章节点，且文章已经被创建！");
        }

        String strCanComment = StrUtil.getNullStr(mfu.getFieldValue(
                "canComment"));
        if (strCanComment.equals(""))
            canComment = false;
        else if (strCanComment.equals("1"))
            canComment = true;
        //logger.info("strCanComment=" + strCanComment);
        String strtid = StrUtil.getNullStr(mfu.getFieldValue("templateId"));
        if (StrUtil.isNumeric(strtid))
            templateId = Integer.parseInt(strtid);

        color = StrUtil.getNullStr(mfu.getFieldValue("color"));
        bold = StrUtil.getNullStr(mfu.getFieldValue("isBold")).equals("true");
        expireDate = DateUtil.parse(mfu.getFieldValue("expireDate"), "yyyy-MM-dd");

        level = StrUtil.toInt(mfu.getFieldValue("level"), 0);
        
        kind = StrUtil.getNullStr(mfu.getFieldValue("kind"));
        
        this.id = (int) SequenceManager.nextID(SequenceManager.OA_DOCUMENT_CMS);

        // 投票处理
        String isvote = mfu.getFieldValue("isvote");
        String[] voptions = null;
        type = TYPE_DOC; // 类型1表示为投票
        String voteresult = "", votestr = "";
        if (isvote != null && isvote.equals("1")) {
            type = TYPE_VOTE;
        }

        // 清缓存
        DocCacheMgr dcm = new DocCacheMgr();
        dcm.refreshCreate(dir_code, lf.getParentCode());

        // 如果不上传文件
        if (isuploadfile.equals("false"))
            return create(dir_code, title, content, type, votestr, voteresult,
                          nick, templateId, author);

        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            // 插入文章标题及相关设置
            parentCode = lf.getParentCode();
            pstmt = conn.prepareStatement(INSERT_DOCUMENT);
            pstmt.setInt(1, id);
            pstmt.setString(2, title);
            pstmt.setString(3, dir_code);
            pstmt.setInt(4, type);
            pstmt.setString(5, votestr);
            pstmt.setString(6, voteresult);
            pstmt.setString(7, nick);
            pstmt.setString(8, keywords);
            pstmt.setInt(9, intisRelateShow);
            pstmt.setInt(10, canComment?1:0);
            pstmt.setInt(11, templateId);
            pstmt.setString(12, parentCode);
            pstmt.setInt(13, examine);
            pstmt.setInt(14, isNew);
            pstmt.setString(15, author);
            pstmt.setString(16, flowTypeCode);
            pstmt.setString(17, color);
            pstmt.setInt(18, bold?1:0);
            if (expireDate==null)
                pstmt.setTimestamp(19, null);
            else
                pstmt.setTimestamp(19, new Timestamp(expireDate.getTime()));
            java.util.Date d = new java.util.Date();
            pstmt.setTimestamp(20, new Timestamp(d.getTime()));
            pstmt.setTimestamp(21, new Timestamp(d.getTime()));
            pstmt.setInt(22, level);
            pstmt.setString(23, kind);
            conn.executePreUpdate();

            pstmt.close();
            pstmt = null;

            // 插入文章中的内容
            DocContent dc = new DocContent();
            dc.create(application, mfu, id, content, 1);
            
            // 将doc的id与上传的临时图片文件相关联，当fckwebedit_new.jsp方式上传时，才会可能有临时图片文件
            String[] tmpAttachIds = mfu.getFieldValues("tmpAttachId");
            if (tmpAttachIds != null) {
                int len = tmpAttachIds.length;
                for (int k = 0; k < len; k++) {
                    Attachment att = new Attachment(Integer.parseInt(tmpAttachIds[
                            k]));
                    att.setDocId(id);
                    att.setPageNum(1);
                    att.save();
                }
            }            
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("服务器内部错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return true;
    }

    /**
     * 用以在不上传图片时直接创建文件
     * @param application ServletContext
     * @param request HttpServletRequest
     * @throws ErrMsgException
     */
    public boolean create(String code_class1, String title, String content,
                          int type, String voteoption, String voteresult,
                          String nick, int templateId, String author) {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(code_class1);
        parentCode = lf.getParentCode();
        Conn conn = new Conn(connname);
        if (id==-1)
            id = (int) SequenceManager.nextID(SequenceManager.OA_DOCUMENT_CMS);
        try {
            // 插入文章标题及相关设置
            PreparedStatement pstmt = conn.prepareStatement(INSERT_DOCUMENT);
            pstmt.setInt(1, id);
            pstmt.setString(2, title);
            pstmt.setString(3, code_class1);
            pstmt.setInt(4, type);
            pstmt.setString(5, voteoption);
            pstmt.setString(6, voteresult);
            pstmt.setString(7, nick);
            pstmt.setString(8, "");
            pstmt.setInt(9, 1);
            pstmt.setInt(10, canComment?1:0);
            pstmt.setInt(11, templateId);
            pstmt.setString(12, parentCode);
            pstmt.setInt(13, examine);
            pstmt.setInt(14, isNew);
            pstmt.setString(15, author);
            pstmt.setString(16, flowTypeCode);
            pstmt.setString(17, color);
            pstmt.setInt(18, bold?1:0);
            if (expireDate==null)
                pstmt.setTimestamp(19, null);
            else
                pstmt.setTimestamp(19, new Timestamp(expireDate.getTime()));
            java.util.Date d = new java.util.Date();
            pstmt.setTimestamp(20, new Timestamp(d.getTime()));
            pstmt.setTimestamp(21, new Timestamp(d.getTime()));
            pstmt.setInt(22, level);
            pstmt.setString(23, kind);
            conn.executePreUpdate();

            // 插入文章中的内容
            DocContent dc = new DocContent();
            dc.create(id, content);

            // 清缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshCreate(code_class1, lf.getParentCode());
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public String get(String field) {
        if (field.equals("title"))
            return getTitle();
        else if (field.equals("content"))
            return getContent(1);
        else if (field.equals("summary"))
            return getSummary();
        else if (field.equals("id"))
            return "" + getID();
        else
            return "";
    }

    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            // 删除文章中的页
            String sql = "select page_num from help_doc_content where doc_id=" + id;
            rs = conn.executeQuery(sql);
            if (rs!=null) {
                while (rs.next()) {
                    int pn = rs.getInt(1);
                    DocContent dc = new DocContent();
                    dc = dc.getDocContent(id, pn);
                    dc.del();
                }
            }
            if (rs!=null) {
                rs.close();
                rs = null;
            }
            PreparedStatement pstmt = conn.prepareStatement(DEL_DOCUMENT);
            pstmt.setInt(1, id);
            conn.executePreUpdate();

            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshDel(id, class1, parentCode);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    private void loadFromDB() {
        // Based on the id in the object, get the message data from the database.
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(LOAD_DOCUMENT);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (!rs.next()) {
                logger.error("文档 " + id +
                             " 在数据库中未找到.");
            } else {
                this.title = rs.getString(1);
                this.class1 = rs.getString(2);
                this.modifiedDate = rs.getTimestamp(3);
                this.canComment = rs.getBoolean(4);
                this.summary = rs.getString(5);
                this.isHome = rs.getBoolean(6);
                this.type = rs.getInt(7);
                this.voteOption = rs.getString(8);
                this.voteResult = rs.getString(9);
                this.examine = rs.getInt(10);
                this.nick = rs.getString(11);
                this.keywords = rs.getString(12);
                this.isRelateShow = rs.getInt(13)==1?true:false;
                this.hit = rs.getInt(14);
                this.templateId = rs.getInt(15);
                this.pageCount = rs.getInt(16);
                this.parentCode = rs.getString(17);
                this.isNew = rs.getInt(18);
                this.author = rs.getString(19);
                this.flowTypeCode = rs.getString(20);
                this.color = rs.getString(21);
                this.bold = rs.getInt(22)==1;
                this.expireDate = rs.getTimestamp(23);
                orgAddr = StrUtil.getNullStr(rs.getString(24));
                createDate = rs.getTimestamp(25);
                level = rs.getInt(26);
                kind = StrUtil.getNullStr(rs.getString(27));
                loaded = true; // 已初始化
            }
        } catch (SQLException e) {
            logger.error("loadFromDB:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public String toString() {
        return "Help document " + id + ":" + title;
    }

    private boolean canComment = true;

    public boolean getCanComment() {
        return canComment;
    }

    public int getType() {
        return type;
    }

    public String getVoteOption() {
        return voteOption;
    }

    public int getExamine() {
        return this.examine;
    }

    public void setExamine(int e) {
        this.examine = e;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public void setIsRelateShow(boolean isRelateShow) {
        this.isRelateShow = isRelateShow;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public void setIsNew(int isNew) {
        this.isNew = isNew;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setFlowTypeCode(String flowTypeCode) {
        this.flowTypeCode = flowTypeCode;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public void setExpireDate(java.util.Date expireDate) {
        this.expireDate = expireDate;
    }

    public void setOrgAddr(String orgAddr) {
        this.orgAddr = orgAddr;
    }

    public void setCreateDate(java.util.Date createDate) {
        this.createDate = createDate;
    }

    public String getVoteResult() {
        return voteResult;
    }

    public String getNick() {
        return nick;
    }

    public java.util.Date getModDate() {
        return modifiedDate;
    }

    public java.util.Date getModifiedDate() {
        return modifiedDate;
    }

    public boolean vote(int id, int votesel) throws
            ErrMsgException {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            String[] rlt = voteResult.split("\\|");
            int len = rlt.length;
            int[] intre = new int[len];
            for (int i = 0; i < len; i++)
                intre[i] = Integer.parseInt(rlt[i]);
            intre[votesel]++;
            String result = "";
            for (int i = 0; i < len; i++) {
                if (result.equals(""))
                    result = "" + intre[i];
                else
                    result += "|" + intre[i];
            }

            String sql = "update help_document set voteresult=" +
                         StrUtil.sqlstr(result)
                         + " where id=" + id;
            logger.info(sql);
            re = conn.executeUpdate(sql) == 1 ? true : false;
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id);
        } catch (SQLException e) {
            logger.error("vote:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * Returns a block of threadID's from a query and performs transparent
     * caching of those blocks. The two parameters specify a database query
     * and a startIndex for the results in that query.
     *
     * @param query the SQL thread list query to cache blocks from.
     * @param startIndex the startIndex in the list to get a block for.
     */
    protected long[] getDocBlock(String query, String groupKey, int startIndex) {
        DocCacheMgr dcm = new DocCacheMgr();
        return dcm.getDocBlock(query, groupKey, startIndex);
    }

    public DocBlockIterator getDocuments(String query, String groupKey,
                                         int startIndex,
                                         int endIndex) {
        if (!SecurityUtil.isValidSql(query))
            return null;
        // 可能取得的infoBlock中的元素的顺序号小于endIndex
        long[] docBlock = getDocBlock(query, groupKey, startIndex);

        return new DocBlockIterator(docBlock, query, groupKey,
                                    startIndex, endIndex);
    }

    /**
     *
     * @param sql String
     * @return int -1 表示sql语句不合法
     */
    public int getDocCount(String sql) {
        DocCacheMgr dcm = new DocCacheMgr();
        return dcm.getDocCount(sql);
    }

    public Document getDocument(int id) {
        DocCacheMgr dcm = new DocCacheMgr();
        return dcm.getDocument(id);
    }

    private int type = TYPE_DOC;
    private String voteOption;
    private String voteResult;
    private String nick;

    public String getKeywords() {
        return keywords;
    }

    public boolean getIsRelateShow() {
        return isRelateShow;
    }

    private String keywords;
    private boolean isRelateShow;
    private boolean loaded = false;

    public boolean isLoaded() {
        if (id==-1)
            return false;
        return loaded;
    }

    public int getHit() {
        return hit;
    }

    public int getTemplateId() {
        return templateId;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String getParentCode() {
        return parentCode;
    }

    public int getIsNew() {
        return isNew;
    }

    public String getAuthor() {
        return author;
    }

    public String getFlowTypeCode() {
        return flowTypeCode;
    }

    public String getColor() {
        return color;
    }

    public boolean isBold() {
        return bold;
    }

    public java.util.Date getExpireDate() {
        return expireDate;
    }

    public String getOrgAddr() {
        return orgAddr;
    }

    public java.util.Date getCreateDate() {
        return createDate;
    }

    private int hit = 0;
    private int templateId = NOTEMPLATE;

    public boolean AddContentPage(ServletContext application,
                                  CMSMultiFileUploadBean mfu, String content) throws
            ErrMsgException {
        String action = StrUtil.getNullStr(mfu.getFieldValue("action"));
        int afterpage = -1;
        if (action.equals("insertafter")) {
            String insafter = StrUtil.getNullStr(mfu.getFieldValue("afterpage"));
            if (StrUtil.isNumeric(insafter))
                afterpage = Integer.parseInt(insafter);
        }

        int pageNo = 1;
        if (afterpage!=-1)
            pageNo = afterpage + 1;
        else
            pageNo = pageCount + 1;

        String isuploadfile = StrUtil.getNullString(mfu.getFieldValue(
                "isuploadfile"));
        DocContent dc = new DocContent();
        if (isuploadfile.equals("false")) {
            if (dc.createWithoutFile(application, mfu, id, content, pageNo)) {
                pageCount++;
                return UpdatePageCount(pageCount);
            }
        }
        else {
            if (dc.create(application, mfu, id, content, pageNo)) {
                pageCount++;
                return UpdatePageCount(pageCount);
            }
        }
        return false;
    }

    public boolean EditContentPage(ServletContext application,
                                                CMSMultiFileUploadBean mfu) throws ErrMsgException {
        String strpageNum = StrUtil.getNullStr(mfu.getFieldValue("pageNum"));
        int pageNum = Integer.parseInt(strpageNum);

        DocContent dc = new DocContent();
        dc = dc.getDocContent(id, pageNum);
        dc.setContent(content);
        dc.save(application, mfu);

        return true;
    }

    /**
     * 变更目录
     * @param newDirCode
     * @return
     * @throws ErrMsgException
     */
    public synchronized boolean UpdateDir(String newDirCode) throws
            ErrMsgException {
        String sql = "update help_document set class1=? where id=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            //更新文件内容
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newDirCode);
            pstmt.setInt(2, id);
            conn.executePreUpdate();
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }
    
    public synchronized boolean UpdatePageCount(int pagecount) throws
            ErrMsgException {
        String sql = "update help_document set modifiedDate=?,page_count=? where id=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            //更新文件内容
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, new Timestamp(new java.util.Date().getTime()));
            pstmt.setInt(2, pagecount);
            pstmt.setInt(3, id);
            conn.executePreUpdate();
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("服务器内部错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    private int pageCount = 1;
    private String parentCode;

    public Vector getAttachments(int pageNum) {
        DocContent dc = new DocContent();
        dc = dc.getDocContent(id, pageNum);
        if (dc==null)
            return null;
        return dc.getAttachments();
    }

    public Attachment getAttachment(int pageNum, int att_id) {
        Iterator ir = getAttachments(pageNum).iterator();
        while (ir.hasNext()) {
            Attachment at = (Attachment)ir.next();
            if (at.getId()==att_id)
                return at;
        }
        return null;
    }

    public Document getTemplate() {
        if (templateId == this.NOTEMPLATE)
            return null;
        else
            return getDocument(templateId);
    }

    public String getAbstract(HttpServletRequest request, int len, boolean isText) {
        int MAX_LEN2 = 10000;

        String docContent = StrUtil.getLeft(content, len);

        // 对未完成的标签补齐，以免出现<im或<tab这样的标签
        int idx1 = docContent.lastIndexOf('<');
        int idx2 = docContent.lastIndexOf('>');
        // 如果截取时，未取到 > ，则继续往前取，直到取到为止
        // System.out.println("MsgUtil.java getAbstract: idx1=" + idx1 + " idx2=" + idx2);
        if ((idx2 == -1 && idx1 >= 0) || (idx1 > idx2)) {
            String ct3 = content;
            int idx3 = ct3.indexOf('>', idx1);
            if (idx3!=-1) {
                if (idx3 < MAX_LEN2) {
                    docContent = ct3.substring(0, idx3 + 1);
                }
            }
        }

        // 对于ActiveX对象进行预处理
        idx2 = docContent.toLowerCase().lastIndexOf("</object>");
        idx1 = docContent.toLowerCase().lastIndexOf("<object");
        if ((idx2 == -1 && idx1 >= 0) || idx1 > idx2) {
            String ct2 = content.toLowerCase();
            int idx3 = ct2.indexOf("</object>");
            if (idx3 != -1)
                docContent += content.substring(docContent.length(), docContent.length() + idx3 + 9);
            else
                docContent = content.substring(0, idx1);
        }

        String str = "";
        try {
            Parser myParser;
            NodeList nodeList = null;
            myParser = Parser.createParser(docContent, "utf-8");
            NodeFilter textFilter = new NodeClassFilter(TextNode.class);
            NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
            NodeFilter imgFilter = new NodeClassFilter(ImageTag.class);
            // 暂时不处理 meta
            // NodeFilter metaFilter = new NodeClassFilter(MetaTag.class);
            OrFilter lastFilter = new OrFilter();
            lastFilter.setPredicates(new NodeFilter[] {textFilter, linkFilter,
                                     imgFilter});
            nodeList = myParser.parse(lastFilter);
            Node[] nodes = nodeList.toNodeArray();
            for (int i = 0; i < nodes.length; i++) {
                Node anode = (Node) nodes[i];
                String line = "";
                if (anode instanceof TextNode) {
                    TextNode textnode = (TextNode) anode;
                    // line = textnode.toPlainTextString().trim();
                    line = textnode.getText();
                } else if (anode instanceof ImageTag) {
                    if (!isText) {
                        ImageTag imagenode = (ImageTag) anode;
                        String url = imagenode.getImageURL();
                        String ext = StrUtil.getFileExt(url).toLowerCase();
                        // 如果地址完整
                        if (ext.equals("gif") || ext.equals("png") ||
                            ext.equals("jpg") || ext.equals("jpeg") ||
                            ext.equals("bmp")) {
                            // System.out.println("MsgUtil.java getAbstract:" + imagenode.toHtml() + " url=" + imagenode.getImageURL());
                            if (imagenode.getImageURL().startsWith("http"))
                                ; // line = "<div align=center>" + imagenode.toHtml() + "</div>";
                            else if (imagenode.getImageURL().startsWith("/")) {
                                ; //line = "<div align=center>" + imagenode.toHtml() + "</div>";
                            } else { // 相对路径
                                // line = "<div align=center><img src='" + request.getContextPath() + "/forum/" + imagenode.getImageURL() + "'></div>";
                                url = request.getContextPath() + "/forum/" +
                                      imagenode.getImageURL();
                            }
                            line =
                                    "<div align=center><a onfocus=this.blur() href=\"" +
                                    url + "\" target=_blank><IMG SRC=\"" + url +
                                    "\" border=0 alt=" +
                                    SkinUtil.LoadString(request,
                                    "res.cn.js.fan.util.StrUtil",
                                    "click_open_win") + " onload=\"javascript:if(this.width>screen.width*0.4) this.width=screen.width*0.4\"></a></div><BR>";
                            // System.out.println(line);
                        }
                    }
                }
                if (line==null || line.trim().equals(""))
                    continue;
                str += line;
            }
        }
        catch (ParserException e) {
            LogUtil.getLog(getClass()).error("getAbstract:" + e.getMessage());
        }
        return str;
    }

    /**
     * 清空回收站
     * @throws ErrMsgException
     */
    public void clearDustbin() throws ErrMsgException {
        String sql = "select id from help_document where examine=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, EXAMINE_DUSTBIN);
            ResultSet rs = conn.executePreQuery();
            while (rs.next()) {
                Document doc = getDocument(rs.getInt(1));
                doc.del();
                // 更新缓存
                DocCacheMgr dcm = new DocCacheMgr();
                dcm.refreshUpdate(doc.getId(), doc.getDirCode(),
                                  doc.getParentCode());
            }
        } catch (SQLException e) {
            logger.error(StrUtil.trace(e));
            throw new ErrMsgException("Err db.");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }    

    public boolean UpdateLevel() throws ErrMsgException {
        String sql = "update help_document set doc_level=? where id=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, level);
            pstmt.setInt(2, id);
            conn.executePreUpdate();
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id, class1, parentCode);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("Err db.");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public boolean UpdateExamine(int examine) throws ErrMsgException {
        String sql = "update document set examine=? where id=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, examine);
            pstmt.setInt(2, id);
            conn.executePreUpdate();
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id, class1, parentCode);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("Err db.");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    /**
     * 查找自afterDate后，是否存在标题为title的主题贴，用于采集时进行判断
     * @param title String
     * @param afterDate Date
     * @return boolean
     */
    public boolean isDocWithTitleExist(String title, java.util.Date afterDate) {
        boolean re = false;
        String sql = "select id from help_document where title=? and createDate>=?";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {title,
                                                "" +
                                                DateUtil.toLongString(afterDate)});
            re = ri.size() > 0;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("" + e.getMessage());
        }
        return re;
    }

    public boolean uploadDocument(FileUpload TheBean) throws ErrMsgException {
        String strdocId = StrUtil.getNullStr(TheBean.getFieldValue("doc_id"));
        String strfileId = StrUtil.getNullStr(TheBean.getFieldValue("file_id"));

        int docId = Integer.parseInt(strdocId);
        int fileId = Integer.parseInt(strfileId);

        Document doc = getDocument(docId);
        Attachment att = doc.getAttachment(1, fileId);
        if (att == null) {
            throw new ErrMsgException("取文件" + docId + "的附件" + fileId + "时，未找到！");
        }

        Vector v = TheBean.getFiles();
        if (v.size() > 0) {
            FileInfo fi = (FileInfo) v.get(0);
            String fullPath = Global.getRealPath() + att.getVisualPath() + "/";
            fi.write(fullPath, att.getDiskName());
            
            String previewfile=fullPath+att.getDiskName();
        	String ext = StrUtil.getFileExt(att.getDiskName());
        	if (ext.equals("doc") || ext.equals("docx") || ext.equals("xls") || ext.equals("xlsx")) {
        		createOfficeFilePreviewHTML(previewfile);
        	}
            
            return true;
        } else
            return false;
    }    
    
    public int getId() {
        return id;
    }

    /**
     * 判断是否支持jacob操作
     * @param previewfile
     * @return
     */
    public static boolean createOfficeFilePreviewHTML(String previewfile) {
    	boolean returnValue = false;
    	if (IS_JACOB_SUPPORTED==-1) {
    		//判断操作系统，windows可以生成预览文件
			if ("\\".equals(File.separator)) {
				//根据上传文件生成预览文件
				Document.jacobFilePreview(previewfile);
				//判断是否成功生成html预览文件
				String htmlfile=previewfile.substring(0, previewfile.lastIndexOf("."))+".html";
				File fileExist=new File(htmlfile);
				if(fileExist.exists()){
					returnValue=true;
				}else{
					returnValue=false;
				}
			} else if ("/".equals(File.separator)) {
				returnValue=false;
			} 
    	}
    	return returnValue;
    }
    
    /**
     * 删除文件夹及其下子文件
     * @param file
     */
    public static void fileDelete(File file) {
		if (!file.exists())
			return;
		if (file.isFile()) {
			file.delete();
		} else {
			for (File f : file.listFiles()) {
				fileDelete(f);
			}
			file.delete();
		}
	}
    
    /**
     * 把word文件转换成html文件
     * @param docfile 要转换的word文件路径
     * @param htmlfile 转换后的html文件路径
     */
    public static void wordToHtml(String docfile,String htmlfile){
    	ActiveXComponent app = null;
		try {
			app = new ActiveXComponent("Word.application");// 启动word
			//设置word不可见
			app.setProperty("Visible", new Variant(false));
			//获得document对象
			Dispatch docs=app.getProperty("Documents").toDispatch();
			//打开文件
			Dispatch doc=Dispatch.invoke(docs, "Open", Dispatch.Method,
					new Object[]{docfile,new Variant(false),new Variant(true)}, new int[1]).toDispatch();
			//保存成新文件
			Dispatch.invoke(doc, "SaveAs", Dispatch.Method, 
					new Object[]{htmlfile,new Variant(8)}, new int[1]);
			Variant f=new Variant(false);
			Dispatch.call(doc, "Close", f);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		catch (java.lang.UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
		catch (NoClassDefFoundError e) {
			e.printStackTrace();
		}
		finally{
			if (app!=null)
				app.invoke("Quit", new Variant[] {});
		}
	}
    
    /**
     * excel文件转换成html文件
     * @param xlsfile
     * @param htmlfile
     */
    public static void excelToHtml(String xlsfile,String htmlfile){
    	ActiveXComponent app = null;
		try {
			app = new ActiveXComponent("Excel.application");
			app.setProperty("Visible", new Variant(false));
			Dispatch excels=app.getProperty("Workbooks").toDispatch();
			Dispatch excel=Dispatch.invoke(excels, "Open", Dispatch.Method,
					new Object[]{xlsfile,new Variant(false),new Variant(true)}, new int[1]).toDispatch();
			Dispatch.invoke(excel, "SaveAs", Dispatch.Method, 
					new Object[]{htmlfile,new Variant(44)}, new int[1]);
			Variant f=new Variant(false);
			Dispatch.call(excel, "Close", f);
			ComThread.Release();//关闭进程
		} catch (Exception e) {
			e.printStackTrace();
		}
		catch (java.lang.UnsatisfiedLinkError e) {
			e.printStackTrace();
		}	
		catch (NoClassDefFoundError e) {
			e.printStackTrace();
		}		
		finally{
			if (app!=null)
				app.invoke("Quit", new Variant[] {});
		}
	}
    
    /**
     * 附件添加、修改后，预览文件对应操作（先删掉原有文件，再根据修改后内容生成新的文件）
     * @param filepath
     */
    public static void jacobFilePreview(String filepath){
    	//生成的html文件
    	String htmlfile=filepath.substring(0,filepath.lastIndexOf("."))+".html";
    	//word生成的htnl文件对应的files文件夹
    	String existWordFile=filepath.substring(0,filepath.lastIndexOf("."))+".files";
    	//excel生成的htnl文件对应的files文件夹
    	String existExcelFile=filepath.substring(0,filepath.lastIndexOf("."))+".files";
    	//获取上传文件后缀	
    	String fileType=filepath.substring(filepath.lastIndexOf(".")+1);
    	
    	//先删除原有html文件
    	File htmlFile=new File(htmlfile);
    	if(htmlFile.exists()){
    		if(htmlFile.isFile()){
    			htmlFile.delete();
    		}
    	}
    	
    	//根据文件类型调用对应方法
    	if(fileType.equals("doc") || fileType.equals("docx")){
    		File wordFile=new File(existWordFile);
    		if(wordFile.exists()){
    			Document.fileDelete(wordFile);
    		}
    		wordToHtml(filepath, htmlfile);
    	}
    	
    	if(fileType.equals("xls") || fileType.equals("xlsx")){
    		File excelFile=new File(existExcelFile);
    		if(excelFile.exists()){
    			Document.fileDelete(excelFile);
    		}
    		excelToHtml(filepath, htmlfile);
    	}
    }
    
    /**
     * 附件删除后，预览文件对应删除
     * @param filepath
     */
    public static void jacobFileDelete(String filepath){
    	String htmlfile=filepath.substring(0,filepath.lastIndexOf("."))+".html";
    	String existWordFile=filepath.substring(0,filepath.lastIndexOf("."))+".files";
    	String existExcelFile=filepath.substring(0,filepath.lastIndexOf("."))+".files";
    		
    	String fileType=filepath.substring(filepath.lastIndexOf(".")+1);
    	File htmlFile=new File(htmlfile);
    	if(htmlFile.exists()){
    		if(htmlFile.isFile()){
    			htmlFile.delete();
    		}
    	}
    	
    	if(fileType.equals("doc") || fileType.equals("docx")){
    		File wordFile=new File(existWordFile);
    		if(wordFile.exists()){
    			Document.fileDelete(wordFile);
    		}
    	}
    	
    	if(fileType.equals("xls") || fileType.equals("xlsx")){
    		File excelFile=new File(existExcelFile);
    		if(excelFile.exists()){
    			Document.fileDelete(excelFile);
    		}
    	}
    }
    
    private int isNew = 0;
    private String author;
    private String flowTypeCode = "";
    private String color;
    private boolean bold;
    private java.util.Date expireDate;
    private String orgAddr;
    private java.util.Date createDate;
    private int level;
    
    private String kind;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getKind() {
		return kind;
	}
}

