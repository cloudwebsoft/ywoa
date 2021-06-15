package cn.js.fan.module.cms;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.module.cms.plugin.*;
import cn.js.fan.module.cms.plugin.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.forum.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;

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
public class Document implements java.io.Serializable, ITagSupport {
    public static final int NOTEMPLATE = -1;
    String connname = "";
    private String subjects;
    int id = -1;
    String title;
    String content;
    String date;
    String class1;
    java.util.Date modifiedDate;
    String summary;
    boolean isHome = false;
    int examine = 0;

    public static final int TYPE_DOC = 0;
    public static final int TYPE_VOTE = 1;
    public static final int TYPE_LINK = 2;
    public static final int TYPE_IMG = 3;

    public static final int EXAMINE_NOT = 0; // 未审核
    public static final int EXAMINE_NOTPASS = 1; // 未通过
    public static final int EXAMINE_PASS = 2; //　审核通过

    public static final int EXAMINE_DUSTBIN = 10; // 删除

    public static final int LEVEL_TOP = 100;

    public static final int PAGE_TYPE_MANUAL = 0;
    public static final int PAGE_TYPE_TAG = 1;
    // public static final int PAGE_TYPE_CHAR_NUMBE = 2;

    public static String PAGE_TAG = "\\[page\\]";

    transient Logger logger = Logger.getLogger(Document.class.getName());

    private static final String INSERT_DOCUMENT =
            "INSERT into document (id, title, class1, doc_type, nick, keywords, isrelateshow, can_comment, hit, template_id, parent_code, examine, isNew, author, flowTypeCode, modifiedDate, color, isbold, expire_date,source,page_template_id,createDate,page_type,doc_level) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String LOAD_DOCUMENT =
            "SELECT title, class1, modifiedDate, can_comment,summary,ishome,doc_type,examine,nick,keywords,isrelateshow,hit,template_id,page_count,parent_code,isNew,author,flowTypeCode,color,isbold,expire_date,source,page_template_id,createDate,doc_level,page_type,page_no FROM document WHERE id=?";

    private static final String DEL_DOCUMENT =
            "delete FROM document WHERE id=?";
    private static final String SAVE_DOCUMENT =
            "UPDATE document SET title=?, can_comment=?, ishome=?, examine=?,keywords=?,isrelateshow=?,template_id=?,class1=?,isNew=?,author=?,flowTypeCode=?,modifiedDate=?,parent_code=?,color=?,isbold=?,expire_date=?,source=?,doc_type=?,page_template_id=?,page_type=?,doc_level=?,page_no=?,createDate=? WHERE id=?";
    private static final String SAVE_SUMMARY =
            "UPDATE document SET summary=? WHERE id=?";
    private static final String SAVE_HIT =
            "UPDATE document SET hit=? WHERE id=?";

    public Document() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Document:默认数据库名为空！");
    }

    /**
     * 从数据库中取出数据
     * @param id int
     */
    public Document(int id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Document:conname can not be empty.");
        this.id = id;
        loadFromDB();
    }

    public void renew() {
        if (logger == null)
            logger = Logger.getLogger(Document.class.getName());
    }

    public TemplateDb getTemplateDb() throws ErrMsgException {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(getDirCode());
        TemplateCatalogDb tcd = lf.getTemplateCatalogDb();
        if (tcd==null) {
            tcd = new TemplateCatalogDb();
            tcd = tcd.getDefaultTemplateCatalogDb();
            // System.out.println(getClass() + " tcd=" + tcd);
        }
        TemplateDb td = new TemplateDb();
        if (tcd.getInt("doc")!=TemplateDb.TYPE_CODE_DEFAULT) {
            td = td.getTemplateDb(tcd.getInt("doc"));
        }
        else {
            td = td.getDefaultTemplate(TemplateDb.TYPE_CODE_DOC);
        }
        return td;
    }

