package com.redmoon.forum.plugin;

import java.sql.*;
import java.util.*;

import com.cloudwebsoft.framework.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.plugin.base.*;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;
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
public class ScoreRecordDb extends ObjectDb {
    public static final String OPERATION_EXCHANGE = "exchange";
    public static final String OPERATION_PAY = "pay";
    public static final String OPERATION_TRANSFER = "transfer";

    public static final int OP_TYPE_NONE = -1;

    public static final int OP_TYPE_REGIST = 0;
    public static final int OP_TYPE_LOGIN = 1;
    public static final int OP_TYPE_ADD_NEW = 2;
    public static final int OP_TYPE_ADD_REPLY = 3;
    public static final int OP_TYPE_DEL_MSG = 4;
    public static final int OP_TYPE_ADD_ATTACHMENT = 5;
    public static final int OP_TYPE_DEL_ATTACHMENT = 6;
    public static final int OP_TYPE_SET_ELITE = 7;

    public static final long MSG_ID_NONE = 0; // 0表示记录与贴子ID无关联

    public ScoreRecordDb() {
    }

    public ScoreRecordDb(int id) {
        this.id = id;
        init();
        load(new JdbcTemplate());
    }

    public void initDB() {
        this.tableName = "sq_score_record";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new ScoreRecordCache(this);

        this.QUERY_CREATE =
                "insert into " + tableName + " (id, title, lydate, buyer, seller, from_score, to_score, operation, from_value, to_value, msg_id, op_type, op_desc) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        this.QUERY_SAVE =
                "update " + tableName + " set title=?, lydate=?, buyer=?, seller=?, from_score=?, to_score=?, operation=?, from_value=?, to_value=? where id=?";
        this.QUERY_DEL =
                "delete from " + tableName + " where id=?";
        this.QUERY_LOAD =
                "select title, lydate, buyer, seller, from_score, to_score, operation, from_value, to_value,msg_id,op_type,op_desc from " + tableName + " where id=?";
        this.QUERY_LIST = "select id from " + tableName ;
        isInitFromConfigDB = false;
    }

    public ScoreRecordDb getScoreRecordDb(int id) {
        return (ScoreRecordDb) getObjectDb(new Integer(id));
    }

    public IObjectDb getObjectRaw(PrimaryKey pk) {
        return new ScoreRecordDb(pk.getIntValue());
    }

    public boolean create() {
        return create(new JdbcTemplate());
    }

