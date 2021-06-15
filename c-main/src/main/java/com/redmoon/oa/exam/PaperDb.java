package com.redmoon.oa.exam;

import java.sql.*;
import java.util.*;
import java.util.Date;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.oa.db.SequenceManager;

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
public class PaperDb extends ObjectDb {
    private int id;

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;

    public static final int MULTI_ALL_RIGHT_SCORE = 0;
    public static final int MULTI_NOT_ALL_RIGHT_SCORE = 1;

    /**
     * 按指定时间考试
     */
    public static final int MODE_SPECIFY = 0;
    /**
     * 按有效期考试
     */
    public static final int MODE_PERIOD = 1;

    public PaperDb() {
        init();
    }

    public PaperDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "oa_exam_paper";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new PaperCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE =
                "insert into " + tableName + " (subject,totalper,singlecount,singleper,multicount,multiper,judgecount,judgeper,starttime,endtime,settime,defaultSet,limitCount,title,id,testtime,major,is_manual,single_total,multi_total,judge_total,answercount,answerper,answer_total,multi_score_rule,not_all_right_multiper,mode) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set subject=?,totalper=?,singlecount=?,singleper=?,multicount=?,multiper=?,judgecount=?,judgeper=?,starttime=?,endtime=?,settime=?,defaultSet=?,limitCount=?,title=?,testtime=?,major=?,is_manual=?,single_total=?,multi_total=?,judge_total=?,answercount=?,answerper=?,answer_total=?,multi_score_rule=?,not_all_right_multiper=?,mode=? where id=?";
        QUERY_LIST = "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select subject,totalper,singlecount,singleper,multicount,multiper,judgecount,judgeper,starttime,endtime,settime ,defaultSet,limitCount,title,testtime,major,is_manual,single_total,multi_total,judge_total,answercount,answerper,answer_total,multi_score_rule,not_all_right_multiper,mode from oa_exam_paper where id=?";
    }

    public String getSearchSql(String userName, String title, String major, String beginDate, String endDate) {
        Date ed = DateUtil.parse(endDate, "yyyy-MM-dd");
        if (ed != null) {
            ed = DateUtil.addDate(ed, 1);
        }
        String sql;
        String sqlExamMajor = MajorPriv.getMajorsOfUser(userName);
        if (sqlExamMajor.equals("")) {
            sql = "select id from oa_exam_paper where 1=1";
        } else {
            sql = "select id from oa_exam_paper where major in (" + sqlExamMajor + ")";
        }
        if (!"".equals(major) && !"exam_major".equals(major)) {
            sql += " and major =" + StrUtil.sqlstr(major);
        }
        if (!"".equals(beginDate)) {
            sql += " and starttime >=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
        }
        if (!"".equals(endDate)) {
            sql += " and endtime <=" + SQLFilter.getDateStr(DateUtil.format(ed, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (!title.equals(""))
            sql += " and title like " + StrUtil.sqlstr("%" + title + "%");

        sql += " order by id desc";
        return sql;
    }

    public PaperDb getPaperDb(int id) {
        return (PaperDb) getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, subject);
            ps.setInt(2, totalper);
            ps.setInt(3, singleCount);
            ps.setInt(4, singleper);
            ps.setInt(5, multiCount);
            ps.setInt(6, multiper);
            ps.setInt(7, judgeCount);
            ps.setInt(8, judgeper);
            if (startTime != null) {
                ps.setTimestamp(9, new java.sql.Timestamp(startTime.getTime()));
            } else {
                ps.setDate(9, null);
            }
            if (endTime != null) {
                ps.setTimestamp(10, new java.sql.Timestamp(endTime.getTime()));
            } else {
                ps.setDate(10, null);
            }
            if (setTime != null) {
                ps.setDate(11, new java.sql.Date(setTime.getTime()));
            } else {
                ps.setDate(11, null);
            }
            ps.setBoolean(12, defaultSet);
            ps.setInt(13, limitCount);
            ps.setString(14, title);
            id = (int) SequenceManager.nextID(SequenceManager.EXAM_PAPER);
            ps.setInt(15, id);
            ps.setInt(16, testTime);
            ps.setString(17, major);
            ps.setInt(18, manual ? 1 : 0);
            ps.setInt(19, singleTotal);
            ps.setInt(20, multiTotal);
            ps.setInt(21, judgeTotal);
            ps.setInt(22, answerCount);
            ps.setInt(23, answerper);
            ps.setInt(24, answerTotal);
            ps.setInt(25, multiScoreRule);
            ps.setInt(26, notAllRightMuntiper);
            ps.setInt(27, mode);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                PaperCache rc = new PaperCache(this);
                rc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作失败！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * del
     *
     * @return boolean
     */
    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                PaperCache rc = new PaperCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("del: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new PaperDb(pk.getIntValue());
    }

    /**
     * load
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            // subject,totalper,singlecount,singleper,multicount,multiper,judgecount,judgeper,testtime,starttime,mark
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                subject = rs.getString(1);
                totalper = rs.getInt(2);
                singleCount = rs.getInt(3);
                singleper = rs.getInt(4);
                multiCount = rs.getInt(5);
                multiper = rs.getInt(6);
                judgeCount = rs.getInt(7);
                judgeper = rs.getInt(8);
                startTime = rs.getTimestamp(9);
                endTime = rs.getTimestamp(10);
                setTime = rs.getDate(11);
                defaultSet = rs.getBoolean(12);
                limitCount = rs.getInt(13);
                title = rs.getString(14);
                testTime = rs.getInt(15);
                major = rs.getString(16);
                manual = rs.getInt(17) == 1;
                singleTotal = rs.getInt(18);
                multiTotal = rs.getInt(19);
                judgeTotal = rs.getInt(20);
                answerCount = rs.getInt(21);
                answerper = rs.getInt(22);
                answerTotal = rs.getInt(23);
                multiScoreRule = rs.getInt(24);
                notAllRightMuntiper = rs.getInt(25);
                mode = rs.getInt(26);
                loaded = true;
                primaryKey.setValue(new Integer(id));
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

    /**
     * save
     *
     * @return boolean
     */
    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, subject);
            ps.setInt(2, totalper);
            ps.setInt(3, singleCount);
            ps.setInt(4, singleper);
            ps.setInt(5, multiCount);
            ps.setInt(6, multiper);
            ps.setInt(7, judgeCount);
            ps.setInt(8, judgeper);
            if (startTime != null) {
                ps.setTimestamp(9, new java.sql.Timestamp(startTime.getTime()));
            } else {
                ps.setDate(9, null);
            }
            if (endTime != null) {
                ps.setTimestamp(10, new java.sql.Timestamp(endTime.getTime()));
            } else {
                ps.setDate(10, null);
            }
            if (setTime != null) {
                ps.setDate(11, new java.sql.Date(setTime.getTime()));
            } else {
                ps.setDate(11, null);
            }
            ps.setBoolean(12, defaultSet);
            ps.setInt(13, limitCount);
            ps.setString(14, title);
            ps.setInt(15, testTime);
            ps.setString(16, major);
            ps.setInt(17, manual ? 1 : 0);
            ps.setInt(18, singleTotal);
            ps.setInt(19, multiTotal);
            ps.setInt(20, judgeTotal);
            ps.setInt(21, answerCount);
            ps.setInt(22, answerper);
            ps.setInt(23, answerTotal);
            ps.setInt(24, multiScoreRule);
            ps.setInt(25, notAllRightMuntiper);
            ps.setInt(26, mode);
            ps.setInt(27, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                PaperCache rc = new PaperCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("save: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * 取出全部信息置于result中
     */
    public Vector list(String sql) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        try {
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    result.addElement(getPaperDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            logger.error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    /**
     * 列出当前待考或已考的试卷
     *
     * @return
     */
    public ListResult listExam(String userName, String major, int kind, int curPage, int pageSize) throws
            ErrMsgException {
        String curDate = DateUtil.format(DateUtil.addDate(new java.util.Date(), 1), "yyyy-MM-dd");
        // 待考
        if (kind == 1) {
            String sql = "select id from oa_exam_paper p where starttime<=" + SQLFilter.getDateStr(curDate, "yyyy-MM-dd") + " and endtime>=" + SQLFilter.getDateStr(curDate, "yyyy-MM-dd");
            if (!"".equals(major) && !"exam_major".equals(major)) {
                sql += " and major = " + StrUtil.sqlstr(major);
            }
            sql += " order by id asc";
            return listResult(sql, curPage, pageSize);
        } else {
            // 已考
            String sql = "select distinct p.id from oa_exam_paper p, oa_exam_score s where s.paperid=p.id and s.userName=" + StrUtil.sqlstr(userName);
            if (!"".equals(major) && !"exam_major".equals(major)) {
                sql += " and major = " + StrUtil.sqlstr(major);
            }
            sql += " order by id asc";
            return listResult(sql, curPage, pageSize);
        }
    }

    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();

        ListResult lr = new ListResult();
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

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

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
                    PaperDb ug = getPaperDb(rs.getInt(1));
                    result.addElement(ug);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setSingleper(int singleper) {
        this.singleper = singleper;
    }

    public void setMultiCount(int multiCount) {
        this.multiCount = multiCount;
    }

    public void setMultiper(int multiper) {
        this.multiper = multiper;
    }

    public void setJudgeCount(int judgeCount) {
        this.judgeCount = judgeCount;
    }

    public void setStartTime(java.util.Date startTime) {
        this.startTime = startTime;
    }

    public void setJudgeper(int judgeper) {
        this.judgeper = judgeper;
    }

    public void setTotalper(int totalper) {
        this.totalper = totalper;
    }

    public void setSingleCount(int singleCount) {
        this.singleCount = singleCount;
    }

    public void setEndtime(java.util.Date endTime) {
        this.endTime = endTime;
    }

    public void setSetTime(java.util.Date setTime) {
        this.setTime = setTime;
    }

    public void setDefaultSet(boolean defaultSet) {
        this.defaultSet = defaultSet;
    }

    public void setLimitCount(int limitCount) {
        this.limitCount = limitCount;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }


    public int getSingleper() {
        return singleper;
    }

    public int getMultiCount() {
        return multiCount;
    }

    public int getMultiper() {
        return multiper;
    }

    public int getJudgeCount() {
        return judgeCount;
    }

    public java.util.Date getStartTime() {
        return startTime;
    }

    public int getJudgeper() {
        return judgeper;
    }

    public int getTotalper() {
        return totalper;
    }

    public int getSingleCount() {
        return singleCount;
    }

    public java.util.Date getEndTime() {
        return endTime;
    }

    public java.util.Date getSetTime() {
        return setTime;
    }

    public boolean getDefaultSet() {
        return defaultSet;
    }

    public int getLimitCount() {
        return limitCount;
    }

    public String getTitle() {
        return title;
    }

    public int getTestTime() {
        return testTime;
    }

    public void setTesttime(int testTime) {
        this.testTime = testTime;
    }

    /**
     * 科目，已无用
     */
    private String subject;
    /**
     * 总分
     */
    private int totalper;
    /**
     * 单选题数量
     */
    private int singleCount;
    /**
     * 单选题分值/题
     */
    private int singleper;
    /**
     * 多选题数量
     */
    private int multiCount;
    /**
     * 多选题分值/题
     */
    private int multiper;
    /**
     * 判断题数量
     */
    private int judgeCount;
    /**
     * 判断题分值/题
     */
    private int judgeper;
    /**
     * 考试时长
     */
    private int testTime;
    /**
     * 开始时间
     */
    private java.util.Date startTime;
    /**
     * 结束时间
     */
    private java.util.Date endTime;
    /**
     * 无用
     */
    private java.util.Date setTime;
    /**
     * 无用
     */
    private boolean defaultSet;
    /**
     * 无用
     */
    private int limitCount = 1;
    /**
     * 试卷名称
     */
    private String title;
    /**
     * 专业
     */
    private String major;
    /**
     * 是否手工组卷
     */
    private boolean manual;
    /**
     * 单选题总分
     */
    int singleTotal;
    /**
     * 多选题总分
     */
    int multiTotal;
    /**
     * 判断题总分
     */
    int judgeTotal;
    /**
     * 问答题数量
     */
    int answerCount;
    /**
     * 问答题分值/题
     */
    int answerper;
    /**
     * 问答题总分
     */
    int answerTotal;
    /**
     * 多选题部分答对是否计分，0表示部分答对不记分，1表示部分答对记分
     */
    int multiScoreRule;
    /**
     * 部分答对计分的分值
     */
    int notAllRightMuntiper;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * 模式，0指定时间段，1有效期
     */
    int mode;

    /**
     * @return the notAllRightMuntiper
     */
    public int getNotAllRightMuntiper() {
        return notAllRightMuntiper;
    }

    /**
     * @param notAllRightMuntiper the notAllRightMuntiper to set
     */
    public void setNotAllRightMuntiper(int notAllRightMuntiper) {
        this.notAllRightMuntiper = notAllRightMuntiper;
    }

    /**
     * @return the singleTotal
     */
    public int getSingleTotal() {
        return singleTotal;
    }

    /**
     * @param singleTotal the singleTotal to set
     */
    public void setSingleTotal(int singleTotal) {
        this.singleTotal = singleTotal;
    }

    /**
     * @return the multiTotal
     */
    public int getMultiTotal() {
        return multiTotal;
    }

    /**
     * @param multiTotal the multiTotal to set
     */
    public void setMultiTotal(int multiTotal) {
        this.multiTotal = multiTotal;
    }

    /**
     * @return the judgeTotal
     */
    public int getJudgeTotal() {
        return judgeTotal;
    }

    /**
     * @param judgeTotal the judgeTotal to set
     */
    public void setJudgeTotal(int judgeTotal) {
        this.judgeTotal = judgeTotal;
    }

    /**
     * @return the major
     */
    public String getMajor() {
        return major;
    }

    /**
     * @param major the major to set
     */
    public void setMajor(String major) {
        this.major = major;
    }

    /**
     * @return the manual
     */
    public boolean isManual() {
        return manual;
    }

    /**
     * @param manual the manual to set
     */
    public void setManual(boolean manual) {
        this.manual = manual;
    }

    /**
     * @return the answercount
     */
    public int getAnswerCount() {
        return answerCount;
    }

    /**
     * @param answerCount the answerCount to set
     */
    public void setAnswerCount(int answerCount) {
        this.answerCount = answerCount;
    }

    /**
     * @return the answerper
     */
    public int getAnswerper() {
        return answerper;
    }

    /**
     * @param answerper the answerper to set
     */
    public void setAnswerper(int answerper) {
        this.answerper = answerper;
    }

    /**
     * @return the answerTotal
     */
    public int getAnswerTotal() {
        return answerTotal;
    }

    /**
     * @param answerTotal the answerTotal to set
     */
    public void setAnswerTotal(int answerTotal) {
        this.answerTotal = answerTotal;
    }

    /**
     * @return the multiScoreRule
     */
    public int getMultiScoreRule() {
        return multiScoreRule;
    }

    /**
     * @param multiScoreRule the multiScoreRule to set
     */
    public void setMultiScoreRule(int multiScoreRule) {
        this.multiScoreRule = multiScoreRule;
    }
}
