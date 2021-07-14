package com.redmoon.oa.exam;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.oa.db.SequenceManager;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

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
public class UserAnswerDb extends ObjectDb {
    private int id;

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;

    /**
     * 问答题未评分，用于is_correct字段，该字段为0时表示答题错误，为1时表示答题正确
     */
    public static final int ANSWER_NOT_CHECKED = 2;
    /**
     * 问题答题已评分
     */
    public static final int ANSWER_CHECKED = 3;

    public UserAnswerDb() {
        init();
    }

    public UserAnswerDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "oa_exam_useranswer";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new UserAnswerCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE =
                "insert into " + tableName + " (score_id,question_id,user_answer,is_correct,id,score) values (?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set score_id=?,question_id=?,user_answer=?,is_correct=?,score=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select score_id,question_id,user_answer,is_correct,score from " +
                tableName + " where id=?";
    }

    public UserAnswerDb getUserAnswerDb(int id) {
        return (UserAnswerDb) getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, scoreId);
            ps.setInt(2, questionId);
            ps.setString(3, userAnswer);
            ps.setInt(4, isCorrect);
            id = (int) SequenceManager.nextID(SequenceManager.EXAM_USERANSWER);
            ps.setInt(5, id);
            ps.setInt(6, score);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                UserAnswerCache rc = new UserAnswerCache(this);
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
                UserAnswerCache rc = new UserAnswerCache(this);
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
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new UserAnswerDb(pk.getIntValue());
    }

    /**
     * load
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            // test_id,question_id,user_answer,is_correct,multicount,multiper,judgecount,judgeper,testtime,starttime,mark
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                scoreId = rs.getInt(1);
                questionId = rs.getInt(2);
                userAnswer = rs.getString(3);
                isCorrect = rs.getInt(4);
                score = rs.getInt(5);
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

    public Vector getUserAnswersOfScore(int scoreId) {
        String sql = "select id from " + tableName + " where score_id=" + scoreId;
        return list(sql);
    }

    public void delOfScore(int scoreId) {
        String sql = "delete from " + tableName + " where score_id=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            jt.executeUpdate(sql, new Object[]{new Integer(scoreId)});
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
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
            ps.setInt(1, scoreId);
            ps.setInt(2, questionId);
            ps.setString(3, userAnswer);
            ps.setInt(4, isCorrect);
            ps.setInt(5, score);
            ps.setInt(6, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                UserAnswerCache rc = new UserAnswerCache(this);
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
                    result.addElement(getUserAnswerDb(rs.getInt(1)));
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
                    UserAnswerDb ug = getUserAnswerDb(rs.getInt(1));
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

    public void setScoreId(int scoreId) {
        this.scoreId = scoreId;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public int getScoreId() {
        return scoreId;
    }


    public boolean isCorrect() {
        return correct;
    }

    public int getQuestionId() {
        return questionId;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    private int scoreId;
    private int questionId;
    private String userAnswer;
    private boolean correct;
    private int isCorrect;
    private int score;

    /**
     * @return the score
     */
    public int getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * @return the isCorrect
     */
    public int getIsCorrect() {
        return isCorrect;
    }

    /**
     * @param isCorrect the isCorrect to set
     */
    public void setIsCorrect(int isCorrect) {
        this.isCorrect = isCorrect;
    }
}
