package com.redmoon.forum;

import java.sql.ResultSet;
import java.sql.SQLException;
import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.db.Conn;
import java.sql.PreparedStatement;
import cn.js.fan.util.StrUtil;
import cn.js.fan.base.ObjectBlockIterator;
import cn.js.fan.util.DateUtil;
import com.redmoon.forum.person.UserDb;
import java.util.Iterator;

/**
 *
 * <p>Title: 在线用户详细信息管理</p>
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
public class OnlineUserDb extends ObjectDb {
    public OnlineUserDb() {
    }

    public OnlineUserDb(String name) {
        this.name = name;
        init();
        load();
    }

    public OnlineUserDb getOnlineUserDb(String name) {
        return (OnlineUserDb)getObjectDb(name);
    }

    public ObjectBlockIterator getOnlineUsers(String query,
                                         int startIndex,
                                         int endIndex) {
        // if (!SecurityUtil.isValidSql(query))
        //     return null;
        //可能取得的infoBlock中的元素的顺序号小于endIndex
        Object[] docBlock = getObjectBlock(query, startIndex);

        return new ObjectBlockIterator(this, docBlock, query,
                                    startIndex, endIndex);
    }

    public void refreshOnlineUser() {
        // logger.info("refreshOnlineUser1:" + DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));

        // 删除超时在位用户,超时时间为20分钟
        Config cfg = Config.getInstance();
        int expire = cfg.getIntProperty("forum.refreshOnlineExpire");

        long expiremilli = expire * 60000;
        long expiretime = System.currentTimeMillis() - expiremilli;
        String sql = "select name from sq_online where staytime<" + StrUtil.sqlstr("" + expiretime);
        Iterator ir = list(sql).iterator();
        while (ir.hasNext()) {
            OnlineUserDb olu = (OnlineUserDb)ir.next();
            olu.del();
        }

        OnlineUserCache uc = new OnlineUserCache(this);
        uc.refreshCreate();

        // 刷新在线缓存
        OnlineUserCache oc = new OnlineUserCache(this);
        oc.refreshAll();
    }

    public boolean create() throws ErrMsgException {
        boolean isvalid = false;
        String sql =
                "insert into sq_online (name,ip,isguest,covered,logtime,staytime) values (?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, ip);
            ps.setInt(3, guest?1:0);
            ps.setInt(4, covered?1:0);
            ps.setString(5, "" + System.currentTimeMillis());
            ps.setString(6, "" + System.currentTimeMillis());
            isvalid = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            isvalid = false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (isvalid) {
            OnlineUserCache uc = new OnlineUserCache(this);
            uc.refreshCreate();
        }
        // 刷新在线缓存
        OnlineUserCache oc = new OnlineUserCache(this);
        oc.refreshAll();

        // 刷新超时在线用户人数，改为在调度中刷新
        // refreshOnlineUser();

        return isvalid;
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        OnlineUserCache uc = new OnlineUserCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return uc.getObjectDb(pk);
    }

    public synchronized boolean del() {
        // 记录在线时长
        if (!isGuest()) {
            Config cfg = Config.getInstance();
            if (cfg.getBooleanProperty("forum.isOnlineTimeRecord")) {
                UserDb ud = new UserDb();
                long millis = System.currentTimeMillis() - logTime.getTime();
                float hour = (float) millis / 3600000;
                ud = ud.getUser(name);
                ud.setOnlineTime(ud.getOnlineTime() + hour);
                ud.save();
            }
        }

        boolean isvalid = false;
        String sql =
                "delete from sq_online where name=?";
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            isvalid = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
            isvalid = false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (isvalid) {
            OnlineUserCache uc = new OnlineUserCache(this);
            primaryKey.setValue(name);
            uc.refreshDel(primaryKey);

            uc.refreshAll();
        }

        return isvalid;
    }

    public int getObjectCount(String sql) {
        OnlineUserCache uc = new OnlineUserCache(this);
        return uc.getObjectCount(sql);
    }

    public Object[] getObjectBlock(String query, int startIndex) {
        OnlineUserCache dcm = new OnlineUserCache(this);
        return dcm.getObjectBlock(query, startIndex);
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new OnlineUserDb(pk.getStrValue());
    }

    public void setQueryCreate() {
    }

    public void setQueryDel() {

    }

    public void setQueryList() {
    }

    public void setQuerySave() {
        this.QUERY_SAVE =
            "update sq_online set boardcode=?,ip=?,doing=?,logtime=?,staytime=?,isguest=?,covered=? where name=?";
    }

    public synchronized boolean save() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, boardCode);
            ps.setString(2, ip);
            ps.setString(3, doing);
            ps.setString(4, DateUtil.toLongString(logTime));
            ps.setString(5, DateUtil.toLongString(stayTime));
            ps.setInt(6, guest?1:0);
            ps.setInt(7, covered?1:0);
            ps.setString(8, name);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            logger.error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            OnlineUserCache uc = new OnlineUserCache(this);
            primaryKey.setValue(name);
            uc.refreshSave(primaryKey);
        }
        return re;
    }

    public void setPrimaryKey() {
        primaryKey = new PrimaryKey("name", PrimaryKey.TYPE_STRING);
    }

    public void setQueryLoad() {
        QUERY_LOAD = "select boardcode,ip,doing,logtime,staytime,isguest,covered from sq_online where name=?";
    }

    public synchronized void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setString(1, name);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                boardCode = rs.getString(1);
                ip = StrUtil.getNullString(rs.getString(2));
                doing = rs.getString(3);
                logTime = DateUtil.parse(rs.getString(4));
                stayTime = DateUtil.parse(rs.getString(5));
                guest = rs.getInt(6)==1?true:false;
                covered = rs.getInt(7)==1?true:false;
                loaded = true;
            }
        } catch (Exception e) {
            logger.error("load: " + e.getMessage());
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
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setDoing(String action) {
        this.doing = action;
    }

    public void setLogTime(java.util.Date logTime) {
        this.logTime = logTime;
    }

    public void setStayTime(java.util.Date stayTime) {
        this.stayTime = stayTime;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public void setCovered(boolean covered) {
        this.covered = covered;
    }

    public String getName() {
        return name;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getIp() {
        return ip;
    }

    public String getDoing() {
        return doing;
    }

    public java.util.Date getLogTime() {
        return logTime;
    }

    public java.util.Date getStayTime() {
        return stayTime;
    }

    public boolean isGuest() {
        return guest;
    }

    public boolean isCovered() {
        return covered;
    }

    public boolean setUserInBoard(String boardcode) {
        if (boardCode!=null && boardCode.equals(boardcode))
            return true;
        String oldboardcode = StrUtil.getNullStr(boardcode);
        this.boardCode = boardcode;
        boolean re = save();
        if (re) {
            // 刷新在线缓存
            OnlineUserCache ou = new OnlineUserCache(this);
            if (!oldboardcode.equals("")) {
                ou.refreshBoardCount(ou.BOARDCOUNTSQL, oldboardcode);
                ou.refreshBoardCount(ou.BOARDUSERCOUNTSQL, oldboardcode);
            }
            ou.refreshBoardCount(ou.BOARDCOUNTSQL, boardcode);
            ou.refreshBoardCount(ou.BOARDUSERCOUNTSQL, boardcode);
        }
        return re;
    }

    public int getAllCount() {
        OnlineUserCache ou = new OnlineUserCache(this);
        return ou.getAllCount();
    }

    public int getAllUserCount() {
        OnlineUserCache ou = new OnlineUserCache(this);
        return ou.getAllUserCount();
    }

    public int getBoardCount(String boardcode) {
        OnlineUserCache ou = new OnlineUserCache(this);
        return ou.getBoardCount(boardcode);
    }

    public int getBoardUserCount(String boardcode) {
        OnlineUserCache ou = new OnlineUserCache(this);
        return ou.getBoardUserCount(boardcode);
    }

    private String name;
    private String boardCode;
    private String ip;
    private String doing;
    private java.util.Date logTime;
    private java.util.Date stayTime;
    private boolean guest = false;
    private boolean covered = false;

}
