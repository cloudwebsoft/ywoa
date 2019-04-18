package cn.js.fan.module.cms;

import java.sql.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.forum.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.io.File;
import cn.js.fan.web.Global;

/**
 * <p>Title: </p>
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
public class SubjectListDb extends ObjectDb {
    public static final int LEVEL_TOP = 100;

    public SubjectListDb() {
        init();
    }

    public SubjectListDb(long id) {
        this.id = id;
        init();
        load(new JdbcTemplate());
    }

    public void initDB() {
        this.tableName = "cws_cms_subject_doc";
        primaryKey = new PrimaryKey("code", PrimaryKey.TYPE_LONG);
        objectCache = new SubjectListCache(this);

        QUERY_CREATE = "insert into cws_cms_subject_doc (id, code, doc_id, doc_level, create_date) values (?,?,?,?,?)";
        QUERY_SAVE = "update cws_cms_subject_doc set doc_level=? where id=?";
        QUERY_LIST = "select id from cws_cms_subject_doc where code=? order by doc_level desc, doc_id desc";
        QUERY_DEL = "delete from cws_cms_subject_doc where id=?";
        QUERY_LOAD = "select code,doc_id,doc_level from cws_cms_subject_doc where code=?";
        isInitFromConfigDB = false;
    }

    /**
     * create
     *
     * @param jdbcTemplate JdbcTemplate
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this com.cloudwebsoft.framework.base.IObjectDb method
     */
    public boolean create(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException {
        id = SequenceMgr.nextID(SequenceMgr.CMS_SUBJECT_LIST_ID);
        try {
            jt.executeUpdate(QUERY_CREATE, new Object[]{new Long(id), code, new Integer(docId), new Integer(level), "" + DateUtil.toLong(createDate)});

            SubjectListCache slc = new SubjectListCache(this);
            slc.refreshCreate();
            slc.refreshList(code);
        }
        catch (SQLException e) {
            LogUtil.getLog("create:" + e.getMessage());
            throw new ResKeyException("err_db");
        }
        return false;
    }

    /**
     * 置文章所属的专题
     * @param jt JdbcTemplate
     * @param subjectCodes String[]
     * @param docId int
     * @return boolean
     * @throws ResKeyException
     * @throws ErrMsgException
     */
    public boolean setDocBelongtoSubjects(JdbcTemplate jt, String[] subjectCodes, int docId) throws
            ResKeyException, ErrMsgException {
        String[] oldSubjectCodes = getSubjectsOfDoc(docId);
        int len = subjectCodes.length;
        SubjectListDb sld = new SubjectListDb();
        boolean re = false;
        int oldlen = oldSubjectCodes.length;
        // 找出新增的所属专题，添加
        for (int i = 0; i < len; i++) {
            boolean isFound = false;
            for (int j=0; j < oldlen; j++) {
                if (oldSubjectCodes[j].equals(subjectCodes[i])) {
                    isFound = true;
                    break;
                }
            }
            // 在原来所属的专题中未找到，则说明是新增的
            if (!isFound) {
                sld.setDocId(docId);
                sld.setCode(subjectCodes[i]);
                re = sld.create(jt);
            }
        }
        // 找出被删除的所属旧专题，将其删除
        for (int i = 0; i < oldlen; i++) {
            boolean isFound = false;
            for (int j=0; j < len; j++) {
                if (oldSubjectCodes[i].equals(subjectCodes[j])) {
                    isFound = true;
                    break;
                }
            }
            // 在新的专题中未找到，则说明应该被删除
            if (!isFound) {
                sld = sld.getSubjectListDbOfDoc(oldSubjectCodes[i], docId);
                if (sld!=null)
                    sld.del(jt);
            }
        }
        return re;
    }

    public SubjectListDb getSubjectListDbOfDoc(String subjectCode, int docId) throws ResKeyException {
        String sql = "select id from cws_cms_subject_doc where code=? and doc_id=?";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{subjectCode, new Integer(docId)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                return getSubjectListDb(rr.getLong(1));
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getSubjectListDbOfDoc:" + e.getMessage());
            throw new ResKeyException("err_db");
        }
        return null;
    }

    /**
     * 取得文章所属的专题编号
     * @param docId int
     * @return String[] 编号的数组
     */
    public String[] getSubjectsOfDoc(int docId) {
        String sql = "select code from cws_cms_subject_doc where doc_id=?";
        String[] re = null;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql,
                                                new Object[] {new Integer(docId)});
            re = new String[ri.size()];
            int i = 0;
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                re[i] = rr.getString(1);
                i++;
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getSubjectsOfDoc:" + e.getMessage());
        }
        return re;
    }

    public void delBatch(HttpServletRequest request) throws ErrMsgException, ResKeyException {
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids==null)
            return;
        int len = ids.length;
        JdbcTemplate jt = new JdbcTemplate();
        for (int i=0; i<len; i++) {
            getSubjectListDb(Long.parseLong(ids[i])).del(jt);
        }
    }

    /**
     * del
     *
     * @param jdbcTemplate JdbcTemplate
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this com.cloudwebsoft.framework.base.IObjectDb method
     */
    public boolean del(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException {
        String sql = "delete from cws_cms_subject_doc where id=?";
        int ret = 0;
        try {
            ret = jt.executeUpdate(sql, new Object[] {new Long(id)});

            SubjectListCache slc = new SubjectListCache(this);
            slc.refreshDel(primaryKey);
            slc.refreshList(code);
        }
        catch (SQLException e) {
            throw new ResKeyException("err_db");
        }
        return ret==1;
    }

    /**
     * getObjectRaw
     *
     * @param primaryKey PrimaryKey
     * @return IObjectDb
     * @todo Implement this com.cloudwebsoft.framework.base.IObjectDb method
     */
    public IObjectDb getObjectRaw(PrimaryKey pk) {
        return new SubjectListDb(pk.getLongValue());
    }

    /**
     * load
     *
     * @param jdbcTemplate JdbcTemplate
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this com.cloudwebsoft.framework.base.IObjectDb method
     */
    public void load(JdbcTemplate jt) {
        String sql = "select code,doc_id,doc_level from cws_cms_subject_doc where id=?";
        try {
            ResultIterator ri = jt.executeQuery(sql,
                                                new Object[] {new Long(id)});
            int i = 0;
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                code = rr.getString(1);
                docId = rr.getInt(2);
                level = rr.getInt(3);

                primaryKey.setValue(new Long(id));
                loaded = true;
                i++;
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        }
    }

    /**
     * save
     *
     * @param jdbcTemplate JdbcTemplate
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this com.cloudwebsoft.framework.base.IObjectDb method
     */
    public boolean save(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException {
        String sql = "update cws_cms_subject_doc set doc_level=? where id=?";
        int ret = 0;
        try {
            ret = jt.executeUpdate(sql, new Object[] {new Integer(level), new Long(id)});
            SubjectListCache slc = new SubjectListCache(this);
            slc.refreshSave(primaryKey);
            slc.refreshList(code);
        }
        catch (SQLException e) {
            throw new ResKeyException("err_db");
        }
        return ret==1;
    }

    public SubjectListDb getSubjectListDb(long id) {
        return (SubjectListDb)getObjectDb(new Long(id));
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public int getDocId() {
        return docId;
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public int getLevel() {
        return level;
    }

    public java.util.Date getCreateDate() {
        return createDate;
    }

    public int getDocCount(String sql) {
        DocCacheMgr dcm = new DocCacheMgr();
        return dcm.getDocCount(sql);
    }

    protected long[] getDocBlock(String query, String groupKey, int startIndex) {
        SubjectListCache slc = new SubjectListCache(this);
        return slc.getDocBlock(query, groupKey, startIndex);
    }

    public DocBlockIterator getDocuments(String query, String groupKey,
                                         int startIndex,
                                         int endIndex) {
        // 可能取得的infoBlock中的元素的顺序号小于endIndex
        long[] docBlock = getDocBlock(query, groupKey, startIndex);

        return new DocBlockIterator(docBlock, query, groupKey,
                                    startIndex, endIndex);
    }

    public static String getDocHtmlPath(String subjectCode, Document doc) {
        java.util.Date d = doc.getCreateDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String year = "" + (cal.get(cal.YEAR));
        String month = "" + (cal.get(cal.MONTH) + 1);
        return "doc/subject/" + subjectCode + "/" + year + "/" + month;
    }

    public static String getDocHtmlName(String subjectCode, Document doc, int pageNum) {
        Config cfg = new Config();
        return getDocHtmlPath(subjectCode, doc) + "/" + doc.getId() + "_" + pageNum + "." + cfg.getProperty("cms.html_ext");
    }

    private int docId;
    private long id;
    private String code;
    private int level = 0;
    private java.util.Date createDate;
}
