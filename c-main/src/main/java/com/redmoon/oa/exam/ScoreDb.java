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
 * <p>Title: 答卷</p>
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
public class ScoreDb extends ObjectDb {
    private int id;

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;

    public ScoreDb() {
        init();
    }

    public ScoreDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "oa_exam_score";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new ScoreCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE =
                "insert into " + tableName + " (userName,paperId,endtime,score,mobile,id,is_prj,remarks) values (?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set userName=?,paperId=?,endtime=?, score=? ,mobile=?,is_prj=?,remarks=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select userName,paperId,endtime,score,mobile,is_prj,remarks from " + tableName + " where id=?";
    }

    public ScoreDb getScoreDb(int id) {
        return (ScoreDb) getObjectDb(new Integer(id));
    }

    /**
     * 判断用户是否已参加过考试
     * @param paperId int
     * @return boolean
     */
    public boolean isUserExamed(String userName, int paperId) {
        return getScoreOfPaper(userName, paperId).size()>0;
    }

    /**
     * 取得用户的得分，按分值从大到小排
     * @param userName String
     * @param paperId int 试卷号
     * @return int
     */
    public Vector getScoreOfPaper(String userName, int paperId) {
        String sql = "select id from " + tableName + " where userName=" + StrUtil.sqlstr(userName) + " and paperid=" + paperId + " order by score desc";
        return list(sql);
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, userName);
            ps.setInt(2, paperId);
            if (endtime != null) {
                ps.setTimestamp(3, new java.sql.Timestamp(endtime.getTime()));
            } else {
                ps.setDate(3, null);
            }
            ps.setInt(4, score);
            ps.setString(5, mobile);
            id = (int) SequenceManager.nextID(SequenceManager.EXAM_SCORE);
            ps.setInt(6, id);
            ps.setInt(7, isprj);
            ps.setString(8, remarks);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                ScoreCache rc = new ScoreCache(this);
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
                UserAnswerDb uad = new UserAnswerDb();
                uad.delOfScore(id);

                ScoreCache rc = new ScoreCache(this);
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
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new ScoreDb(pk.getIntValue());
    }

    /**
     * load
     *
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                userName = rs.getString(1);
                paperId = rs.getInt(2);
                endtime = rs.getTimestamp(3);
                score = rs.getInt(4);
                mobile = rs.getString(5);
                isprj = rs.getInt(6);
                remarks = rs.getString(7);
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
            ps.setString(1,userName);
            ps.setInt(2, paperId);
             if (endtime != null) {
                 ps.setDate(3, new java.sql.Date(endtime.getTime()));
             } else {
                 ps.setDate(3, null);
             }
            ps.setInt(4, score);
            ps.setString(5, mobile);
            ps.setInt(6, isprj);
            ps.setString(7, remarks);
            ps.setInt(8, id);
            System.out.println(getClass() + " score:"+ score + " id :" + id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                ScoreCache rc = new ScoreCache(this);
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
                    result.addElement(getScoreDb(rs.getInt(1)));
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
     * 取得卷子的分数
     * @param paperId
     * @return
     */
    public Vector getScores(int paperId) {
        String sql = "select id from oa_exam_score where paperid =" + paperId;
        return list(sql);
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
                    ScoreDb ug = getScoreDb(rs.getInt(1));
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

    public void setPaperId(int paperId) {
        this.paperId = paperId;
    }

    public void setEndtime(java.util.Date endtime) {
        this.endtime = endtime;
    }

    public void setScore(int score) {
        this.score = score;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUserName() {
        return userName;
    }

    public int getPaperId() {
        return paperId;
    }

    public java.util.Date getEndtime() {
        return endtime;
    }
    public int getScore() {
        return score;
    }

    public String getMobile() {
        return mobile;
    }

	public int getIsprj() {
		return isprj;
	}

	public void setIsprj(int isprj) {
		this.isprj = isprj;
	}
    // userName,paperId,singlecount,singleper,multicount,multiper,judgecount,judgeper,testtime,starttime,mark
    private String userName;
    private int paperId;
    private java.util.Date endtime;
    private int score;
    private String mobile;
    private int isprj;
    /**
	 * @return the remarks
	 */
	public String getRemarks() {
		return remarks;
	}

	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	private String remarks;

}