/*
    public TemplateDb getTemplateDb() throws ErrMsgException {
        int pageTemplateId = getPageTemplateId();
        TemplateDb td = new TemplateDb();

        if (pageTemplateId == -1) {
            // 取其目录的模板
            Leaf lf = new Leaf();
            lf = lf.getLeaf(class1);
            int templateDocId = lf.getTemplateDocId();
            if (templateDocId != -1) {
                td = (TemplateDb) td.getQObjectDb(new Integer(templateDocId));
                if (td == null)
                    throw new ErrMsgException("模板" + templateDocId + "不存在!");
            } else {
                td = td.getDefaultTemplate(TemplateDb.TYPE_CODE_DOC);
                if (td == null)
                    throw new ErrMsgException("默认模板不存在!");
            }
        } else {
            td = (TemplateDb) td.getQObjectDb(new Integer(pageTemplateId));
            if (td == null)
                throw new ErrMsgException("模板" + pageTemplateId + "不存在!");
        }
        return td;
    }
 */

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
            create(code, title, content, 0, nick, leaf.getTemplateId(), nick);
            this.id = getFirstIDByCode(code);
            // 更改目录中的doc_id
            //logger.info("id=" + id);
            leaf.setDocID(id);
            leaf.update();

            PluginMgr pm = new PluginMgr();
            PluginUnit pu = pm.getPluginUnitOfDir(code);
            if (pu != null) {
                IPluginDocumentAction ipda = pu.getDocumentAction();
                ipda.create(this);
            }
        }
        return id;
    }

    /**
     * 删除一个目录下面的所有文章
     * @param code String
     */
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
        String sql = "select id from document where class1=" +
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
        String sql = "select id from document where class1=" +
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

    public String getContent(int pageNum) {
        if (pageType == PAGE_TYPE_TAG) {
            DocContent dc = new DocContent();
            dc = dc.getDocContent(id, 1);
            String[] ary = StrUtil.split(dc.getContent(), PAGE_TAG);
            if (ary == null)
                return ""; // 内容为空
            else if (ary.length < pageNum)
                return null;
            else
                return ary[pageNum - 1];
        } else {
            DocContent dc = new DocContent();
            dc = dc.getDocContent(id, pageNum);
            if (dc != null)
                return dc.getContent();
            else
                return null;
        }
    }

    public long getDocCountNoCache(String listsql) {
        int total = 0;
        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            ResultSet rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return total;
    }

    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        ListResult lr = new ListResult();
        Vector result = new Vector();
        lr.setTotal(total);
        lr.setResult(result);
        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); //尽量减少内存的使用

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    Document cmm = getDocument(rs.getInt(1));
                    result.addElement(cmm);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("listResult: DB operate error.");
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
        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    /**
     * 列出从 start 至 end 的文章，
     * @param sql String
     * @param start int 索引值， zero based
     * @param end int 索引值， zero based
     * @return Vector
     */
    public Vector list(String sql, int start, int end) {
        start += 1; // 索引从0开始
        end += 1;

        ResultSet rs = null;
        Vector result = new Vector();
        Conn conn = new Conn(connname);
        try {
            conn.setMaxRows(end); //尽量减少内存的使用
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return result;
            } else {
                // defines the number of rows that will be read from the database when the ResultSet needs more rows
                rs.setFetchSize(end - start + 1); // rs一次从POOL中所获取的记录数

                if (rs.absolute(start) == false) {
                    return result;
                }
                do {
                    int id = rs.getInt(1);
                    Document doc = getDocument(id);
                    result.addElement(doc);
                } while (rs.next());
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

    public Vector list(String sql, Object[] params) {
        Vector result = new Vector();
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, params);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                int id = rr.getInt(1);
                Document doc = getDocument(id);
                result.addElement(doc);
            }
        } catch (SQLException e) {
            logger.error("list: " + e.getMessage());
        }
        return result;
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

            // System.out.println(getClass() + " sql=" + sql);

            if (rs == null) {
                return result;
            } else {
                // defines the number of rows that will be read from the database when the ResultSet needs more rows
                rs.setFetchSize(count); // rs一次从POOL中所获取的记录数
                while (rs.next()) {
                    int id = rs.getInt(1);
                    Document doc = getDocument(id);
                    result.addElement(doc);
                    // System.out.println(getClass() + " id=" + id + " count=" + count);
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
            // 插件名称
            Leaf lf = new Leaf();
            lf = lf.getLeaf(class1);
            String pluginCode = lf.getPluginCode();
            if (pluginCode != null && pluginCode.equals(PluginUnit.DEFAULT))
                return dc.getContent();
            else {
                PluginMgr pm = new PluginMgr();
                PluginUnit pu = pm.getPluginUnit(pluginCode);
                IPluginRender ipr = pu.getUI(request).getRender();
                return ipr.RenderContent(request, dc);
            }
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

    public synchronized boolean UpdateWithoutFile(ServletContext application,
                                                  CMSMultiFileUploadBean mfu) throws
            ErrMsgException {
        // 取得表单中域的信息
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));
        author = StrUtil.getNullString(mfu.getFieldValue("author"));
        title = StrUtil.getNullString(mfu.getFieldValue("title"));
        // logger.info("FilePath=" + FilePath);
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
        String strisRelateShow = StrUtil.getNullStr(mfu.getFieldValue(
                "isRelateShow"));
        int intisRelateShow = 0;
        if (StrUtil.isNumeric(strisRelateShow)) {
            intisRelateShow = Integer.parseInt(strisRelateShow);
            if (intisRelateShow == 1)
                isRelateShow = true;
        }

        flowTypeCode = StrUtil.getNullString(mfu.getFieldValue("flowTypeCode"));

        color = StrUtil.getNullStr(mfu.getFieldValue("color"));
        bold = StrUtil.getNullStr(mfu.getFieldValue("isBold")).equals("true");
        expireDate = DateUtil.parse(mfu.getFieldValue("expireDate"),
                                    "yyyy-MM-dd");
        source = StrUtil.getNullStr(mfu.getFieldValue("source"));
        try {
            pageTemplateId = Integer.parseInt(mfu.getFieldValue(
                    "pageTemplateId"));
        } catch (Exception e) {
            pageTemplateId = -1;
        }
        String docType = StrUtil.getNullStr(mfu.getFieldValue("docType"));
        if (!docType.equals("" + TYPE_VOTE)) {
            type = Integer.parseInt(docType);
        }

        if (!dir_code.equals(class1)) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(dir_code);
            parentCode = lf.getParentCode();
        }

        String subjects = StrUtil.getNullStr(mfu.getFieldValue("subjects"));
        String strPageType = StrUtil.getNullStr(mfu.getFieldValue("pageType"));
        pageType = StrUtil.toInt(strPageType, PAGE_TYPE_MANUAL);
        createDate = DateUtil.parse(mfu.getFieldValue("createDate"), "yyyy-MM-dd HH:mm:ss");

        int oldlevel = level;
        level = StrUtil.toInt(StrUtil.getNullStr(mfu.getFieldValue("level")), 0);

        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            // 更新文件内容
            pstmt = conn.prepareStatement(SAVE_DOCUMENT);
            pstmt.setString(1, title);
            pstmt.setInt(2, canComment ? 1 : 0);
            pstmt.setBoolean(3, isHome);
            pstmt.setInt(4, examine);
            pstmt.setString(5, keywords);
            pstmt.setInt(6, intisRelateShow);
            pstmt.setInt(7, templateId);
            pstmt.setString(8, dir_code);
            pstmt.setInt(9, isNew);
            pstmt.setString(10, author);
            pstmt.setString(11, flowTypeCode);
            pstmt.setString(12, "" + System.currentTimeMillis());
            pstmt.setString(13, parentCode);
            pstmt.setString(14, color);
            pstmt.setInt(15, bold ? 1 : 0);
            pstmt.setString(16, DateUtil.toLongString(expireDate));
            pstmt.setString(17, source);
            pstmt.setInt(18, type);
            pstmt.setInt(19, pageTemplateId);
            pstmt.setInt(20, pageType);
            pstmt.setInt(21, level);
            pstmt.setInt(22, pageNo);
            pstmt.setString(23, DateUtil.toLongString(createDate));
            pstmt.setInt(24, id);
            conn.executePreUpdate();

            // 置贴子的专题
            if (!subjects.equals("")) {
                String[] subjectAry = StrUtil.split(subjects, ",");
                SubjectListDb sld = new SubjectListDb();
                sld.setDocBelongtoSubjects(new JdbcTemplate(), subjectAry, id);
            }

            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            if (oldexamine == examine && oldlevel == level) {
                dcm.refreshUpdate(id);
            } else {
                dcm.refreshUpdate(id, class1, parentCode);

                // 更新专题的缓存
                SubjectListCache slc = new SubjectListCache(new SubjectListDb());
                if (!subjects.equals("")) {
                    String[] subjectAry = StrUtil.split(subjects, ",");
                    int len = subjectAry.length;
                    for (int i = 0; i < len; i++) {
                        slc.refreshList(subjectAry[i]);
                    }
                }
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
            throw new ErrMsgException("DB operate error.");
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    /**
     * 清空回收站
     */
    public void clearDustbin() throws ErrMsgException {
        String sql = "select id from document where examine=?";
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
            logger.error(e.getMessage());
            throw new ErrMsgException("Err db.");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean UpdateLevel() throws ErrMsgException {
        String sql = "update document set doc_level=? where id=?";
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
            logger.error("UpdateExamine:" + e.getMessage());
            throw new ErrMsgException("Err db.");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public boolean UpdateIsHome(boolean isHome) throws
            ErrMsgException {
        String sql = "update document set isHome=? where id=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            // 更新文件内容
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, isHome ? 1 : 0);
            pstmt.setInt(2, id);
            conn.executePreUpdate();
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            // dcm.refreshUpdate(id);
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

    public boolean increaseHit() throws
            ErrMsgException {
        hit++;
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
            throw new ErrMsgException("DB operate error.");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public synchronized boolean UpdateSummaryWithoutFile(ServletContext
            application,
            CMSMultiFileUploadBean mfu) throws
            ResKeyException {
        Conn conn = new Conn(connname);
        boolean re = true;
        try {
            //取得表单中域的信息
            String idstr = StrUtil.getNullString(mfu.getFieldValue("id"));
            if (!StrUtil.isNumeric(idstr))
                throw new ResKeyException(SkinUtil.ERR_ID);
            id = Integer.parseInt(idstr);
            summary = StrUtil.getNullString(mfu.getFieldValue("htmlcode"));
            /*
             String FilePath = StrUtil.getNullString(mfu.getFieldValue(
                                "filepath"));
                        // 处理附件
             String tempAttachFilePath = application.getRealPath("/") + FilePath +
                                                    "/";
                        mfu.setSavePath(tempAttachFilePath); //取得目录
                        File f = new File(tempAttachFilePath);
                        if (!f.isDirectory()) {
                            f.mkdirs();
                        }
                        // 写入磁盘
                        mfu.writeAttachment(true);

                        Vector attachs = mfu.getAttachments();
                        Iterator ir = attachs.iterator();
                        String sql = "";
                        while (ir.hasNext()) {
                            FileInfo fi = (FileInfo) ir.next();
             String filepath = mfu.getSavePath() + fi.getDiskName();
                            sql +=
                                    "insert into document_attach (fullpath,doc_id,name,diskname,visualpath,page_num) values (" +
                                    StrUtil.sqlstr(filepath) + "," + id + "," +
                                    StrUtil.sqlstr(fi.getName()) +
             "," + StrUtil.sqlstr(fi.getDiskName()) + "," +
                                    StrUtil.sqlstr(FilePath) + "," + 0 + ");";
                        }
                        if (!sql.equals(""))
                            conn.executeUpdate(sql);
             */
            PreparedStatement pstmt = null; //更新文件内容
            pstmt = conn.prepareStatement(SAVE_SUMMARY);
            pstmt.setString(1, summary);
            pstmt.setInt(2, id);
            re = conn.executePreUpdate() == 1 ? true : false;
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

    public synchronized boolean UpdateSummary(ServletContext application,
                                              CMSMultiFileUploadBean mfu) throws
            ResKeyException {
        String isuploadfile = StrUtil.getNullString(mfu.getFieldValue(
                "isuploadfile"));
        // logger.info("filepath=" + mfu.getFieldValue("filepath"));
        if (isuploadfile.equals("false"))
            return UpdateSummaryWithoutFile(application, mfu);

        String FilePath = StrUtil.getNullString(mfu.getFieldValue("filepath"));
        String tempAttachFilePath = Global.getRealPath() + FilePath +
                                    "/";
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
                        File virtualFile = new File(Global.getRealPath() +
                                fpath);
                        virtualFile.delete();
                    }
                }

            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            // 从数据库中删除图像
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
                    int imgId = (int) SequenceMgr.nextID(SequenceMgr.CMS_IMAGES);
                    sql =
                            "insert into cms_images (id, path,mainkey,kind,subkey) values (" +
                            imgId + "," +
                            StrUtil.sqlstr(filepath) + "," + id +
                            ",'document'," + 0 + ")";
                    conn.executeUpdate(sql);
                }
            } else
                throw new ErrMsgException(mfu.getErrMessage());
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
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

    public synchronized boolean updateTemplateId() throws ErrMsgException {
        return save();
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
        examine = StrUtil.toInt(strexamine, EXAMINE_PASS);
        keywords = StrUtil.getNullStr(mfu.getFieldValue("keywords"));
        String strisRelateShow = StrUtil.getNullStr(mfu.getFieldValue(
                "isRelateShow"));
        int intisRelateShow = 0;
        if (StrUtil.isNumeric(strisRelateShow)) {
            intisRelateShow = Integer.parseInt(strisRelateShow);
            if (intisRelateShow == 1)
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
        expireDate = DateUtil.parse(mfu.getFieldValue("expireDate"),
                                    "yyyy-MM-dd");
        source = StrUtil.getNullStr(mfu.getFieldValue("source"));
        try {
            pageTemplateId = Integer.parseInt(mfu.getFieldValue(
                    "pageTemplateId"));
        } catch (Exception e) {
            pageTemplateId = -1;
        }
        String docType = StrUtil.getNullStr(mfu.getFieldValue("docType"));
        if (!docType.equals("" + TYPE_VOTE)) {
            type = Integer.parseInt(docType);
        }

        if (!dir_code.equals(class1)) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(dir_code);
            if (lf.getType() == Leaf.TYPE_DOCUMENT) {
                throw new ErrMsgException(lf.getName() + " 为文章型节点,不能更改至该目录！");
            }
            parentCode = lf.getParentCode();
        }

        String subjects = StrUtil.getNullStr(mfu.getFieldValue("subjects"));

        int oldlevel = level;
        level = StrUtil.toInt(StrUtil.getNullStr(mfu.getFieldValue("level")), 0);

        // 检查创建日期有没有更改
        java.util.Date newCreateDate = DateUtil.parse(mfu.getFieldValue("createDate"), "yyyy-MM-dd HH:mm:ss");
        boolean isCreatDateModified = DateUtil.compare(newCreateDate, createDate)!=0;
        if (isCreatDateModified)
            createDate = newCreateDate;

        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            // 更新文件内容
            pstmt = conn.prepareStatement(SAVE_DOCUMENT);
            pstmt.setString(1, title);
            pstmt.setInt(2, canComment ? 1 : 0);
            pstmt.setBoolean(3, isHome);
            pstmt.setInt(4, examine);
            pstmt.setString(5, keywords);
            pstmt.setInt(6, intisRelateShow);
            pstmt.setInt(7, templateId);
            pstmt.setString(8, dir_code);
            pstmt.setInt(9, isNew);
            pstmt.setString(10, author);
            pstmt.setString(11, flowTypeCode);
            pstmt.setString(12, "" + System.currentTimeMillis());
            pstmt.setString(13, parentCode);
            pstmt.setString(14, color);
            pstmt.setInt(15, bold ? 1 : 0);
            pstmt.setString(16, DateUtil.toLongString(expireDate));
            pstmt.setString(17, source);
            pstmt.setInt(18, type);
            pstmt.setInt(19, pageTemplateId);
            String strPageType = StrUtil.getNullStr(mfu.getFieldValue(
                    "pageType"));
            pageType = StrUtil.toInt(strPageType, PAGE_TYPE_MANUAL);
            pstmt.setInt(20, pageType);
            pstmt.setInt(21, level);
            pstmt.setInt(22, pageNo);
            pstmt.setString(23, DateUtil.toLongString(createDate));
            pstmt.setInt(24, id);
            conn.executePreUpdate();

            // 更新第一页的内容
            DocContent dc = new DocContent();
            dc = dc.getDocContent(id, 1);
            dc.save(application, mfu);

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

            // 置文章的专题
            if (!subjects.equals("")) {
                String[] subjectAry = StrUtil.split(subjects, ",");
                SubjectListDb sld = new SubjectListDb();
                sld.setDocBelongtoSubjects(new JdbcTemplate(), subjectAry, id);
            }
        } catch (SQLException e) {
            logger.error("update:" + e.getMessage());
            throw new ErrMsgException("DB operate error.");
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            if (oldexamine == examine && oldlevel == level && !isCreatDateModified) {
                dcm.refreshUpdate(id);
            } else {
                dcm.refreshUpdate(id, class1, parentCode);

                // 更新专题的缓存
                SubjectListCache slc = new SubjectListCache(new SubjectListDb());
                if (!subjects.equals("")) {
                    String[] subjectAry = StrUtil.split(subjects, ",");
                    int len = subjectAry.length;
                    for (int i = 0; i < len; i++) {
                        slc.refreshList(subjectAry[i]);
                    }
                }
            }
            // 如果是更改了类别
            if (!dir_code.equals(class1)) {
                dcm.refreshChangeDirCode(class1, dir_code);
                class1 = dir_code;
            }
        }
        return true;
    }

    public boolean create(ServletContext application,
                          CMSMultiFileUploadBean mfu, String nick) throws
            ResKeyException, ErrMsgException {
        String isuploadfile = StrUtil.getNullString(mfu.getFieldValue(
                "isuploadfile"));

        // 取得表单中域的信息
        author = StrUtil.getNullString(mfu.getFieldValue("author"));
        title = StrUtil.getNullString(mfu.getFieldValue("title"));
        content = StrUtil.getNullString(mfu.getFieldValue("htmlcode"));
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));
        keywords = StrUtil.getNullStr(mfu.getFieldValue("keywords"));
        String strisRelateShow = StrUtil.getNullStr(mfu.getFieldValue(
                "isRelateShow"));
        int intisRelateShow = 0;
        if (StrUtil.isNumeric(strisRelateShow)) {
            intisRelateShow = Integer.parseInt(strisRelateShow);
            if (intisRelateShow == 1)
                isRelateShow = true;
        }

        String strexamine = StrUtil.getNullStr(mfu.getFieldValue("examine"));
        if (StrUtil.isNumeric(strexamine)) {
            examine = Integer.parseInt(strexamine);
        } else
            examine = 0;

        String strisnew = StrUtil.getNullStr(mfu.getFieldValue("isNew"));
        if (StrUtil.isNumeric(strisnew))
            isNew = Integer.parseInt(strisnew);
        else
            isNew = 0;

        flowTypeCode = StrUtil.getNullStr(mfu.getFieldValue("flowTypeCode"));
        String strCreateDate = StrUtil.getNullStr(mfu.getFieldValue("createDate"));
        if (strCreateDate.trim().equals(""))
            createDate = new java.util.Date();
        else
            createDate = DateUtil.parse(strCreateDate, "yyyy-MM-dd HH:mm:ss");

        // 检查目录节点中是否允许插入文章
        Directory dir = new Directory();
        // logger.info("update: dir_code=" + dir_code + " title=" + title);
        Leaf lf = dir.getLeaf(dir_code);
        if (lf.getType() == 0)
            throw new ResKeyException("res.cms.Document", "err_dir_type_none");
        if (lf.getType() == 1) {
            if (getFirstIDByCode(dir_code) != -1)
                throw new ResKeyException("res.cms.Document",
                                          "err_dir_type_doc_created");
            // throw new ErrMsgException("该目录节点为文章节点，且文章已经被创建！");
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

        this.id = (int) SequenceMgr.nextID(SequenceMgr.DOCUMENT);

        // 投票处理
        String docType = mfu.getFieldValue("docType");
        String[] voptions = null;
        type = TYPE_DOC; // 类型1表示为投票
        if (docType.equals("1")) {
            type = TYPE_VOTE;

            String voteoption = mfu.getFieldValue("vote").trim();
            if (!voteoption.equals("")) {
                voptions = voteoption.split("\\r\\n");
            }
            // if (voteoption.indexOf("|") != -1)
            //    throw new ResKeyException("res.cms.Document", "err_vote_option");
            // throw new ErrMsgException("投票选项中不能包含|");

            DocPollDb mpd = new DocPollDb();
            String epdate = StrUtil.getNullString(mfu.getFieldValue(
                    "expire_date"));
            java.util.Date expireDate = DateUtil.parse(epdate, "yyyy-MM-dd");
            String strMaxChoices = StrUtil.getNullString(mfu.getFieldValue(
                    "max_choice"));
            int maxChoices = StrUtil.toInt(strMaxChoices, 1);
            // 创建投票项
            mpd.create(new JdbcTemplate(), new Object[] {
                new Long(id), expireDate, new Integer(maxChoices)
            });

            int vlen = voptions.length;
            // 创建投票选项
            DocPollOptionDb mpod = new DocPollOptionDb();
            for (int i = 0; i < vlen; i++) {
                mpod.create(new JdbcTemplate(), new Object[] {
                    new Long(id), new Integer(i), voptions[i]
                });
            }
        } else if (docType.equals("2")) {
            type = TYPE_LINK;
        }

        color = StrUtil.getNullStr(mfu.getFieldValue("color"));
        bold = StrUtil.getNullStr(mfu.getFieldValue("isBold")).equals("true");
        expireDate = DateUtil.parse(mfu.getFieldValue("expireDate"),
                                    "yyyy-MM-dd");
        source = StrUtil.getNullStr(mfu.getFieldValue("source"));
        pageTemplateId = StrUtil.toInt(mfu.getFieldValue("pageTemplateId"), -1);

        String subjects = StrUtil.getNullStr(mfu.getFieldValue("subjects"));

        String strPageType = StrUtil.getNullStr(mfu.getFieldValue("pageType"));
        pageType = StrUtil.toInt(strPageType, PAGE_TYPE_MANUAL);
        level = StrUtil.toInt(StrUtil.getNullStr(mfu.getFieldValue("level")), 0);

        // 清缓存
        DocCacheMgr dcm = new DocCacheMgr();
        dcm.refreshCreate(dir_code, lf.getParentCode());

        // 如果不上传文件
        if (isuploadfile.equals("false"))
            return create(dir_code, title, content, type,
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
            pstmt.setString(5, nick);
            pstmt.setString(6, keywords);
            pstmt.setInt(7, intisRelateShow);
            pstmt.setInt(8, canComment ? 1 : 0);
            pstmt.setInt(9, hit);
            pstmt.setInt(10, templateId);
            pstmt.setString(11, parentCode);
            pstmt.setInt(12, examine);
            pstmt.setInt(13, isNew);
            pstmt.setString(14, author);
            pstmt.setString(15, flowTypeCode);
            pstmt.setString(16, "" + System.currentTimeMillis());
            pstmt.setString(17, color);
            pstmt.setInt(18, bold ? 1 : 0);
            pstmt.setString(19, DateUtil.toLongString(expireDate));
            pstmt.setString(20, source);
            pstmt.setInt(21, pageTemplateId);
            pstmt.setString(22, DateUtil.toLongString(createDate));
            pstmt.setInt(23, pageType);
            pstmt.setInt(24, level);
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

            lf.setDocCount(lf.getDocCount() + 1);
            lf.save(new JdbcTemplate());

            // 置贴子的专题
            if (!subjects.equals("")) {
                String[] subjectAry = StrUtil.split(subjects, ",");
                SubjectListDb sld = new SubjectListDb();
                sld.setDocBelongtoSubjects(new JdbcTemplate(), subjectAry, id);
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
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
     */
    public boolean create(String code_class1, String title, String content,
                          int type,
                          String nick, int templateId, String author) {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(code_class1);
        parentCode = lf.getParentCode();
        Conn conn = new Conn(connname);
        this.id = (int) SequenceMgr.nextID(SequenceMgr.DOCUMENT);
        try {
            // 插入文章标题及相关设置
            PreparedStatement pstmt = conn.prepareStatement(INSERT_DOCUMENT);
            pstmt.setInt(1, id);
            pstmt.setString(2, title);
            pstmt.setString(3, code_class1);
            pstmt.setInt(4, type);
            pstmt.setString(5, nick);
            pstmt.setString(6, "");
            pstmt.setInt(7, 1);
            pstmt.setInt(8, canComment ? 1 : 0);
            pstmt.setInt(9, hit);
            pstmt.setInt(10, templateId);
            pstmt.setString(11, parentCode);
            pstmt.setInt(12, examine);
            pstmt.setInt(13, isNew);
            pstmt.setString(14, author);
            pstmt.setString(15, flowTypeCode);
            pstmt.setString(16, "" + System.currentTimeMillis());
            pstmt.setString(17, color);
            pstmt.setInt(18, bold ? 1 : 0);
            pstmt.setString(19, DateUtil.toLongString(expireDate));
            pstmt.setString(20, source);
            pstmt.setInt(21, pageTemplateId);
            if (createDate==null)
                createDate = new java.util.Date();
            pstmt.setString(22, DateUtil.toLongString(createDate));
            pstmt.setInt(23, pageType);
            pstmt.setInt(24, level);
            conn.executePreUpdate();

            // 插入文章中的内容
            DocContent dc = new DocContent();
            dc.create(id, content, 1);

            lf.setDocCount(lf.getDocCount() + 1);
            lf.save(new JdbcTemplate());
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
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
            String sql = "select page_num from doc_content where doc_id=" + id;
            rs = conn.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    int pn = rs.getInt(1);
                    DocContent dc = new DocContent();
                    dc = dc.getDocContent(id, pn);
                    dc.del();
                }
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            PreparedStatement pstmt = conn.prepareStatement(DEL_DOCUMENT);
            pstmt.setInt(1, id);
            conn.executePreUpdate();

            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshDel(id, class1, parentCode);

            Leaf lf = new Leaf();
            lf = lf.getLeaf(class1);
            if (lf!=null) {
                lf.setDocCount(lf.getDocCount() - 1);
                lf.save(new JdbcTemplate());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
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
                logger.error("loadFromDB: Document id= " + id + " not found." +
                             StrUtil.trace(new Exception()));
            } else {
                this.title = rs.getString(1);
                this.class1 = rs.getString(2);
                this.modifiedDate = DateUtil.parse(rs.getString(3));
                this.canComment = rs.getBoolean(4);
                this.summary = rs.getString(5);
                this.isHome = rs.getBoolean(6);
                this.type = rs.getInt(7);
                this.examine = rs.getInt(8);
                this.nick = rs.getString(9);
                this.keywords = rs.getString(10);
                this.isRelateShow = rs.getInt(11) == 1 ? true : false;
                this.hit = rs.getInt(12);
                this.templateId = rs.getInt(13);
                this.pageCount = rs.getInt(14);
                this.parentCode = rs.getString(15);
                this.isNew = rs.getInt(16);
                this.author = rs.getString(17);
                this.flowTypeCode = rs.getString(18);
                color = StrUtil.getNullString(rs.getString(19));
                bold = rs.getInt(20) == 1;
                String s = rs.getString(21);
                if (s == null)
                    expireDate = null;
                else
                    expireDate = s.equals("0") ? null :
                                 DateUtil.parse(s);
                source = StrUtil.getNullStr(rs.getString(22));
                pageTemplateId = rs.getInt(23);
                createDate = DateUtil.parse(rs.getString(24));
                if (createDate == null) //  为了兼容以前未加createDate字段时的文章
                    createDate = modifiedDate;
                level = rs.getInt(25);
                pageType = rs.getInt(26);
                pageNo = rs.getInt(27);
                loaded = true; // 已初始化
            }
        } catch (SQLException e) {
            logger.error("loadFromDB:" + e.getMessage());
        } finally {
            /*
                         if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
                         }*/
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public String toString() {
        return "Document " + id + ":" + title;
    }

    private boolean canComment = true;

    public boolean getCanComment() {
        return canComment;
    }

    public int getType() {
        return type;
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

    public void setSource(String source) {
        this.source = source;
    }

    public void setPageTemplateId(int pageTemplateId) {
        this.pageTemplateId = pageTemplateId;
    }

    public String getNick() {
        return nick;
    }

    public java.util.Date getModifiedDate() {
        return this.modifiedDate;
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

    private int type = 0;
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
        if (id == -1)
            return false;
        return loaded;
    }

    public int getHit() {
        return hit;
    }

    public int getTemplateId() {
        return templateId;
    }

    /**
     * 当文档加挂插件时，如果插件中含有页数，则以插件的页数为准
     * @return int
     */
    public int getPageCountPlugin() {
        PluginMgr pm = new PluginMgr();
        PluginUnit pu = pm.getPluginUnitOfDir(getDirCode());
        if (pu != null) {
            IPluginDocument ipd = pu.getUnit().getDocument(id);
            if (ipd.getPageCount() != ipd.PAGE_COUNT_NONE)
                return ipd.getPageCount();
        }
        return getPageCount();
    }

    public int getPageCount() {
        if (pageType == PAGE_TYPE_TAG) {
            DocContent dc = new DocContent();
            dc = dc.getDocContent(id, 1);
            String[] ary = StrUtil.split(dc.getContent(), PAGE_TAG);
            if (ary == null)
                return 1; // 内容为空
            else
                return ary.length;
        } else
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

    public String getSource() {
        return source;
    }

    public int getPageTemplateId() {
        return pageTemplateId;
    }

    private int hit = 0;
    private int templateId = NOTEMPLATE;

    /**
     * 加一页内容
     * @param application ServletContext
     * @param mfu CMSMultiFileUploadBean
     * @param content String
     * @return boolean
     */
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
        if (afterpage != -1)
            pageNo = afterpage + 1;
        else
            pageNo = pageCount + 1;

        String isuploadfile = StrUtil.getNullString(mfu.getFieldValue(
                "isuploadfile"));

        DocContent dc = new DocContent();
        if (isuploadfile.equals("false")) {
            if (dc.createWithoutFile(application, mfu, id, content, pageNo)) {
                // pageCount++;
                // System.out.println(getClass() + " pageNo=" + pageNo + " pageCount=" + pageCount);
                // return UpdatePageCount(pageCount);
                return true;
            }
        } else {
            if (dc.create(application, mfu, id, content, pageNo)) {
                // pageCount++;
                // return UpdatePageCount(pageCount);
                return true;
            }
        }
        return false;
    }

    /**
     * 编辑文章内容页
     * @param application ServletContext
     * @param mfu CMSMultiFileUploadBean
     * @return boolean
     */
    public boolean EditContentPage(ServletContext application,
                                   CMSMultiFileUploadBean mfu) throws
            ErrMsgException {
        String strpageNum = StrUtil.getNullStr(mfu.getFieldValue("pageNum"));
        int pageNum = Integer.parseInt(strpageNum);

        DocContent dc = new DocContent();
        dc = dc.getDocContent(id, pageNum);
        dc.setContent(content);
        dc.save(application, mfu);

        return true;
    }

    public synchronized boolean UpdatePageCount(int pagecount) throws
            ErrMsgException {
        this.pageCount = pagecount;
        return save();
    }

    public boolean save() throws
            ErrMsgException {
        String sql = "update document set source=?,page_no=?,page_count=?,template_id=? where id=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, source);
            pstmt.setInt(2, pageNo);
            pstmt.setInt(3, pageCount);
            pstmt.setInt(4, templateId);
            pstmt.setInt(5, id);
            conn.executePreUpdate();
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id);
        } catch (SQLException e) {
            logger.error("UpdateSource:" + e.getMessage());
            throw new ErrMsgException("DB operate error.");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public synchronized boolean UpdateSource(String source) throws
            ErrMsgException {
        this.source = source;
        return save();
    }

    private int pageCount = 1;
    private String parentCode;

    public Vector getAttachments(int pageNum) {
        DocContent dc = new DocContent();
        dc = dc.getDocContent(id, pageNum);
        if (dc == null)
            return null;
        return dc.getAttachments();
    }

    public Attachment getAttachment(int pageNum, int att_id) {
        Iterator ir = getAttachments(pageNum).iterator();
        while (ir.hasNext()) {
            Attachment at = (Attachment) ir.next();
            if (at.getId() == att_id)
                return at;
        }
        return null;
    }

    /*
    // 已无用处 2008.4.4
    public Document getTemplate() {
        if (templateId == this.NOTEMPLATE)
            return null;
        else
            return getDocument(templateId);
    }
    */

    public boolean uploadDocument(FileUpload TheBean) throws
            ResKeyException {
        String strdocId = StrUtil.getNullStr(TheBean.getFieldValue("doc_id"));
        String strfileId = StrUtil.getNullStr(TheBean.getFieldValue("file_id"));

        int docId = Integer.parseInt(strdocId);
        int fileId = Integer.parseInt(strfileId);

        DocumentMgr dm = new DocumentMgr();
        Document doc = dm.getDocument(docId);
        Attachment att = doc.getAttachment(1, fileId);
        if (att == null) {
            throw new ResKeyException("res.cms.Document", "err_att_not_found",
                                      new Object[] {"" + docId, "" + fileId});
            // throw new ErrMsgException("取文件" + docId + "的附件" + fileId + "时，未找到！");
        }

        Vector v = TheBean.getFiles();
        if (v.size() > 0) {
            FileInfo fi = (FileInfo) v.get(0);
            String fullPath = Global.getRealPath() + att.getVisualPath() + "/";
            fi.write(fullPath, att.getDiskName());
            return true;
        } else
            return false;
    }

    public int getId() {
        return id;
    }

    public java.util.Date getCreateDate() {
        return createDate;
    }

    public int getLevel() {
        return level;
    }

    public int getPageType() {
        return pageType;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreateDate(java.util.Date createDate) {
        this.createDate = createDate;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setPageType(int pageType) {
        this.pageType = pageType;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public String getDocHtmlPath() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(createDate);
        String year = "" + (cal.get(cal.YEAR));
        String month = "" + (cal.get(cal.MONTH) + 1);
        return "doc" + "/" + year + "/" + month;
    }

    public String getDocHtmlName(int pageNum) {
        Config cfg = new Config();
        return getDocHtmlPath() + "/" + getId() + "_" + pageNum + "." +
                cfg.getProperty("cms.html_ext");
    }

    /**
     * 查找自afterDate后，是否存在标题为title的主题贴，用于采集时进行判断
     * @param title String
     * @param afterDate Date
     * @return boolean
     */
    public boolean isDocWithTitleExist(String title, java.util.Date afterDate) {
        boolean re = false;
        String sql = "select id from document where title=? and createDate>=?";
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

    /**
     * 用于Lunce中填充索引
     * @param beginDate long 0 表示不限
     * @param endDate long 0 表示不限
     * @return Vector
     */
    public Vector list(long beginDate, long endDate) {
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        String sql = "";
        if (beginDate != 0 && endDate != 0) {
            sql = "select id from document where createDate>=" + beginDate +
                  " and createDate<=" + endDate + " and examine=" +
                  EXAMINE_PASS;
        } else if (beginDate == 0 && endDate != 0) {
            sql = "select id from document where createDate<=" + endDate +
                  " and examine=" + EXAMINE_PASS;
        } else if (beginDate != 0 && endDate == 0) {
            sql = "select id from document where createDate>=" + beginDate +
                  " and examine=" + EXAMINE_PASS;
        } else {
            sql = "select id from document where examine=" + EXAMINE_PASS;
        }
        try {
            rs = conn.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    v.addElement(getDocument(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            logger.error("list(beginDate, endDate)" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }


    /**
     * 更改文章所属的目录
     */
    public synchronized boolean ChangeDir(String newDirCode) throws
            ErrMsgException {
        Directory dir = new Directory();
        Leaf newlf = dir.getLeaf(newDirCode);
        if (newDirCode.equals("") || newDirCode.equals("not") || newlf == null) {
            throw new ErrMsgException("请选择目录");
        }

        if (newlf.getType() == Leaf.TYPE_DOCUMENT) {
            throw new ErrMsgException("不能迁移至文章型目录节点");
        }

        boolean re = false;
        PreparedStatement ps = null;
        Conn conn = new Conn(connname);
        try {
            String sql = "update document set class1=?,parent_code=? where id=?";
            ps = conn.prepareStatement(sql);
            ps = conn.prepareStatement(sql);
            ps.setString(1, newDirCode);
            ps.setString(2, newlf.getParentCode());
            ps.setLong(3, id);
            re = conn.executePreUpdate() == 1;

            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id);
            dcm.refreshChangeDirCode(class1, newDirCode);
        } catch (Exception e) {
            Logger.getLogger(Document.class.getName()).error(e.getMessage());
            throw new ErrMsgException("数据库出错!");
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception e) {}
                ps = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * 取得文章中的第一个图片的路径，注意只有当为高级发布的文章时，才通过此方法获取，而当采用普通方式时，图片是放在attachment中的
     * @return String
     */
    public String getFirstImagePathOfDoc() {
        String sql =
                "select path from cms_images where mainkey=? order by id asc";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {id});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                return rr.getString(1);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getFirstImagePathOfDoc:" + e.getMessage());
        }
        return null;
    }

    private int isNew = 0;
    private String author;
    private String flowTypeCode = "";
    private String color;
    private boolean bold = false;
    private java.util.Date expireDate;
    private String source;
    private int pageTemplateId = NOTEMPLATE; // -1表示使用目录节点的模板

    private java.util.Date createDate;
    private int level = 0;
    private int pageType = PAGE_TYPE_MANUAL;
    private int pageNo = 1;
}

