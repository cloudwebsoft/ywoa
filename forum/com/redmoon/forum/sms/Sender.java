package com.redmoon.forum.sms;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
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
public class Sender extends ObjectRaw {
    public static final int MSG_STATUS_FORSEND = 1;
    public static final int MSG_STATUS_SENDED = 0;

    public Sender() {
        init();
    }

    public Sender(int sequenceNo) {
        this.sequenceNo = sequenceNo;
        init();
        load();
    }

    public void init() {
        super.init();
        this.connname = Global.getDefaultDB();
    }

    public void initDB() {
        tableName = "sms_send_record";
        QUERY_LOAD = "select sequenceno,orgaddr,destaddr,sendtime,validtime,serviceid,feecode,feetype,msgtext,sendstatus,msgfmt from " + tableName + " where sequenceno=?";
        QUERY_CREATE = "insert into " + tableName + " (orgaddr,destaddr,sendtime,validtime,serviceid,feecode,feetype,msgtext,sendstatus,msgfmt) values (?,?,?,?,?,?,?,?,?,?)";
        QUERY_DEL = "delete from " + tableName + " where sequenceno=?";
    }

    public Sender getSender(int no) {
        return new Sender(no);
    }

    public static boolean send(String orgAddr, String destAddr, String sendTime, String validTime, String serviceId, String feeCode, String feeType, String msgText, int msgFormat) {
        Sender sd = new Sender();
        sd.setOrgAddr(orgAddr);
        sd.setDestAddr(destAddr);
        sd.setSendTime(sendTime);
        sd.setValidTime(validTime);
        sd.setServiceId(serviceId);
        sd.setFeeCode(feeCode);
        sd.setFeeType(feeType);
        sd.setMsgText(msgText);
        sd.setMsgFormat(msgFormat);
        return sd.create();
    }

    /**
     * 发送一条短信
     * @return boolean
     */
    public boolean create() {
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        boolean re = false;
        try {
            ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, orgAddr);
            ps.setString(2, destAddr);
            ps.setString(3, sendTime);
            ps.setString(4, validTime);
            ps.setString(5, serviceId);
            ps.setString(6, feeCode);
            ps.setString(7, feeType);
            ps.setString(8, msgText);
            ps.setInt(9, sendStatus);
            ps.setInt(10, msgFormat);
            re = conn.executePreUpdate()==1?true:false;
        } catch (Exception e) {
            logger.error("create: " + e.getMessage());
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
     * @return boolean
     * @todo Implement this cn.js.fan.base.ObjectRaw method
     */
    public boolean del() {
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        boolean re = false;
        try {
            ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, sequenceNo);
            re = conn.executePreUpdate()==1?true:false;
        } catch (Exception e) {
            logger.error("list: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public ListResult list(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
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
                return null;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return null;
                }
                do {
                    Sender lp = getSender(rs.getInt(1));
                    result.addElement(lp);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("list:数据库出错！");
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
        ListResult lr = new ListResult();
        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    /**
     * 取出全部信息置于result中
     * @param sql String
     * @return Vector
     */
    public Vector list() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LIST);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    Sender lp = getSender(rs.getInt(1));
                    result.addElement(lp);
                }
            }
        } catch (Exception e) {
            logger.error("list: " + e.getMessage());
        } finally {
            /*
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }*/
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    /**
     *
     * @todo Implement this cn.js.fan.base.ObjectRaw method
     */
    public void load() {
    }

    /**
     *
     * @return boolean
     * @todo Implement this cn.js.fan.base.ObjectRaw method
     */
    public boolean save() {
        return false;
    }

    public void setOrgAddr(String orgAddr) {
        this.orgAddr = orgAddr;
    }

    public void setDestAddr(String destAddr) {
        this.destAddr = destAddr;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public void setValidTime(String validTime) {
        this.validTime = validTime;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setFeeCode(String feeCode) {
        this.feeCode = feeCode;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public void setMsgFormat(int msgFormat) {
        this.msgFormat = msgFormat;
    }

    public String getOrgAddr() {
        return orgAddr;
    }

    public String getDestAddr() {
        return destAddr;
    }

    public String getSendTime() {
        return sendTime;
    }

    public String getValidTime() {
        return validTime;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getFeeCode() {
        return feeCode;
    }

    public String getFeeType() {
        return feeType;
    }

    public String getMsgText() {
        return msgText;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public int getMsgFormat() {
        return msgFormat;
    }

    public void setSequenceNo(int sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public static boolean isValidMobile(String mobile) {
        if (!StrUtil.isNumeric(mobile))
            return false;
        if (mobile.length()!=11)
            return false;
        /*
        if (mobile.startsWith("159") || mobile.startsWith("134") || mobile.startsWith("135") || mobile.startsWith("136") || mobile.startsWith("137") || mobile.startsWith("138") || mobile.startsWith("139"))
            return true;
        else
            return false;
        */
        return true;
    }

    private String orgAddr;
    private String destAddr;
    private String sendTime;
    private String validTime;
    private String serviceId;
    private String feeCode;
    private String feeType;
    private String msgText;
    private int sendStatus = MSG_STATUS_FORSEND;
    private int msgFormat;
    private int sequenceNo;
}
