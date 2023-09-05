package com.redmoon.oa.sms;

import java.sql.*;
import java.util.Vector;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;
import com.cloudwebsoft.framework.db.JdbcTemplate;

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
public class SMSSendRecordDb extends ObjectDb {

    public SMSSendRecordDb() {
        init();
    }

    public SMSSendRecordDb(int id) {
        this.id = id;
        init();
        load();
    }

    public void initDB() {
        tableName = "sms_send_record";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new SMSSendRecordCache(this);
        isInitFromConfigDB = false;
        //userName info fileNumber myDate duty deposal remark
        QUERY_CREATE =
                "insert into " + tableName +
                " (id,userName,sendMobile,msgText,sendTime,orgAddr,receiver,is_timing,time_send,batch) values (?,?,?,?,?,?,?,?,?,?)";
        QUERY_LIST = "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select id,userName,sendMobile,msgText,sendTime,orgAddr,is_sended,send_count,receiver,msg_id,is_timing,time_send,batch from " +
                     tableName + " where id=?";
    }


    public SMSSendRecordDb getSMSSendRecordDb(int id) {
        return (SMSSendRecordDb) getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            id = (int) SequenceManager.nextID(SequenceManager.OA_SMS_SEND_RECORD);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, id);
            ps.setString(2, userName);
            ps.setString(3, sendMobile);
            ps.setString(4, msgText);
            Timestamp st = null;
            sendTime = new java.util.Date();
            st = new Timestamp(sendTime.getTime());
            ps.setTimestamp(5, st);
            ps.setString(6, orgAddr);
            ps.setString(7, receiver);
            ps.setInt(8, timing?1:0);
            if (timeSend==null)
                ps.setTimestamp(9, null);
            else {
                st = new Timestamp(timeSend.getTime());
                ps.setTimestamp(9, st);
            }
            if (batch==0) {
                batch = getBatchCanUse();
            }
            ps.setLong(10,batch);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                SMSSendRecordCache ssrc = new SMSSendRecordCache(this);
                ssrc.refreshCreate();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
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
             SMSSendRecordCache src = new SMSSendRecordCache(this);
             src.refreshDel(primaryKey);
         } catch (SQLException e) {
             LogUtil.getLog(getClass()).error("del:" + e.getMessage());
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
        return new SMSSendRecordDb(pk.getIntValue());
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
             PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
             ps.setInt(1, id);
             rs = conn.executePreQuery();
             if (rs != null && rs.next()) {
                 id = rs.getInt(1);
                 userName = rs.getString(2);
                 sendMobile = rs.getString(3);
                 msgText = rs.getString(4);
                 sendTime = rs.getTimestamp(5);
                 orgAddr = rs.getString(6);
                 sended = rs.getInt(7)==1;
                 sendCount = rs.getInt(8);
                 receiver = StrUtil.getNullStr(rs.getString(9));
                 msgId = rs.getInt(10);
                 timing = rs.getInt(11)==1;
                 timeSend = rs.getTimestamp(12);
                 batch = rs.getLong(13);

                 loaded = true;
                 primaryKey.setValue(new Integer(id));
             }
         } catch (SQLException e) {
             LogUtil.getLog(getClass()).error("load: " + e.getMessage());
         } finally {
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (SQLException e) {}
                 rs = null;
             }
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
    }


    /**
     * 当短信发送成功后，置发送状态为已发送，并置与会人员的短信发送状态为已发送
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() throws ErrMsgException {
        String sql = "update " + tableName + " set is_sended=?,send_count=?,msg_id=?,sendtime=?,is_timing=?,time_send=?,batch=? where id=?";
         Conn conn = new Conn(connname);
         boolean re = false;
         try {
             PreparedStatement ps = conn.prepareStatement(sql);
             ps.setInt(1, sended?1:0);
             ps.setInt(2, sendCount);
             ps.setInt(3, msgId);
             ps.setTimestamp(4, new java.sql.Timestamp(sendTime.getTime()));
             ps.setInt(5, timing?1:0);
             if(timeSend==null){
                 ps.setTimestamp(6, null);
             }else{
                 ps.setTimestamp(6, new java.sql.Timestamp(timeSend.getTime()));
             }
             ps.setLong(7,batch);
             ps.setInt(8, id);

             re = conn.executePreUpdate() == 1 ? true : false;
             SMSSendRecordCache src = new SMSSendRecordCache(this);
             src.refreshSave(primaryKey);
         } catch (SQLException e) {
             LogUtil.getLog(getClass()).error("save:" + e.getMessage());
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
                    result.addElement(getSMSSendRecordDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    @Override
    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        listsql = listsql.toLowerCase();
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
                    SMSSendRecordDb ssrd = getSMSSendRecordDb(rs.getInt(1));
                    result.addElement(ssrd);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
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

    public static long getBatchCanUse() throws SQLException {
        long maxBatch = 0l;
        String sql = "select max(batch) from sms_send_record";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri =jt.executeQuery(sql);
        if(ri.hasNext()){
            ResultRecord rd = (ResultRecord)ri.next();
            maxBatch = rd.getLong(1);
        }
        maxBatch ++;
        return maxBatch;
    }

    public int getCount(long batch) throws SQLException {
        int count = 0;
        String sql = "select count(*) from sms_send_record where batch = "+batch;
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = jt.executeQuery(sql);
        if(ri.hasNext()){
            ResultRecord rd = (ResultRecord)ri.next();
            count = rd.getInt(1);
        }
        return count;
    }

    public int getCount(long batch, String userName) throws SQLException {
        int count = 0;
        String sql = "select count(*) from sms_send_record where batch = "+batch+" and userName="+StrUtil.sqlstr(userName);
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = jt.executeQuery(sql);
        if(ri.hasNext()){
            ResultRecord rd = (ResultRecord)ri.next();
            count = rd.getInt(1);
        }
        return count;

    }


    public void setId(int id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSendMobile(String sendMobile) {
        this.sendMobile = sendMobile;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public void setSendTime(java.util.Date sendTime) {
        this.sendTime = sendTime;
    }

    public void setOrgAddr(String orgAddr) {
        this.orgAddr = orgAddr;
    }

    public void setSended(boolean sended) {
        this.sended = sended;
    }

    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getSendMobile() {
        return sendMobile;
    }

    public String getMsgText() {
        return msgText;
    }

    public java.util.Date getSendTime() {
        return sendTime;
    }

    public String getOrgAddr() {
        return orgAddr;
    }

    public boolean isSended() {
        return sended;
    }

    public int getSendCount() {
        return sendCount;
    }

    public String getReceiver() {
        return receiver;
    }

    public int getMsgId() {
        return msgId;
    }

    public boolean isTiming() {
         return timing;
    }

    public void setTiming(boolean timing) {
        this.timing = timing;
    }

    public java.util.Date getTimeSend() {
        return timeSend;
    }

    public void setTimeSend(java.util.Date timeSend) {
        this.timeSend = timeSend;
    }

    public long getBatch(){
        return batch;
    }

    public void setBatch(long batch){
        this.batch = batch;
    }


    private int id;
    private String userName;
    private String sendMobile;
    private String msgText;
    private java.util.Date sendTime;
    private String orgAddr;
    private boolean sended = false;
    private int sendCount = 0;
    private boolean timing;
    private java.util.Date timeSend;
    private long batch = 0;//批次

    /**
     * 接收者，可能为系统用户真实姓名，也可能不存在，为了方便查询
     */
    private String receiver;
    private int msgId;
}
