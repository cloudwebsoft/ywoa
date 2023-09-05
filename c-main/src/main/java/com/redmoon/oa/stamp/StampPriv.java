package com.redmoon.oa.stamp;

import java.sql.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.*;
import com.redmoon.oa.db.SequenceManager;

/**
 * 印章权限
 * @author Administrator
 *
 */
public class StampPriv extends ObjectDbA {
    int id;
    int see = 1;
    int stampId;
    private String name = "";
    public static final int TYPE_USER = 1;
    public static final int TYPE_ROLE = 2;

    public static final int PRIV_SEE = 0;

    public StampPriv(int id) {
        this.id = id;
        init();
        load();
    }

    public StampPriv() {
        init();
    }
    
    public void init() {
        super.init();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * 取得用户的私章
     * @param userName
     * @return
     */
    public StampDb getPersonalStamp(String userName) {
    	UserDb user = new UserDb();
    	user = user.getUserDb(userName);
    	String sql = "select id from oa_stamp_priv where (name=" + StrUtil.sqlstr(userName) + " and priv_type=" + TYPE_USER + ")";
    	
    	StampDb sd = new StampDb();
    	Iterator ir = list(sql).iterator();
    	if (ir.hasNext()) {
    		StampPriv sp = (StampPriv)ir.next();
    		return sd.getStampDb(sp.getStampId());
    	}
    	return null;
    }
    /**
     * 取得用户可以使用的印章
     * @param userName
     * @return
     */
    public Vector<StampDb> getStampsOfUser(String userName) {
    	UserDb user = new UserDb();
    	user = user.getUserDb(userName);
    	RoleDb[] roles = user.getRoles();
    	String sql = "select id from oa_stamp_priv where (name=" + StrUtil.sqlstr(userName) + " and priv_type=" + TYPE_USER + ")";
    	
    	if (roles!=null) {
    		int len = roles.length;
        	String rstr = "";
            for (RoleDb rd : roles) {
                if ("".equals(rstr)) {
                    rstr = StrUtil.sqlstr(rd.getCode());
                } else {
                    rstr += "," + StrUtil.sqlstr(rd.getCode());
                }
            }
    		sql += " or (name in (" + rstr + ") and priv_type=" + TYPE_ROLE + ")";
    	}
    	
    	Vector<StampDb> v = new Vector<>();
    	StampDb sd = new StampDb();
        for (Object o : list(sql)) {
            StampPriv sp = (StampPriv) o;
            v.addElement(sd.getStampDb(sp.getStampId()));
        }
    	return v;
    }
    
    public StampPriv getStampPriv(int stampId, String name, int type) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            String sql = "select id from oa_stamp_priv where stamp_id=? and name=? and priv_type=?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, stampId);
            ps.setString(2, name);
            ps.setInt(3, type);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                if (rs.next()) {
                    return new StampPriv(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getStampPriv: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;
    }    

    public String getTypeDesc() {
        if (type==TYPE_USER)
            return "用户";
        else if (type==TYPE_ROLE)
            return "角色";
        else
            return "";
    }

    public void setQueryList() {
        QUERY_LIST =
                "select id from oa_stamp_priv where stamp_id=? order by priv_type desc, name";
    }

    public void setQueryLoad() {
        QUERY_LOAD =
                "select stamp_id,name,priv_type,see from oa_stamp_priv where id=?";
    }

    public void setQueryDel() {
        QUERY_DEL = "delete from oa_stamp_priv where id=?";
    }

    public void setQuerySave() {
        QUERY_SAVE =
                "update oa_stamp_priv set see=? where id=?";
    }

    public void setQueryAdd() {
        QUERY_ADD =
                "insert into oa_stamp_priv (name,priv_type,see,stamp_id,id) values (?,?,?,?,?)";
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                stampId = rs.getInt(1);
                name = rs.getString(2);
                type = rs.getInt(3);
                see = rs.getInt(4);
                loaded = true;
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public int getStampId() {
        return stampId;
    }

    public void setStampId(int stampId) {
        this.stampId = stampId;
    }

    public String getName() {
        return name;
    }

    public void setType(int t) {
        this.type = t;
    }

    public void setName(String n) {
        this.name = n;
    }

    public int getType() {
        return type;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String toString() {
        return "StampPriv is " + stampId + ":" + name;
    }

    private int type = 0;

    public synchronized boolean save() {
        RMConn conn = new RMConn(connname);
        int r = 0;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setInt(1, see);
            ps.setInt(6, id);
            r = conn.executePreUpdate();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        }
        return r == 1 ? true : false;
    }
    
    public boolean canUserSee(HttpServletRequest request) {
    	Privilege pvg = new Privilege();
    	if (pvg.isUserPrivValid(request, "admin"))
    		return true;
    	else {
    		String username = pvg.getUser(request);
    		return canUserSee(username);
    	}
    }

    public boolean canUserSee(String username) {
        if (username.equals(UserDb.ADMIN))
            return true;
        UserDb user = new UserDb();
        UserGroupDb[] groups = null;
        if (username != null)
            user = user.getUserDb(username);
        
        return canUserDo(stampId, user, user.getRoles(), PRIV_SEE);
    }

    /**
     * 查询单个结点leaf（并不往上查其父节点），user对其是否有权限
     * @return boolean
     */
    public boolean canUserDo(int stampId, UserDb user, RoleDb[] roles, int privType) {
        StampPriv leafPriv = new StampPriv();
        leafPriv.setStampId(stampId);
        // list该节点的所有拥有权限的用户
        Vector r = leafPriv.list();
        Iterator ir = r.iterator();
        while (ir.hasNext()) {
            // 遍历每个权限项
            StampPriv lp = (StampPriv) ir.next();
            if (lp.getType()==TYPE_ROLE) {
                if (roles!=null) {
                        // 判断该用户所属的角色是否有权限
                        int len = roles.length;
                        for (int i = 0; i < len; i++) {
                            if (roles[i].getCode().equals(lp.getName())) {
                                if (privType == PRIV_SEE) {
                                    if (lp.getSee() == 1)
                                        return true;
                                }
                                break;
                            }
                        }
                }
            }
            else if (lp.getType()==TYPE_USER) { //　个人用户
                if (lp.getName().equals(user.getName())) {
                    if (privType == PRIV_SEE) {
                        if (lp.getSee() == 1)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean add(String name, int type, int stampId) throws
            ErrMsgException {
        // 检查节点是否已存在
        Conn conn = new Conn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_ADD);
            ps.setString(1, name);
            ps.setInt(2, type);
            ps.setInt(3, see);

            ps.setInt(4, stampId);

            id = (int)SequenceManager.nextID(SequenceManager.OA_STAMP_PRIV);
            ps.setInt(5, id);

            r = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("add:" + e.getMessage());
            throw new ErrMsgException("请检查是否有重复项存在！");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return r;
    }

    public StampPriv getStampPriv(int id) {
        StampPriv sp = null;
        try {
        	sp = new StampPriv(id);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return sp;
    }

    public boolean delPrivsOfStamp(int stampId) {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            String sql = "delete from oa_stamp_priv where stamp_id=?";
            PreparedStatement ps = rmconn.prepareStatement(sql);
            ps.setInt(1, stampId);
            r = rmconn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            return false;
        }
        return r;
    }

    public boolean del() {
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            PreparedStatement ps = rmconn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            r = rmconn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            return false;
        }
        return r;
    }

    public int getSee() {
        return see;
    }

    public void setSee(int see) {
        this.see = see;
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
                    StampPriv lp = getStampPriv(rs.getInt(1));
                    result.addElement(lp);
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
                    StampPriv lp = getStampPriv(rs.getInt(1));
                    result.addElement(lp);
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("list: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    /**
     * 取出全部信息置于result中
     * @return Vector
     */
    public Vector list() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(QUERY_LIST);
            ps.setInt(1, stampId);
            rs = ps.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    StampPriv lp = getStampPriv(rs.getInt(1));
                    result.addElement(lp);
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("list: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }
    
    public static Vector getUsersCanSee(int stampId) {
        Vector v = new Vector();
        String sql =
                "select name from oa_stamp_priv where stamp_id=? and see=1 and priv_type=?";
        ResultIterator ri = null;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ri = jt.executeQuery(sql, new Object[] {new Integer(stampId), new Integer(TYPE_USER)});

            ResultRecord rr = null;
            UserDb user;
            UserMgr um = new UserMgr();
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                String userName = rr.getString(1);
                user = um.getUserDb(userName);
                v.addElement(user);
            }

            // 2010-8-25修复此bug，原来是将所有的role加入了返回值
            RoleMgr rm = new RoleMgr();
            RoleDb role;
            sql = "select name from oa_stamp_priv where stamp_id=? and see=1 and priv_type=?";
            ri = jt.executeQuery(sql, new Object[] {new Integer(stampId), new Integer(TYPE_ROLE)});
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                String roleCode = rr.getString(1);
                role = rm.getRoleDb(roleCode);

                // 检查是否有重复，不重复则加入v
                Vector v2 = role.getAllUserOfRole();
                Iterator ir2 = v2.iterator();
                while (ir2.hasNext()) {
                    UserDb ud2 = (UserDb)ir2.next();
                    boolean isRepeat = false;
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        UserDb ud = (UserDb) ir.next();
                        if (ud2.getName().equals(ud.getName())) {
                            isRepeat = true;
                            break;
                        }
                    }
                    if (!isRepeat) {
                        v.addElement(ud2);
                    }
                }
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(StampPriv.class).error(e);
        }
        return v;
    }      

    private boolean loaded = false;
    private int examine = 0;

}
