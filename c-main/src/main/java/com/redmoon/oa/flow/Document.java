package com.redmoon.oa.flow;

import java.io.File;
import java.sql.*;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.ServletContext;
import cn.js.fan.base.ITagSupport;
import cn.js.fan.db.Conn;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.oa.base.IAttachment;
import com.redmoon.oa.db.SequenceManager;

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
	/**
	 * 套红前的模板默认值为-1
	 */
    public static final int NOTEMPLATE = -1;
    String connname = "";
    int id = -1;
    String title;
    String content;
    String date;
    String class1;
    Date modifiedDate;
    String summary;
    boolean isHome = false;
    int examine = 0;

    public static final int EXAMINE_NOT = 0; // 未审核
    public static final int EXAMINE_NOTPASS = 1; // 未通过
    public static final int EXAMINE_PASS = 2; //　审核通过
    
    private static final String INSERT_DOCUMENT =
            "INSERT into flow_document (id, title, class1, type, voteoption, voteresult, nick, keywords, isrelateshow, can_comment, hit, template_id, parent_code, examine, isNew, author, flowTypeCode, modifiedDate, flow_id) VALUES (?,?,?,?,?,?,?,?,?,?,0,?,?,?,?,?,?,?,?)";

    private static final String LOAD_DOCUMENT =
            "SELECT title, class1, modifiedDate, can_comment,summary,ishome,type,voteOption,voteResult,examine,nick,keywords,isrelateshow,hit,template_id,page_count,parent_code,isNew,author,flowTypeCode,flow_id FROM flow_document WHERE id=?";

    private static final String DEL_DOCUMENT =
            "delete FROM flow_document WHERE id=?";
    private static final String SAVE_DOCUMENT =
            "UPDATE flow_document SET title=?, can_comment=?, ishome=?, modifiedDate=?,examine=?,keywords=?,isrelateshow=?,template_id=?,class1=?,isNew=?,author=?,flowTypeCode=? WHERE id=?";
    private static final String SAVE_SUMMARY =
            "UPDATE flow_document SET summary=? WHERE id=?";
    private static final String SAVE_HIT =
            "UPDATE flow_document SET hit=? WHERE id=?";

    public Document() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("Document:默认数据库名为空！");
    }

    /**
     * 从数据库中取出数据
     * @param id int
     * @throws ErrMsgException
     */
    public Document(int id) {
        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("Directory:默认数据库名为空！");
        }
        this.id = id;
        loadFromDB();
    }

    public void renew() {
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
            title = "";
            content = " "; // TEXT 类型字段，必须加一空格符，否则在读出时会出错
            Leaf leaf = new Leaf();
            leaf = leaf.getLeaf(code);
            create(code, title, content, 0, "", "", nick, leaf.getTemplateId(), nick);
            this.id = getFirstIDByCode(code);
            // 更改目录中的doc_id
            //LogUtil.getLog(getClass()).info("id=" + id);
            leaf.setDocID(id);
            leaf.update();
        }
        return id;
    }

    public Vector getDocumentsByDirCode(String code) {
        Vector v = new Vector();
        String sql = "select id from flow_document where class1=" +
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
            LogUtil.getLog(getClass()).error(e.getMessage());
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
     * 取得结点为code的文章的ID，只取第一个
     * @param code String
     * @return int -1未取得
     */
    public int getFirstIDByCode(String code) {
        String sql = "select id from flow_document where class1=" +
                     StrUtil.sqlstr(code);
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sql);
            if (rs != null && rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
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
            LogUtil.getLog(getClass()).error("list: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
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

    public synchronized boolean UpdateIsHome(boolean isHome) throws
            ErrMsgException {
        String sql = "update flow_document set isHome=? where id=?";
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
            LogUtil.getLog(getClass()).error(e.getMessage());
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
            LogUtil.getLog(getClass()).error(e.getMessage());
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
                        "insert document_attach (fullpath,doc_id,name,diskname,visualpath,page_num) values (" +
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
            re = conn.executePreUpdate()==1?true:false;
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id);
        } catch (SQLException e) {
            re = false;
            LogUtil.getLog(getClass()).error(e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    public synchronized boolean updateTemplateId() {
        String sql = "update flow_document set template_id=? where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, templateId);
            pstmt.setInt(2, id);
            re = conn.executePreUpdate() == 1;
            if (re) {
                // 更新缓存
                DocCacheMgr dcm = new DocCacheMgr();
                dcm.refreshUpdate(id);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        finally {
            conn.close();
        }
        return re;
    }

    /**
     * 用以创建流程时同步生成文件
     * @throws ErrMsgException
     */
    public boolean create(String code_class1, String title, String content,
                          int type, String voteoption, String voteresult,
                          String nick, int templateId, String author) {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(code_class1);
        parentCode = lf.getParentCode();
        Conn conn = new Conn(connname);
        this.id = (int) SequenceManager.nextID(SequenceManager.OA_DOCUMENT_FLOW);
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
            pstmt.setTimestamp(17, new Timestamp(new java.util.Date().getTime()));
            pstmt.setLong(18, flowId);
            conn.executePreUpdate();

            // 20201227 注释，因启用了form_archive
            // 插入文章中的内容
            // DocContent dc = new DocContent();
            // dc.create(id, content);

        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create2:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    @Override
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
            String sql = "select page_num from flow_doc_content where doc_id=" + id;
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
            LogUtil.getLog(getClass()).error(e.getMessage());
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {LogUtil.getLog(getClass()).error(e);}
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
                LogUtil.getLog(getClass()).error("文档 " + id +
                             " 在数据库中未找到.");
            } else {
                this.title = rs.getString(1);
                this.class1 = rs.getString(2);
                this.modifiedDate = rs.getDate(3);
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
                this.flowId = rs.getLong(21);
                loaded = true; // 已初始化
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("loadFromDB:" + e.getMessage());
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
        return "Flow document " + id + ":" + title;
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

    public String getVoteResult() {
        return voteResult;
    }

    public String getNick() {
        return nick;
    }

    public String getModifiedDate() {
        Date d = modifiedDate;
        if (d != null)
            return d.toString().substring(0, 10);
        else
            return "";
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

            String sql = "update flow_document set voteresult=" +
                         StrUtil.sqlstr(result)
                         + " where id=" + id;
            LogUtil.getLog(getClass()).info(sql);
            re = conn.executeUpdate(sql) == 1 ? true : false;
            // 更新缓存
            DocCacheMgr dcm = new DocCacheMgr();
            dcm.refreshUpdate(id);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("vote:" + e.getMessage());
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
        //可能取得的infoBlock中的元素的顺序号小于endIndex
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
        return getDirCode();
        // return flowTypeCode;
    }

    private int hit = 0;
    private int templateId = NOTEMPLATE;

    public synchronized boolean UpdatePageCount(int pagecount) throws
            ErrMsgException {
        String sql = "update flow_document set modifiedDate=?,page_count=? where id=?";
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
            LogUtil.getLog(getClass()).error(e.getMessage());
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

    public Vector<IAttachment> getAttachments(int pageNum) {
        DocContent dc = new DocContent();
        dc = dc.getDocContent(id, pageNum);
        if (dc==null) {
            return null;
        }
        return dc.getAttachments();
    }

    /**
     * 判断是否已有附件被盖章
     * @param
     * @return
     */
    public boolean isSealed() {
        Vector<IAttachment> v = getAttachments(1);
        for (IAttachment a : v) {
            if (a.isSealed()) {
                return true;
            }
        }
        return false;
    }

    public Attachment getAttachment(int pageNum, int att_id) {
        Iterator ir = getAttachments(pageNum).iterator();
        while (ir.hasNext()) {
            Attachment at = (Attachment)ir.next();
            if (at.getId()==att_id) {
                return at;
            }
        }
        return null;
    }

    public Document getTemplate() {
        if (templateId == NOTEMPLATE) {
            return null;
        } else {
            return getDocument(templateId);
        }
    }

    private int isNew = 0;
    private String author;

    /**
     * 暂无用，流程类型存于class1字段中
     */
    @Deprecated
    private String flowTypeCode = "";

    private long flowId = 0;

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}
}

