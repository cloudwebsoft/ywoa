package com.redmoon.oa.exam;

import java.sql.*;

import java.util.*;

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
public class QuestionDb extends ObjectDb {
    private int id;

    public static final int TYPE_SINGLE = 0;
    public static final int TYPE_MULTI = 1;
    public static final int TYPE_JUDGE = 2;
    public static final int TYPE_ANSWER = 3;
    
    // 表示题目是否有效
    public static final int IS_VALID = 0;
    
    public QuestionDb() {
        init();
    }

    public QuestionDb(int id) {
        this.id = id;
        init();
        load();
    }

    public static String getTypeDesc(int type) {
        if (type==TYPE_SINGLE)
            return "单选题";
        else if (type==TYPE_MULTI)
            return "多选题";
        else if (type==TYPE_JUDGE)
            return "判断题";
        else if (type==TYPE_ANSWER)
            return "问答题";
        else
            return "";
    }

    public String getTypeDesc() {
        return getTypeDesc(type);
    }

    public int getId() {
        return id;
    }

    public void initDB() {
    	
        tableName = "oa_exam_database";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new QuestionCache(this);
        isInitFromConfigDB = false;
        // subject,type,question,chooseA,chooseB,chooseC,chooseD,chooseE,chooseF,answer,mark

        QUERY_CREATE =
                "insert into " + tableName + " (subject,exam_type,question,chooseA,chooseB,chooseC,chooseD,chooseE,chooseF,answer,id,major,is_valid) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set subject=?,exam_type=?,question=?,chooseA=?,chooseB=?,chooseC=?,chooseD=?,chooseE=?,chooseF=?,answer=?,major=?,is_valid=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        /**
         * 2017-10-26修改QUERY_LOAD 原因：新增试题原来科目保存的是名称，修改成保存为科目id；
         */
        QUERY_LOAD = "select subject,exam_type,question,chooseA,chooseB,chooseC,chooseD,chooseE,chooseF,answer,major,is_valid from oa_exam_database where id=?";
    }

    public QuestionDb getQuestionDb(int id) {
        return (QuestionDb) getObjectDb(new Integer(id));
    }
    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            //subject,type,question,chooseA,chooseB,chooseC,chooseD,chooseE,chooseF,answer,mark
            ps.setString(1,subject);
            ps.setInt(2, type);
            ps.setString(3, question);
            ps.setString(4, chooseA);
            ps.setString(5, chooseB);
            ps.setString(6, chooseC);
            ps.setString(7, chooseD);
            ps.setString(8, chooseE);
            ps.setString(9, chooseF);
            ps.setString(10, answer);
            id = (int)SequenceManager.nextID(SequenceManager.EXAM_QUESTION);
            ps.setInt(11, id);
            ps.setString(12, major);
            ps.setInt(13, isValid);

            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                QuestionCache rc = new QuestionCache(this);
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
     * 
     * @Description: 返回值为题目id的新建方法 用于文件上传保存题目id
     * @return
     * @throws ErrMsgException
     */
    public int create1() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            //subject,type,question,chooseA,chooseB,chooseC,chooseD,chooseE,chooseF,answer,mark
            ps.setString(1,subject);
            ps.setInt(2, type);
            ps.setString(3, question);
            ps.setString(4, chooseA);
            ps.setString(5, chooseB);
            ps.setString(6, chooseC);
            ps.setString(7, chooseD);
            ps.setString(8, chooseE);
            ps.setString(9, chooseF);
            ps.setString(10, answer);
            id = (int)SequenceManager.nextID(SequenceManager.EXAM_QUESTION);
            ps.setInt(11, id);
            ps.setString(12, major);
            ps.setInt(13, isValid);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                QuestionCache rc = new QuestionCache(this);
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
        return id;
    }
    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                QuestionCache rc = new QuestionCache(this);
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
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new QuestionDb(pk.getIntValue());
    }

    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
         // subject,type,question,chooseA,chooseB,chooseC,chooseD,chooseE,chooseF,answer,mark
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                subject = StrUtil.getNullStr(rs.getString(1));
                type = rs.getInt(2);
                question = StrUtil.getNullStr(rs.getString(3));
                chooseA = StrUtil.getNullStr(rs.getString(4));
                chooseB = StrUtil.getNullStr(rs.getString(5));
                chooseC = StrUtil.getNullStr(rs.getString(6));
                chooseD = StrUtil.getNullStr(rs.getString(7));
                chooseE = StrUtil.getNullStr(rs.getString(8));
                chooseF = StrUtil.getNullStr(rs.getString(9));
                answer = StrUtil.getNullStr(rs.getString(10));
                major = StrUtil.getNullStr(rs.getString(11));
                isValid = rs.getInt(12);
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
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1,subject);
            ps.setInt(2, type);
            ps.setString(3, question);
            ps.setString(4, chooseA);
            ps.setString(5, chooseB);
            ps.setString(6, chooseC);
            ps.setString(7, chooseD);
            ps.setString(8, chooseE);
            ps.setString(9, chooseF);
            ps.setString(10, answer);
            ps.setString(11, major);
            ps.setInt(12, isValid);
            ps.setInt(13, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                QuestionCache rc = new QuestionCache(this);
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
                    result.addElement(getQuestionDb(rs.getInt(1)));
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
                    QuestionDb ug = getQuestionDb(rs.getInt(1));
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

    public void setChooseA(String chooseA) {
        this.chooseA = chooseA;
    }

    public void setChooseB(String chooseB) {
        this.chooseB = chooseB;
    }

    public void setChooseC(String chooseC) {
        this.chooseC = chooseC;
    }

    public void setChooseD(String chooseD) {
        this.chooseD = chooseD;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setChooseE(String chooseE) {
        this.chooseE = chooseE;
    }

    public void setChooseF(String chooseF) {
        this.chooseF = chooseF;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
    public String getSubject() {
        return subject;
    }


    public String getChooseA() {
        return chooseA;
    }

    public String getChooseB() {
        return chooseB;
    }

    public String getChooseC() {
        return chooseC;
    }

    public String getChooseD() {
        return chooseD;
    }

    public String getAnswer() {
        return answer;
    }

    public String getChooseE() {
        return chooseE;
    }

    public String getChooseF() {
        return chooseF;
    }

    public int getType() {
        return type;
    }
   

    public String getQuestion() {
        return question;
    }
    
	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}
	
	public int getIsValid() {
		return isValid;
	}

	public void setIsValid(int isValid) {
		this.isValid = isValid;
	}

    // subject,type,question,chooseA,chooseB,chooseC,chooseD,chooseE,chooseF,answer,mark
    private String subject;
    private int type;
    private String question;
    private String chooseA;
    private String chooseB;
    private String chooseC;
    private String chooseD;
    private String chooseE;
    private String chooseF;
    private String answer;
    private String major;//新增字段专业专项
    private int isValid;

}