    public boolean create(JdbcTemplate jt) {
        id = (int) SequenceMgr.nextID(SequenceMgr.SQ_SCORE_RECORD);
        lydate = Long.toString(System.currentTimeMillis());
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_CREATE, new Object[] {new Integer(id), title, lydate, buyer, seller, fromScore, toScore, operation, new Double(fromValue), new Double(toValue), new Long(msgId), new Integer(opType), opDesc})==1;
            if (re) {
                ScoreRecordCache src = new ScoreRecordCache(this);
                src.refreshCreate();
                // 记录得分
                ScoreLogDb sld = new ScoreLogDb();
                try {
                    if (!buyer.equals(IPluginScore.SELLER_SYSTEM)) {
                        sld.create(new JdbcTemplate(), new Object[] {
                            new Long(SequenceMgr.nextID(SequenceMgr.
                                    SQ_SCORE_LOG)),
                                    buyer, fromScore, new Float(-fromValue),
                                    new Long(id), new java.util.Date()
                        });
                    }
                    if (!seller.equals(IPluginScore.SELLER_SYSTEM)) {
                        sld.create(new JdbcTemplate(), new Object[] {
                            new Long(SequenceMgr.nextID(SequenceMgr.
                                    SQ_SCORE_LOG)),
                                    seller, fromScore, new Float(fromValue),
                                    new Long(id), new java.util.Date()
                        });
                    }
                }
                catch (ResKeyException e) {
                    LogUtil.getLog("com.redmoon.forum.plugin.ScoreRecordDb:").error("create:" + StrUtil.trace(e));
                }
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        }
        return re;
    }

    /**
     * 取得系统操作类型的描述
     * @param request HttpServletRequest
     * @param opType int
     * @return String
     */
    public static String getSysOperateDesc(HttpServletRequest request, int opType) {
        if (opType==OP_TYPE_NONE)
            return "";
        else
            return SkinUtil.LoadString(request, "res.forum.plugin.ScoreRecordDb", "OP_" + opType);
    }

    public static boolean recordSysOperate(String userName, long msgId, String scoreCode, int value, String operation, int opType) {
        Config cfg = Config.getInstance();
        if (!cfg.getBooleanProperty("forum.isRecordSysOperation")) {
            return false;
        }
        ScoreRecordDb srd = new ScoreRecordDb();
        if (value>=0) {
            srd.setBuyer(IPluginScore.SELLER_SYSTEM);
            srd.setSeller(userName);
        }
        else {
            srd.setSeller(IPluginScore.SELLER_SYSTEM);
            srd.setBuyer(userName);
            value = -value;
        }
        srd.setFromScore(scoreCode);
        srd.setToScore(scoreCode);
        srd.setFromValue(value);
        srd.setToValue(value);
        srd.setOperation(operation);
        srd.setMsgId(msgId);
        srd.setOpType(opType);
        return srd.create(new JdbcTemplate());
    }

    public boolean del(JdbcTemplate jt) throws ErrMsgException {
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_DEL, new Object[] {new Integer(id)})==1;
            if (re) {
                ScoreRecordCache src = new ScoreRecordCache(this);
                primaryKey.setValue(new Integer(id));
                src.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("del: " + e.getMessage());
        }
        return re;
    }

    public boolean save(JdbcTemplate jt) throws ErrMsgException {
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_SAVE, new Object[] {title, lydate, buyer, seller, fromScore, toScore, operation, new Double(fromValue), new Double(toValue), new Integer(id)})==1;
            if (re) {
                ScoreRecordCache src = new ScoreRecordCache(this);
                primaryKey.setValue(new Integer(id));
                src.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("save: " + e.getMessage());
        }
        return re;
    }

    public void load(JdbcTemplate jt) {
        try {
            ResultIterator ri = jt.executeQuery(QUERY_LOAD, new Object[] {new Integer(id)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                title = StrUtil.getNullStr(rr.getString(1));
                lydate = StrUtil.getNullStr(rr.getString(2));
                buyer = StrUtil.getNullStr(rr.getString(3));
                seller = StrUtil.getNullStr(rr.getString(4));
                fromScore = StrUtil.getNullStr(rr.getString(5));
                toScore = StrUtil.getNullStr(rr.getString(6));
                operation = StrUtil.getNullStr(rr.getString(7));
                fromValue = rr.getDouble(8);
                toValue = rr.getDouble(9);
                msgId = rr.getLong(10);
                opType = rr.getInt(11);
                opDesc = StrUtil.getNullStr(rr.getString(12));
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            logger.error("load: " + e.getMessage());
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
                    ScoreRecordDb srd = getScoreRecordDb(rs.getInt(1));
                    result.addElement(srd);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
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

    public void setId(int id) {
        this.id = id;
    }

    public void setLydate(String lydate) {
        this.lydate = lydate;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public void setFromScore(String fromScore) {
        this.fromScore = fromScore;
    }

    public void setToScore(String toScore) {
        this.toScore = toScore;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFromValue(double fromValue) {
        this.fromValue = fromValue;
    }

    public void setToValue(double toValue) {
        this.toValue = toValue;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public void setOpType(int opType) {
        this.opType = opType;
    }

    public void setOpDesc(String opDesc) {
        this.opDesc = opDesc;
    }

    public int getId() {
        return id;
    }

    public String getLydate() {
        return lydate;
    }

    public String getBuyer() {
        return buyer;
    }

    public String getSeller() {
        return seller;
    }

    public String getFromScore() {
        return fromScore;
    }

    public String getToScore() {
        return toScore;
    }

    public String getOperation() {
        return operation;
    }

    public String getTitle() {
        return title;
    }

    public double getFromValue() {
        return fromValue;
    }

    public double getToValue() {
        return toValue;
    }

    public long getMsgId() {
        return msgId;
    }

    public int getOpType() {
        return opType;
    }

    public String getOpDesc() {
        return opDesc;
    }

    private int id;
    private String lydate;
    private String buyer;
    private String seller;
    private String fromScore;
    private String toScore;
    private String operation;
    private String title;
    private double fromValue;
    private double toValue;
    private long msgId = MSG_ID_NONE;
    private int opType = OP_TYPE_NONE;
    private String opDesc;

}
