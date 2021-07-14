package com.redmoon.forum.security;

import java.sql.*;
import java.util.Date;
import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.base.ObjectBlockIterator;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.SequenceMgr;
import com.cloudwebsoft.framework.util.IPUtil;

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
public class ForbidIPRangeDb extends ObjectDb {
    public ForbidIPRangeDb() {
    }


    public ForbidIPRangeDb(int id) {
        this.id = id;
        init();
        load();
    }

    public ForbidIPRangeDb getForbidIPRangeDb(int id) {
        return (ForbidIPRangeDb)getObjectDb(new Integer(id));
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new ForbidIPRangeDb(pk.getIntValue());
    }

    public void initDB() {
        this.tableName = "sq_forbid_ip_range";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new ForbidIPRangeCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT into " + tableName + " (ip_begin,ip_end,user_name,add_date,reason,id) VALUES (?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT ip_begin,ip_end,user_name,add_date,reason FROM " + tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET user_name=?,reason=? WHERE ip=?";
        this.QUERY_LIST = "select id from " + tableName + " order by add_date desc";
        isInitFromConfigDB = false;
    }

    public boolean save() throws ErrMsgException {
        return false;
    }

    public void load() {
        // Based on the id in the object, get the message data from the database.
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // "SELECT user_name,add_date,reason FROM " + tableName + " WHERE ip=?";
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(this.QUERY_LOAD);
            pstmt.setInt(1, id);
            //url,title,image,userName,sort,kind
            rs = conn.executePreQuery();
            if (rs.next()) {
                begin = rs.getLong(1);
                end = rs.getLong(2);
                userName = rs.getString(3);
                try {
                    addDate = DateUtil.parse(rs.getString(4));
                }
                catch (Exception e) {

                }
                reason = rs.getString(5);
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del() throws ErrMsgException {
        Conn conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = new Conn(connname);
            pstmt = conn.prepareStatement(this.QUERY_DEL);
            pstmt.setInt(1, id);
            if (conn.executePreUpdate()==1) {
                ForbidIPRangeCache mc = new ForbidIPRangeCache(this);
                mc.refreshDel(primaryKey);
                return true;
            }
            else
                return false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("Error db operate");
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean create() throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        PreparedStatement pstmt = null;
        // "INSERT into " + tableName + " (ip,user_name,add_date,reason) VALUES (?,?,?,?)";
        try {
            conn = new Conn(connname);
            pstmt = conn.prepareStatement(this.QUERY_CREATE);
            pstmt.setLong(1, begin);
            pstmt.setLong(2, end);
            pstmt.setString(3, userName);
            pstmt.setString(4, "" + System.currentTimeMillis());
            pstmt.setString(5, reason);
            id = (int)SequenceMgr.nextID(SequenceMgr.SQ_FORBID_IP_RANGE);
            pstmt.setInt(6, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                ForbidIPRangeCache mc = new ForbidIPRangeCache(this);
                mc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("Error db operate");
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public String getUserName() {
        return userName;
    }

    public Date getAddDate() {
        return addDate;
    }

    public String getReason() {
        return reason;
    }

    public int getId() {
        return id;
    }

    public long getBegin() {
        return begin;
    }

    public long getEnd() {
        return end;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAddDate(Date addDate) {
        this.addDate = addDate;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public boolean isValid(String ip) {
        /*
        String[] ary = StrUtil.split(ip, "\\.");
        String u = "";
        int len = ary.length;
        for (int i=0; i<len; i++) {
            if (!ary[i].equals("*")) {
                if (ary[i].length()<3)
                    ary[i] = StrUtil.PadString(ary[i], '0', 3, true);
            } else {
                ary[i] = "000";
            }
            u += ary[i];
        }

        // 注意0:0:0:0:0:0:0:1 IPV6这样的地址，暂且直接让其通过
        long ul = 0;
        try {
            ul = Long.parseLong(u);
        }
        catch (Exception e) {
            return true;
        }
        */
       long ul = IPUtil.ip2long(ip);
       if (ul==0) // 如果为IPV6格式
           return true;

        int count = getObjectCount(this.QUERY_LIST);
        ObjectBlockIterator ir = getObjects(this.QUERY_LIST, 0, count);
        while (ir.hasNext()) {
            ForbidIPRangeDb fir = (ForbidIPRangeDb)ir.next();
            long begin = fir.getBegin();
            long end = fir.getEnd();
            /*
            String[] ary1 = StrUtil.split(begin, "\\.");
            String b = "";
            len = ary1.length;
            for (int i=0; i<len; i++) {
                if (!ary1[i].equals("*")) {
                    if (ary1[i].length()<3)
                        ary1[i] = StrUtil.PadString(ary1[i], '0', 3, true);
                } else {
                    ary1[i] = "000";
                }
                b += ary1[i];
            }
            String[] ary2 = StrUtil.split(end, "\\.");
            len = ary1.length;
            String e = "";
            for (int i=0; i<len; i++) {
                if (!ary2[i].equals("*")) {
                    if (ary2[i].length()<3)
                        ary2[i] = StrUtil.PadString(ary2[i], '0', 3, true);
                } else {
                    ary2[i] = "000";
                }
                e += ary2[i];
            }

            // System.out.println("ForbidIPRangeDb.java u=" + u + " b=" + b + " e=" + e);

            long bl = Long.parseLong(b);
            long el = Long.parseLong(e);
            */

            if (ul>=begin && ul<=end) {
                reason = fir.getReason();
                return false;
            }
        }
        return true;
    }

    private String userName;
    private Date addDate;
    private String reason;
    private int id;
    private long begin;
    private long end;

}
