package com.redmoon.oa.pvg;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import cn.js.fan.base.ObjectBlockIterator;
import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.Config;
import com.redmoon.oa.person.UserDb;

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
    public static final String ALLCOUNTSQL = "select count(*) from oa_online";

    public final static String CLIENT_ANDROID = "Android";
	public final static String CLIENT_IOS = "IOS";
	public final static String CLIENT_PC = "PC";
	
    private String sessionId;
    
    private String client;
    
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
        Config cfg = new Config();
        int expire = cfg.getInt("refreshOnlineExpire");

        long expiremilli = expire * 1000;
        long expiretime = System.currentTimeMillis() - expiremilli;
        String sql = "select name from oa_online where staytime<" + StrUtil.sqlstr("" + expiretime);

        // com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).info("refreshOnlineUser sql=" + sql);

        Iterator ir = list(sql).iterator();
        while (ir.hasNext()) {
            OnlineUserDb olu = (OnlineUserDb)ir.next();
            olu.del();
        }
        OnlineUserCache uc = new OnlineUserCache(this);
        uc.refreshCreate();
    }

    public boolean create() {
        boolean isvalid = false;
        String sql =
                "insert into oa_online (name,ip,covered,logtime,staytime,session_id,client) values (?,?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, ip);
            ps.setInt(3, covered?1:0);
            ps.setString(4, "" + System.currentTimeMillis());
            ps.setString(5, "" + System.currentTimeMillis());
            ps.setString(6, sessionId);
            ps.setString(7, client);
            isvalid = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("create:" + e.getMessage());
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
        oc.refreshCreate();

        // 刷新超时在线用户人数，改为在调度中刷新
        // refreshOnlineUser();

        return isvalid;
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        OnlineUserCache uc = new OnlineUserCache(this);
        primaryKey.setValue(primaryKeyValue);
        return uc.getObjectDb(primaryKey);
    }

    public synchronized boolean del() {
        // 记录在线时长
        UserDb ud = new UserDb();
        long millis = System.currentTimeMillis() - logTime.getTime();
        float hour = (float) millis / 3600000;
        ud = ud.getUserDb(name);
        ud.setOnlineTime(ud.getOnlineTime() + hour);
        ud.save();

        boolean isvalid = false;
        String sql =
                "delete from oa_online where name=?";
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            isvalid = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("del:" + e.getMessage());
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
            uc.refreshCreate();
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
        // 下句会导致并发效率问题，当人数达到70人左右时
        // QUERY_LIST = "select o.name,d.dept_code from oa_online o, dept_user d where o.name=d.user_name order by d.dept_code asc, o.logtime asc";
        QUERY_LIST = "select name from oa_online where name<>'system' order by logtime asc";
    }

    public void setQuerySave() {
        this.QUERY_SAVE =
            "update oa_online set ip=?,doing=?,logtime=?,staytime=?,covered=?,session_id=?,client=? where name=?";
    }

    public synchronized boolean save() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, ip);
            ps.setString(2, doing);
            ps.setString(3, DateUtil.toLongString(logTime));
            ps.setString(4, DateUtil.toLongString(stayTime));
            ps.setInt(5, covered?1:0);
            ps.setString(6, sessionId);
            ps.setString(7, client);
            ps.setString(8, name);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("save:" + e.getMessage());
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
        QUERY_LOAD = "select ip,doing,logtime,staytime,covered,session_id,client from oa_online where name=?";
    }

    public synchronized void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setString(1, name);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                ip = StrUtil.getNullString(rs.getString(1));
                doing = rs.getString(2);
                logTime = DateUtil.parse(rs.getString(3));
                stayTime = DateUtil.parse(rs.getString(4));
                covered = rs.getInt(5)==1?true:false;
                sessionId = StrUtil.getNullStr(rs.getString(6));
                client = StrUtil.getNullStr(rs.getString(7));
                loaded = true;
            }
        } catch (Exception e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("load: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void setName(String name) {
        this.name = name;
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

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getName() {
        return name;
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

    public String getSessionId() {
        return sessionId;
    }

    public int getAllCount() {
        OnlineUserCache ou = new OnlineUserCache(this);
        return ou.getAllCount();
    }

    public void setClient(String client) {
		this.client = client;
	}

	public String getClient() {
		return client;
	}

	private String name;
    private String ip;
    private String doing;
    private java.util.Date logTime;
    private java.util.Date stayTime;
    private boolean guest = false;
    private boolean covered = false;

}
